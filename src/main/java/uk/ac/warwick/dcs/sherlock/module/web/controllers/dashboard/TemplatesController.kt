package uk.ac.warwick.dcs.sherlock.module.web.controllers.dashboard

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
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TemplateRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.EngineDetectorWrapper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.TemplateWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotAjaxRequest
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotTemplateOwner

/**
 * The controller that deals with the templates pages
 */
@Controller
class TemplatesController {
    @Autowired
    private val templateRepository: TemplateRepository? = null

    @Autowired
    private val tDetectorRepository: TDetectorRepository? = null

    /**
     * Handles GET requests to the templates page
     *
     * @return the path to the templates page template
     */
    @GetMapping("/dashboard/templates")
    fun indexGet(): String {
        return "dashboard/templates/index"
    }

    /**
     * Handles GET requests to the template list page
     *
     * @param account the account of the current user
     * @param isAjax whether the request was ajax or not
     * @param model holder for model attributes
     *
     * @return the path to the template list page template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/dashboard/templates/list")
    @Throws(NotAjaxRequest::class)
    fun listGetFragment(
        @ModelAttribute("account") account: AccountWrapper,
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/templates")

        model.addAttribute(
            "templates",
            templateRepository?.let { TemplateWrapper.findByAccountAndPublic(account.account, it) }
        )

        return "dashboard/templates/fragments/list"
    }

    /**
     * Handles GET requests to the add template page
     *
     * @param model holder for model attributes
     *
     * @return the path to the add template page template
     */
    @GetMapping("/dashboard/templates/add")
    fun addGet(model: Model): String {
        val languages = SherlockRegistry.getLanguages()
        val language = languages.iterator().next()
        model.addAttribute("templateForm", TemplateForm(language))
        model.addAttribute("detectorList", EngineDetectorWrapper.getDetectors(language))
        model.addAttribute("languageList", languages)
        return "dashboard/templates/add"
    }

    /**
     * Handles POST requests to the add template page
     *
     * @param templateForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param account the account of the current user
     * @param isAjax whether the request was ajax or not
     * @param model holder for model attributes
     *
     * @return the path to the add template page template
     *
     * @throws NotTemplateOwner if the user attempts to modify a template that is public and not theirs
     */
    @PostMapping("/dashboard/templates/add")
    @Throws(NotTemplateOwner::class)
    fun addPost(
        @ModelAttribute templateForm: @Valid TemplateForm,
        result: BindingResult,
        @ModelAttribute("account") account: AccountWrapper,
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (result.hasErrors()) {
            model.addAttribute("detectorList", EngineDetectorWrapper.getDetectors(templateForm.getLanguage()))
            model.addAttribute("languageList", SherlockRegistry.getLanguages())
            return "dashboard/templates/add"
        }

        assert(templateRepository != null)
        assert(tDetectorRepository != null)

        val templateWrapper = TemplateWrapper(templateForm, account.account, templateRepository, tDetectorRepository)
        return "redirect:/dashboard/templates/manage/" + (templateWrapper.template.id)
    }

    /**
     * Handles GET requests to the detectors list page, which lists
     * the detectors available for the specified language
     *
     * @param model holder for model attributes
     * @param isAjax whether the request was ajax or not
     * @param language the language to fetch the detectors for
     *
     * @return the path to the detectors list template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/dashboard/templates/detectors/{language}")
    @Throws(NotAjaxRequest::class)
    fun detectorsGetFragment(
        model: Model,
        @ModelAttribute("isAjax") isAjax: Boolean,
        @PathVariable("language") language: String?
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/templates")

        model.addAttribute("detectorList", EngineDetectorWrapper.getDetectors(language))
        return "dashboard/templates/fragments/details_detectors"
    }
}
