package uk.ac.warwick.dcs.sherlock.module.web.data.wrappers

import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Account
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.TDetector
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.TParameter
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Template
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.TemplateForm
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TDetectorRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TParameterRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TemplateRepository
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotTemplateOwner
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.TemplateNotFound
import java.util.*
import java.util.function.Consumer

/**
 * The wrapper that manages the job templates
 */
class TemplateWrapper {
    /**
     * The job template entity
     */
    @JvmField
    val template: Template

    /**
     * Whether the current user owns the template
     */
    @JvmField
    val isOwner: Boolean

    /**
     * Initialise the wrapper using the form to create a new template
     *
     * @param templateForm the form to use
     * @param account the account of current user
     * @param templateRepository the database repository
     * @param tDetectorRepository the database repository
     *
     * @throws NotTemplateOwner if the user is not the owner of the template
     */
    constructor(
        templateForm: TemplateForm,
        account: Account?,
        templateRepository: TemplateRepository?,
        tDetectorRepository: TDetectorRepository?
    ) {
        this.template = Template()
        template.account = account
        this.isOwner = true
        this.update(templateForm, templateRepository, tDetectorRepository)
    }

    /**
     * Initialise the template wrapper using an id to find one in the database
     *
     * @param id the id of the template
     * @param account the account of the current user
     * @param templateRepository the database repository
     *
     * @throws TemplateNotFound if the template was not found
     */
    constructor(
        id: Long,
        account: Account?,
        templateRepository: TemplateRepository?
    ) {
        val foundTemplate = templateRepository?.findByIdAndPublic(id, account)
            ?: throw TemplateNotFound("Template not found.")

        this.template = foundTemplate
        this.isOwner = foundTemplate.account === account
    }

    /**
     * Initialise the template wrapper using an existing template
     *
     * @param template the template to manage
     * @param account the account of the current user
     */
    constructor(template: Template, account: Account?) {
        this.template = template
        this.isOwner = template.account === account
    }

    val ownerName: String?
        /**
         * Get the name of the owner
         *
         * @return the name
         */
        get() = template.account!!.username

    val isPublic: Boolean
        /**
         * Whether the template is public
         *
         * @return the result
         */
        get() = template.isPublic

    val detectors: List<DetectorWrapper>
        /**
         * Get the list of detector wrappers active in the template
         *
         * @return the list
         */
        get() {
            val wrapperList: MutableList<DetectorWrapper> = ArrayList()
            template.detectors.forEach(Consumer { d: TDetector? ->
                wrapperList.add(
                    DetectorWrapper(
                        d,
                        this.isOwner
                    )
                )
            })
            return wrapperList
        }

    /**
     * Update the template using the form supplied
     *
     * @param templateForm the form to use
     * @param templateRepository the database repository
     * @param templateDetectorRepository the database repository
     *
     * @throws NotTemplateOwner if the user is not the template owner
     */
    @Throws(NotTemplateOwner::class)
    fun update(
        templateForm: TemplateForm,
        templateRepository: TemplateRepository?,
        templateDetectorRepository: TDetectorRepository?
    ) {
        if (!this.isOwner) throw NotTemplateOwner("You are not the owner of this template.")

        template.name = templateForm.getName()
        template.language = templateForm.getLanguage()
        template.isPublic = templateForm.isPublic()
        templateRepository?.save(template)

        val activeDetectors = EngineDetectorWrapper.getDetectorNames(template.language)

        val toRemove: MutableList<String?> = ArrayList()

        val toAdd: MutableList<String?> = ArrayList(templateForm.getDetectors())
        template.detectors.forEach(Consumer { d: TDetector -> toAdd.remove(d.name) })

        template.detectors.forEach(Consumer { d: TDetector -> toRemove.add(d.name) })
        toRemove.removeAll(templateForm.getDetectors())

        for (add in toAdd) {
            templateDetectorRepository?.save(TDetector(add, template))
        }

        for (remove in toRemove) {
            Objects.requireNonNull(templateDetectorRepository?.findByNameAndTemplate(remove, template))?.let {
                templateDetectorRepository?.delete(it)
            }
        }

        val toCheck: MutableList<String?> = ArrayList(toAdd)
        template.detectors.map { d: TDetector -> d.name }.forEach { toCheck.add(it) }
        toCheck.removeAll(toRemove)

        for (check in toCheck) {
            if (!activeDetectors.contains(check)) {
                Objects.requireNonNull(templateDetectorRepository?.findByNameAndTemplate(check, template))?.let {
                    templateDetectorRepository?.delete(it)
                }
            }
        }
    }

    /**
     * Make a copy the template
     *
     * @param account the account of the current user
     * @param templateRepository the database repository
     * @param tDetectorRepository the database repository
     * @param tParameterRepository the database repository
     *
     * @return the new template
     */
    fun copy(
        account: AccountWrapper?,
        templateRepository: TemplateRepository?,
        tDetectorRepository: TDetectorRepository?,
        tParameterRepository: TParameterRepository?
    ): Template {
        val template = Template()
        template.account = account?.account
        template.language = this.template.language
        template.isPublic = false
        template.name = this.template.name + " - Copy"
        templateRepository?.save(template)

        for (detector in this.template.detectors) {
            val newDetector = TDetector(detector.name, template)

            tDetectorRepository?.save(newDetector)

            for (parameter in detector.parameters) {
                val newParameter = TParameter(
                    parameter.name,
                    parameter.value,
                    parameter.postprocessing,
                    newDetector
                )

                tParameterRepository?.save(newParameter)
            }
        }

        return template
    }

    /**
     * Delete the template
     *
     * @param templateRepository the database repository
     *
     * @throws NotTemplateOwner if the user is not the template owner
     */
    @Throws(NotTemplateOwner::class)
    fun delete(templateRepository: TemplateRepository?) {
        if (!this.isOwner) throw NotTemplateOwner("You are not the owner of this template.")

        templateRepository?.delete(template)
    }

    companion object {
        /**
         * Get the list of templates that are public or owned by the user
         *
         * @param account the account of the current user
         * @param templateRepository the database repository
         *
         * @return the list of templates
         */
        fun findByAccountAndPublic(account: Account, templateRepository: TemplateRepository?): List<TemplateWrapper> {
            val wrapperList = mutableListOf<TemplateWrapper>()
            val templateList = templateRepository?.findByAccountAndPublic(account)
            return templateList?.mapNotNull {
                it?.let { TemplateWrapper(it, account) }
            } ?: wrapperList
        }

        /**
         * Get the list of templates that are public, owned by the user and filter
         * by the language supplied
         *
         * @param account the account of the current user
         * @param templateRepository the database repository
         * @param language the language to filter by
         *
         * @return the list of templates
         */
        fun findByAccountAndPublicAndLanguage(
            account: Account,
            templateRepository: TemplateRepository?,
            language: String?
        ): List<TemplateWrapper> {
            val wrapperList = mutableListOf<TemplateWrapper>()
            val templateList = templateRepository?.findByAccountAndPublicAndLanguage(account, language)
            return templateList?.mapNotNull {
                it?.let { TemplateWrapper(it, account) }
            } ?: wrapperList
        }
    }
}
