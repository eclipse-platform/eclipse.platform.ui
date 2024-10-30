/************************************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Mickael Istria (Red Hat Inc.) - [484157] Add zoom test
 *     Dirk Steinkamp <dirk.steinkamp@gmx.de> - [576377] Add multi caret selection commands test
 ************************************************************************************************/
package org.eclipse.ui.editors.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.jface.text.tests.codemining.CodeMiningTest;

import org.eclipse.ui.internal.texteditor.stickyscroll.DefaultStickyLinesProviderTest;
import org.eclipse.ui.internal.texteditor.stickyscroll.StickyScrollingControlTest;
import org.eclipse.ui.internal.texteditor.stickyscroll.StickyScrollingHandlerTest;

/**
 * Test Suite for org.eclipse.ui.editors.
 *
 * @since 3.0
 */
@RunWith(Suite.class)
@SuiteClasses({
		ChainedPreferenceStoreTest.class,
		DocumentProviderRegistryTest.class,
		EncodingChangeTests.class,
		GotoLineTest.class,
		SegmentedModeTest.class,
		MarkerAnnotationOrderTest.class,
		ZoomTest.class,
		FileDocumentProviderTest.class,
		TextFileDocumentProviderTest.class,
		FindNextActionTest.class,
		StatusEditorTest.class,
		TextNavigationTest.class,
		LargeFileTest.class, CaseActionTest.class,
		TextMultiCaretNavigationTest.class,
		TextMultiCaretSelectionCommandsTest.class,

		StickyScrollingControlTest.class,
		StickyScrollingHandlerTest.class,
		DefaultStickyLinesProviderTest.class,

		CodeMiningTest.class,
})
public class EditorsTestSuite {
	// see @SuiteClasses
}
