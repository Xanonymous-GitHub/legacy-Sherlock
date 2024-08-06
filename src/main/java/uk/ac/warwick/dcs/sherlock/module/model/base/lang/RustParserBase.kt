package uk.ac.warwick.dcs.sherlock.module.model.base.lang

import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.TokenStream

abstract class RustParserBase(input: TokenStream) : Parser(input) {
    fun next(expect: Char): Boolean {
        return _input.LA(1) == expect.code
    }
}