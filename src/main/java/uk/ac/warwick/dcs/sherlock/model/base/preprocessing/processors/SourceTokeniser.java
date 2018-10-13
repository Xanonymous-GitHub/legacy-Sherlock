package uk.ac.warwick.dcs.sherlock.model.base.preprocessing.processors;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import uk.ac.warwick.dcs.sherlock.api.model.ILexerSpecification;
import uk.ac.warwick.dcs.sherlock.api.model.IPreProcessor;
import uk.ac.warwick.dcs.sherlock.api.model.Language;
import uk.ac.warwick.dcs.sherlock.model.base.preprocessing.StandardLexer;

import java.util.stream.Stream;

public class SourceTokeniser implements IPreProcessor {

	@Override
	public Class<? extends ILexerSpecification> getLexerSpecification() {
		return StandardLexer.class;
	}

	/**
	 * Tokenises the source of a file
	 * @param lexer input of lexer instance containing the unprocessed lines
	 * @param lang  reference of the language of the lexer
	 *
	 * @return stream of tokenised source, 1 line per string
	 */
	@Override
	public Stream<String> process(Lexer lexer, Language lang) {
		Vocabulary vocab = lexer.getVocabulary();
		Stream.Builder<String> builder = Stream.builder();
		StringBuilder active = new StringBuilder(); //use string builder for much faster concatenation

		for (Token t : lexer.getAllTokens()) {
			switch (StandardLexer.channels.values()[t.getChannel()]) {
				case LINE_ENDING:
					builder.add(active.toString());
					active.setLength(0); //clear the string builder
					break;
				case DEFAULT:
					active.append(vocab.getSymbolicName(t.getType())).append(" ");
					break;
				default:
					break;
			}
		}

		return builder.build();
	}

}
