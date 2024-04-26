package uk.ac.warwick.dcs.sherlock.module.model.base.detection;

import uk.ac.warwick.dcs.sherlock.api.model.detection.PairwiseDetector;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.PreProcessingStrategy;
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.VariableExtractor;

public class VariableNameDetector extends PairwiseDetector<VariableNameDetectorWorker> {

    public VariableNameDetector() {
        super("Variable Name Detector", "Detector which scores files based on how many variables are exactly duplicated between them", VariableNameDetectorWorker.class, PreProcessingStrategy.of("variables", VariableExtractor.class));
    }
}
