package uk.ac.warwick.dcs.sherlock.api.report;

import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;
import uk.ac.warwick.dcs.sherlock.api.util.ITuple;

import java.util.List;

public interface ISubmissionMatchItem {

    /**
     * @return the ISourceFile this item bleongs to
     */
    ISourceFile file();

    /**
     * @return the line numbers the match was found in
     */
    List<ITuple<Integer, Integer>> lineNumbers();

    /**
     * @return the score for this file
     */
    float score();

}
