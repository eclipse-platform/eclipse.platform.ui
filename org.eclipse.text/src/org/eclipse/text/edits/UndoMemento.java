/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.edits;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * This class encapsulates the reverse changes of an executed text 
 * edit tree. To apply an undo memento to a document use method
 * <code>apply(IDocument)</code>.
 */
public final class UndoMemento {

	private List fEdits;
	
	/**
	 * Returns whether this undo memento can be applied to the
	 * given document or not.
	 * 
	 * @return <code>true</code> if the undo memento can be
	 *  applied to the given document. Otherwise <code>false
	 *  </code> is returned.
	 */
	public boolean canBeApplied(IDocument document) {
		Assert.isNotNull(document);
		return checkBufferLength(document.getLength()) != null;
	}
	 
	/**
	 * Applies this undo memento to the given document.
	 * 
	 * @param document the document to be manipulated
	 * 
	 * @exception MalformedTreeException is thrown if the undo isn't
	 *  in a valid state. This exception is thrown before any edit
	 *  is executed. So the document is still in its original state.
	 * @exception BadLocationException is thrown if one of the edits
	 *  of the undo can't be executed. The state of the document is
	 *  undefined if this exception is thrown.
	 * 
	 * @see TextEdit#apply(IDocument)
	 */ 
	public UndoMemento apply(IDocument document) throws MalformedTreeException, BadLocationException {
		Assert.isNotNull(document);
		TextEdit failure= checkBufferLength(document.getLength());
		if (failure != null)
			throw new MalformedTreeException(failure.getParent(), failure, TextEditMessages.getString("UndoMemento.invalid_length")); //$NON-NLS-1$
		return perform(document);		
	}

	/* package */ UndoMemento() {
		fEdits= new ArrayList(5);
	}
	
	/* package */ void add(ReplaceEdit edit) {
		fEdits.add(edit);
	}

	private TextEdit checkBufferLength(int bufferLength) {
		for (int i= fEdits.size() - 1; i >= 0; i--) {
			ReplaceEdit edit= (ReplaceEdit)fEdits.get(i);
			if (edit.getExclusiveEnd() > bufferLength)
				return edit;
			bufferLength+= getDelta(edit);
		}
		return null;
	}
		
	private UndoMemento perform(IDocument document) throws BadLocationException {
		Updater updater= null;
		try {
			updater= Updater.createUndoUpdater();
			document.addDocumentListener(updater);
			for (int i= fEdits.size() - 1; i >= 0; i--) {
				((TextEdit)fEdits.get(i)).perform(document);
			}
			return updater.undo;
		} finally {
			if (updater != null)
				document.removeDocumentListener(updater);
		}
	}
	
	private int getDelta(ReplaceEdit edit) {
		return edit.getText().length() - edit.getLength();
	}
}

