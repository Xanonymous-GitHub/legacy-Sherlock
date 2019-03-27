package uk.ac.warwick.dcs.sherlock.engine.storage;

import uk.ac.warwick.dcs.sherlock.api.common.ICodeBlockGroup;
import uk.ac.warwick.dcs.sherlock.api.common.ISourceFileHelper;
import uk.ac.warwick.dcs.sherlock.engine.component.IResultJob;
import uk.ac.warwick.dcs.sherlock.api.common.ISubmission;
import uk.ac.warwick.dcs.sherlock.engine.component.IWorkspace;
import uk.ac.warwick.dcs.sherlock.engine.exception.ResultJobUnsupportedException;
import uk.ac.warwick.dcs.sherlock.engine.exception.SubmissionUnsupportedException;
import uk.ac.warwick.dcs.sherlock.engine.exception.WorkspaceUnsupportedException;
import uk.ac.warwick.dcs.sherlock.engine.report.ReportManager;

import java.util.*;

public interface IStorageWrapper extends ISourceFileHelper {

	/**
	 * Shutdown the database
	 */
	void close();

	/**
	 * Create a new submission, if one does not exist for the name in the workspace
	 *
	 * @param workspace      workspace to create submission in
	 * @param submissionName submission name to show to the user, should identify the file or files
	 *
	 * @return the new submission instance, or null if one of the same name already exists for the workspace
	 */
	ISubmission createSubmission(IWorkspace workspace, String submissionName) throws WorkspaceUnsupportedException;

	/**
	 * Create a new IWorkspace instance
	 *
	 * @param name name to create workspace under
	 * @param lang language of the workspace
	 *
	 * @return instance
	 */
	IWorkspace createWorkspace(String name, String lang);

	/**
	 * Fetches the class of ICodeBlockGroup used in this IStorageWrapper implementation
	 *
	 * @return class for ICodeBlockGroup
	 */
	Class<? extends ICodeBlockGroup> getCodeBlockGroupClass();

	/**
	 * Fetches a submission for the passed name if one exists, else returns null
	 *
	 * @param workspace      workspace to search for submission
	 * @param submissionName submission name
	 *
	 * @return submission if exists, else null
	 */
	ISubmission getSubmissionFromName(IWorkspace workspace, String submissionName) throws WorkspaceUnsupportedException;

	/**
	 * @param ids workspace ids to fetch
	 *
	 * @return a list of all workspaces matching the passed ids in the database
	 */
	List<IWorkspace> getWorkspaces(List<Long> ids);

	/**
	 * Get all stored workspaces
	 *
	 * @return a list of all workspaces in the database
	 */
	List<IWorkspace> getWorkspaces();

	/**
	 * Returns a prepared report manager for the IResultJob, uses cached instances were possible
	 * @param resultJob result to generate reports for
	 * @return instance of ReportManager
	 */
	ReportManager getReportGenerator(IResultJob resultJob) throws ResultJobUnsupportedException;

	/**
	 * Stores the passed code block groups to the database
	 *
	 * @param groups list of the code block groups to store
	 *
	 * @return was successful?
	 */
	boolean storeCodeBlockGroups(List<ICodeBlockGroup> groups);

	/**
	 * Store file in the database
	 *
	 * @param workspace   workspace to add file to
	 * @param filename    filename uploaded, used as the identifier to show to the user, identifying the file or files
	 * @param fileContent raw content of the file
	 */
	@Deprecated
	void storeFile(IWorkspace workspace, String filename, byte[] fileContent) throws WorkspaceUnsupportedException, SubmissionUnsupportedException;

	/**
	 * Store file to a submission
	 *
	 * @param submission  submission to add file to
	 * @param filename    filename uploaded, used as the identifier to show to the user, identifying the file or files
	 * @param fileContent raw content of the submission file
	 */
	void storeFile(ISubmission submission, String filename, byte[] fileContent) throws SubmissionUnsupportedException;

}