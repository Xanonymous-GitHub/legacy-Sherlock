package uk.ac.warwick.dcs.sherlock.module.model.base.detection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import tw.xcc.gumtree.model.GumTree
import uk.ac.warwick.dcs.sherlock.api.model.detection.IDetector
import uk.ac.warwick.dcs.sherlock.api.model.detection.ModelDataItem
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.PreProcessingStrategy
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.TrimWhitespaceOnly
import java.io.Serializable

open class ASTDiffDetector : IDetector<ASTDiffDetectorWorker> {
    var params = Params()
    protected lateinit var theShell: ASTDiffDetectorJavaShell

    private fun <T> List<T>.pairCombinations(): List<Pair<T, T>> {
        if (isEmpty()) return emptyList()
        if (size == 1) return listOf(first() to first())
        return mapIndexed { index, left ->
            subList(index + 1, size).map { right -> left to right }
        }.flatten()
    }

    final override fun buildWorkers(data: List<ModelDataItem>): List<ASTDiffDetectorWorker> =
        runBlocking(Dispatchers.Default) {
            params = params.copy(
                scoreOfSingleInsertAction = theShell.scoreOfSingleInsertAction,
                scoreOfSingleUpdateAction = theShell.scoreOfSingleUpdateAction,
                scoreOfSingleDeleteAction = theShell.scoreOfSingleDeleteAction,
                scoreOfTreeMoveAction = theShell.scoreOfTreeMoveAction,
                scoreOfTreeInsertAction = theShell.scoreOfTreeInsertAction,
                scoreOfTreeDeleteAction = theShell.scoreOfTreeDeleteAction,
            )
            data.sortedBy { it.file.fileDisplayName }.map {
                async { it.file to ASTDiffRegistry.transformToGumTreeFrom(it.file) }
            }.awaitAll()
        }.pairCombinations().map {
            ASTDiffDetectorWorker(
                GumTree(it.first.second) to GumTree(it.second.second),
                it.first.first to it.second.first,
                this
            )
        }

    final override fun getDescription(): String = "Detects AST differences between submissions"

    final override fun getDisplayName(): String = "AST Diff Detector"

    final override fun getPreProcessors(): List<PreProcessingStrategy> {
        val strategy = PreProcessingStrategy.of("AST Diff strategy", TrimWhitespaceOnly::class.java)
        return listOf(strategy)
    }

    data class Params @JvmOverloads constructor(
        val scoreOfSingleInsertAction: Float = 1f,
        val scoreOfSingleUpdateAction: Float = 1f,
        val scoreOfSingleDeleteAction: Float = 1f,
        val scoreOfTreeMoveAction: Float = 1f,
        val scoreOfTreeInsertAction: Float = 5f,
        val scoreOfTreeDeleteAction: Float = 5f,
    ) : Serializable
}