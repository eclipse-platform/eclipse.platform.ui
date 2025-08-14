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
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import org.eclipse.core.filebuffers.tests.ResourceHelper;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.ui.texteditor.GotoLineAction;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Tests the GotoLineAction.
 *
 * @since 3.1
 */
public class GotoLineTest {

	private static final String ORIGINAL_CONTENT= "line1\nline2\nline3";

	private IFile fFile;

	private String getOriginalContent() {
		return ORIGINAL_CONTENT;
	}

	@Before
	public void setUp() throws Exception {
		IFolder folder= ResourceHelper.createFolder("GoToLineTestProject/goToLineTests/");
		fFile= ResourceHelper.createFile(folder, "file.txt", getOriginalContent());
	}

	@After
	public void tearDown() throws Exception {
		ResourceHelper.deleteProject("GoToLineTestProject");
		fFile= null;
		TestUtil.cleanUp();
	}

	@Test
	public void testGoToFirstLine() {
		goToLine(0, 0);
	}

	@Test
	public void testGoToLastLine() {
		goToLine(2, 2);
	}

	@Test
	public void testGoToInvalidLine() {
		goToLine(1, 1);
		goToLine(-1, 1);
		goToLine(3, 1);
	}

	private void goToLine(int line, int expectedResult) {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchPage page= workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			IEditorPart part= IDE.openEditor(page, fFile);

			if (part instanceof ITextEditor) {
				ITextEditor editor= (ITextEditor) part;
				IAction action= editor.getAction(ITextEditorActionConstants.GOTO_LINE);
				Accessor accessor= new Accessor(action, GotoLineAction.class);
				accessor.invoke("gotoLine", new Class[] { int.class }, Integer.valueOf(line));
				Control control= part.getAdapter(Control.class);
				if (control instanceof StyledText) {
					int caretLine= -1;
					StyledText styledText= (StyledText) control;
					int caret= styledText.getCaretOffset();
					try {
						IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
						caretLine= document.getLineOfOffset(caret);
					} catch (BadLocationException e1) {
						fail();
					}
					assertEquals(expectedResult, caretLine);
				} else
					fail();
			} else
				fail();
		} catch (PartInitException e) {
			fail();
		}
	}
}
