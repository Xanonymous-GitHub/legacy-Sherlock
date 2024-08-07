package uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing

import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.IPostProcessor
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.ModelTaskProcessedResults

class ASTDiffPostProcessor: IPostProcessor<ASTDiffResult> {
    override fun processResults(
        files: List<ISourceFile>,
        rawResults: List<ASTDiffResult>,
    ): ModelTaskProcessedResults {
        println(">>> ASTDiffPostProcessor.processResults")
        rawResults.forEach {
            println(it.editScript?.filter { action ->
                action.node.info.posOfLine != -1
            })
            println(it.detectorParams)
        }
        return ModelTaskProcessedResults()
    }
}