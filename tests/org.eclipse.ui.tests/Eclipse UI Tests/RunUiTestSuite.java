import org.eclipse.ui.test.harness.launcher.TestUIMain;

/**
 * RunUiTestSuite runs all of the basic UI tests.
 * 
 * The standard execution args are "-dev bin -data d:/temp/eclipse"
 */
public class RunUiTestSuite extends AbstractSniffTest {
	public static void main(String[] args) {
		AbstractSniffTest test = new RunUiTestSuite();
		test.runTest(args, "ui.TestSuite", true);
	}
}

