package uk.ac.warwick.dcs.sherlock.module.web.controllers.dashboard.workspace

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.SubmissionsForm
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.WorkspaceForm
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TemplateRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.WorkspaceRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.TemplateWrapper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.WorkspaceWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.*

/**
 * The controller that deals with the manage workspace pages
 */
@Controller
class WorkspaceController {
    @Autowired
    private val workspaceRepository: WorkspaceRepository? = null

    @Autowired
    private val templateRepository: TemplateRepository? = null

    /**
     * Handles the GET request to the manage workspace page
     *
     * @return the path to the manage template
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}")
    fun manageGet(): String {
        return "dashboard/workspaces/manage"
    }

    /**
     * Handles the GET request to the workspace details fragment
     *
     * @param pathid the id of the workspace
     * @param workspaceWrapper the workspace being managed
     * @param isAjax whether the request was ajax or not
     * @param model holder for model attributes
     *
     * @return the path to the workspace details fragment
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/details")
    @Throws(NotAjaxRequest::class)
    fun detailsGetFragment(
        @PathVariable("pathid") pathid: Long,
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper?,
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/workspaces/manage/$pathid")

        model.addAttribute("workspaceForm", WorkspaceForm(workspaceWrapper))
        model.addAttribute("languageList", SherlockRegistry.getLanguages())
        return "dashboard/workspaces/fragments/details"
    }

    /**
     * Handles POST requests to the workspace details fragment
     *
     * @param pathid the id of the workspace
     * @param workspaceWrapper the workspace being managed
     * @param isAjax whether the request was ajax or not
     * @param workspaceForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param model holder for model attributes
     *
     * @return the path to the details fragment template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @PostMapping("/dashboard/workspaces/manage/{pathid}/details")
    @Throws(NotAjaxRequest::class)
    fun detailsPostFragment(
        @PathVariable("pathid") pathid: Long,
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper,
        @ModelAttribute("isAjax") isAjax: Boolean,
        @ModelAttribute workspaceForm: @Valid WorkspaceForm?,
        result: BindingResult,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/workspaces/manage/$pathid")

        if (!result.hasErrors()) {
            workspaceWrapper.set(workspaceForm)
            model.addAttribute("success_msg", "workspaces.details.updated")
        }

        model.addAttribute("languageList", SherlockRegistry.getLanguages())
        return "dashboard/workspaces/fragments/details"
    }

    /**
     * Handles GET requests to the upload submissions page
     *
     * @return the path to the upload template
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/submissions/upload")
    fun uploadGetFragment(): String {
        return "dashboard/workspaces/submissions/upload"
    }

    /**
     * Handles POST requests to the upload submissions page
     *
     * @param pathid the id of the workspace
     * @param workspaceWrapper the workspace being managed
     * @param isAjax whether the request was ajax or not
     * @param submissionsForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param model holder for model attributes
     *
     * @return the path to the upload confirmed template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @PostMapping("/dashboard/workspaces/manage/{pathid}/submissions/upload")
    @Throws(NotAjaxRequest::class)
    fun uploadPostFragment(
        @PathVariable("pathid") pathid: Long,
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper,
        @ModelAttribute("isAjax") isAjax: Boolean,
        @ModelAttribute submissionsForm: @Valid SubmissionsForm?,
        result: BindingResult,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/workspaces/manage/$pathid")

        if (!result.hasErrors()) {
            try {
                val collisions = workspaceWrapper.addSubmissions(submissionsForm)
                model.addAttribute("collisions", collisions)

                if (collisions.isEmpty()) {
                    model.addAttribute("success_msg", "workspaces.submissions.uploaded.no_dups")
                } else {
                    if (submissionsForm!!.getDuplicate() == 0) { //replace
                        model.addAttribute("success_msg", "workspaces.submissions.uploaded.replaced")
                    } else if (submissionsForm.getDuplicate() == 1) { //keep
                        model.addAttribute("success_msg", "workspaces.submissions.uploaded.kept")
                    } else { //merge
                        model.addAttribute("success_msg", "workspaces.submissions.uploaded.merged")
                    }
                }
            } catch (e: NoFilesUploaded) {
                result.reject("error.file.empty")
            } catch (e: FileUploadFailed) {
                result.reject("error.file.failed")
            }
        }

        return "dashboard/workspaces/submissions/uploadConfirm"
    }

    /**
     * Handles the GET requests for the submissions list fragment
     *
     * @param pathid the id of the workspace
     * @param isAjax whether the request was ajax or not
     *
     * @return the path to the submissions list template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/submissions")
    @Throws(NotAjaxRequest::class)
    fun submissionsGetFragment(
        @PathVariable("pathid") pathid: Long,
        @ModelAttribute("isAjax") isAjax: Boolean
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/workspaces/manage/$pathid")

        return "dashboard/workspaces/fragments/submissions"
    }

    /**
     * Handles GET requests to the run analysis fragment
     *
     * @param pathid the id of the workspace
     * @param workspaceWrapper the workspace being managed
     * @param account the account of the current user
     * @param isAjax whether the request was ajax or not
     * @param model holder for model attributes
     *
     * @return the path to the run template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/run")
    @Throws(NotAjaxRequest::class)
    fun runGetFragment(
        @PathVariable("pathid") pathid: Long,
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper,
        @ModelAttribute("account") account: AccountWrapper,
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/workspaces/manage/$pathid")

        assert(templateRepository != null)
        model.addAttribute(
            "templates",
            TemplateWrapper.findByAccountAndPublicAndLanguage(
                account.account,
                templateRepository,
                workspaceWrapper.language
            )
        )
        return "dashboard/workspaces/fragments/run"
    }

    /**
     * Handles POST requests to the run analysis fragment
     *
     * @param pathid the id of the workspace
     * @param template_id the id of the template to run
     * @param workspaceWrapper the workspace being managed
     * @param account the account of the current user
     * @param isAjax whether the request was ajax or not
     * @param model holder for model attributes
     *
     * @return the path to the run template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     * @throws TemplateNotFound if the template was not found
     */
    @PostMapping("/dashboard/workspaces/manage/{pathid}/run")
    @Throws(NotAjaxRequest::class, TemplateNotFound::class)
    fun runPostFragment(
        @PathVariable("pathid") pathid: Long,
        @RequestParam(value = "template_id", required = true) template_id: Long,
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper,
        @ModelAttribute("account") account: AccountWrapper,
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/workspaces/manage/$pathid")

        assert(templateRepository != null)
        val templateWrapper = TemplateWrapper(template_id, account.account, templateRepository)

        var jobId: Long = 0
        try {
            jobId = workspaceWrapper.runTemplate(templateWrapper)
        } catch (e: TemplateContainsNoDetectors) {
            model.addAttribute("warning_msg", "workspaces.analysis.no_detectors")
        } catch (e: ClassNotFoundException) {
            model.addAttribute("warning_msg", "workspaces.analysis.detector_missing")
        } catch (e: DetectorNotFound) {
            model.addAttribute("warning_msg", "workspaces.analysis.detector_missing")
        } catch (e: ParameterNotFound) {
            model.addAttribute("warning_msg", "workspaces.analysis.parameter_missing")
        } catch (e: NoFilesUploaded) {
            model.addAttribute("warning_msg", "workspaces.analysis.no_files")
        }

        if (jobId == 0L) {
            if (!model.containsAttribute("warning_msg")) {
                model.addAttribute("warning_msg", "workspaces.analysis.failed")
            }
            model.addAttribute("templates", TemplateWrapper.findByAccountAndPublic(account.account, templateRepository))
            return "dashboard/workspaces/fragments/run"
        } else {
            return "redirect:/dashboard/workspaces/manage/" + workspaceWrapper.id + "/results/" + jobId + "?msg=analysis_started"
        }
    }

    /**
     * Handles GET requests to the results fragment
     *
     * @param pathid the id of the workspace
     * @param workspaceWrapper the workspace being managed
     * @param isAjax whether the request was ajax or not
     * @param model holder for model attributes
     *
     * @return the path to the results fragment template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/results")
    @Throws(NotAjaxRequest::class)
    fun resultsGetFragment(
        @PathVariable("pathid") pathid: Long,
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper,
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/workspaces/manage/$pathid")

        val jobs = workspaceWrapper.getiWorkspace().jobs
        model.addAttribute("jobs", jobs)

        return "dashboard/workspaces/fragments/results"
    }

    /**
     * Handles GET requests to the delete workspace page
     *
     * @return the path to the delete template
     */
    @GetMapping("/dashboard/workspaces/{pathid}/delete")
    fun deleteGet(): String {
        return "dashboard/workspaces/delete"
    }

    /**
     * Handles POST requests to the delete workspace page
     *
     * @param workspaceWrapper the workspace being managed
     *
     * @return the path to the delete template
     */
    @PostMapping("/dashboard/workspaces/{pathid}/delete")
    fun deletePost(@ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper): String {
        workspaceWrapper.delete(workspaceRepository)
        return "redirect:/dashboard/workspaces?msg=deleted_workspace"
    }

    /**
     * Gets the workspace where the id equals the "pathid" path variable
     *
     * @param account the account of the current user
     * @param pathid the workspace id
     * @param model holder for model attributes
     *
     * @return the workspace wrapper
     *
     * @throws IWorkspaceNotFound if the workspace was not found in the Engine database
     * @throws WorkspaceNotFound if the workspace was not found in the web database
     */
    @ModelAttribute("workspace")
    @Throws(IWorkspaceNotFound::class, WorkspaceNotFound::class)
    private fun getWorkspaceWrapper(
        @ModelAttribute("account") account: AccountWrapper,
        @PathVariable(value = "pathid") pathid: Long,
        model: Model
    ): WorkspaceWrapper {
        val workspaceWrapper = WorkspaceWrapper(pathid, account.account, workspaceRepository)
        model.addAttribute("workspace", workspaceWrapper)
        return workspaceWrapper
    }
}