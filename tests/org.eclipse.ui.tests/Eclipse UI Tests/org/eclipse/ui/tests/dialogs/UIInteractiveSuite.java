package org.eclipse.ui.tests.dialogs;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test all areas of the UI.
 */
public class UIInteractiveSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new UIInteractiveSuite();
	}
	
	/**
	 * Construct the test suite.
	 */
	public UIInteractiveSuite() {
		addTest(new TestSuite(UIPreferences.class));
		addTest(new TestSuite(UIWizards.class));
		addTest(new TestSuite(UIDialogs.class));
		addTest(new TestSuite(UIMessageDialogs.class));
		addTest(new TestSuite(UIErrorDialogs.class));
	}


}