package uk.ac.warwick.dcs.sherlock.module.web.data.models.internal

import org.json.JSONArray
import org.json.JSONObject
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile
import uk.ac.warwick.dcs.sherlock.api.util.ITuple
import uk.ac.warwick.dcs.sherlock.engine.report.SubmissionMatch
import uk.ac.warwick.dcs.sherlock.module.web.data.results.ResultsHelper
import java.util.function.Consumer

/**
 * Stores the details of a match between two files
 */
class FileMatch(match: SubmissionMatch) {
    /**
     * Get the id of this match
     *
     * @return the id
     */
    /**
     * The ID of this file match
     */
    var id: Int = 0
        /**
         * Set the id for this match, also updates the colour
         *
         * @param id the new id
         */
        set(id) {
            field = id
            this.colour = ResultsHelper.getColour(id)
        }

    /**
     * The map of files involved in the match to the matched blocks
     */
    private val map: MutableMap<ISourceFile, List<CodeBlock>> =
        HashMap()

    /**
     * Get the reason for this match
     *
     * @return the reason text
     */
    /**
     * The reason this match was detected
     */
    val reason: String = match.reason()

    /**
     * Get the score for this match
     *
     * @return the score
     */
    /**
     * The score associated with this match
     */
    var score: Float = 0f

    /**
     * Get the highlight CSS colour
     *
     * @return the colour
     */
    /**
     * The colour to highlight the lines of this match
     */
    var colour: String
        private set

    /**
     * Initialise this match
     *
     * @param match the engine match to initialise this object with
     */
    init {
        for (item in match.items()) {
            val blocks: MutableList<CodeBlock> = ArrayList()
            item.lineNumbers().forEach(Consumer { t: ITuple<Int?, Int?> ->
                blocks.add(
                    CodeBlock(
                        t.key!!, t.value!!
                    )
                )
            })
            map[item.file()] = blocks
            this.score = item.score() * 100
        }

        //        this.score = match.getScore();

        //Generate a random colour
        this.colour = ResultsHelper.randomColour()
    }

    /**
     * Get the map
     *
     * @return the map
     */
    fun getMap(): Map<ISourceFile, List<CodeBlock>> {
        return map
    }

    /**
     * Get the list of code blocks for a file
     *
     * @param file the file to get the blocks for
     *
     * @return the list
     */
    private fun getCodeBlocks(file: ISourceFile): List<CodeBlock> {
        return map.getOrDefault(file, ArrayList())
    }

    /**
     * Get a string listing all the line numbers involved from the file
     *
     * @param file the file to get the list for
     *
     * @return the comma separated list
     */
    fun getFileLines(file: ISourceFile): String {
        return getLines(getCodeBlocks(file))
    }

    /**
     * Given a list of code blocks, generates a comma separated string
     * of the line numbers involved in this match
     *
     * e.g. 2,5-10,19-20
     *
     * @param list the list of code blocks to use
     *
     * @return the comma separated list
     */
    private fun getLines(list: List<CodeBlock>): String {
        val lines: MutableList<String> = mutableListOf()

        for (cb in list) {
            if (cb.startLine == cb.endLine) {
                lines.add(cb.startLine.toString() + "")
            } else {
                lines.add(cb.startLine.toString() + "-" + cb.endLine)
            }
        }

        return lines.joinToString(", ")
    }

    /**
     * Convert this object to a JSON object, used by the JavaScript in the UI
     *
     * @return the JSON equivalent of this object
     */
    fun toJSON(): JSONObject {
        val result = JSONObject()
        result.put("reason", reason)
        result.put("score", score)
        result.put("colour", colour)

        val matches = JSONArray()
        for ((entryFile, entryList) in map) {
            val match = JSONObject()

            match.put("id", entryFile.persistentId)
            match.put("name", entryFile.fileIdentifier)
            match.put("displayName", entryFile.fileDisplayName)
            match.put("submission", entryFile.submission.id)
            match.put("submissionName", entryFile.submission.name)

            val lines: MutableSet<Int?> = mutableSetOf()
            entryList.forEach(Consumer { cb: CodeBlock -> lines.addAll(cb.toLineNumList()) })
            match.put("lines", JSONArray(lines))

            matches.put(match)
        }

        result.put("matches", matches)

        return result
    }
}
