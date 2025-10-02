/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
package org.eclipse.ui.workbench.texteditor.tests;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectClasses;

import org.eclipse.ui.internal.findandreplace.FindReplaceLogicTest;
import org.eclipse.ui.internal.findandreplace.overlay.FindReplaceOverlayTest;

import org.eclipse.ui.workbench.texteditor.tests.minimap.MinimapPageTest;
import org.eclipse.ui.workbench.texteditor.tests.minimap.MinimapWidgetTest;
import org.eclipse.ui.workbench.texteditor.tests.revisions.ChangeRegionTest;
import org.eclipse.ui.workbench.texteditor.tests.revisions.HunkComputerTest;
import org.eclipse.ui.workbench.texteditor.tests.revisions.RangeTest;
import org.eclipse.ui.workbench.texteditor.tests.rulers.RulerTestSuite;

/**
 * Test Suite for org.eclipse.ui.workbench.texteditor.
 *
 * @since 3.0
 */
@Suite
@SelectClasses({
		HippieCompletionTest.class,
		RangeTest.class,
		ChangeRegionTest.class,
		RulerTestSuite.class,
		HunkComputerTest.class,
		ScreenshotTest.class,
		AbstractTextZoomHandlerTest.class,
		DocumentLineDifferTest.class,
		MinimapPageTest.class,
		MinimapWidgetTest.class,
		TextEditorPluginTest.class,
		TextViewerDeleteLineTargetTest.class,
		FindReplaceDialogTest.class,
		FindReplaceOverlayTest.class,
		FindReplaceLogicTest.class,
})
public class WorkbenchTextEditorTestSuite {
	// see @SelectClasses
}
