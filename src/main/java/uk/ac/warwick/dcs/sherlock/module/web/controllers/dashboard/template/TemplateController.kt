package uk.ac.warwick.dcs.sherlock.module.web.controllers.dashboard.template

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.TemplateForm
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TDetectorRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TParameterRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TemplateRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.EngineDetectorWrapper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.TemplateWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotAjaxRequest
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotTemplateOwner
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.TemplateNotFound

/**
 * The controller that deals with the manage template pages
 */
@Controller
class TemplateController {
    @Autowired
    private val templateRepository: TemplateRepository? = null

    @Autowired
    private val tDetectorRepository: TDetectorRepository? = null

    @Autowired
    private val tParameterRepository: TParameterRepository? = null

    /**
     * Handles GET requests to the manage template page
     *
     * @return the path to the manage template
     */
    @GetMapping("/dashboard/templates/manage/{pathid}")
    fun manageGet(): String {
        return "dashboard/templates/manage"
    }

    /**
     * Handles GET requests to the template details page
     *
     * @param pathid the id of the template
     * @param templateWrapper the template being managed
     * @param isAjax whether the request was ajax or not
     * @param model holder for model attributes
     *
     * @return the path to the details template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/dashboard/templates/manage/{pathid}/details")
    @Throws(NotAjaxRequest::class)
    fun detailsGetFragment(
        @PathVariable("pathid") pathid: Long,
        @ModelAttribute("template") templateWrapper: TemplateWrapper,
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/templates/manage/$pathid")

        model.addAttribute("templateForm", TemplateForm(templateWrapper))
        model.addAttribute("detectorList", EngineDetectorWrapper.getDetectors(templateWrapper.template.language))
        model.addAttribute("languageList", SherlockRegistry.getLanguages())
        return "dashboard/templates/fragments/details"
    }

    /**
     * Handles POST requests to the details template page
     *
     * @param pathid the id of the template
     * @param templateWrapper the template being managed
     * @param isAjax  whether the request was ajax or not
     * @param templateForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param model holder for model attributes
     *
     * @return the path to the details template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     * @throws NotTemplateOwner if the user attempts to modify a template that is public and not theirs
     */
    @PostMapping("/dashboard/templates/manage/{pathid}/details")
    @Throws(NotAjaxRequest::class, NotTemplateOwner::class)
    fun detailsPostFragment(
        @PathVariable("pathid") pathid: Long,
        @ModelAttribute("template") templateWrapper: TemplateWrapper,
        @ModelAttribute("isAjax") isAjax: Boolean,
        @ModelAttribute templateForm: @Valid TemplateForm,
        result: BindingResult,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/templates/manage/$pathid")

        if (!result.hasErrors()) {
            templateWrapper.update(templateForm, templateRepository, tDetectorRepository)
            model.addAttribute("success_msg", "templates.details.updated")
        }

        model.addAttribute("templateForm", templateForm)
        model.addAttribute("detectorList", EngineDetectorWrapper.getDetectors(templateWrapper.template.language))
        model.addAttribute("languageList", SherlockRegistry.getLanguages())
        return "dashboard/templates/fragments/details"
    }

    /**
     * Handles GET requests to the template detectors page
     *
     * @param pathid the id of the template
     * @param templateWrapper the template being managed
     * @param isAjax whether the request was ajax or not
     *
     * @return the path of the detectors template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/dashboard/templates/manage/{pathid}/detectors")
    @Throws(NotAjaxRequest::class)
    fun detectorsGetFragment(
        @PathVariable("pathid") pathid: Long,
        @ModelAttribute("template") templateWrapper: TemplateWrapper?,
        @ModelAttribute("isAjax") isAjax: Boolean
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/templates/manage/$pathid")

        return "dashboard/templates/fragments/detectors"
    }

    /**
     * Handles POST requests to the copy template page
     *
     * @param account the account of the current user
     * @param templateWrapper the template being managed
     *
     * @return a redirect to the new template
     */
    @PostMapping("/dashboard/templates/{pathid}/copy")
    fun copyPost(
        @ModelAttribute("account") account: AccountWrapper?,
        @ModelAttribute("template") templateWrapper: TemplateWrapper
    ): String {
        val newTemplate = templateWrapper.copy(account, templateRepository, tDetectorRepository, tParameterRepository)
        return "redirect:/dashboard/templates/manage/" + newTemplate.id
    }

    /**
     * Handles GET requests to the delete template page
     *
     * @return the path to the delete page
     */
    @GetMapping("/dashboard/templates/{pathid}/delete")
    fun deleteGet(): String {
        return "dashboard/templates/delete"
    }

    /**
     * Handles POST requests to the delete template page
     *
     * @param templateWrapper the template being managed
     *
     * @return a redirect to the templates page
     *
     * @throws NotTemplateOwner if the user attempts to modify a template that is public and not theirs
     */
    @PostMapping("/dashboard/templates/{pathid}/delete")
    @Throws(NotTemplateOwner::class)
    fun deletePost(
        @ModelAttribute("template") templateWrapper: TemplateWrapper
    ): String {
        templateWrapper.delete(templateRepository)
        return "redirect:/dashboard/templates?msg=deleted_template"
    }

    /**
     * Handles GET requests to the template details fragment
     *
     * @param isAjax whether the request was ajax or not
     *
     * @return the path to the details template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/dashboard/templates/details/{pathid}")
    @Throws(NotAjaxRequest::class)
    fun templateDetailsGetFragment(@ModelAttribute("isAjax") isAjax: Boolean): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/workspaces")
        return "dashboard/templates/fragments/template_details"
    }

    /**
     * Gets the template where the id equals the "pathid" path variable
     *
     * @param account the account of the current user
     * @param pathid the template id from the path variable
     * @param model holder for model attributes
     *
     * @return the template wrapper
     *
     * @throws TemplateNotFound if the template was not found
     */
    @ModelAttribute("templateWrapper")
    @Throws(TemplateNotFound::class)
    private fun getTemplateWrapper(
        @ModelAttribute("account") account: AccountWrapper,
        @PathVariable(value = "pathid") pathid: Long,
        model: Model
    ): TemplateWrapper {
        val templateWrapper = TemplateWrapper(pathid, account.account, templateRepository)
        model.addAttribute("templateWrapper", templateWrapper)
        return templateWrapper
    }
}