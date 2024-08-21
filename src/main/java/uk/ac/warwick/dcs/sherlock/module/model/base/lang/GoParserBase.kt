package uk.ac.warwick.dcs.sherlock.module.model.base.lang

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.TokenStream

/**
 * All parser methods that used in grammar (p, prev, notLineTerminator, etc.)
 * should start with lower case char similar to parser rules.
 */
abstract class GoParserBase protected constructor(input: TokenStream) : Parser(input) {
    /**
     * Returns true if the current Token is a closing bracket (")" or "}")
     */
    protected fun closingBracket(): Boolean {
        val stream = _input as BufferedTokenStream
        val prevTokenType = stream.LA(1)

        return prevTokenType == GoParser.R_CURLY || prevTokenType == GoParser.R_PAREN
    }
}
