/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.editor;

import org.eclipse.ant.internal.ui.editor.AntEditor;
import org.eclipse.ant.internal.ui.editor.text.XMLTextHover;
import org.eclipse.ant.tests.ui.editor.performance.EditorTestHelper;
import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.PartInitException;

public class AntEditorTests extends AbstractAntUITest {

    public AntEditorTests(String name) {
        super(name);
    }
    
    public void testHover() throws PartInitException, BadLocationException {
        try {
		    IFile file= getIFile("refid.xml");
		    AntEditor editor= (AntEditor)EditorTestHelper.openInEditor(file, "org.eclipse.ant.ui.internal.editor.AntEditor", true);
		    XMLTextHover hover= new XMLTextHover(editor);
		    int offset= getOffsetWithinLine(editor, 9, 20);
		    IRegion region= hover.getHoverRegion(editor.getViewer(), offset);
		    String hoverText= hover.getHoverInfo(editor.getViewer(), region);
		    String correctResult= "<html><body text=\"#000000\" bgcolor=\"#FFFF88\"><font size=-1><h5>Path Elements:</h5><ul><li>";
		    assertTrue("Expected the following hover text to start with: " + correctResult, hoverText.startsWith(correctResult));
		    
        } finally {
            EditorTestHelper.closeAllEditors();    
        }
    }
    
    public void testMacroDefOpenDeclaration() throws PartInitException, BadLocationException {
    	try {
    		IFile file= getIFile("macrodef.xml");
    		AntEditor editor= (AntEditor)EditorTestHelper.openInEditor(file, "org.eclipse.ant.ui.internal.editor.AntEditor", true);
    		int offset = getOffsetWithinLine(editor, 12, 9);
    		editor.selectAndReveal(offset, 0);
    		
    		editor.openReferenceElement();
    		ITextSelection selection= (ITextSelection) editor.getSelectionProvider().getSelection();
    		assertTrue("Selection is not correct: " + selection.getText(), "macrodef".equals(selection.getText()));
    		
    		offset = getOffsetWithinLine(editor, 14, 9);
     		editor.selectAndReveal(offset, 1);
     		
     		editor.openReferenceElement();
    		selection= (ITextSelection) editor.getSelectionProvider().getSelection();
    		assertTrue("Selection is not correct: " + selection.getText(), "macrodef".equals(selection.getText()));
    	} finally {
    		EditorTestHelper.closeAllEditors();    
    	}
    }
    
    public void testMacroDefAttributeOpenDeclaration() throws PartInitException, BadLocationException {
    	try {
    		IFile file= getIFile("macrodef.xml");
    		AntEditor editor= (AntEditor)EditorTestHelper.openInEditor(file, "org.eclipse.ant.ui.internal.editor.AntEditor", true);
    		int offset = getOffsetWithinLine(editor, 12, 16);
    		editor.selectAndReveal(offset, 1);
    		
    		editor.openReferenceElement();
    		ITextSelection selection= (ITextSelection) editor.getSelectionProvider().getSelection();
    		assertTrue("Selection is not correct: " + selection.getText(), "attribute".equals(selection.getText()));
    	} finally {
    		EditorTestHelper.closeAllEditors();    
    	}
    }
    
    public void testRefOpenDeclaration() throws PartInitException, BadLocationException {
    	try {
    		IFile file= getIFile("refid.xml");
    		AntEditor editor= (AntEditor)EditorTestHelper.openInEditor(file, "org.eclipse.ant.ui.internal.editor.AntEditor", true);
    		int offset = getOffsetWithinLine(editor, 9, 20);
    		editor.selectAndReveal(offset, 2);
    		
    		editor.openReferenceElement();
    		ITextSelection selection= (ITextSelection) editor.getSelectionProvider().getSelection();
    		assertTrue("Selection is not correct: " + selection.getText(), "path".equals(selection.getText()));
    	} finally {
    		EditorTestHelper.closeAllEditors();    
    	}
    }
    
    public void testTargetOpenDeclaration() throws PartInitException, BadLocationException {
    	try {
    		IFile file= getIFile("refid.xml");
    		AntEditor editor= (AntEditor)EditorTestHelper.openInEditor(file, "org.eclipse.ant.ui.internal.editor.AntEditor", true);
    		int offset = getOffsetWithinLine(editor, 0, 18);
    		editor.selectAndReveal(offset, 1);
    		
    		editor.openReferenceElement();
    		ITextSelection selection= (ITextSelection) editor.getSelectionProvider().getSelection();
    		assertTrue("Selection is not correct: " + selection.getText(), "target".equals(selection.getText()));
    	} finally {
    		EditorTestHelper.closeAllEditors();    
    	}
    }
    
    public void testHoverRegionWithSpaces() throws PartInitException, BadLocationException {
        try {
            IFile file= getIFile("refid.xml");
            AntEditor editor= (AntEditor)EditorTestHelper.openInEditor(file, "org.eclipse.ant.ui.internal.editor.AntEditor", true);
            XMLTextHover hover= new XMLTextHover(editor);
            //in the middle of the "compile" target of the depends attribute
            int offset= getOffsetWithinLine(editor, 22, 39);
            IRegion region= hover.getHoverRegion(editor.getViewer(), offset);
            
            assertNotNull(region);
            assertTrue("Region incorrect. Expected length of 7 and offset of 552. Got length of " + region.getLength() + " and offset of " + region.getOffset(), region.getLength() == 7 && region.getOffset() == 552);
            
        } finally {
            EditorTestHelper.closeAllEditors();    
        }
    }

	private int getOffsetWithinLine(AntEditor editor, int lineNumber, int offsetInLine) throws BadLocationException {
		IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
		int offset= document.getLineOffset(lineNumber) + offsetInLine;
		return offset;
	}
}
