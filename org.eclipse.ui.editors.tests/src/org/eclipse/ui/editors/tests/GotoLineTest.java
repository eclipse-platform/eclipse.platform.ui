/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
public class GotoLineTest extends TestCase {
	
	private static final String ORIGINAL_CONTENT= "line1\nline2\nline3"; 
	
	public static Test suite() {
		return new TestSuite(GotoLineTest.class);
	}
	
	private IFile fFile;
	
	private String getOriginalContent() {
		return ORIGINAL_CONTENT;
	}

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		IFolder folder= ResourceHelper.createFolder("GoToLineTestProject/goToLineTests/");
		fFile= ResourceHelper.createFile(folder, "file.txt", getOriginalContent());
	}
	
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		ResourceHelper.deleteProject("GoToLineTestProject");
		fFile= null;
	}
	
	public void testGoToFirstLine() {
		goToLine(0, 0);
	}
	
	public void testGoToLastLine() {
		goToLine(2, 2);
	}
	
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
				accessor.invoke("gotoLine", new Class[] {int.class}, new Integer[] {new Integer(line)});
				Control control= (Control) part.getAdapter(Control.class);
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
