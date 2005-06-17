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

import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;


public class SegmentedModeTest extends TestCase {
	
	private static final String ORIGINAL_CONTENT= "this\nis\nthe\ncontent\nof\nthe\nfile"; 
	
	public static Test suite() {
		return new TestSuite(SegmentedModeTest.class);
	}
	
	private IFile fFile;
	
	private String getOriginalContent() {
		return ORIGINAL_CONTENT;
	}

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		fFile= ResourceHelper.createFile(folder, "file.txt", getOriginalContent());
	}
	
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		ResourceHelper.deleteProject("project");
	}
	
	/*
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=70934
	 */
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
					
					Control control= (Control) part.getAdapter(Control.class);
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
}
