/*******************************************************************************
 * Copyright (c) 2000, 2016, 2019 IBM Corporation and others.
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
 *     Tim Neumann <tim.neumann@advantest.com> - Bug 485167
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import org.eclipse.ui.tests.largefile.LargeFileLimitsPreferenceHandlerTest;
import org.eclipse.ui.tests.markers.Bug75909Test;
import org.eclipse.ui.tests.markers.DeclarativeFilterActivityTest;
import org.eclipse.ui.tests.markers.DeclarativeFilterDeclarationTest;
import org.eclipse.ui.tests.markers.MarkerHelpRegistryReaderTest;
import org.eclipse.ui.tests.markers.MarkerHelpRegistryTest;
import org.eclipse.ui.tests.markers.MarkerQueryTest;
import org.eclipse.ui.tests.markers.MarkerSortUtilTest;
import org.eclipse.ui.tests.markers.MarkerSupportRegistryTests;
import org.eclipse.ui.tests.markers.MarkerTesterTest;
import org.eclipse.ui.tests.markers.MarkerViewTests;
import org.eclipse.ui.tests.markers.MarkerViewUtilTest;
import org.eclipse.ui.tests.markers.ResourceMappingMarkersTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	EditorActionBarsTest.class,
	ActionSetExpressionTest.class,
	PopupMenuExpressionTest.class,
	Bug41931Test.class,
	Bug75909Test.class,
	Bug78470Test.class,
	DeclarativeFilterActivityTest.class,
	DeclarativeFilterDeclarationTest.class,
	ResourceMappingMarkersTest.class,
	MarkerSupportRegistryTests.class,
	MarkerSortUtilTest.class,
	MarkerViewTests.class,
	MarkerViewUtilTest.class,
	MarkerHelpRegistryTest.class,
	MarkerHelpRegistryReaderTest.class,
	MarkerQueryTest.class,
	Bug99858Test.class,
	WorkbenchWindowSubordinateSourcesTests.class,
	ReopenMenuTest.class,
	UtilTest.class,
	MarkerTesterTest.class,
	TextHandlerTest.class,
	PerspectiveSwitcherTest.class,
	StickyViewManagerTest.class,
	FileEditorMappingTest.class,
	WorkbenchSiteProgressServiceModelTagsTest.class,
	WorkbenchPageTest.class,
	SaveablesListTest.class,
	Bug540297WorkbenchPageFindViewTest.class,
	Bug549139Test.class,
	LargeFileLimitsPreferenceHandlerTest.class,
	WorkbookEditorsHandlerTest.class,
})
public class InternalTestSuite {}
