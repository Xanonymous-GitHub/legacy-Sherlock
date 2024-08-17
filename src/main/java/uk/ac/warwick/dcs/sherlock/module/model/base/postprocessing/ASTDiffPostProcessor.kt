package uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing

import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import tw.xcc.gumtree.model.operations.Action
import tw.xcc.gumtree.model.operations.SingleDeleteAction
import tw.xcc.gumtree.model.operations.SingleInsertAction
import tw.xcc.gumtree.model.operations.SingleUpdateAction
import tw.xcc.gumtree.model.operations.TreeDeleteAction
import tw.xcc.gumtree.model.operations.TreeInsertAction
import tw.xcc.gumtree.model.operations.TreeMoveAction
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.IPostProcessor
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.ModelTaskProcessedResults
import uk.ac.warwick.dcs.sherlock.api.util.Tuple

class ASTDiffPostProcessor : IPostProcessor<ASTDiffResult> {
    private suspend fun calculateActionsScore(
        actions: List<Action>,
        cardinality: Double
    ): Double = coroutineScope {
        return@coroutineScope actions.map {
            // TODO: The algorithm for calculating the score of each action.
            async { it.node.childCount() }
        }.awaitAll().sum() * cardinality
    }

    private fun calculateScore(rawResults: ASTDiffResult, maxScore: Int): Int {
        if (rawResults.isEmpty) return 0

        requireNotNull(rawResults.editScript)
        if (rawResults.editScript.isEmpty()) return maxScore

        val params = rawResults.detectorParams

        return runBlocking(Dispatchers.Default) {
            return@runBlocking maxScore - rawResults.editScript.groupBy { it }
                .map {
                    async {
                        when (it.key) {
                            is SingleInsertAction -> {
                                calculateActionsScore(it.value, params.scoreOfSingleInsertAction.toDouble())
                            }

                            is SingleUpdateAction -> {
                                calculateActionsScore(it.value, params.scoreOfSingleUpdateAction.toDouble())
                            }

                            is SingleDeleteAction -> {
                                calculateActionsScore(it.value, params.scoreOfSingleDeleteAction.toDouble())
                            }

                            is TreeMoveAction -> {
                                calculateActionsScore(it.value, params.scoreOfTreeMoveAction.toDouble())
                            }

                            is TreeInsertAction -> {
                                calculateActionsScore(it.value, params.scoreOfTreeInsertAction.toDouble())
                            }

                            is TreeDeleteAction -> {
                                calculateActionsScore(it.value, params.scoreOfTreeDeleteAction.toDouble())
                            }

                            else -> 0.0
                        }
                    }
                }.awaitAll().sum().roundToInt().coerceIn(0, maxScore)
        }
    }

    override fun processResults(
        files: List<ISourceFile>,
        rawResults: List<ASTDiffResult>,
    ): ModelTaskProcessedResults {
        val result = ModelTaskProcessedResults()
        rawResults.forEach {
            val file1LineNums = it.sourcePair.first.totalLineCount
            val file2LineNums = it.sourcePair.second.totalLineCount
            val maxScore = listOf(file1LineNums, file2LineNums).average().toInt() * FULL_SCORE_OF_EACH_LINE

            val score = calculateScore(it, maxScore)
                .toFloat()
                .div(maxScore.toFloat())

            println(
                "The plagiarism score between " +
                    "${it.sourcePair.first.fileDisplayName} and ${it.sourcePair.second.fileDisplayName} is:"
            )
            println(score)

            it.editScript?.forEach { action ->
                print("Action: ${action.name}, Node: [${action.node.info.type.name}] ")
                val line = action.node.info.line
                val posOfLine = action.node.info.posOfLine
                if (line != -1 && posOfLine != -1) {
                    print(action.node.info.text.encoded())
                    println(", Line: $line, Position: $posOfLine")
                } else {
                    println()
                }
            }

            val newResultGroup = result.addGroup()
            newResultGroup.addCodeBlock(
                it.sourcePair.first,
                score,
                Tuple(0, it.sourcePair.first.totalLineCount)
            )
            newResultGroup.addCodeBlock(
                it.sourcePair.second,
                score,
                Tuple(0, it.sourcePair.second.totalLineCount)
            )
            newResultGroup.comment = "AST Diff Match Group"
            newResultGroup.setDetectionType("BASE_BODY_REPLACE_CALL")

            // TODO: Show the result in the UI, and remove the above experimental code.
        }
        return result
    }

    private fun String.encoded(): String = Json.encodeToString(String.serializer(), this)

    companion object {
        private const val FULL_SCORE_OF_EACH_LINE = 30
    }
}