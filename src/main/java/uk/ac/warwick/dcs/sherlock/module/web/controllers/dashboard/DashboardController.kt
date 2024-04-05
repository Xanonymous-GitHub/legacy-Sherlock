package uk.ac.warwick.dcs.sherlock.module.web.controllers.dashboard

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import uk.ac.warwick.dcs.sherlock.api.component.IWorkspace
import uk.ac.warwick.dcs.sherlock.api.executor.IJobStatus
import uk.ac.warwick.dcs.sherlock.engine.SherlockEngine
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotAjaxRequest

/**
 * The controller that deals with the dashboard homepage
 */
@Controller
class DashboardController {
    /**
     * Handles GET requests to the dashboard home
     *
     * @return the path to the dashboard page
     */
    @GetMapping("/dashboard/index")
    fun index(): String {
        return "dashboard/index"
    }

    @GetMapping("/dashboard/index/queue")
    @Throws(NotAjaxRequest::class)
    fun queueGetFragment(
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/index")

        updateModel(model)
        return "dashboard/fragments/queue"
    }

    @PostMapping("/dashboard/index/queue/{id}")
    @Throws(NotAjaxRequest::class)
    fun queuePostFragment(
        @ModelAttribute("isAjax") isAjax: Boolean,
        @PathVariable("id") id: Long,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/index")

        var list = SherlockEngine.executor.allJobStatuses
        list = list.stream().filter { j: IJobStatus -> j.id.toLong() == id }.toList()

        if (list.size == 1) {
            val jobStatus = list.first()

            if (jobStatus.isFinished) {
                SherlockEngine.executor.dismissJob(jobStatus)
                model.addAttribute("success_msg", "messages.dismissed_job")
            } else {
                SherlockEngine.executor.cancelJob(jobStatus)
                model.addAttribute("success_msg", "messages.cancelled_job")
            }
        }

        updateModel(model)
        return "dashboard/fragments/queue"
    }

    @GetMapping("/dashboard/index/statistics")
    @Throws(NotAjaxRequest::class)
    fun statisticsGetFragment(
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/dashboard/index")

        val workspaces = SherlockEngine.storage.workspaces
        model.addAttribute("workspaces", workspaces.size)
        model.addAttribute("submissions", workspaces.stream().mapToInt { w: IWorkspace -> w.submissions.size }.sum())

        return "dashboard/fragments/statistics"
    }

    private fun updateModel(model: Model): Model {
        model.addAttribute(
            "jobs",
            SherlockEngine.executor.allJobStatuses
        )

        model.addAttribute("executor", SherlockEngine.executor)

        return model
    }
}