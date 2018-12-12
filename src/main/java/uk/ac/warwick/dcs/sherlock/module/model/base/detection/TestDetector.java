package uk.ac.warwick.dcs.sherlock.module.model.base.detection;

import org.antlr.v4.runtime.*;
import uk.ac.warwick.dcs.sherlock.api.model.AbstractPairwiseDetector;
import uk.ac.warwick.dcs.sherlock.api.model.IPreProcessingStrategy;
import uk.ac.warwick.dcs.sherlock.api.model.Language;
import uk.ac.warwick.dcs.sherlock.api.util.IndexedString;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaParser;
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.VariableExtractor;

import java.util.*;

public class TestDetector extends AbstractPairwiseDetector {

	private static final Language[] languages = { Language.JAVA };

	@DetectorParameter (name = "Test Param", defaultValue = 0, minimumBound = 0, maxumumBound = 10, step = 1)
	public int testParam;

	@Override
	public AbstractPairwiseDetector.AbstractPairwiseDetectorWorker getAbstractPairwiseDetectorWorker() {
		return new TestDetectorWorker();
	}

	@Override
	public String getDisplayName() {
		return "Test Detector";
	}

	@Override
	public Rank getRank() {
		return Rank.PRIMARY;
	}

	@Override
	public Class<? extends Lexer> getLexer(Language lang) {
		return JavaLexer.class;
	}

	@Override
	public Class<? extends Parser> getParser(Language lang) {
		return JavaParser.class;
	}

	@Override
	public List<IPreProcessingStrategy> getPreProcessors() {
		return Collections.singletonList(IPreProcessingStrategy.of("variables", VariableExtractor.class));
	}

	@Override
	public Language[] getSupportedLanguages() {
		return languages;
	}

	public class TestDetectorWorker extends AbstractPairwiseDetectorWorker {

		@Override
		public void execute() {
			List<IndexedString> lines = this.file1.getPreProcessedLines("variables");
			int originalSize = lines.size();

			for (IndexedString checkLine : lines) {
				this.file2.getPreProcessedLines("variables").stream().filter(x -> !x.valueEquals(checkLine)).forEach(lines::remove);
			}

			float per = lines.size()/(float) originalSize;
			System.out.println(per + " - " + lines.toString());

			// Then output to a postProcessor, output the line numbers in EACH FILE
		}
	}
}
