package uk.ac.warwick.dcs.sherlock.module.web.controllers.dashboard.workspace

import jakarta.servlet.http.HttpServletRequest
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import uk.ac.warwick.dcs.sherlock.api.component.IJob
import uk.ac.warwick.dcs.sherlock.engine.SherlockEngine
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.WorkspaceRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.results.JobResultsData
import uk.ac.warwick.dcs.sherlock.module.web.data.results.ResultsHelper
import uk.ac.warwick.dcs.sherlock.module.web.data.results.SubmissionResultsData
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.WorkspaceWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.*

/**
 * The controller that deals with the workspace results pages
 */
@Controller
class ResultsController {
    @Autowired
    private val workspaceRepository: WorkspaceRepository? = null

    /**
     * Handles GET requests to the results page
     *
     * @param model holder for model attributes
     * @param results the job results object
     *
     * @return the path to the results template
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}")
    fun viewGet(
        model: Model,
        @ModelAttribute("results") results: JobResultsData
    ): String {
        val status = SherlockEngine.executor.getJobStatus(results.job)

        if (status == null) {
            model.addAttribute("finished", true)
            model.addAttribute("status_message", "Finished")
            model.addAttribute("status_progress", 100)
        } else {
            model.addAttribute("finished", (status.message == "Finished"))
            model.addAttribute("status_message", status.message)
            model.addAttribute("status_progress", status.progressInt)
        }

        return "dashboard/workspaces/results/view"
    }

    // @GetMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}/json")
    @RequestMapping(
        value = ["/dashboard/workspaces/manage/{pathid}/results/{jobid}/json"],
        method = [RequestMethod.GET],
        produces = ["application/json"]
    )
    @ResponseBody
    fun jsonGet(
        @ModelAttribute("results") results: JobResultsData
    ): String {
        val result = JSONObject()
        val status = SherlockEngine.executor.getJobStatus(results.job)

        if (status == null) {
            result.put("message", "Finished")
            result.put("progress", 100)
        } else {
            result.put("message", status.message)
            result.put("progress", status.progressInt)
        }

        return result.toString()
    }

    /**
     * Handles GET requests to the rerun page
     *
     * @return the path to the rerun template
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}/rerun")
    fun rerunGet(): String {
        return "dashboard/workspaces/results/rerun"
    }

    /**
     * Handles POST requests to the rerun page
     *
     * @param pathid the workspace id
     * @param jobid the job id
     * @param results the job results object
     *
     * @return redirect to the job results page
     */
    @PostMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}/rerun")
    fun rerunPost(
        @PathVariable(value = "pathid") pathid: Long,
        @PathVariable(value = "jobid") jobid: Long,
        @ModelAttribute("results") results: JobResultsData
    ): String {
        SherlockEngine.executor.submitJob(results.job)
        return "redirect:/dashboard/workspaces/manage/$pathid/results/$jobid"
    }

    @PostMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}/dismiss")
    fun dismissPost(
        @PathVariable(value = "pathid") pathid: Long,
        @PathVariable(value = "jobid") jobid: Long,
        @ModelAttribute("results") results: JobResultsData
    ): String {
        val status = SherlockEngine.executor.getJobStatus(results.job)

        var msg = ""
        if (status != null) {
            if (status.isFinished) {
                SherlockEngine.executor.dismissJob(status)
                msg = "?msg=dismissed_job"
            } else {
                SherlockEngine.executor.cancelJob(status)
                msg = "?msg=cancelled_job"
            }
        }

        return "redirect:/dashboard/workspaces/manage/$pathid/results/$jobid$msg"
    }

    /**
     * Handles GET requests to the graph fragment
     *
     * @param isAjax whether or not the request was ajax or not
     * @param pathid the id of the workspace
     * @param jobid the id of the job
     *
     * @return the path to the graph fragment template
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @RequestMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}/graph")
    @Throws(NotAjaxRequest::class)
    fun graphGetFragment(
        @ModelAttribute("isAjax") isAjax: Boolean,
        @PathVariable(value = "pathid") pathid: Long,
        @PathVariable(value = "jobid") jobid: Long
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/workspaces/manage/$pathid/results/$jobid")
        return "dashboard/workspaces/results/fragments/graph"
    }

    /**
     * Handles GET requests to the network graph page
     *
     * @param model holder for model attributes
     * @param request the http request information
     *
     * @return the path to the graph template
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}/network")
    fun networkGet(model: Model, request: HttpServletRequest): String {
        if (request.parameterMap.containsKey("start")) {
            model.addAttribute("start", request.parameterMap["start"])
        } else {
            model.addAttribute("start", "-1")
        }

        return "dashboard/workspaces/results/network"
    }

    /**
     * Handles GET requests to the report submission page
     *
     * @param workspaceWrapper the workspace being managed
     * @param id the id of the submission to report on
     * @param model holder for model attributes
     *
     * @return the path to the report template
     *
     * @throws SubmissionNotFound if the submission was not found
     * @throws MapperException if there was an issue initialising the line mappers
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}/report/{submission}")
    @Throws(
        SubmissionNotFound::class, MapperException::class
    )
    fun reportGet(
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper?,
        @ModelAttribute("results") jobResultsData: JobResultsData,
        @PathVariable(value = "submission") id: Long,
        model: Model
    ): String {
        val submission = ResultsHelper.getSubmission(workspaceWrapper, id)

        val wrapper = SubmissionResultsData(jobResultsData.job, submission)

        model.addAttribute("submission", submission)
        model.addAttribute("wrapper", wrapper)
        return "dashboard/workspaces/results/report"
    }

    /**
     * Handles GET requests for the compare submission page
     *
     * @param workspaceWrapper the workspace being managed
     * @param id1 the first submission to compare
     * @param id2 the second submission to compare
     * @param model holder for model attributes
     *
     * @return the path to the compare template
     *
     * @throws SubmissionNotFound if the submission was not found
     * @throws MapperException if there was an issue initialising the line mappers
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}/compare/{submission1}/{submission2}")
    @Throws(
        SubmissionNotFound::class, MapperException::class, CompareSameSubmission::class
    )
    fun comparisonGet(
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper?,
        @ModelAttribute("results") jobResultsData: JobResultsData,
        @PathVariable(value = "submission1") id1: Long,
        @PathVariable(value = "submission2") id2: Long,
        model: Model
    ): String {
        if (id1 == id2) {
            throw CompareSameSubmission("You cannot compare a submission with itself")
        }

        val submission1 = ResultsHelper.getSubmission(workspaceWrapper, id1)
        val submission2 = ResultsHelper.getSubmission(workspaceWrapper, id2)

        val wrapper = SubmissionResultsData(jobResultsData.job, submission1, submission2)

        model.addAttribute("submission1", submission1)
        model.addAttribute("submission2", submission2)
        model.addAttribute("wrapper", wrapper)
        return "dashboard/workspaces/results/compare"
    }

    /**
     * Handles GET requests to the delete results page
     *
     * @return the path to the delete template
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}/delete")
    fun deleteGet(): String {
        return "dashboard/workspaces/results/delete"
    }

    /**
     * Handles GET requests to the delete results page
     *
     * @param workspaceWrapper the workspace being managed
     * @param resultsWrapper the results to delete
     *
     * @return the path to the delete template
     */
    @PostMapping("/dashboard/workspaces/manage/{pathid}/results/{jobid}/delete")
    fun deletePost(
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper,
        @ModelAttribute("results") resultsWrapper: JobResultsData
    ): String {
        resultsWrapper.job.remove()
        return "redirect:/dashboard/workspaces/manage/" + workspaceWrapper.id + "?msg=deleted_job"
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

    /**
     * Gets the results for the job with the id "jobid"
     *
     * @param workspaceWrapper the workspace being managed
     * @param jobid the job id to find the results
     * @param model holder for model attributes
     *
     * @return the job results wrapper
     *
     * @throws ResultsNotFound if the job was not found in the database
     */
    @ModelAttribute("results")
    @Throws(ResultsNotFound::class)
    private fun getResults(
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper,
        @PathVariable(value = "jobid") jobid: Long,
        model: Model
    ): JobResultsData {
        var iJob: IJob? = null

        for (job in workspaceWrapper.jobs) if (job.persistentId == jobid) iJob = job


        if (iJob == null) throw ResultsNotFound("Result not found")

        val wrapper = JobResultsData(iJob)
        model.addAttribute("results", wrapper)
        return wrapper
    }
}