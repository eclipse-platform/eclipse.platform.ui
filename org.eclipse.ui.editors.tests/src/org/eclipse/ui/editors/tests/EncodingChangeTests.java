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

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.tests.ResourceHelper;

import org.eclipse.text.tests.Accessor;

import org.eclipse.ui.editors.text.DefaultEncodingSupport;
import org.eclipse.ui.editors.text.IEncodingSupport;
import org.eclipse.ui.editors.text.TextEditor;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.StatusTextEditor;


/**
 * Tests changing of encoding via IFile and via encoding support.
 * 
 * @since 3.1
 */
public class EncodingChangeTests extends TestCase {
	
	private static final String ORIGINAL_CONTENT= "line1\nline2\nline3"; 
	
	public static Test suite() {
		return new TestSuite(EncodingChangeTests.class);
	}
	
	private IFile fFile;
	private int fCount;
	
	private String getOriginalContent() {
		return ORIGINAL_CONTENT;
	}

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		IFolder folder= ResourceHelper.createFolder("EncodingChangeTestProject/EncodingChangeTests/");
		fFile= ResourceHelper.createFile(folder, "file" + fCount + ".txt", getOriginalContent());
		fFile.setCharset(null, null);
		fCount++;
	}
	
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		while (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().readAndDispatch()) {
		}
		try {
			ResourceHelper.deleteProject("EncodingChangeTestProject");
		} catch (CoreException ex) {
			// ignore
		}
		fFile= null;
		while (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().readAndDispatch()) {
		}
	}
	
	public void testChangeEncodingViaFile() {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchPage page= workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			fFile.setCharset("US-ASCII", null);
		} catch (CoreException ex) {
			fail();
		}
		try {
			IEditorPart part= IDE.openEditor(page, fFile);
			if (part instanceof TextEditor) {
				TextEditor editor= (TextEditor)part;
				Accessor accessor= new Accessor(editor, StatusTextEditor.class);
				while (editor.getSite().getShell().getDisplay().readAndDispatch()) {
				}
				ScrolledComposite composite= (ScrolledComposite)accessor.get("fStatusControl");
				assertNull(composite);
				DefaultEncodingSupport encodingSupport= (DefaultEncodingSupport)editor.getAdapter(IEncodingSupport.class);
				assertEquals("US-ASCII", encodingSupport.getEncoding());
			} else
				fail();
		} catch (PartInitException e) {
			fail();
		}
	}
	
	public void testChangeEncodingViaEncodingSupport() {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchPage page= workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			IEditorPart part= IDE.openEditor(page, fFile);
			if (part instanceof TextEditor) {
				TextEditor editor= (TextEditor)part;
				while (editor.getSite().getShell().getDisplay().readAndDispatch()) {
				}
				DefaultEncodingSupport encodingSupport= (DefaultEncodingSupport)editor.getAdapter(IEncodingSupport.class);
				encodingSupport.setEncoding("US-ASCII");
				Accessor accessor= new Accessor(editor, StatusTextEditor.class);
				while (editor.getSite().getShell().getDisplay().readAndDispatch()) {
				}
				ScrolledComposite composite= (ScrolledComposite)accessor.get("fStatusControl");
				assertNull(composite);
				String actual= null;
				try {
					actual= fFile.getCharset(false);
				} catch (CoreException e1) {
					fail();
				}
				assertEquals("US-ASCII", actual);
			} else
				fail();
		} catch (PartInitException e) {
			fail();
		}
	}
	
	public void testAInvalidEncoding() {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchPage page= workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			fFile.setCharset("nonexistent", null);
		} catch (CoreException e2) {
			fail();
		}
		try {
			IEditorPart part= IDE.openEditor(page, fFile);
			if (part instanceof TextEditor) {
				TextEditor editor= (TextEditor)part;
				Accessor accessor= new Accessor(editor, StatusTextEditor.class);
				while (editor.getSite().getShell().getDisplay().readAndDispatch()) {
				}
				ITextFileBuffer fileBuffer= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getFullPath());
				DefaultEncodingSupport encodingSupport= (DefaultEncodingSupport)editor.getAdapter(IEncodingSupport.class);
				String expected= encodingSupport.getStatusMessage(fileBuffer.getStatus());
				Composite composite= (Composite)accessor.get("fStatusControl");
				ScrolledComposite scrolledComposite= (ScrolledComposite)composite.getChildren()[0];
				Label statusText= (Label)((Composite)scrolledComposite.getContent()).getChildren()[5];
				String actual= statusText.getText();
				assertEquals(expected, actual);
			} else
				fail();
		} catch (PartInitException e) {
			fail();
		}
	}
}
