package org.eclipse.ui.tests.dialogs;

import junit.framework.*;
import junit.textui.TestRunner;


/**
 * Test all areas of the UI.
 */
public class UIAutomatedSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new UIAutomatedSuite();
	}
	
	/**
	 * Construct the test suite.
	 */
	public UIAutomatedSuite() {
		addTest(new TestSuite(UIDialogsAuto.class));
		addTest(new TestSuite(UIWizardsAuto.class));
		addTest(new TestSuite(UIPreferencesAuto.class));
		addTest(new TestSuite(UIMessageDialogsAuto.class));
		addTest(new TestSuite(UINewWorkingSetWizardAuto.class));
		addTest(new TestSuite(UIEditWorkingSetWizardAuto.class));		
	}
}