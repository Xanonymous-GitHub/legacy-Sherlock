package uk.ac.warwick.dcs.sherlock.engine.report;

import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;
import uk.ac.warwick.dcs.sherlock.api.report.ISubmissionMatchItem;
import uk.ac.warwick.dcs.sherlock.api.util.ITuple;

import java.util.List;

/**
 * Stored by SubmissionMatch to ensure data for a given file remains together.
 *
 * @param file        The file this item belongs to
 * @param score       The score for this file, for the given block
 * @param lineNumbers The line numbers in this file where the match is
 */
public record SubmissionMatchItem(
        ISourceFile file,
        float score,
        List<ITuple<Integer, Integer>> lineNumbers
) implements ISubmissionMatchItem {

    /**
     * Initialise a new SubmissionMatchItem.
     *
     * @param file        The file the match was found in
     * @param score       The score assigned to this match
     * @param lineNumbers The location of the match in the file
     */
    public SubmissionMatchItem {
    }

    /**
     * @return the ISourceFile this item bleongs to
     */
    @Override
    public ISourceFile file() {
        return this.file;
    }

    /**
     * @return the line numbers the match was found in
     */
    @Override
    public List<ITuple<Integer, Integer>> lineNumbers() {
        return this.lineNumbers;
    }

    /**
     * @return the score for this file
     */
    @Override
    public float score() {
        return this.score;
    }
}
