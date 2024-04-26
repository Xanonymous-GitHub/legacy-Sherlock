package uk.ac.warwick.dcs.sherlock.engine.report;

import uk.ac.warwick.dcs.sherlock.api.report.ISubmissionMatch;

import java.util.List;

/**
 * Object to be sent to web report pages, detailing a particular match between files in different submissions.
 *
 * @param reason The description of the type of plagiarism for this match (from DetectionType)
 * @param items  The contents of the matches; each has an ISourceFile, a score, and line numbers.
 */
public record SubmissionMatch(
        String reason,
        List<SubmissionMatchItem> items
) implements ISubmissionMatch<SubmissionMatchItem> {
    /**
     * Initialise a new SubmissionMatch object.
     *
     * @param reason description of plagiarism type
     * @param items  SubmissionMatchItems to populate this object with (see SubmissionMatchItem constructor)
     */
    public SubmissionMatch {
    }

    /**
     * @return the description for this match
     */
    @Override
    public String reason() {
        return this.reason;
    }


    /**
     * @return a list of SubmissionMatchItems, each containing an ISourceFile, a score, and a set of line numbers.
     */
    @Override
    public List<SubmissionMatchItem> items() {
        return this.items;
    }
}
