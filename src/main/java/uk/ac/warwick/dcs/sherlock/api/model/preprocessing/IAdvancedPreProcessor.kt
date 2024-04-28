package uk.ac.warwick.dcs.sherlock.api.model.preprocessing

import org.antlr.v4.runtime.Lexer
import uk.ac.warwick.dcs.sherlock.api.util.IndexedString

/**
 * Advanced preprocessor implementation, used to directly access and preprocess from a specific lexer
 *
 * @param Antlr lexer implementation (compiled)
 * */
interface IAdvancedPreProcessor<T : Lexer> {
    /**
     * Pre-process with a lexer
     *
     * @param lexer lexer instance
     * @return list of processed strings, indexed by line number
     */
    fun process(lexer: T): List<IndexedString>
}
