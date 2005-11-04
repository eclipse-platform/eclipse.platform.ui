package org.eclipse.jface.tests.binding.scenarios;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.swt.SWTDatabindingContext;
import org.eclipse.jface.tests.binding.scenarios.model.SampleData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract base class of the JFace binding scenario test classes.
 */

abstract public class ScenariosTestCase extends TestCase {

	private Composite composite;

	private SWTDatabindingContext dbc;

	private Display display;

	private boolean disposeDisplay = false;

	private Shell shell;

	protected Text dummyText;

	protected Composite getComposite() {
		return composite;
	}

	protected SWTDatabindingContext getDbc() {
		return dbc;
	}

	public Shell getShell() {
		if (shell != null) {
			return shell;
		}
		Shell result = BindingScenariosTestSuite.getShell();
		if (result == null) {
			display = Display.getDefault();
			if (Display.getDefault() == null) {
				display = new Display();
				disposeDisplay = true;
			}
			shell = new Shell(display, SWT.SHELL_TRIM);
			shell.setLayout(new FillLayout());
			result = shell;
		}
		result.setText(getName()); // In the case that the shell() becomes
		// visible.
		return result;
	}

	protected void spinEventLoop(int secondsToWaitWithNoEvents) {
		if (!composite.isVisible() && secondsToWaitWithNoEvents > 0) {
			composite.getShell().open();
		}
		while (composite.getDisplay().readAndDispatch()) {
			// do nothing, just process events
		}
		try {
			Thread.sleep(secondsToWaitWithNoEvents * 1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	protected void setButtonSelectionWithEvents(Button button, boolean value) {
		if (button.getSelection() == value) {
			return;
		}
		button.setSelection(value);
		pushButtonWithEvents(button);
	}

	protected void pushButtonWithEvents(Button button) {
		button.notifyListeners(SWT.Selection, null);
	}

	protected void setUp() throws Exception {
		composite = new Composite(getShell(), SWT.NONE);
		composite.setLayout(new GridLayout());
		SampleData.initializeData(); // test may manipulate the data... let
		// all start from fresh
		dbc = SampleData.getDatabindingContext(composite);
		dummyText = new Text(getComposite(), SWT.NONE);
		dummyText.setText("dummy");
	}

	protected void tearDown() throws Exception {
		getShell().setVisible(false); // same Shell may be reUsed across tests
		composite.dispose();
		composite = null;
		if (shell != null) {
			shell.close();
			shell.dispose();
		} else
			dbc.dispose();
		if (display != null && disposeDisplay) {
			display.dispose();
		}
	}

	protected void enterText(Text text, String string) {
		text.notifyListeners(SWT.FocusIn, null);
		text.setText(string);
		text.notifyListeners(SWT.FocusOut, null);
	}

	protected void assertArrayEquals(Object[] expected, Object[] actual) {
		assertEquals(Arrays.asList(expected), Arrays.asList(actual));
	}

}
