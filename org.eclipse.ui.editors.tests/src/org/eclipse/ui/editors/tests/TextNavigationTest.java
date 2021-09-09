/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/*
 * Note: this test would better fit in the org.eclipse.ui.workbench.texteditor bundle, but initializing
 * and editor from this bundle is quite tricky without the IDE and EFS utils.
 */
public class TextNavigationTest {

	private static File file;
	private static AbstractTextEditor editor;
	private static StyledText widget;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException, PartInitException, CoreException {
		file = File.createTempFile(TextNavigationTest.class.getName(), ".txt");
		Files.write(file.toPath(), "  abc".getBytes());
		editor = (AbstractTextEditor)IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), EFS.getStore(file.toURI()));
		widget = (StyledText) editor.getAdapter(Control.class);
	}

	@AfterClass
	public static void tearDownAfterClass() {
		editor.dispose();
		file.delete();
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
}
