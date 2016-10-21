/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jonah Graham (Kichwa Coders) - Bug 465684 - Fix initial setVisibleRegion of 0, 0
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import org.eclipse.core.filebuffers.tests.ResourceHelper;

import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.ui.texteditor.ITextEditor;


public class SegmentedModeTest {

	private static final String ORIGINAL_CONTENT= "this\nis\nthe\ncontent\nof\nthe\nfile";

	private IFile fFile;

	private String getOriginalContent() {
		return ORIGINAL_CONTENT;
	}

	@Before
	public void setUp() throws Exception {
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		fFile= ResourceHelper.createFile(folder, "file.txt", getOriginalContent());
	}

	@After
	public void tearDown() throws Exception {
		ResourceHelper.deleteProject("project");
		TestUtil.cleanUp();
	}

	/*
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=70934
	 */
	@Test
	public void testSegmentation() {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchPage page= workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			IEditorPart part= IDE.openEditor(page, fFile);

			try {
				if (part instanceof ITextEditor) {
					ITextEditor editor= (ITextEditor) part;

					editor.showHighlightRangeOnly(true);
					editor.setHighlightRange(5, 0, true);

					Control control= part.getAdapter(Control.class);
					if (control instanceof StyledText) {
						StyledText styledText= (StyledText) control;
						int caret= styledText.getCaretOffset();
						styledText.replaceTextRange(caret, 0, "really ");

						StringBuffer buffer= new StringBuffer(getOriginalContent());
						buffer.insert(5, "really ");
						IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
						assertEquals(buffer.toString(), document.get());
					}
				}
			} finally {
				page.saveEditor(part, false);
			}

		} catch (PartInitException e) {
			assertTrue(false);
		}
	}

	/*
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=465684
	 */
	@Test
	public void testShowNothing() {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchPage page= workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			while (Display.getDefault().readAndDispatch()) {
			}
			IEditorPart part= IDE.openEditor(page, fFile);

			try {
				if (part instanceof ITextEditor) {
					ITextEditor editor= (ITextEditor)part;

					editor.showHighlightRangeOnly(true);
					editor.setHighlightRange(0, 0, true);

					Control control= part.getAdapter(Control.class);
					if (control instanceof StyledText) {
						StyledText styledText= (StyledText)control;
						String text= styledText.getText();
						assertEquals("", text);
					}
				}
			} finally {
				page.saveEditor(part, false);
			}

		} catch (PartInitException e) {
			assertTrue(false);
		}
	}
}
