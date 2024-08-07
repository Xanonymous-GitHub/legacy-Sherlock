package uk.ac.warwick.dcs.sherlock.module.model.base.detection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import uk.ac.warwick.dcs.sherlock.api.model.detection.IDetector
import uk.ac.warwick.dcs.sherlock.api.model.detection.ModelDataItem
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.PreProcessingStrategy
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.TrimWhitespaceOnly

open class ASTDiffDetector : IDetector<ASTDiffDetectorWorker> {
    private fun <T> List<T>.pairCombinations(): List<Pair<T, T>> {
        if (isEmpty()) return emptyList()
        if (size == 1) return listOf(first() to first())
        return mapIndexed { index, left ->
            subList(index + 1, size).map { right -> left to right }
        }.flatten()
    }

    final override fun buildWorkers(data: List<ModelDataItem>): List<ASTDiffDetectorWorker> =
        runBlocking(Dispatchers.Default) {
            data.map {
                async { ASTDiffRegistry.transformToGumTreeFrom(it.file) }
            }.awaitAll()
        }.pairCombinations().map { ASTDiffDetectorWorker(it, this) }

    final override fun getDescription(): String = "Detects AST differences between submissions"

    final override fun getDisplayName(): String = "AST Diff Detector"

    final override fun getPreProcessors(): List<PreProcessingStrategy> {
        val strategy = PreProcessingStrategy.of("AST Diff strategy", TrimWhitespaceOnly::class.java)
        return listOf(strategy)
    }
}

\ No newline at end of file
