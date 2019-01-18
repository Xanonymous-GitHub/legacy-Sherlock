package uk.ac.warwick.dcs.sherlock.engine.report;

import java.util.*;

/**
 * This class represents the report for a single file.
 */
public class FileReport {

	//The id for this file as generated by ISourceFile.getPersistentId()
	private long persistentId;

	/**
	 * The main text of the report. Each element in the list should be a separate component e.g. the first item describes one similarity, the second item describes, the next, etc.
	 */
	private List<String> reportBody;

	/**
	 * @param persistentId the unique identifier for the source file as generated by ISourceFile.getPersistentId()
	 */
	public FileReport(long persistentId) {
		this.persistentId = persistentId;
		reportBody = new ArrayList<String>();
	}

	/**
	 * Adds a single string of content to the report.
	 *
	 * @param content The paragraph/sentence/etc. of text to be added to the report.
	 */
	public void AddReportString(String content) {
		reportBody.add(content);
	}

	/**
	 * Adds multiple strings of content to the report.
	 *
	 * @param content A list of strings, each one being a single paragraph/sentence/etc. to be added to the report, in order.
	 */
	public void AddReportString(List<String> content) {
		reportBody.addAll(content);
	}
}
