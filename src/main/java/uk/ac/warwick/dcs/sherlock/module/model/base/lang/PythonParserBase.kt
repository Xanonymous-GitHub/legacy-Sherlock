package uk.ac.warwick.dcs.sherlock.module.model.base.lang

import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.TokenStream

abstract class PythonParserBase protected constructor(input: TokenStream) : Parser(input) {
    // https://docs.python.org/3/reference/lexical_analysis.html#soft-keywords
    fun isEqualToCurrentTokenText(tokenText: String): Boolean {
        return this.currentToken.text == tokenText
    }

    fun isnotEqualToCurrentTokenText(tokenText: String): Boolean {
        return !this.isEqualToCurrentTokenText(tokenText) // for compatibility with the Python 'not' logical operator
    }
}
