import org.eclipse.ui.test.harness.launcher.TestUIMain;

/**
 * RunSessionRestoreTest runs the second half of our session
 * presistance tests.
 * 
 * The standard execution args are "-dev bin -data d:/temp/eclipse"
 */
public class RunSessionRestoreTest extends AbstractSniffTest {
	public static void main(String[] args) {
		AbstractSniffTest test = new RunSessionRestoreTest();
		test.runTest(args, "ui.api.SessionRestoreTest", false);
	}
}

