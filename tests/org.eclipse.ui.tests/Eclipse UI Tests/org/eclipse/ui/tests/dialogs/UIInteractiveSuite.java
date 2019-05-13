/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ui.tests.compare.UIComparePreferences;

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
		addTest(new TestSuite(UIComparePreferences.class));
		addTest(new TestSuite(DeprecatedUIPreferences.class));
		addTest(new TestSuite(UIWizards.class));
		addTest(new TestSuite(DeprecatedUIWizards.class));
		addTest(new TestSuite(UIDialogs.class));
		addTest(new TestSuite(DeprecatedUIDialogs.class));
		addTest(new TestSuite(UIMessageDialogs.class));
		addTest(new TestSuite(UIErrorDialogs.class));
		addTest(new TestSuite(UIFilteredResourcesSelectionDialog.class));
	}

}
