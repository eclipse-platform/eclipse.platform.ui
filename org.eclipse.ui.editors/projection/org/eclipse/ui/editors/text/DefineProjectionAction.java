/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;

import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * CollapseAction.java
 */
public class DefineProjectionAction extends TextEditorAction {
	
	/**
	 * Constructor for CollapseAction.
	 * @param bundle
	 * @param prefix
	 * @param editor
	 */
	public DefineProjectionAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}
	
	/*
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		ProjectionTextEditor editor= (ProjectionTextEditor) getTextEditor();
		ITextSelection s= (ITextSelection) editor.getSelectionProvider().getSelection();
		
		IDocumentProvider provider= editor.getDocumentProvider();
		IDocument document= provider.getDocument(editor.getEditorInput());
		
		try {
			
			int line1= document.getLineOfOffset(s.getOffset());
			int start= document.getLineOffset(line1);
			
			int line2= document.getLineOfOffset(s.getOffset() + s.getLength());
			int lineStart= document.getLineOffset(line2);
			int end= lineStart +  document.getLineLength(line2);
			
			if (line2 > line1) {
//				System.out.println("lines " + line1 + " and " + line2 + "  are defined as collapsable.");
				editor.defineProjection(start, end - start);
			}
						
		} catch (BadLocationException x) {
		}
	}
}
