package uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing

import tw.xcc.gumtree.model.operations.Action
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.AbstractModelTaskRawResult

data class ASTDiffResult(
    val editScript: List<Action>? = null
) : AbstractModelTaskRawResult() {
    override fun isEmpty(): Boolean = editScript == null

    override fun testType(baseline: AbstractModelTaskRawResult?): Boolean {
        return baseline is ASTDiffResult
    }
}
