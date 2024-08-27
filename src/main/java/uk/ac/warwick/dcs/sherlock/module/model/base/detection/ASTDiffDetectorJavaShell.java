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
        maximumBound = 1,
        minimumBound = 0,
        step = 0.01f
    )
    public float scoreOfSingleInsertAction;
    @AdjustableParameter(
        name = "score of Single Update Action",
        defaultValue = 0.3f,
        maximumBound = 1,
        minimumBound = 0,
        step = 0.01f
    )
    public float scoreOfSingleUpdateAction;
    @AdjustableParameter(
        name = "score of Single Delete Action",
        defaultValue = 0.8f,
        maximumBound = 1,
        minimumBound = 0,
        step = 0.01f
    )
    public float scoreOfSingleDeleteAction;
    @AdjustableParameter(
        name = "score of Structural Insert Action",
        defaultValue = 1,
        maximumBound = 1,
        minimumBound = 0,
        step = 0.01f
    )
    public float scoreOfTreeInsertAction;
    @AdjustableParameter(
        name = "score of Structural Move Action",
        defaultValue = 0.01f,
        maximumBound = 1,
        minimumBound = 0,
        step = 0.01f
    )
    public float scoreOfTreeMoveAction;
    @AdjustableParameter(
        name = "score of Structural Delete Action",
        defaultValue = 0.8f,
        maximumBound = 1,
        minimumBound = 0,
        step = 0.01f
    )
    public float scoreOfTreeDeleteAction;
}