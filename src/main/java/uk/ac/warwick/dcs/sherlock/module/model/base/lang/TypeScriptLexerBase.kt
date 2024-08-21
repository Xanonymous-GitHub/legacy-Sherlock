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
abstract class TypeScriptLexerBase(input: CharStream) : Lexer(input) {
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
     * Keeps track of the current depth of nested template string backticks.
     * E.g., after the X in:
     *
     *
     * `${a ? `${X
     *
     *
     * templateDepth will be 2. This variable is necessary to determine if a `}` is a
     * plain CloseBrace, or one that closes an expression inside a template string.
     */
    private var templateDepth = 0

    /**
     * Keeps track of the depth of open- and close-braces. Used for expressions like:
     *
     *
     * `${[1, 2, 3].map(x => { return x * 2;}).join("")}`
     *
     *
     * where the '}' from `return x * 2;}` should not become a `TemplateCloseBrace`
     * token but rather a `CloseBrace` token.
     */
    private var bracesDepth = 0

    fun setUseStrictDefault(value: Boolean) {
        strictDefault = value
        useStrictCurrent = value
    }

    fun IsStrictMode(): Boolean {
        return useStrictCurrent
    }

    fun StartTemplateString() {
        this.bracesDepth = 0
    }

    fun IsInTemplateString(): Boolean {
        return this.templateDepth > 0 && this.bracesDepth == 0
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
        bracesDepth++
        useStrictCurrent = !scopeStrictModes.isEmpty() && scopeStrictModes.peek() || strictDefault
        scopeStrictModes.push(useStrictCurrent)
    }

    protected fun ProcessCloseBrace() {
        bracesDepth--
        useStrictCurrent = if (!scopeStrictModes.isEmpty()) scopeStrictModes.pop() else strictDefault
    }

    protected fun ProcessStringLiteral() {
        if (lastToken == null || lastToken!!.type == TypeScriptLexer.OpenBrace) {
            val text = text
            if (text == "\"use strict\"" || text == "'use strict'") {
                if (!scopeStrictModes.isEmpty()) scopeStrictModes.pop()
                useStrictCurrent = true
                scopeStrictModes.push(true)
            }
        }
    }

    protected fun IncreaseTemplateDepth() {
        templateDepth++
    }

    protected fun DecreaseTemplateDepth() {
        templateDepth--
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
            TypeScriptLexer.Identifier, TypeScriptLexer.NullLiteral, TypeScriptLexer.BooleanLiteral, TypeScriptLexer.This, TypeScriptLexer.CloseBracket, TypeScriptLexer.CloseParen, TypeScriptLexer.OctalIntegerLiteral, TypeScriptLexer.DecimalLiteral, TypeScriptLexer.HexIntegerLiteral, TypeScriptLexer.StringLiteral, TypeScriptLexer.PlusPlus, TypeScriptLexer.MinusMinus ->  // After any of the tokens above, no regex literal can follow.
                false

            else ->  // In all other cases, a regex literal _is_ possible.
                true
        }
    }
}