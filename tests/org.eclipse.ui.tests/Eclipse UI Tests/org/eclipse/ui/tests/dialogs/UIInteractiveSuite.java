/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
    }

}