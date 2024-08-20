package uk.ac.warwick.dcs.sherlock.module.model.base.lang

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Token

abstract class RustLexerBase(input: CharStream) : Lexer(input) {
    private var lt1: Token? = null
    private var lt2: Token? = null

    override fun nextToken(): Token {
        val next = super.nextToken()

        if (next.channel == Token.DEFAULT_CHANNEL) {
            // Keep track of the last token on the default channel.
            this.lt2 = this.lt1
            this.lt1 = next
        }

        return next
    }

    fun SOF(): Boolean {
        return _input.LA(-1) <= 0
    }

    fun next(expect: Char): Boolean {
        return _input.LA(1) == expect.code
    }

    fun floatDotPossible(): Boolean {
        val next = _input.LA(1)
        // only block. _ identifier after float
        if (next == '.'.code || next == '_'.code) return false
        if (next == 'f'.code) {
            // 1.f32
            if (_input.LA(2) == '3'.code && _input.LA(3) == '2'.code) return true
            //1.f64
            return _input.LA(2) == '6'.code && _input.LA(3) == '4'.code
        }
        if (next >= 'a'.code && next <= 'z'.code) return false
        return next < 'A'.code || next > 'Z'.code
    }

    fun floatLiteralPossible(): Boolean {
        if (this.lt1 == null || this.lt2 == null) return true
        if (lt1!!.type != RustLexer.DOT) return true
        return when (lt2!!.type) {
            RustLexer.CHAR_LITERAL, RustLexer.STRING_LITERAL, RustLexer.RAW_STRING_LITERAL, RustLexer.BYTE_LITERAL, RustLexer.BYTE_STRING_LITERAL, RustLexer.RAW_BYTE_STRING_LITERAL, RustLexer.INTEGER_LITERAL, RustLexer.DEC_LITERAL, RustLexer.HEX_LITERAL, RustLexer.OCT_LITERAL, RustLexer.BIN_LITERAL, RustLexer.KW_SUPER, RustLexer.KW_SELFVALUE, RustLexer.KW_SELFTYPE, RustLexer.KW_CRATE, RustLexer.KW_DOLLARCRATE, RustLexer.GT, RustLexer.RCURLYBRACE, RustLexer.RSQUAREBRACKET, RustLexer.RPAREN, RustLexer.KW_AWAIT, RustLexer.NON_KEYWORD_IDENTIFIER, RustLexer.RAW_IDENTIFIER, RustLexer.KW_MACRORULES -> false
            else -> true
        }
    }
}