package uk.ac.warwick.dcs.sherlock.module.web.data.models.internal

import uk.ac.warwick.dcs.sherlock.module.web.data.results.ResultsHelper

/**
 * Stores the submission name, id and score to display on the
 * results section of the website
 */
class SubmissionScore(
    /**
     * The id of the submission
     */
    var id: Long,

    /**
     * The name of the submission
     */
    var name: String,

    /**
     * The score of this submission, which is either:
     * - the overall score if this object is stored in the key of the results map
     * - the relative score if this object is stored in the value of the results map
     */
    score: Float
) {
    var score: Float = score
        set(value) {
            field = Math.round(value).toFloat()
        }

    val scoreGroup: Int
        /**
         * All scores are grouped into 10 groups:
         * 0-10, 10-20, 20-30, 30-40, 40-50, 50-60, 60-70, 70-80, 80-90 or 90-100
         * Get the group this score belongs to.
         *
         * @return the score group
         */
        get() = ResultsHelper.getScoreGroup(score)
}
