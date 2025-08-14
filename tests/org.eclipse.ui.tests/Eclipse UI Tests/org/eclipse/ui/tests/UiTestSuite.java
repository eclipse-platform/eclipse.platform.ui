/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *     Red Hat Inc. - Bug 474132
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 504029
 *     Lucas Bullen (Red Hat Inc.) - Bugs 519525, 520250, and 520251
 *     Tim Neumann <tim.neumann@advantest.com> - Bug 485167
 *******************************************************************************/
package org.eclipse.ui.tests;

import org.eclipse.ui.internal.ide.ChooseWorkspaceDialogTests;
import org.eclipse.ui.internal.ide.DirectoryProposalContentAssistTestSuite;
import org.eclipse.ui.tests.activities.ActivitiesTestSuite;
import org.eclipse.ui.tests.adaptable.AdaptableTestSuite;
import org.eclipse.ui.tests.api.ApiTestSuite;
import org.eclipse.ui.tests.api.StartupTest;
import org.eclipse.ui.tests.commands.CommandsTestSuite;
import org.eclipse.ui.tests.concurrency.ConcurrencyTestSuite;
import org.eclipse.ui.tests.contexts.ContextsTestSuite;
import org.eclipse.ui.tests.databinding.WorkbenchDatabindingTest;
import org.eclipse.ui.tests.datatransfer.DataTransferTestSuite;
import org.eclipse.ui.tests.decorators.DecoratorsTestSuite;
import org.eclipse.ui.tests.dialogs.FilteredResourcesSelectionDialogTestSuite;
import org.eclipse.ui.tests.dialogs.UIAutomatedSuite;
import org.eclipse.ui.tests.dynamicplugins.DynamicPluginsTestSuite;
import org.eclipse.ui.tests.encoding.EncodingTestSuite;
import org.eclipse.ui.tests.fieldassist.FieldAssistTestSuite;
import org.eclipse.ui.tests.filteredtree.FilteredTableTests;
import org.eclipse.ui.tests.filteredtree.FilteredTreeTests;
import org.eclipse.ui.tests.filteredtree.PatternFilterTest;
import org.eclipse.ui.tests.internal.InternalTestSuite;
import org.eclipse.ui.tests.intro.IntroTestSuite;
import org.eclipse.ui.tests.keys.KeysTestSuite;
import org.eclipse.ui.tests.leaks.LeaksTestSuite;
import org.eclipse.ui.tests.menus.MenusTestSuite;
import org.eclipse.ui.tests.multieditor.MultiEditorTestSuite;
import org.eclipse.ui.tests.multipageeditor.MultiPageEditorTestSuite;
import org.eclipse.ui.tests.navigator.NavigatorTestSuite;
import org.eclipse.ui.tests.operations.OperationsTestSuite;
import org.eclipse.ui.tests.preferences.PreferencesTestSuite;
import org.eclipse.ui.tests.preferences.ViewerItemsLimitTest;
import org.eclipse.ui.tests.progress.ProgressTestSuite;
import org.eclipse.ui.tests.propertysheet.PropertySheetTestSuite;
import org.eclipse.ui.tests.quickaccess.QuickAccessTestSuite;
import org.eclipse.ui.tests.releng.PluginActivationTests;
import org.eclipse.ui.tests.services.ServicesTestSuite;
import org.eclipse.ui.tests.statushandlers.StatusHandlingTestSuite;
import org.eclipse.ui.tests.stress.OpenCloseTest;
import org.eclipse.ui.tests.systeminplaceeditor.OpenSystemInPlaceEditorTest;
import org.eclipse.ui.tests.themes.ThemesTestSuite;
import org.eclipse.ui.tests.zoom.ZoomTestSuite;
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
	PluginActivationTests.class,
	ProgressTestSuite.class,
	PropertySheetTestSuite.class,
	AdaptableTestSuite.class,
	MultiPageEditorTestSuite.class,
	CommandsTestSuite.class,
	ContextsTestSuite.class,
	ConcurrencyTestSuite.class,
	FilteredTableTests.class,
	FilteredTreeTests.class,
	PatternFilterTest.class,
	StatusHandlingTestSuite.class,
	MenusTestSuite.class,
	QuickAccessTestSuite.class,
	FilteredResourcesSelectionDialogTestSuite.class,
	DirectoryProposalContentAssistTestSuite.class,
	InternalTestSuite.class,
	LeaksTestSuite.class,
	StyledStringHighlighterTest.class,
	ZoomTestSuite.class,
	DynamicPluginsTestSuite.class,
	IntroTestSuite.class,
	MultiEditorTestSuite.class,
	OpenSystemInPlaceEditorTest.class,
	WorkbenchDatabindingTest.class,
	ChooseWorkspaceDialogTests.class,
	ViewerItemsLimitTest.class,
	OpenCloseTest.class
})
public class UiTestSuite {
}
