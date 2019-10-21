/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
package org.eclipse.ui.tests.dynamicplugins;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for dynamic plug-in support.
 */
@RunWith(Suite.class)
@Ignore("Bug 405296")
@Suite.SuiteClasses({
	StatusHandlerTests.class,
	AcceleratorConfigurationsExtensionDynamicTest.class,
	AcceleratorScopesExtensionDynamicTest.class,
	ActionDefinitionsExtensionDynamicTest.class,
	ActionSetTests.class,
	ActivitySupportTests.class,
	BindingsExtensionDynamicTest.class,
	BrowserTests.class,
	CommandsExtensionDynamicTest.class,
	ContextsExtensionDynamicTest.class,
	HandlersExtensionDynamicTest.class,
	PreferencePageTests.class,
	KeywordTests.class,
	PropertyPageTests.class,
	HelpSupportTests.class,
	EncodingTests.class,
	DecoratorTests.class,
	StartupTests.class,
	EditorTests.class,
	IntroTests.class,
	PerspectiveTests.class,
	ViewTests.class,
	NewWizardTests.class,
	ObjectContributionTests.class,
	WorkingSetTests.class,
	DynamicSupportTests.class,
	DynamicContributionTest.class,
	DynamicInvalidContributionTest.class,
	DynamicInvalidControlContributionTest.class,
})

public class DynamicPluginsTestSuite {
}
