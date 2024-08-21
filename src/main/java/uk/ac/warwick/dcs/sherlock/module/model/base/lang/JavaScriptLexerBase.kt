package uk.ac.warwick.dcs.sherlock.module.model.base.lang

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Token
import java.util.ArrayDeque
import java.util.Deque

/**
 * All lexer methods that used in grammar (IsStrictMode)
 * should start with Upper Case Char similar to Lexer rules.
 */
abstract class JavaScriptLexerBase(input: CharStream) : Lexer(input) {
    /**
     * Stores values of nested modes. By default, mode is strict or
     * defined externally (useStrictDefault)
     */
    private val scopeStrictModes: Deque<Boolean> = ArrayDeque()

    private var lastToken: Token? = null

    /**
     * The Default value of strict mode
     * Can be defined externally by setUseStrictDefault
     */
    private var strictDefault: Boolean = false

    /**
     * The Current value of strict mode
     * Can be defined during parsing, see StringFunctions.js and StringGlobal.js samples
     */
    private var useStrictCurrent = false

    /**
     * Preserves depth due to braces including template literals.
     */
    private var currentDepth = 0

    /**
     * Preserves the starting depth of template literals to correctly handle braces inside template literals.
     */
    private var templateDepthStack: Deque<Int> = ArrayDeque()

    fun IsStartOfFile(): Boolean {
        return lastToken == null
    }

    fun setUseStrictDefault(value: Boolean) {
        strictDefault = value
        useStrictCurrent = value
    }

    fun IsStrictMode(): Boolean {
        return useStrictCurrent
    }

    fun IsInTemplateString(): Boolean {
        return !templateDepthStack.isEmpty() && templateDepthStack.peek() == currentDepth
    }

    /**
     * Return the next token from the character stream and records this last
     * token in case it resides on the default channel. This recorded token
     * is used to determine when the lexer could possibly match a regex
     * literal. Also changes scopeStrictModes stack if tokenize special
     * string 'use strict';
     *
     * @return the next token from the character stream.
     */
    override fun nextToken(): Token {
        val next = super.nextToken()

        if (next.channel == Token.DEFAULT_CHANNEL) {
            // Keep track of the last token on the default channel.
            this.lastToken = next
        }

        return next
    }

    protected fun ProcessOpenBrace() {
        currentDepth++
        useStrictCurrent = !scopeStrictModes.isEmpty() && scopeStrictModes.peek() || strictDefault
        scopeStrictModes.push(useStrictCurrent)
    }

    protected fun ProcessCloseBrace() {
        useStrictCurrent = if (!scopeStrictModes.isEmpty()) scopeStrictModes.pop() else strictDefault
        currentDepth--
    }

    protected fun ProcessTemplateOpenBrace() {
        currentDepth++
        templateDepthStack.push(currentDepth)
    }

    protected fun ProcessTemplateCloseBrace() {
        templateDepthStack.pop()
        currentDepth--
    }

    protected fun ProcessStringLiteral() {
        if (lastToken == null || lastToken!!.type == JavaScriptLexer.OpenBrace) {
            val text = text
            if (text == "\"use strict\"" || text == "'use strict'") {
                if (!scopeStrictModes.isEmpty()) scopeStrictModes.pop()
                useStrictCurrent = true
                scopeStrictModes.push(true)
            }
        }
    }

    /**
     * Returns `true` if the lexer can match a regex literal.
     */
    protected fun IsRegexPossible(): Boolean {
        if (this.lastToken == null) {
            // No token has been produced yet: at the start of the input,
            // no division is possible, so a regex literal _is_ possible.
            return true
        }

        return when (lastToken!!.type) {
            JavaScriptLexer.Identifier, JavaScriptLexer.NullLiteral, JavaScriptLexer.BooleanLiteral, JavaScriptLexer.This, JavaScriptLexer.CloseBracket, JavaScriptLexer.CloseParen, JavaScriptLexer.OctalIntegerLiteral, JavaScriptLexer.DecimalLiteral, JavaScriptLexer.HexIntegerLiteral, JavaScriptLexer.StringLiteral, JavaScriptLexer.PlusPlus, JavaScriptLexer.MinusMinus ->  // After any of the tokens above, no regex literal can follow.
                false

            else ->  // In all other cases, a regex literal _is_ possible.
                true
        }
    }

    override fun reset() {
        scopeStrictModes.clear()
        this.lastToken = null
        this.strictDefault = false
        this.useStrictCurrent = false
        this.currentDepth = 0
        this.templateDepthStack = ArrayDeque()
        super.reset()
    }
}
