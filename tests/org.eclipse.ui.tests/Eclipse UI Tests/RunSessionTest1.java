import org.eclipse.ui.test.harness.launcher.TestUIMain;

/**
 * RunSessionTest1 runs the first half of our session
 * presistance tests.
 * 
 * The standard execution args are "-dev bin -data d:/temp/eclipse"
 */
public class RunSessionTest1 extends AbstractSniffTest {
	public static void main(String[] args) {
		AbstractSniffTest test = new RunSessionTest1();
		test.runTest(args, "ui.api.SessionTest1", true);
	}
}

