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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IMultiTextSelection;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.MultiTextSelection;
import org.eclipse.jface.text.Region;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/*
 * Note: this test would better fit in the org.eclipse.ui.workbench.texteditor bundle, but initializing
 * and editor from this bundle is quite tricky without the IDE and EFS utils.
 */
public class TextMultiCaretNavigationTest {

	private static File file;
	private static AbstractTextEditor editor;
	private static StyledText widget;

	@Before
	public void setUpBeforeClass() throws IOException, PartInitException, CoreException {
		file = File.createTempFile(TextMultiCaretNavigationTest.class.getName(), ".txt");
		Files.write(file.toPath(), "  abc\n    1234\nxyz".getBytes());
		editor = (AbstractTextEditor)IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), EFS.getStore(file.toURI()));
		widget = (StyledText) editor.getAdapter(Control.class);
	}

	@After
	public void tearDown() {
		editor.close(false);
		file.delete();
		TestUtil.cleanUp();
	}


	@Test
	public void testShiftHome() {
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		editor.getSelectionProvider().setSelection(new MultiTextSelection(document,
				new IRegion[] { new Region(5, 0), new Region(14, 0), new Region(18, 0), }));
		assertEquals(5, widget.getCaretOffset());

		editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_START).run();
		IMultiTextSelection selection = (IMultiTextSelection) editor.getSelectionProvider().getSelection();
		assertArrayEquals(new IRegion[] { new Region(2, 3), new Region(10, 4), new Region(15, 3) },
				selection.getRegions());
		assertEquals(2, widget.getCaretOffset());

		editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_START).run();
		selection = (IMultiTextSelection) editor.getSelectionProvider().getSelection();
		assertArrayEquals(new IRegion[] { new Region(0, 5), new Region(6, 8), new Region(15, 3) },
				selection.getRegions());
		assertEquals(0, widget.getCaretOffset());
	}

	@Test
	public void testShiftEnd() {
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		editor.getSelectionProvider().setSelection(new MultiTextSelection(document,
				new IRegion[] { new Region(0, 0), new Region(6, 0), new Region(15, 0), }));
		assertEquals(0, widget.getCaretOffset());

		editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_END).run();
		IMultiTextSelection selection = (IMultiTextSelection) editor.getSelectionProvider().getSelection();
		assertArrayEquals(new IRegion[] { new Region(0, 5), new Region(6, 8), new Region(15, 3) },
				selection.getRegions());
		assertEquals(5, widget.getCaretOffset());
	}

	@Test
	public void testShiftEndHomeHome() {
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		editor.getSelectionProvider().setSelection(new MultiTextSelection(document,
				new IRegion[] { new Region(0, 0), new Region(6, 0), new Region(15, 0), }));
		assertEquals(0, widget.getCaretOffset());

		editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_END).run();
		IMultiTextSelection selection = (IMultiTextSelection) editor.getSelectionProvider().getSelection();
		assertArrayEquals(new IRegion[] { new Region(0, 5), new Region(6, 8), new Region(15, 3) },
				selection.getRegions());
		assertEquals(5, widget.getCaretOffset());

		editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_START).run();
		selection = (IMultiTextSelection) editor.getSelectionProvider().getSelection();
		assertArrayEquals(new IRegion[] { new Region(0, 2), new Region(6, 4), new Region(15, 0) },
				selection.getRegions());
		assertEquals(2, widget.getCaretOffset()); // Bug 577727

		editor.getAction(ITextEditorActionDefinitionIds.SELECT_LINE_START).run();
		selection = (IMultiTextSelection) editor.getSelectionProvider().getSelection();
		assertArrayEquals(new IRegion[] { new Region(0, 0), new Region(6, 0), new Region(15, 0) },
				selection.getRegions());
		assertEquals(0, widget.getCaretOffset());
	}

}
