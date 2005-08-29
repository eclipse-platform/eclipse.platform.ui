/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for dynamic plug-in support.
 */
public class DynamicPluginsTestSuite extends TestSuite {
    /**
     * Returns the suite.  This is required to
     * use the JUnit Launcher.
     */
    public static Test suite() {
        return new DynamicPluginsTestSuite();
    }

	/**
	 * Construct the test suite.
	 */
	public DynamicPluginsTestSuite() {
		addTest(new TestSuite(
				AcceleratorConfigurationsExtensionDynamicTest.class));
		addTest(new TestSuite(AcceleratorScopesExtensionDynamicTest.class));
		addTest(new TestSuite(ActionDefinitionsExtensionDynamicTest.class));
	    addTest(new TestSuite(ActionSetTests.class));
		addTest(new TestSuite(ActivitySupportTests.class));
		addTest(new TestSuite(BindingsExtensionDynamicTest.class));
		addTest(new TestSuite(BrowserTests.class));
		addTest(new TestSuite(CommandsExtensionDynamicTest.class));
		addTest(new TestSuite(ContextsExtensionDynamicTest.class));
		addTest(new TestSuite(HandlersExtensionDynamicTest.class));
		addTest(new TestSuite(PreferencePageTests.class));
		addTest(new TestSuite(KeywordTests.class));
		addTest(new TestSuite(PropertyPageTests.class));
		addTest(new TestSuite(HelpSupportTests.class));
		addTest(new TestSuite(EncodingTests.class));
		addTest(new TestSuite(DecoratorTests.class));
		addTest(new TestSuite(StartupTests.class));
		addTest(new TestSuite(EditorTests.class));
		addTest(new TestSuite(IntroTests.class));
		addTest(new TestSuite(PerspectiveTests.class));
		addTest(new TestSuite(ViewTests.class));
		addTest(new TestSuite(ActionSetTests.class));
		addTest(new TestSuite(NewWizardTests.class));
		addTest(new TestSuite(ObjectContributionTests.class));
		addTest(new TestSuite(DynamicSupportTests.class));
	}
}
