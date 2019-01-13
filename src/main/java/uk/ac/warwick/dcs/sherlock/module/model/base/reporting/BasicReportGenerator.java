package uk.ac.warwick.dcs.sherlock.module.model.base.reporting;

import uk.ac.warwick.dcs.sherlock.engine.report.AbstractReportGenerator;
import uk.ac.warwick.dcs.sherlock.api.common.ICodeBlock;
import uk.ac.warwick.dcs.sherlock.api.common.ICodeBlockGroup;

import java.util.*;

/**
 * TODO: currently still based on old ICodeBlockPair rather than ICodeBlockGroup.
 * <p>
 * A fairly basic first pass to generate the presentable reports shown to the user. This uses ICodeBlockPair but could be adapted to the stuff in ModelProcessedRessults; I thought it would be better
 * to have the report stuff separate for the time being though.
 * <p>
 * rough plan/overview of the process: - have file with descriptions of problems - take a given file pair - go through each CodeBlock in CodeBlockPair - Generate string by taking problem description
 * according to the DetectionType enum of the block - Replace string generic parts with relevant line numbers etc. - Make larger string joining together all of the reasons along the way - done???
 */
public class BasicReportGenerator extends AbstractReportGenerator {

	BasicReportGenerator(String descriptionFileName) {
		super(descriptionFileName);
	}

	@Override
	public String GenerateReport(List<? extends ICodeBlockGroup> codeBlockGroups) {
		StringJoiner stringJoiner = new StringJoiner("\n");

		for (ICodeBlockGroup codeBlockGroup : codeBlockGroups) {
			//Get the base description for this type of plagiarism
			String currentDescription = baseDescriptions.get(codeBlockGroup.getDetectionType());

			//Get the line numbers for these code blocks and format the description using them
			//NB with this basic approach, requires the base descriptions to keep line numbers in consistent order
			List<Integer> lineNumbers = new ArrayList<Integer>();
			for (ICodeBlock codeBlock : codeBlockGroup.getCodeBlocks()) {
				//lineNumbers.addAll(codeBlock.getLineNumbers()); //broken
			}

			//Kind of gross but not sure if string.format can just take a list
			String lineNumberDesc = String.format(currentDescription, lineNumbers.get(0), lineNumbers.get(1), lineNumbers.get(2), lineNumbers.get(3));

			//TODO: further formatting with e.g. variable names etc.

			stringJoiner.add(lineNumberDesc);
		}

		//Final formatting
		String outputReport = stringJoiner.toString();

		return outputReport;
	}
}
