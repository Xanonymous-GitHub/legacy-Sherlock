package uk.ac.warwick.dcs.sherlock.module.web.controllers.dashboard.workspace

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile
import uk.ac.warwick.dcs.sherlock.api.component.ISubmission
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.WorkspaceRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.results.ResultsHelper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.WorkspaceWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.IWorkspaceNotFound
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.SourceFileNotFound
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.SubmissionNotFound
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.WorkspaceNotFound

/**
 * The controller that deals with the workspace submission pages
 */
@Controller
class SubmissionController {
    @Autowired
    private val workspaceRepository: WorkspaceRepository? = null

    /**
     * Handles GET requests to the view submission page
     *
     * @return the path to the view template
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/submission/{submissionid}")
    fun viewGet(): String {
        return "dashboard/workspaces/submissions/view"
    }

    /**
     * Handles GET requests to the view file page
     *
     * @param submission the submission being managed
     * @param fileid     the id of the file
     * @return the plaintext contents of the file
     * @throws SourceFileNotFound if the file was not found
     */
    @GetMapping(
        value = ["/dashboard/workspaces/manage/{pathid}/submission/{submissionid}/file/{fileid}/{filename}"],
        produces = ["text/plain"]
    )
    @ResponseBody
    @Throws(
        SourceFileNotFound::class
    )
    fun fileGet(
        @ModelAttribute("submission") submission: ISubmission,
        @PathVariable(value = "fileid") fileid: Long,
        @ModelAttribute("isPrinting") isPrinting: Boolean
    ): String {
        val sourceFile = this.getFile(submission, fileid)

        if (isPrinting) {
            var result = ""
            var i = 1
            for (line in sourceFile.fileContentsAsStringList) {
                result += i.toString() + " | " + line + System.lineSeparator()
                i++
            }
            return result
        }

        return sourceFile.fileContentsAsString
    }

    /**
     * Handles GET requests to the delete submission page
     *
     * @return the path to the delete template
     */
    @GetMapping("/dashboard/workspaces/manage/{pathid}/submission/{submissionid}/delete")
    fun deleteGet(): String {
        return "dashboard/workspaces/submissions/delete"
    }

    /**
     * Handles POST requests to the delete submission page
     *
     * @param workspaceWrapper the workspace being managed
     * @param submission       the submission being managed
     * @return a direct to the workspace page
     */
    @PostMapping("/dashboard/workspaces/manage/{pathid}/submission/{submissionid}/delete")
    fun deletePost(
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper,
        @ModelAttribute("submission") submission: ISubmission
    ): String {
        submission.remove()
        return "redirect:/dashboard/workspaces/manage/" + workspaceWrapper.id + "?msg=deleted_submission"
    }

    /**
     * Handles GET requests to the delete file page
     *
     * @param submission the submission being managed
     * @param fileid     the id of the file to delete
     * @param model      holder for model attributes
     * @return the path to the delete file template
     * @throws SourceFileNotFound if the file doesn't exist
     */
    @GetMapping(value = ["/dashboard/workspaces/manage/{pathid}/submission/{submissionid}/file/{fileid}/{filename}/delete"])
    @Throws(
        SourceFileNotFound::class
    )
    fun deleteFileGet(
        @ModelAttribute("submission") submission: ISubmission,
        @PathVariable(value = "fileid") fileid: Long,
        model: Model
    ): String {
        val sourceFile = this.getFile(submission, fileid)
        model.addAttribute("file", sourceFile)
        return "dashboard/workspaces/submissions/deleteFile"
    }

    /**
     * Handles POST requests to the delete file page
     *
     * @param workspaceWrapper the workspace being managed
     * @param submission       the submission being managed
     * @param fileid           the id of the file to delete
     * @return a direct to the workspace page
     * @throws SourceFileNotFound if the file doesn't exist
     */
    @PostMapping(value = ["/dashboard/workspaces/manage/{pathid}/submission/{submissionid}/file/{fileid}/{filename}/delete"])
    @Throws(
        SourceFileNotFound::class
    )
    fun deleteFilePost(
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper,
        @ModelAttribute("submission") submission: ISubmission,
        @PathVariable(value = "fileid") fileid: Long
    ): String {
        val sourceFile = this.getFile(submission, fileid)
        sourceFile.remove()
        return "redirect:/dashboard/workspaces/manage/" + workspaceWrapper.id + "/submission/" + submission.id + "?msg=deleted_file"
    }

    /**
     * Gets the workspace where the id equals the "pathid" path variable
     *
     * @param account the account of the current user
     * @param pathid  the workspace id
     * @param model   holder for model attributes
     * @return the workspace wrapper
     * @throws IWorkspaceNotFound if the workspace was not found in the Engine database
     * @throws WorkspaceNotFound  if the workspace was not found in the web database
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
     * Gets the submission where the id equals the "submissionid" path variable
     *
     * @param workspaceWrapper the workspace being managed
     * @param submissionid     the id of the submission to find
     * @param model            holder for model attributes
     * @return the submission wrapper
     * @throws SubmissionNotFound if the submission was not found
     */
    @ModelAttribute("submission")
    @Throws(SubmissionNotFound::class)
    private fun getSubmission(
        @ModelAttribute("workspace") workspaceWrapper: WorkspaceWrapper,
        @PathVariable(value = "submissionid") submissionid: Long,
        model: Model
    ): ISubmission {
        val submission = ResultsHelper.getSubmission(workspaceWrapper, submissionid)
        model.addAttribute("submission", submission)
        return submission
    }

    /**
     * Gets the file where the id equals the "fileid" variable
     *
     * @param submission the submission to find the file in
     * @param fileid     the id of the file to delete
     * @return the file
     * @throws SourceFileNotFound if the file doesn't exist
     */
    @Throws(SourceFileNotFound::class)
    private fun getFile(submission: ISubmission, fileid: Long): ISourceFile {
        var sourceFile: ISourceFile? = null

        for (temp in submission.allFiles) {
            if (temp.persistentId == fileid) {
                sourceFile = temp
            }
        }

        if (sourceFile == null) {
            throw SourceFileNotFound("File not found")
        }

        return sourceFile
    }
}