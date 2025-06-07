/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.ui.editors.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/*
 * Note: this test would better fit in the org.eclipse.ui.workbench.texteditor bundle, but initializing
 * and editor from this bundle is quite tricky without the IDE and EFS utils.
 */
public class TextNavigationTest {

	private File file;
	private AbstractTextEditor editor;
	private StyledText widget;
	private IDocument fDocument;

	@Before
	public void setUp() throws IOException, PartInitException, CoreException {
		file = File.createTempFile(TextNavigationTest.class.getName(), ".txt");
		Files.write(file.toPath(), "  abc".getBytes());
		editor = (AbstractTextEditor)IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), EFS.getStore(file.toURI()));
		fDocument = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		widget = (StyledText) editor.getAdapter(Control.class);
	}

	@After
	public void tearDown() {
		editor.close(false);
		file.delete();
		TestUtil.cleanUp();
	}

	@Test
	public void testHome() {
		IPreferenceStore preferenceStore = EditorsPlugin.getDefault().getPreferenceStore();
		boolean previousPrefValue = preferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_NAVIGATION_SMART_HOME_END);
		preferenceStore.setValue(AbstractTextEditor.PREFERENCE_NAVIGATION_SMART_HOME_END, false);
		fDocument.set("line1\nline2");
		editor.selectAndReveal(fDocument.getLength(), 0);
		editor.getAction(ITextEditorActionDefinitionIds.LINE_START).run();
		try {
			assertEquals(6, ((ITextSelection) editor.getSelectionProvider().getSelection()).getOffset());
			editor.getAction(ITextEditorActionDefinitionIds.LINE_START).run();
			assertEquals(6, ((ITextSelection) editor.getSelectionProvider().getSelection()).getOffset());
		} finally {
			preferenceStore.setValue(AbstractTextEditor.PREFERENCE_NAVIGATION_SMART_HOME_END, previousPrefValue);
		}
	}

	@Test
	public void testShiftHome() {
		editor.selectAndReveal(5, 0);
		IAction action= editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_START);
		action.run();
		ITextSelection selection= (ITextSelection) editor.getSelectionProvider().getSelection();
		assertEquals(2, selection.getOffset());
		assertEquals(3, selection.getLength());
		assertEquals(2, widget.getCaretOffset());
		action.run();
		selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		assertEquals(0, selection.getOffset());
		assertEquals(5, selection.getLength());
		assertEquals(0, widget.getCaretOffset());
	}

	@Test
	public void testShiftEnd() {
		editor.getSelectionProvider().setSelection(new TextSelection(0, 0));
		IAction action= editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_END);
		action.run();
		ITextSelection selection= (ITextSelection) editor.getSelectionProvider().getSelection();
		assertEquals(0, selection.getOffset());
		assertEquals(5, selection.getLength());
		assertEquals(5, widget.getCaretOffset());
	}

	@Test
	public void testShiftEndMultipleLines() {
		fDocument.set("LINE 1\nLINE 2\n");
		editor.selectAndReveal(12, -7);
		editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_END).run();
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		assertEquals(6, selection.getOffset());
		assertEquals(6, selection.getLength());
		assertEquals(6, widget.getCaretOffset());
	}

	@Test
	public void testShiftEndHomeHome() {
		editor.getSelectionProvider().setSelection(new TextSelection(0, 0));
		assertEquals(0, widget.getCaretOffset());

		editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_END).run();
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		assertEquals(0, selection.getOffset());
		assertEquals(5, selection.getLength());
		assertEquals(5, widget.getCaretOffset());

		editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_START).run();
		selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		assertEquals(0, selection.getOffset());
		assertEquals(2, selection.getLength());
		assertEquals(2, widget.getCaretOffset()); // Bug 577727

		editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_START).run();
		selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		assertEquals(0, selection.getOffset());
		assertEquals(0, selection.getLength());
		assertEquals(0, widget.getCaretOffset());
	}

	@Test
	public void testEndHomeRevealCaret() {
		editor.getSelectionProvider().setSelection(new TextSelection(0, 0));
		fDocument.set(IntStream.range(0, 2000).mapToObj(i -> "a").collect(Collectors.joining()));
		PlatformUI.getWorkbench().getIntroManager().closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
		assertTrue(DisplayHelper.waitForCondition(widget.getDisplay(), 2000, () -> widget.isVisible()));
		int firstCharX = widget.getTextBounds(0, 0).x;
		assertTrue(firstCharX >= 0 && firstCharX <= widget.getClientArea().width);
		assertEquals(0, widget.getClientArea().x);
		editor.getAction(ITextEditorActionDefinitionIds.LINE_END).run();
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		assertEquals(fDocument.getLength(), selection.getOffset());
		int lastCharX = widget.getTextBounds(fDocument.getLength() - 1, fDocument.getLength() - 1).x;
		assertTrue(lastCharX >= 0 && lastCharX <= widget.getClientArea().width);
		editor.getAction(ITextEditorActionDefinitionIds.LINE_START).run();
		firstCharX = widget.getTextBounds(0, 0).x;
		assertTrue(firstCharX >= 0 && firstCharX <= widget.getClientArea().width);
	}
}
