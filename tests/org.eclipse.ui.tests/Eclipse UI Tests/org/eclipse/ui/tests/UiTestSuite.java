/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - Bug 474132
 *******************************************************************************/
package org.eclipse.ui.tests;

import org.eclipse.ui.tests.activities.ActivitiesTestSuite;
import org.eclipse.ui.tests.adaptable.AdaptableTestSuite;
import org.eclipse.ui.tests.api.ApiTestSuite;
import org.eclipse.ui.tests.api.StartupTest;
import org.eclipse.ui.tests.concurrency.ConcurrencyTestSuite;
import org.eclipse.ui.tests.contexts.ContextsTestSuite;
import org.eclipse.ui.tests.datatransfer.DataTransferTestSuite;
import org.eclipse.ui.tests.decorators.DecoratorsTestSuite;
import org.eclipse.ui.tests.dialogs.UIAutomatedSuite;
import org.eclipse.ui.tests.encoding.EncodingTestSuite;
import org.eclipse.ui.tests.fieldassist.FieldAssistTestSuite;
import org.eclipse.ui.tests.filteredtree.FilteredTreeTests;
import org.eclipse.ui.tests.keys.KeysTestSuite;
import org.eclipse.ui.tests.multipageeditor.MultiPageEditorTestSuite;
import org.eclipse.ui.tests.navigator.NavigatorTestSuite;
import org.eclipse.ui.tests.operations.OperationsTestSuite;
import org.eclipse.ui.tests.preferences.PreferencesTestSuite;
import org.eclipse.ui.tests.progress.ProgressTestSuite;
import org.eclipse.ui.tests.propertysheet.PropertySheetTestSuite;
import org.eclipse.ui.tests.services.ServicesTestSuite;
import org.eclipse.ui.tests.statushandlers.StatusHandlingTestSuite;
import org.eclipse.ui.tests.themes.ThemesTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all areas of the UI.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	StartupTest.class,
	UIAutomatedSuite.class,
	ApiTestSuite.class,
	NavigatorTestSuite.class,
	DecoratorsTestSuite.class,
	DataTransferTestSuite.class,
	PreferencesTestSuite.class,
	KeysTestSuite.class,
	ActivitiesTestSuite.class,
	ThemesTestSuite.class,
	EncodingTestSuite.class,
	OperationsTestSuite.class,
	FieldAssistTestSuite.class,
	ServicesTestSuite.class,
	ProgressTestSuite.class,
	PropertySheetTestSuite.class,
	AdaptableTestSuite.class,
	MultiPageEditorTestSuite.class,
	ContextsTestSuite.class,
	ConcurrencyTestSuite.class,
	FilteredTreeTests.class,
	StatusHandlingTestSuite.class
})
public class UiTestSuite {

	// Not enabled tests:
	// QuickAccessTestSuite.class,
	// InternalTestSuite.class,
	// ZoomTestSuite.class,
	// DynamicPluginsTestSuite.class,
	// CommandsTestSuite.class,
	// DragTestSuite.class,
	// IntroTestSuite.class,
	// MenusTestSuite.class,
	// PresentationsTestSuite.class,
	// LeakTests.class,
	// MultiEditorTestSuite.class,
	// OpenSystemInPlaceEditorTest..class,
}
