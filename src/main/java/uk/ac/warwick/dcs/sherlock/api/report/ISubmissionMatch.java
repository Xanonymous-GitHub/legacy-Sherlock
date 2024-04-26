package uk.ac.warwick.dcs.sherlock.api.report;

import java.util.List;

public interface ISubmissionMatch<T extends ISubmissionMatchItem> {

    /**
     * @return a list of SubmissionMatchItems, each containing an ISourceFile, a score, and a set of line numbers.
     */
    List<T> items();

    /**
     * @return the description for this match
     */
    String reason();

}
