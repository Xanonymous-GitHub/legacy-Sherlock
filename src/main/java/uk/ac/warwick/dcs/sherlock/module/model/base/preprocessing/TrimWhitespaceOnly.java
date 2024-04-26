package uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.IGeneralPreProcessor;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.ILexerSpecification;

import java.util.ArrayList;
import java.util.List;

public class TrimWhitespaceOnly implements IGeneralPreProcessor {

    @Override
    public ILexerSpecification getLexerSpecification() {
        return new StandardLexerSpecification();
    }

    /**
     * Removes the excess whitespace from a sourcefile
     *
     * @param tokens List of tokens to process
     * @param vocab  Lexer vocabulary
     * @param lang   language of source file being processed
     * @return stream of tokens containing comments
     */
    @Override
    public List<? extends Token> process(List<? extends Token> tokens, Vocabulary vocab, String lang) {
        List<Token> result = new ArrayList<>();

        for (Token t : tokens) {

            switch (StandardLexerSpecification.channels.values()[t.getChannel()]) {
                case COMMENT, WHITESPACE, DEFAULT:
                    result.add(t);
                    break;
                case LONG_WHITESPACE:
                    CommonToken temp = new CommonToken(t);
                    temp.setText(" ");
                    result.add(temp);
                    break;
                default:
                    break;
            }
        }

        return result;
    }
}
