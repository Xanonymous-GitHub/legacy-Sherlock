package uk.ac.warwick.dcs.sherlock.module.model.base.detection;

import org.antlr.v4.runtime.*;
import uk.ac.warwick.dcs.sherlock.api.annotation.AdjustableParameter;
import uk.ac.warwick.dcs.sherlock.api.common.IndexedString;
import uk.ac.warwick.dcs.sherlock.api.model.detection.AbstractPairwiseDetector;
import uk.ac.warwick.dcs.sherlock.api.model.detection.AbstractPairwiseDetectorWorker;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.IPreProcessingStrategy;
import uk.ac.warwick.dcs.sherlock.module.model.base.detection.TestDetector.TestDetectorWorker;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaParser;
import uk.ac.warwick.dcs.sherlock.module.model.base.postprocessing.SimpleObjectEqualityRawResult;
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.VariableExtractor;

import java.util.*;

public class TestDetector extends AbstractPairwiseDetector<TestDetectorWorker> {

	private static final String[] languages = { "Java" };

	@AdjustableParameter (name = "Test Param", defaultValue = 0, minimumBound = 0, maxumumBound = 10, step = 1)
	public int testParam;

	@Override
	public TestDetectorWorker getAbstractPairwiseDetectorWorker() {
		return new TestDetector.TestDetectorWorker();
	}

	@Override
	public String getDisplayName() {
		return "Test Detector";
	}

	@Override
	public Class<? extends Lexer> getLexer(String lang) {
		return JavaLexer.class;
	}

	@Override
	public Class<? extends Parser> getParser(String lang) {
		return JavaParser.class;
	}

	@Override
	public List<IPreProcessingStrategy> getPreProcessors() {
		return Collections.singletonList(IPreProcessingStrategy.of("variables", VariableExtractor.class));
	}

	@Override
	public Rank getRank() {
		return Rank.PRIMARY;
	}

	@Override
	public String[] getSupportedLanguages() {
		return languages;
	}

	public class TestDetectorWorker extends AbstractPairwiseDetectorWorker<SimpleObjectEqualityRawResult<String>> {

		@Override
		public void execute() {
			// This detector finds and matches up variables - it only works on declarations of the variable, not every time the variable is called.

			List<IndexedString> linesF1 = this.file1.getPreProcessedLines("variables");
			List<IndexedString> linesF2 = this.file2.getPreProcessedLines("variables");

			List<Integer> usedIndexesF2 = new LinkedList<>();

			SimpleObjectEqualityRawResult<String> res = new SimpleObjectEqualityRawResult<>(this.file1.getFile(), this.file2.getFile(), linesF1.size(), linesF2.size());

			for (IndexedString checkLine : linesF1) {
				linesF2.stream().filter(x -> x.valueEquals(checkLine)).forEach(x -> res.put(checkLine.getValue(), checkLine.getKey(), x.getKey()));
			}

			this.result = res;
		}
	}
}
