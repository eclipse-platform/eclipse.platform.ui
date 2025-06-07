/*******************************************************************************
 * Copyright (c) 2022 Dirk Steinkamp
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Steinkamp <dirk.steinkamp@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IMultiTextSelection;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.MultiTextSelection;
import org.eclipse.jface.text.Region;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.ide.IDE;

import org.eclipse.ui.texteditor.AbstractTextEditor;

/*
 * Note: this test would better fit in the org.eclipse.ui.workbench.texteditor bundle, but initializing
 * an editor from this bundle is quite tricky without the IDE and EFS utils.
 */
public class TextMultiCaretSelectionCommandsTest {
	private static final String MULTI_SELECTION_DOWN = "org.eclipse.ui.edit.text.select.selectMultiSelectionDown";
	private static final String ADD_ALL_MATCHES_TO_MULTI_SELECTION = "org.eclipse.ui.edit.text.select.addAllMatchesToMultiSelection";
	private static final String MULTI_SELECTION_UP = "org.eclipse.ui.edit.text.select.selectMultiSelectionUp";
	private static final String STOP_MULTI_SELECTION = "org.eclipse.ui.edit.text.select.stopMultiSelection";
	private static final String MULTI_CARET_DOWN = "org.eclipse.ui.edit.text.select.multiCaretDown";
	private static final String MULTI_CARET_UP = "org.eclipse.ui.edit.text.select.multiCaretUp";

	private static final String LINE_1 = "private static String a;\n";
	private static final String LINE_2 = "private static String b; // this is a little longer\n";
	private static final String LINE_3 = "\t\tprivate static String c;\n";
	private static final String LINE_4 = "private static String d";

	private static final int L1_LEN = LINE_1.length();
	private static final int L2_LEN = LINE_2.length();
	private static final int L3_LEN = LINE_3.length();
	private static final int L4_LEN = LINE_4.length();

	private static File file;
	private static AbstractTextEditor editor;
	private static StyledText widget;

	@Before
	public void setUpBeforeClass() throws IOException, PartInitException, CoreException {
		file = File.createTempFile(TextMultiCaretSelectionCommandsTest.class.getName(), ".txt");
		Files.write(file.toPath(), (LINE_1 + LINE_2 + LINE_3 + LINE_4) //
				.getBytes());
		editor = (AbstractTextEditor) IDE.openEditorOnFileStore(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), EFS.getStore(file.toURI()));
		widget = (StyledText) editor.getAdapter(Control.class);
	}

	@After
	public void tearDown() {
		editor.close(false);
		file.delete();
		TestUtil.cleanUp();
	}

	@Test
	public void testMultiSelectionDown_withFirstIdentifierSelected_addsIdenticalIdentifiersToSelection()
			throws Exception {
		setSelection(new IRegion[] { new Region(0, 7) });
		assertEquals(7, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_DOWN);

		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 7), new Region(L1_LEN, 7) }, getSelection());

		executeCommand(MULTI_SELECTION_DOWN);

		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 7), new Region(L1_LEN, 7), new Region(L1_LEN + L2_LEN + 2, 7) },
				getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(7, 0) }, getSelection());
	}

	@Test
	public void testMultiSelectionDown_withSecondIdentifierSelectedIdentifier_addsNextOccurenceToSelection()
			throws Exception {
		setSelection(new IRegion[] { new Region(8, 6) });
		assertEquals(14, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_DOWN);

		assertEquals(14, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(8, 6), new Region(L1_LEN + 8, 6) }, getSelection());
	}

	@Test
	public void testMultiSelectionDown_withSelectionInSecondRow_addsIdenticalIdentifierInThirdRowToSelection()
			throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + 8, 6) });
		assertEquals(L1_LEN + 14, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_DOWN);

		assertEquals(L1_LEN + 14, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 6), new Region(L1_LEN + L2_LEN + 10, 6) },
				getSelection());
	}

	@Test
	public void testMultiSelectionDown_withTwoSelectionsAndAnchorBelow_reducesSelection() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + 8, 6) });
		// It is important here to build up the selection in steps, so the
		// handler can determine an anchor region
		executeCommand(MULTI_SELECTION_UP);
		assertArrayEquals(new IRegion[] { new Region(8, 6), new Region(L1_LEN + 8, 6) }, getSelection());

		executeCommand(MULTI_SELECTION_DOWN);

		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 6) }, getSelection());
	}

	@Test
	public void testMultiSelectionDown_withTwoSelectionsAndAnchorAbove_extendsSelection() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + 8, 6) });
		// It is important here to build up the selection in steps, so the
		// handler can determine an anchor region
		executeCommand(MULTI_SELECTION_DOWN);
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 6), new Region(L1_LEN + L2_LEN + 10, 6) },
				getSelection());

		executeCommand(MULTI_SELECTION_DOWN);

		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 6), new Region(L1_LEN + L2_LEN + 10, 6),
				new Region(L1_LEN + L2_LEN + L3_LEN + 8, 6) }, getSelection());
	}

	// Caret-related tests for ADD_NEXT_MATCH_TO_MULTI_SELECTION
	// that check how the selection is expanded

	@Test
	public void testMultiSelectionDown_withCaretInFirstIdentifier_selectsFullIdentifier() throws Exception {
		setSelection(new IRegion[] { new Region(1, 0) });
		assertEquals(1, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_DOWN);

		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 7) }, getSelection());
	}

	@Test
	public void testMultiSelectionDown_withCaretInSecondIdentifier_selectsFullIdentifier() throws Exception {
		setSelection(new IRegion[] { new Region(11, 0) });
		assertEquals(11, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_DOWN);

		assertEquals(14, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(8, 6) }, getSelection());
	}

	@Test
	public void testMultiSelectionDown_withCaretBetweenIdentifierCharAndNonIdentifierChar_selectsFullIdentifier()
			throws Exception {
		setSelection(new IRegion[] { new Region(23, 0) });
		assertEquals(23, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_DOWN);

		assertEquals(23, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(22, 1) }, getSelection());
	}

	@Test
	public void testMultiSelectionDown_withCaretInSecondRow_selectsFullIdentifier() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + 11, 0) });
		assertEquals(L1_LEN + 11, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_DOWN);

		assertEquals(L1_LEN + 14, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 6) }, getSelection());
	}

	@Test
	public void testMultiSelectionDown_withCaretInIdentifierWithNoFollowingMatch_selectsFullIdentifier()
			throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + L2_LEN + L3_LEN + 11, 0) });
		assertEquals(L1_LEN + L2_LEN + L3_LEN + 11, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_DOWN);

		assertEquals(L1_LEN + L2_LEN + L3_LEN + 14, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + L2_LEN + L3_LEN + 8, 6) }, getSelection());
	}

	@Test
	public void testMultiSelectionDown_withCaretAtEndOfDocument_selectsFullIdentifier() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + L2_LEN + L3_LEN + L4_LEN, 0) });
		assertEquals(L1_LEN + L2_LEN + L3_LEN + L4_LEN, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_DOWN);

		assertEquals(L1_LEN + L2_LEN + L3_LEN + L4_LEN, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + L2_LEN + L3_LEN + L4_LEN - 1, 1) }, getSelection());
	}

	@Test
	public void testAddAllMatches_withSingleSelection_selectsAllOccurences() throws Exception {
		setSelection(new IRegion[] { new Region(0, 7) });
		assertEquals(7, widget.getCaretOffset());

		executeCommand(ADD_ALL_MATCHES_TO_MULTI_SELECTION);

		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 7), new Region(L1_LEN, 7), new Region(L1_LEN + L2_LEN + 2, 7),
				new Region(L1_LEN + L2_LEN + L3_LEN, 7) }, getSelection());
	}

	@Test
	public void testAddAllMatches_withDoubleSelectionOfSameText_selectsAllOccurences() throws Exception {
		setSelection(new IRegion[] { new Region(0, 7), new Region(L1_LEN, 7) });
		assertEquals(7, widget.getCaretOffset());

		executeCommand(ADD_ALL_MATCHES_TO_MULTI_SELECTION);

		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 7), new Region(L1_LEN, 7), new Region(L1_LEN + L2_LEN + 2, 7),
				new Region(L1_LEN + L2_LEN + L3_LEN, 7) }, getSelection());
	}

	@Test
	public void testAddAllMatches_withDoubleSelectionOfDifferentTexts_doesNotChangeSelection() throws Exception {
		setSelection(new IRegion[] { new Region(0, 7), new Region(8, 7) });
		assertEquals(7, widget.getCaretOffset());

		executeCommand(ADD_ALL_MATCHES_TO_MULTI_SELECTION);

		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 7), new Region(8, 7) }, getSelection());
	}

	@Test
	public void testAddAllMatches_withCaretInIdentifier_selectsAllOccurencesOfIdentifier() throws Exception {
		setSelection(new IRegion[] { new Region(2, 0) });
		assertEquals(2, widget.getCaretOffset());

		executeCommand(ADD_ALL_MATCHES_TO_MULTI_SELECTION);

		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 7), new Region(L1_LEN, 7), new Region(L1_LEN + L2_LEN + 2, 7),
				new Region(L1_LEN + L2_LEN + L3_LEN, 7) }, getSelection());
	}

	@Test
	public void testMultiSelectionUp_withCaretInIdentifier_selectsFullIdentifier() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + 11, 0) });
		assertEquals(L1_LEN + 11, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_UP);

		assertEquals(L1_LEN + 8 + 6, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 6) }, getSelection());
	}

	@Test
	public void testMultiSelectionUp_withSingleSelectionAndNoPreviousMatch_doesNothing() throws Exception {
		setSelection(new IRegion[] { new Region(8, 6) });
		assertEquals(14, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_UP);

		assertEquals(14, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(8, 6) }, getSelection());
	}

	@Test
	public void testMultiSelectionUp_withTwoSelections_removesSecondSelection() throws Exception {
		setSelection(new IRegion[] { new Region(8, 6), new Region(L1_LEN + 8, 6) });
		assertEquals(14, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_UP);

		assertEquals(14, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(8, 6) }, getSelection());
	}

	@Test
	public void testMultiSelectionUp_withThreeSelections_removesThirdSelection() throws Exception {
		setSelection(
				new IRegion[] { new Region(8, 6), new Region(L1_LEN + 8, 6), new Region(L1_LEN + L2_LEN + 10, 6) });
		assertEquals(14, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_UP);

		assertEquals(14, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(8, 6), new Region(L1_LEN + 8, 6) }, getSelection());
	}

	@Test
	public void testMultiSelectionUp_withTwoSelectionsAndAnchorAbove_reducesSelection() throws Exception {
		setSelection(new IRegion[] { new Region(8, 6) });
		// It is important here to build up the selection in steps, so the
		// handler can determine an anchor region
		executeCommand(MULTI_SELECTION_DOWN);
		assertArrayEquals(new IRegion[] { new Region(8, 6), new Region(L1_LEN + 8, 6) }, getSelection());

		executeCommand(MULTI_SELECTION_UP);

		assertArrayEquals(new IRegion[] { new Region(8, 6) }, getSelection());
	}

	@Test
	public void testMultiSelectionUp_withTwoSelectionsAndAnchorBelow_extendsSelection() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + L2_LEN + 10, 6) });
		// It is important here to build up the selection in steps, so the
		// handler can determine an anchor region
		executeCommand(MULTI_SELECTION_UP);
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 6), new Region(L1_LEN + L2_LEN + 10, 6) },
				getSelection());

		executeCommand(MULTI_SELECTION_UP);

		assertArrayEquals(
				new IRegion[] { new Region(8, 6), new Region(L1_LEN + 8, 6), new Region(L1_LEN + L2_LEN + 10, 6) },
				getSelection());
	}

	@Test
	public void testStopMultiSelection_withSingleSelection_doesNotChangeSelectionOrCaretOffset() throws Exception {
		setSelection(new IRegion[] { new Region(0, 7) });

		assertEquals(7, widget.getCaretOffset());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 7) }, getSelection());
	}

	@Test
	public void testStopMultiSelection_withMultiSelection_revokesSelectionAndKeepsFirstCaretOffset() throws Exception {
		setSelection(new IRegion[] { new Region(0, 7), new Region(L1_LEN, 7) });

		assertEquals(7, widget.getCaretOffset());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(7, 0) }, getSelection());
	}

	@Test
	public void testStopMultiSelection_withMultiSelectionAndCaretAtBeginning_revokesSelectionAndKeepsFirstCaretOffset()
			throws Exception {
		setSelection(new IRegion[] { new Region(0, 7), new Region(L1_LEN, 7) });
		assertEquals(7, widget.getCaretOffset());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(7, 0) }, getSelection());
	}

	@Test
	public void testStopMultiSelection_withMultiSelectionAndCaretAfterLastSelection_revokesSelectionAndKeepsCaretOffset()
			throws Exception {
		setSelection(new IRegion[] { new Region(0, 7), new Region(L1_LEN, 7), new Region(L1_LEN + L2_LEN, 7) });
		assertEquals(7, widget.getCaretOffset());

		executeCommand(MULTI_SELECTION_DOWN);
		executeCommand(MULTI_SELECTION_DOWN);

		// TODO How to place the caret at the end without dismissing the
		// selection? Should rather be 57
		assertEquals(7, widget.getCaretOffset());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(7, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(7, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretDown_withCaret_addsCaretsInNextLines() throws Exception {
		setSelection(new IRegion[] { new Region(0, 0) });
		assertEquals(0, widget.getCaretOffset());

		executeCommand(MULTI_CARET_DOWN);

		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0), new Region(L1_LEN, 0) }, getSelection());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_DOWN);

		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0), new Region(L1_LEN, 0), new Region(L1_LEN + L2_LEN, 0) },
				getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretDown_withTwoCaretsAndAnchorRegionBelow_removesFirstCaret() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN, 0) });
		assertEquals(L1_LEN, widget.getCaretOffset());

		executeCommand(MULTI_CARET_UP);

		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0), new Region(L1_LEN, 0) }, getSelection());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_DOWN);

		assertEquals(L1_LEN, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN, 0) }, getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(L1_LEN, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretDown_withSingleSelection_addsSelectionInNextLine() throws Exception {
		setSelection(new IRegion[] { new Region(0, 3) });
		assertEquals(3, widget.getCaretOffset());

		executeCommand(MULTI_CARET_DOWN);

		assertEquals(3, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 3), new Region(L1_LEN, 3) }, getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(3, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(3, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretDown_withSingleCaretAtEndOfLongerLine_addsCaretAtEndOfNextLine() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + L2_LEN, 0) });
		assertEquals(L1_LEN + L2_LEN, widget.getCaretOffset());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_DOWN);

		assertEquals(L1_LEN + L2_LEN, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + L2_LEN, 0), new Region(L1_LEN + L2_LEN + L3_LEN, 0) },
				getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(L1_LEN + L2_LEN, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + L2_LEN, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretDown_withSingleCaretInLineAboveLineWithTabs_addsCaretInNextLineRespectingTabs()
			throws Exception {
		widget.setTabs(4); // Make default explicit
		setSelection(new IRegion[] { new Region(L1_LEN + 8, 0) });
		assertEquals(L1_LEN + 8, widget.getCaretOffset());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_DOWN);

		assertEquals(L1_LEN + 8, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 0), new Region(L1_LEN + L2_LEN + 2, 0) },
				getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(L1_LEN + 8, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretDown_withSingleCaretInLineWithTabs_addsCaretInNextLineRespectingTabs() throws Exception {
		widget.setTabs(4); // Make default explicit
		setSelection(new IRegion[] { new Region(L1_LEN + L2_LEN + 2, 0) });
		assertEquals(L1_LEN + L2_LEN + 2, widget.getCaretOffset());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_DOWN);

		assertEquals(L1_LEN + L2_LEN + 2, widget.getCaretOffset());
		assertArrayEquals(
				new IRegion[] { new Region(L1_LEN + L2_LEN + 2, 0), new Region(L1_LEN + L2_LEN + L3_LEN + 8, 0) },
				getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(L1_LEN + L2_LEN + 2, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + L2_LEN + 2, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretDown_withSingleCaretAtEndOfText_doesNotChangeCaret() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + L2_LEN + L3_LEN + L4_LEN, 0) });
		assertEquals(L1_LEN + L2_LEN + L3_LEN + L4_LEN, widget.getCaretOffset());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_DOWN);

		assertEquals(L1_LEN + L2_LEN + L3_LEN + L4_LEN, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + L2_LEN + L3_LEN + L4_LEN, 0) }, getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(L1_LEN + L2_LEN + L3_LEN + L4_LEN, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + L2_LEN + L3_LEN + L4_LEN, 0) }, getSelection());
	}

	/////////////////////////////////////////////////////
	@Test
	public void testMultiCaretUp_withCaret_addsCaretsInPreviousLines() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + L2_LEN, 0) });
		assertEquals(L1_LEN + L2_LEN, widget.getCaretOffset());

		executeCommand(MULTI_CARET_UP);

		assertEquals(L1_LEN, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN, 0), new Region(L1_LEN + L2_LEN, 0) }, getSelection());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_UP);

		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0), new Region(L1_LEN, 0), new Region(L1_LEN + L2_LEN, 0) },
				getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretUp_withTwoCaretsAndAnchorRegionAbove_removesLastCaret() throws Exception {
		setSelection(new IRegion[] { new Region(0, 0) });
		assertEquals(0, widget.getCaretOffset());

		executeCommand(MULTI_CARET_DOWN);

		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0), new Region(L1_LEN, 0) }, getSelection());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_UP);

		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0) }, getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretUp_withSingleSelection_addsSelectionInPreviousLine() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN, 3) });
		assertEquals(L1_LEN + 3, widget.getCaretOffset());

		executeCommand(MULTI_CARET_UP);

		assertEquals(3, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 3), new Region(L1_LEN, 3) }, getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(3, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(3, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretUp_withSingleCaretAtEndOfLongerLine_addsCaretAtEndOfPreviousLine() throws Exception {
		setSelection(new IRegion[] { new Region(L1_LEN + L2_LEN, 0) });
		assertEquals(L1_LEN + L2_LEN, widget.getCaretOffset());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_UP);

		assertEquals(L1_LEN, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN, 0), new Region(L1_LEN + L2_LEN, 0) }, getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(L1_LEN, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretUp_withSingleCaretInLineBelowLineWithTabs_addsCaretInPreviousLineRespectingTabs()
			throws Exception {
		widget.setTabs(4); // Make default explicit
		setSelection(new IRegion[] { new Region(L1_LEN + L2_LEN + L3_LEN + 8, 0) });
		assertEquals(L1_LEN + L2_LEN + L3_LEN + 8, widget.getCaretOffset());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_UP);

		assertEquals(L1_LEN + L2_LEN + 2, widget.getCaretOffset());
		assertArrayEquals(
				new IRegion[] { new Region(L1_LEN + L2_LEN + 2, 0), new Region(L1_LEN + L2_LEN + L3_LEN + 8, 0) },
				getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(L1_LEN + L2_LEN + 2, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + L2_LEN + 2, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretUp_withSingleCaretInLineWithTabs_addsCaretInPreviousLineRespectingTabs()
			throws Exception {
		widget.setTabs(4); // Make default explicit
		setSelection(new IRegion[] { new Region(L1_LEN + L2_LEN + 2, 0) });
		assertEquals(L1_LEN + L2_LEN + 2, widget.getCaretOffset());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_UP);

		assertEquals(L1_LEN + 8, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 0), new Region(L1_LEN + L2_LEN + 2, 0) },
				getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(L1_LEN + 8, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(L1_LEN + 8, 0) }, getSelection());
	}

	@Test
	public void testMultiCaretUp_withSingleCaretAtBeginningOfText_doesNotChangeCaret() throws Exception {
		setSelection(new IRegion[] { new Region(0, 0) });
		assertEquals(0, widget.getCaretOffset());

		widget.setSize(800, 500); // make sure the widget is not size (0,0)
		executeCommand(MULTI_CARET_UP);

		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0) }, getSelection());

		executeCommand(STOP_MULTI_SELECTION);
		assertEquals(0, widget.getCaretOffset());
		assertArrayEquals(new IRegion[] { new Region(0, 0) }, getSelection());
	}

	// Helper methods

	private void executeCommand(String commandId) throws Exception {
		Command command = PlatformUI.getWorkbench().getService(ICommandService.class).getCommand(commandId);
		command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, null, null));
	}

	private void setSelection(IRegion[] regions) {
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		editor.getSelectionProvider().setSelection(new MultiTextSelection(document, regions));
	}

	private IRegion[] getSelection() {
		return ((IMultiTextSelection) editor.getSelectionProvider().getSelection()).getRegions();
	}
}
