/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.ui.workbench.texteditor.tests.revisions.ChangeRegionTest;
import org.eclipse.ui.workbench.texteditor.tests.revisions.HunkComputerTest;
import org.eclipse.ui.workbench.texteditor.tests.revisions.RangeTest;
import org.eclipse.ui.workbench.texteditor.tests.rulers.RulerTestSuite;


/**
 * Test Suite for org.eclipse.ui.workbench.texteditor.
 *
 * @since 3.0
 */
@RunWith(Suite.class)
@SuiteClasses({
		FindReplaceDialogTest.class,
		HippieCompletionTest.class,
		RangeTest.class,
		ChangeRegionTest.class,
		RulerTestSuite.class,
		HunkComputerTest.class,
		ScreenshotTest.class
})
public class WorkbenchTextEditorTestSuite {
	// see @SuiteClasses
}
