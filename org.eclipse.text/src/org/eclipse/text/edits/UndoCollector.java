/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.edits;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;


/* package */ class UndoCollector implements IDocumentListener {

	protected UndoEdit undo;
	private int fOffset;
	private int fLength;
	
	public UndoCollector(TextEdit root) {
		fOffset= root.getOffset();
		fLength= root.getLength();
	}
	
	public void connect(IDocument document) {
		document.addDocumentListener(this);
		undo= new UndoEdit();
	}
	
	public void disconnect(IDocument document) {
		if (undo != null) {
			document.removeDocumentListener(this);
			undo.defineRegion(fOffset, fLength);
		}
	}
	
	public void documentChanged(DocumentEvent event) {
		fLength+= getDelta(event);
	}
	
	private static int getDelta(DocumentEvent event) {
		String text= event.getText();
		return text == null ? -event.getLength() : (text.length() - event.getLength());
	}		

	public void documentAboutToBeChanged(DocumentEvent event) {
		int offset= event.getOffset();
		int currentLength= event.getLength();
		String currentText= null;
		try {
			currentText= event.getDocument().get(offset, currentLength);
		} catch (BadLocationException cannotHappen) {
			Assert.isTrue(false, "Can't happen"); //$NON-NLS-1$
		}

		String newText = event.getText();
		undo.add(new ReplaceEdit(offset, newText != null ? newText.length() : 0, currentText));
	}
}
