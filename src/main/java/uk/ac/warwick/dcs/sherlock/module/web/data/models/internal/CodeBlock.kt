package uk.ac.warwick.dcs.sherlock.module.web.data.models.internal

/**
 * Code blocks used by the results section of the website
 */
class CodeBlock {
    /**
     * Get the start line number
     *
     * @return the line number
     */
    /**
     * The start line number of this code block
     */
    var startLine: Int
        private set

    /**
     * Get the end line number
     *
     * @return the line number
     */
    /**
     * The end line number of this code block
     */
    var endLine: Int
        private set

    /**
     * Get the match ID linked to this block
     *
     * @return the match ID
     */
    /**
     * If set, this is the ID of the FileMatch this code block links to
     */
    var matchId: Int
        private set

    /**
     * Initialise this code block without a match
     *
     * @param startLine the start line number
     * @param endLine the end line number
     */
    constructor(startLine: Int, endLine: Int) {
        this.startLine = startLine
        this.endLine = endLine
        this.matchId = 0
    }

    /**
     * Initialise this code block with a match
     *
     * @param startLine the start line number
     * @param endLine the end line number
     * @param matchId the id of the match linked to this block
     */
    constructor(startLine: Int, endLine: Int, matchId: Int) {
        this.startLine = startLine
        this.endLine = endLine
        this.matchId = matchId
    }

    /**
     * Generate a list of line numbers between the start and end
     * line numbers (inclusive)
     *
     * @return the list of line numbers
     */
    fun toLineNumList(): List<Int> {
        val list: MutableList<Int> = ArrayList()

        for (i in startLine..endLine) {
            list.add(i)
        }

        return list
    }

    /**
     * Compare this code block against another object to see if
     * they match
     *
     * @param other the object to compare against
     *
     * @return whether the two objects are equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val codeBlock = other as CodeBlock
        return startLine == codeBlock.startLine && endLine == codeBlock.endLine && matchId == codeBlock.matchId
    }

    override fun hashCode(): Int {
        var result = startLine
        result = 31 * result + endLine
        result = 31 * result + matchId
        return result
    }
}
