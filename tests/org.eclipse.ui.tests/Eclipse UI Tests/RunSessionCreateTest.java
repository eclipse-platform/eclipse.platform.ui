import org.eclipse.ui.test.harness.launcher.TestUIMain;

/**
 * RunSessionCreateTest runs the first half of our session
 * presistance tests.
 * 
 * The standard execution args are "-dev bin -data d:/temp/eclipse"
 */
public class RunSessionCreateTest extends AbstractSniffTest {
	public static void main(String[] args) {
		AbstractSniffTest test = new RunSessionCreateTest();
		test.runTest(args, "ui.api.SessionCreateTest", true);
	}
}

