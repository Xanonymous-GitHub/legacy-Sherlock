package uk.ac.warwick.dcs.sherlock.module.model.base.detection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import tw.xcc.gumtree.DiffCalculator
import tw.xcc.gumtree.model.GumTree
import tw.xcc.gumtree.model.operations.Action
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile
import uk.ac.warwick.dcs.sherlock.api.model.detection.DetectorWorker
import uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing.ASTDiffResult

class ASTDiffDetectorWorker(
    private val treePair: Pair<GumTree, GumTree>,
    private val sourcePair: Pair<ISourceFile, ISourceFile>,
    parent: ASTDiffDetector,
) : DetectorWorker<ASTDiffResult>(parent) {
    private var editScript: List<Action>? = null
    private val result by lazy {
        ASTDiffResult(
            editScript,
            parent.params,
            sourcePair
        )
    }

    override fun execute() {
        if (treePair.first == treePair.second) {
            return
        }

        val diffCalculator = DiffCalculator()

        editScript = runBlocking(Dispatchers.Default) {
            diffCalculator.computeEditScriptFrom(treePair)
        }
    }

    override fun getRawResult(): ASTDiffResult = result
}