import org.eclipse.ui.test.harness.launcher.TestUIMain;

/**
 * RunSessionTest2 runs the second half of our session
 * presistance tests.
 * 
 * The standard execution args are "-dev bin -data d:/temp/eclipse"
 */
public class RunSessionTest2 extends AbstractSniffTest {
	public static void main(String[] args) {
		AbstractSniffTest test = new RunSessionTest2();
		test.runTest(args, "ui.api.SessionTest2", false);
	}
}

