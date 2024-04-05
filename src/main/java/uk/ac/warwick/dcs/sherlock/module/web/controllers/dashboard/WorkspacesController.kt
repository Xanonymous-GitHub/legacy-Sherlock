package uk.ac.warwick.dcs.sherlock.module.web.controllers.dashboard

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.WorkspaceForm
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.WorkspaceRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.WorkspaceWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotAjaxRequest

/**
 * The controller that deals with the workspaces page
 */
@Controller
class WorkspacesController {
    @Autowired
    private val workspaceRepository: WorkspaceRepository? = null

    /**
     * Handles GET requests to the workspaces page
     *
     * @return the path to the workspaces template
     */
    @GetMapping("/dashboard/workspaces")
    fun indexGet(): String {
        return "dashboard/workspaces/index"
    }

    /**
     * Handles GET requests to the workspaces list fragment
     *
     * @param account the account object of the current user
     * @param isAjax whether the request was ajax or not
     * @param model holder for model attributes
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     *
     * @return the path to the list fragment template
     */
    @GetMapping("/dashboard/workspaces/list")
    @Throws(NotAjaxRequest::class)
    fun listGetFragment(
        @ModelAttribute("account") account: AccountWrapper,
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/workspaces")

        model.addAttribute(
            "workspaces",
            WorkspaceWrapper.findByAccount(account.account, workspaceRepository)
        )

        return "dashboard/workspaces/fragments/list"
    }

    /**
     * Handles GET requests to the add workspace page
     *
     * @param model holder for model attributes
     *
     * @return the path to the add workspace template
     */
    @GetMapping("/dashboard/workspaces/add")
    fun addGet(model: Model): String {
        model.addAttribute("workspaceForm", WorkspaceForm())
        model.addAttribute("languageList", SherlockRegistry.getLanguages())
        return "dashboard/workspaces/add"
    }

    /**
     * Handles POST requests to the add workspace page
     *
     * @param workspaceForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param account the account object of the current user
     * @param model holder for model attributes
     *
     * @return the path to the add workspace template
     */
    @PostMapping("/dashboard/workspaces/add")
    fun addPost(
        @ModelAttribute workspaceForm: @Valid WorkspaceForm?,
        result: BindingResult,
        @ModelAttribute("account") account: AccountWrapper,
        model: Model
    ): String {
        if (result.hasErrors()) {
            model.addAttribute("languageList", SherlockRegistry.getLanguages())
            return "dashboard/workspaces/add"
        }

        val workspaceWrapper = WorkspaceWrapper(workspaceForm, account.account, workspaceRepository)
        return "redirect:/dashboard/workspaces/manage/" + workspaceWrapper.id
    }
}
