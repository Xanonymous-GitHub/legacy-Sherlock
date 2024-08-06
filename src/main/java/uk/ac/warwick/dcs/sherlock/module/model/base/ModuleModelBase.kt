package uk.ac.warwick.dcs.sherlock.module.model.base

import uk.ac.warwick.dcs.sherlock.api.annotation.EventHandler
import uk.ac.warwick.dcs.sherlock.api.annotation.SherlockModule
import uk.ac.warwick.dcs.sherlock.api.event.EventInitialisation
import uk.ac.warwick.dcs.sherlock.api.event.EventPreInitialisation
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry
import uk.ac.warwick.dcs.sherlock.module.model.base.detection.ASTDiffDetectorJavaShell
import uk.ac.warwick.dcs.sherlock.module.model.base.detection.NGramDetector
import uk.ac.warwick.dcs.sherlock.module.model.base.detection.VariableNameDetector
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer
import uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing.ASTDiffPostProcessor
import uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing.ASTDiffResult
import uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing.NGramPostProcessor
import uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing.NGramRawResult
import uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing.SimpleObjectEqualityPostProcessor
import uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing.SimpleObjectEqualityRawResult
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.CommentExtractor
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.CommentRemover
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.TrimWhitespaceOnly
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.VariableExtractor
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.VariableExtractorJava

@SherlockModule
class ModuleModelBase {
    @EventHandler
    fun initialisation(event: EventInitialisation?) {
        SherlockRegistry.registerGeneralPreProcessor(CommentExtractor::class.java)
        SherlockRegistry.registerGeneralPreProcessor(CommentRemover::class.java)
        SherlockRegistry.registerGeneralPreProcessor(TrimWhitespaceOnly::class.java)
        SherlockRegistry.registerAdvancedPreProcessorImplementation(
            "uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.VariableExtractor",
            VariableExtractorJava::class.java
        )

        SherlockRegistry.registerDetector(VariableNameDetector::class.java)
        SherlockRegistry.registerPostProcessor(
            SimpleObjectEqualityPostProcessor::class.java,
            SimpleObjectEqualityRawResult::class.java
        )

        SherlockRegistry.registerDetector(NGramDetector::class.java)
        SherlockRegistry.registerPostProcessor(NGramPostProcessor::class.java, NGramRawResult::class.java)

        @Suppress("DEPRECATION") // FIXME: support using kotlin in entire project !
        SherlockRegistry.registerDetector(ASTDiffDetectorJavaShell::class.java)
        SherlockRegistry.registerPostProcessor(ASTDiffPostProcessor::class.java, ASTDiffResult::class.java)
    }

    @EventHandler
    fun preInitialisation(event: EventPreInitialisation?) {
        SherlockRegistry.registerLanguage("Java", JavaLexer::class.java)

        //SherlockRegistry.registerLanguage("Haskell", HaskellLexer.class); -- found in Sherlock-Extra
        SherlockRegistry.registerAdvancedPreProcessorGroup(VariableExtractor::class.java)
    }
}
