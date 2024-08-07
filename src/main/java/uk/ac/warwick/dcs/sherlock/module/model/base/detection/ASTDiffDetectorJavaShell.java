package uk.ac.warwick.dcs.sherlock.module.model.base.detection;

import uk.ac.warwick.dcs.sherlock.api.annotation.AdjustableParameter;

/**
 * A useless class to ensure the Kotlin code can be applied to the Registry.
 * For more information, see the Kotlin code in {@link ASTDiffDetector}.
 */
public final class ASTDiffDetectorJavaShell extends ASTDiffDetector {
    public ASTDiffDetectorJavaShell() {
        super.setTheShell(this);
    }

    @AdjustableParameter(
        name = "score of Single Insert Action",
        defaultValue = 1,
        maximumBound = 5,
        minimumBound = 0,
        step = 1
    )
    public float scoreOfSingleInsertAction;
    @AdjustableParameter(
        name = "score of Single Update Action",
        defaultValue = 1,
        maximumBound = 5,
        minimumBound = 0,
        step = 1
    )
    public float scoreOfSingleUpdateAction;
    @AdjustableParameter(
        name = "score of Single Delete Action",
        defaultValue = 3,
        maximumBound = 5,
        minimumBound = 0,
        step = 1
    )
    public float scoreOfSingleDeleteAction;
    @AdjustableParameter(
        name = "score of Structural Insert Action",
        defaultValue = 5,
        maximumBound = 10,
        minimumBound = 0,
        step = 1
    )
    public float scoreOfTreeInsertAction;
    @AdjustableParameter(
        name = "score of Structural Move Action",
        defaultValue = 1,
        maximumBound = 5,
        minimumBound = 0,
        step = 1
    )
    public float scoreOfTreeMoveAction;
    @AdjustableParameter(
        name = "score of Structural Delete Action",
        defaultValue = 5,
        maximumBound = 10,
        minimumBound = 0,
        step = 1
    )
    public float scoreOfTreeDeleteAction;
}