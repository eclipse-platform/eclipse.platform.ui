/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.ui.tests.performance;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test all areas of the UI API.
 */
@RunWith(Suite.class)
@SuiteClasses({ //
		GenerateIdentifiersTest.class, //
		WorkbenchPerformanceSuite.class, //
		OpenClosePerspectiveTest.class, //
		PerspectiveSwitchTest.class, //
		OpenCloseWindowTest.class, //
		OpenCloseViewTest.class, //
		OpenCloseEditorTest.class, //
		OpenMultipleEditorTest.class, //
		EditorSwitchTest.class, //
		CommandsPerformanceTest.class, //
		LabelProviderTest.class, //
		ProgressReportingTest.class, //
		OpenProjectExplorerFolderTest.class, //
})
public class UIPerformanceTestSuite {

}
