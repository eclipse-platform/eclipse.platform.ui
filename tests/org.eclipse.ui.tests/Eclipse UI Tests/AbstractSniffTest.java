import org.eclipse.ui.test.harness.launcher.TestUIMain;

/**
 * UiSniff runs the UI sniff tests from your workbench.
 * 
 * The standard execution args are "-dev bin -data d:/temp/eclipse"
 */
public class AbstractSniffTest {

	public void runTest(String[] args, String testId, boolean clean) {
		int size = args.length;
		int newSize = size + 2 + (clean ? 1 : 0);
		String[] newArgs = new String[newSize];
		System.arraycopy(args, 0, newArgs, 0, size);
		newArgs[size] = "-uiTest";
		newArgs[size + 1] = testId;
		if (clean)
			newArgs[size + 2] = "-clean";
		TestUIMain.main(newArgs);
	}
}

