package uk.ac.warwick.dcs.sherlock.module.model.base.lang

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Token
import java.util.ArrayDeque
import java.util.Deque
import java.util.LinkedList

abstract class PythonLexerBase protected constructor(input: CharStream) : Lexer(input) {
    private val INVALID_LENGTH = -1
    private val ERR_TXT = " ERROR: "

    // A stack that keeps track of the indentation lengths
    private var indentLengthStack: Deque<Int>? = null

    // A list where tokens are waiting to be loaded into the token stream
    private var pendingTokens: LinkedList<Token>? = null

    // last pending token types
    private var previousPendingTokenType = 0
    private var lastPendingTokenTypeFromDefaultChannel = 0

    // The number of opened parentheses, square brackets or curly braces
    private var opened = 0

    //  The number of opened parentheses and square brackets in the current lexer mode
    private var paren_or_bracket_openedStack: Deque<Int>? = null
    private var wasSpaceIndentation = false
    private var wasTabIndentation = false
    private var wasIndentationMixedWithSpacesAndTabs = false
    private var curToken: CommonToken? = null // current (under processing) token
    private var ffgToken: Token? = null // following (look ahead) token

    init {
        this.init()
    }

    private fun init() {
        this.indentLengthStack = ArrayDeque()
        this.pendingTokens = LinkedList()
        this.previousPendingTokenType = 0
        this.lastPendingTokenTypeFromDefaultChannel = 0
        this.opened = 0
        this.paren_or_bracket_openedStack = ArrayDeque()
        this.wasSpaceIndentation = false
        this.wasTabIndentation = false
        this.wasIndentationMixedWithSpacesAndTabs = false
        this.curToken = null
        this.ffgToken = null
    }

    override fun nextToken(): Token? { // reading the input stream until a return EOF
        this.checkNextToken()
        return pendingTokens!!.pollFirst() // add the queued token to the token stream
    }

    private fun checkNextToken() {
        if (this.previousPendingTokenType != Token.EOF) {
            this.setCurrentAndFollowingTokens()
            if (indentLengthStack!!.isEmpty()) { // We're at the first token
                this.handleStartOfInput()
            }

            when (curToken!!.type) {
                PythonLexer.LPAR, PythonLexer.LSQB, PythonLexer.LBRACE -> {
                    opened++
                    this.addPendingToken(curToken!!)
                }

                PythonLexer.RPAR, PythonLexer.RSQB, PythonLexer.RBRACE -> {
                    opened--
                    this.addPendingToken(curToken!!)
                }

                PythonLexer.NEWLINE -> this.handleNEWLINEtoken()
                PythonLexer.STRING -> this.handleSTRINGtoken()
                PythonLexer.FSTRING_MIDDLE -> this.handleFSTRING_MIDDLE_token()
                PythonLexer.ERROR_TOKEN -> {
                    this.reportLexerError("token recognition error at: '" + curToken!!.text + "'")
                    this.addPendingToken(curToken!!)
                }

                Token.EOF -> this.handleEOFtoken()
                else -> this.addPendingToken(curToken!!)
            }
            this.handleFORMAT_SPECIFICATION_MODE()
        }
    }

    private fun setCurrentAndFollowingTokens() {
        this.curToken = if (this.ffgToken == null) CommonToken(super.nextToken()) else CommonToken(
            this.ffgToken
        )

        this.handleFStringLexerModes()

        this.ffgToken = if (curToken!!.type == Token.EOF) this.curToken else super.nextToken()
    }

    // initialize the indentLengthStack
    // hide the leading NEWLINE token(s)
    // if exists, find the first statement (not NEWLINE, not EOF token) that comes from the default channel
    // insert a leading INDENT token if necessary
    private fun handleStartOfInput() {
        // initialize the stack with a default 0-indentation length
        indentLengthStack!!.push(0) // this will never be popped off
        while (curToken!!.type != Token.EOF) {
            if (curToken!!.channel == Token.DEFAULT_CHANNEL) {
                if (curToken!!.type == PythonLexer.NEWLINE) {
                    // all the NEWLINE tokens must be ignored before the first statement
                    this.hideAndAddPendingToken(curToken!!)
                } else { // We're at the first statement
                    this.insertLeadingIndentToken()
                    return  // continue the processing of the current token with checkNextToken()
                }
            } else {
                this.addPendingToken(curToken!!) // it can be an EXPLICIT_LINE_JOINING or COMMENT token
            }
            this.setCurrentAndFollowingTokens()
        } // continue the processing of the EOF token with checkNextToken()
    }

    private fun insertLeadingIndentToken() {
        if (this.previousPendingTokenType == PythonLexer.WS) {
            val prevToken = checkNotNull(pendingTokens!!.peekLast()) // WS token
            if (this.getIndentationLength(prevToken.text) != 0) { // there is an "indentation" before the first statement
                val errMsg = "first statement indented"
                this.reportLexerError(errMsg)
                // insert an INDENT token before the first statement to raise an 'unexpected indent' error later by the parser
                this.createAndAddPendingToken(
                    PythonLexer.INDENT, Token.DEFAULT_CHANNEL, this.ERR_TXT + errMsg,
                    curToken!!
                )
            }
        }
    }

    private fun handleNEWLINEtoken() {
        if (this.opened > 0) { // We're in an implicit line joining, ignore the current NEWLINE token
            this.hideAndAddPendingToken(curToken!!)
        } else {
            val nlToken = CommonToken(this.curToken) // save the current NEWLINE token
            val isLookingAhead = ffgToken!!.type == PythonLexer.WS
            if (isLookingAhead) {
                this.setCurrentAndFollowingTokens() // set the next two tokens
            }

            when (ffgToken!!.type) {
                PythonLexer.NEWLINE, PythonLexer.COMMENT, PythonLexer.TYPE_COMMENT -> {
                    this.hideAndAddPendingToken(nlToken)
                    if (isLookingAhead) {
                        this.addPendingToken(curToken!!) // WS token
                    }
                }

                else -> {
                    this.addPendingToken(nlToken)
                    if (isLookingAhead) { // We're on whitespace(s) followed by a statement
                        val indentationLength = if (ffgToken!!.type == Token.EOF) 0 else this.getIndentationLength(
                            curToken!!.text
                        )

                        if (indentationLength != this.INVALID_LENGTH) {
                            this.addPendingToken(curToken!!) // WS token
                            this.insertIndentOrDedentToken(indentationLength) // may insert INDENT token or DEDENT token(s)
                        } else {
                            this.reportError("inconsistent use of tabs and spaces in indentation")
                        }
                    } else { // We're at a newline followed by a statement (there is no whitespace before the statement)
                        this.insertIndentOrDedentToken(0) // may insert DEDENT token(s)
                    }
                }
            }
        }
    }

    private fun insertIndentOrDedentToken(indentLength: Int) {
        var prevIndentLength = indentLengthStack!!.peek()
        if (indentLength > prevIndentLength) {
            this.createAndAddPendingToken(
                PythonLexer.INDENT, Token.DEFAULT_CHANNEL, null,
                ffgToken!!
            )
            indentLengthStack!!.push(indentLength)
        } else {
            while (indentLength < prevIndentLength) { // more than 1 DEDENT token may be inserted to the token stream
                indentLengthStack!!.pop()
                prevIndentLength = indentLengthStack!!.peek()
                if (indentLength <= prevIndentLength) {
                    this.createAndAddPendingToken(
                        PythonLexer.DEDENT, Token.DEFAULT_CHANNEL, null,
                        ffgToken!!
                    )
                } else {
                    this.reportError("inconsistent dedent")
                }
            }
        }
    }

    private fun handleSTRINGtoken() { // remove the \<newline> escape sequences from the string literal
        val line_joinFreeStringLiteral =
            curToken!!.text.replace("\\\\\\r?\\n".toRegex(), "")
        if (curToken!!.text.length == line_joinFreeStringLiteral.length) {
            this.addPendingToken(curToken!!)
        } else {
            val originalSTRINGtoken = CommonToken(this.curToken) // backup the original token
            curToken!!.text = line_joinFreeStringLiteral
            this.addPendingToken(curToken!!) // add the modified token with inline string literal
            this.hideAndAddPendingToken(originalSTRINGtoken) // add the original token to the hidden channel
            // this inserted hidden token allows restoring the original string literal with the \<newline> escape sequences
        }
    }

    private fun handleFSTRING_MIDDLE_token() { // replace the double braces '{{' or '}}' to single braces and hide the second braces
        var fsMid = curToken!!.text
        fsMid =
            fsMid.replace("\\{\\{".toRegex(), "{_").replace("}}".toRegex(), "}_") // replace: {{ --> {_  and   }} --> }_
        val arrOfStr =
            fsMid.split("(?<=[{}])_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // split by {_ or }_
        for (s in arrOfStr) {
            if (s.isNotEmpty()) {
                this.createAndAddPendingToken(
                    PythonLexer.FSTRING_MIDDLE, Token.DEFAULT_CHANNEL, s,
                    ffgToken!!
                )
                val lastCharacter = s.substring(s.length - 1)
                if ("{}".contains(lastCharacter)) {
                    this.createAndAddPendingToken(
                        PythonLexer.FSTRING_MIDDLE, Token.HIDDEN_CHANNEL, lastCharacter,
                        ffgToken!!
                    )
                    // this inserted hidden token allows restoring the original f-string literal with the double braces
                }
            }
        }
    }

    private fun handleFStringLexerModes() { // https://peps.python.org/pep-0498/#specification
        if (!_modeStack.isEmpty) {
            when (curToken!!.type) {
                PythonLexer.LBRACE -> {
                    this.pushMode(DEFAULT_MODE)
                    paren_or_bracket_openedStack!!.push(0)
                }

                PythonLexer.LPAR, PythonLexer.LSQB ->                     // https://peps.python.org/pep-0498/#lambdas-inside-expressions
                    paren_or_bracket_openedStack!!.push(paren_or_bracket_openedStack!!.pop() + 1) // increment the last element
                PythonLexer.RPAR, PythonLexer.RSQB -> paren_or_bracket_openedStack!!.push(
                    paren_or_bracket_openedStack!!.pop() - 1
                ) // decrement the last element
                PythonLexer.COLON -> if (paren_or_bracket_openedStack!!.peek() == 0) {
                    when (_modeStack.peek()) {
                        PythonLexer.SINGLE_QUOTE_FSTRING_MODE, PythonLexer.LONG_SINGLE_QUOTE_FSTRING_MODE, PythonLexer.SINGLE_QUOTE_FORMAT_SPECIFICATION_MODE -> this.mode(
                            PythonLexer.SINGLE_QUOTE_FORMAT_SPECIFICATION_MODE
                        ) // continue in format spec. mode
                        PythonLexer.DOUBLE_QUOTE_FSTRING_MODE, PythonLexer.LONG_DOUBLE_QUOTE_FSTRING_MODE, PythonLexer.DOUBLE_QUOTE_FORMAT_SPECIFICATION_MODE -> this.mode(
                            PythonLexer.DOUBLE_QUOTE_FORMAT_SPECIFICATION_MODE
                        ) // continue in format spec. mode
                    }
                }

                PythonLexer.RBRACE -> when (this._mode) {
                    DEFAULT_MODE, PythonLexer.SINGLE_QUOTE_FORMAT_SPECIFICATION_MODE, PythonLexer.DOUBLE_QUOTE_FORMAT_SPECIFICATION_MODE -> {
                        this.popMode()
                        paren_or_bracket_openedStack!!.pop()
                    }

                    else -> this.reportLexerError("f-string: single '}' is not allowed")
                }
            }
        }
    }

    private fun handleFORMAT_SPECIFICATION_MODE() {
        if (!_modeStack.isEmpty &&
            ffgToken!!.type == PythonLexer.RBRACE
        ) {
            when (curToken!!.type) {
                PythonLexer.COLON, PythonLexer.RBRACE ->                     // insert an empty FSTRING_MIDDLE token instead of the missing format specification
                    this.createAndAddPendingToken(
                        PythonLexer.FSTRING_MIDDLE, Token.DEFAULT_CHANNEL, "",
                        ffgToken!!
                    )
            }
        }
    }

    private fun insertTrailingTokens() {
        when (this.lastPendingTokenTypeFromDefaultChannel) {
            PythonLexer.NEWLINE, PythonLexer.DEDENT -> {}
            else ->                 // insert an extra trailing NEWLINE token that serves as the end of the last statement
                this.createAndAddPendingToken(
                    PythonLexer.NEWLINE, Token.DEFAULT_CHANNEL, null,
                    ffgToken!!
                ) // ffgToken is EOF
        }
        this.insertIndentOrDedentToken(0) // Now insert as many trailing DEDENT tokens as needed
    }

    private fun handleEOFtoken() {
        if (this.lastPendingTokenTypeFromDefaultChannel > 0) {
            // there was a statement in the input (leading NEWLINE tokens are hidden)
            this.insertTrailingTokens()
        }
        this.addPendingToken(curToken!!)
    }

    private fun hideAndAddPendingToken(cToken: CommonToken) {
        cToken.channel = Token.HIDDEN_CHANNEL
        this.addPendingToken(cToken)
    }

    private fun createAndAddPendingToken(type: Int, channel: Int, text: String?, baseToken: Token) {
        val cToken = CommonToken(baseToken)
        cToken.type = type
        cToken.channel = channel
        cToken.stopIndex = baseToken.startIndex - 1
        cToken.text = text ?: ("<" + this.vocabulary.getSymbolicName(type) + ">")

        this.addPendingToken(cToken)
    }

    private fun addPendingToken(token: Token) {
        // save the last pending token type because the pendingTokens linked list can be empty by the nextToken()
        this.previousPendingTokenType = token.type
        if (token.channel == Token.DEFAULT_CHANNEL) {
            this.lastPendingTokenTypeFromDefaultChannel = this.previousPendingTokenType
        }
        pendingTokens!!.addLast(token)
    }

    private fun getIndentationLength(textWS: String): Int { // the textWS may contain spaces, tabs or form feeds
        val TAB_LENGTH = 8 // the standard number of spaces to replace a tab to spaces
        var length = 0
        for (ch in textWS.toCharArray()) {
            when (ch) {
                ' ' -> {
                    this.wasSpaceIndentation = true
                    length += 1
                }

                '\t' -> {
                    this.wasTabIndentation = true
                    length += TAB_LENGTH - (length % TAB_LENGTH)
                }

                '\u000c' -> length = 0
            }
        }

        if (this.wasTabIndentation && this.wasSpaceIndentation) {
            if (!(this.wasIndentationMixedWithSpacesAndTabs)) {
                this.wasIndentationMixedWithSpacesAndTabs = true
                return this.INVALID_LENGTH // only for the first inconsistent indent
            }
        }
        return length
    }

    private fun reportLexerError(errMsg: String) {
        this.errorListenerDispatch.syntaxError(
            this, this.curToken,
            curToken!!.line,
            curToken!!.charPositionInLine, " LEXER" + this.ERR_TXT + errMsg, null
        )
    }

    private fun reportError(errMsg: String) {
        this.reportLexerError(errMsg)

        // the ERROR_TOKEN will raise an error in the parser
        this.createAndAddPendingToken(
            PythonLexer.ERROR_TOKEN, Token.DEFAULT_CHANNEL, this.ERR_TXT + errMsg,
            ffgToken!!
        )
    }

    override fun reset() {
        this.init()
        super.reset()
    }
}
