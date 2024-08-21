package uk.ac.warwick.dcs.sherlock.module.model.base.lang

import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.TokenStream

/**
 * All parser methods that used in grammar (p, prev, notLineTerminator, etc.)
 * should start with lower case char similar to parser rules.
 */
abstract class TypeScriptParserBase(input: TokenStream?) : Parser(input) {
    /**
     * Short form for prev(String str)
     */
    protected fun p(str: String): Boolean {
        return prev(str)
    }

    /**
     * Whether the previous token value equals to @param str
     */
    private fun prev(str: String): Boolean {
        return _input.LT(-1).text == str
    }

    /**
     * Short form for next (String str)
     */
    protected fun n(str: String): Boolean {
        return next(str)
    }

    /**
     * Whether the next token value equals to @param str
     */
    protected fun next(str: String): Boolean {
        return _input.LT(1).text == str
    }

    protected fun notLineTerminator(): Boolean {
        return !here()
    }

    protected fun notOpenBraceAndNotFunction(): Boolean {
        val nextTokenType = _input.LT(1).type
        return nextTokenType != TypeScriptParser.OpenBrace && nextTokenType != TypeScriptParser.Function_
    }

    protected fun closeBrace(): Boolean {
        return _input.LT(1).type == TypeScriptParser.CloseBrace
    }

    /**
     * Returns `true` iff on the current index of the parser's
     * token stream a token of the given `type` exists on the
     * `HIDDEN` channel.
     *
     * @return `true` iff on the current index of the parser's
     * token stream a token of the given `type` exists on the
     * `HIDDEN` channel.
     */
    private fun here(): Boolean {
        // Get the token ahead of the current index.

        val possibleIndexEosToken = this.currentToken.tokenIndex - 1
        val ahead = _input[possibleIndexEosToken]

        // Check if the token resides on the HIDDEN channel and if it's of the
        // provided type.
        return (ahead.channel == Lexer.HIDDEN) && (ahead.type == TypeScriptParser.LineTerminator)
    }

    /**
     * Returns `true` iff on the current index of the parser's
     * token stream a token exists on the `HIDDEN` channel which
     * either is a line terminator, or is a multi line comment that
     * contains a line terminator.
     *
     * @return `true` iff on the current index of the parser's
     * token stream a token exists on the `HIDDEN` channel which
     * either is a line terminator, or is a multi line comment that
     * contains a line terminator.
     */
    protected fun lineTerminatorAhead(): Boolean {
        // Get the token ahead of the current index.

        var possibleIndexEosToken = this.currentToken.tokenIndex - 1
        var ahead = _input[possibleIndexEosToken]

        if (ahead.channel != Lexer.HIDDEN) {
            // We're only interested in tokens on the HIDDEN channel.
            return false
        }

        if (ahead.type == TypeScriptParser.LineTerminator) {
            // There is definitely a line terminator ahead.
            return true
        }

        if (ahead.type == TypeScriptParser.WhiteSpaces) {
            // Get the token ahead of the current whitespaces.
            possibleIndexEosToken = this.currentToken.tokenIndex - 2
            ahead = _input[possibleIndexEosToken]
        }

        // Get the token's text and type.
        val text = ahead.text
        val type = ahead.type

        // Check if the token is, or contains a line terminator.
        return (type == TypeScriptParser.MultiLineComment && (text.contains("\r") || text.contains("\n"))) ||
            (type == TypeScriptParser.LineTerminator)
    }
}