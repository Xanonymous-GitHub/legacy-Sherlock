package uk.ac.warwick.dcs.sherlock.module.model.base.detection;

/**
 * A useless class to ensure the Kotlin code can be applied to the Registry.
 * For more information, see the Kotlin code in {@link ASTDiffDetector}.
 */
@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public class ASTDiffDetectorJavaShell extends ASTDiffDetector {
    // FIXME: This is just a workaround to ensure the kotlin code can be applied to the Registry.

    final String foo = "";

    public void bar() {
        int ggg = 33;
        if (ggg > 32) {
            System.out.println(ggg);
        }
    }

    public String foo(int i) {
        if (i == 0) return "Foo!";
        return "";
    }
}