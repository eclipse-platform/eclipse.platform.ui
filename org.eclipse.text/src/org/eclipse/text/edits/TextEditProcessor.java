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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * A <code>TextEditProcessor</code> manages a set of edits and applies
 * them as a whole to an <code>IDocument</code>.
 * <p>
 * This class isn't intended to be subclassed.
 * 
 * @see org.eclipse.text.edits.TextEdit#apply(IDocument)
 * 
 * @since 3.0
 */
public class TextEditProcessor {
	
	private IDocument fDocument;
	private MultiTextEdit fRoot;
	
	/**
	 * Constructs a new edit processor for the given
	 * document.
	 * 
	 * @param document the document to manipulate
	 */
	public TextEditProcessor(IDocument document) {
		Assert.isNotNull(document);
		fDocument= document;
		fRoot= new MultiTextEdit(0, fDocument.getLength()); 
	}
	
	/**
	 * Returns the document to be manipulated.
	 * 
	 * @return the document
	 */
	public IDocument getDocument() {
		return fDocument;
	}
	
	/**
	 * Returns the edit processor's root edit.
	 * 
	 * @return the processor's root edit
	 */
	public TextEdit getRoot() {
		return fRoot;
	}
	
	/**
	 * Adds an <code>Edit</code> to this edit processor. Adding an edit
	 * to an edit processor transfers ownership of the edit to the 
	 * processor. So after an edit has been added to a processor the 
	 * creator of the edit <b>must</b> not continue modifying the edit.
	 * 
	 * @param edit the edit to add
	 * @exception MalformedTreeException if the text edit can not be 
	 *  added to this edit processor.
	 * 
	 * @see TextEdit#addChild(TextEdit)
	 */
	public void add(TextEdit edit) throws MalformedTreeException {
		checkIntegrity(edit, fDocument);
		fRoot.addChild(edit);
	}
		
	/**
	 * Checks if the processor can execute all its edits.
	 * 
	 * @return <code>true</code> if the edits can be executed. Return  <code>false
	 * 	</code>otherwise. One major reason why edits cannot be executed are wrong 
	 *  offset or length values of edits. Calling perform in this case will very
	 *  likely end in a <code>BadLocationException</code>.
	 */
	public boolean canPerformEdits() {
		return checkBufferLength(fRoot, fDocument.getLength()) == null;
	}
	
	/**
	 * Executes the text edits.
	 * 
	 * @return an object representing the undo of the executed edits
	 * @exception MalformedTreeException is thrown if the edit tree isn't
	 *  in a valid state. This exception is thrown before any edit is executed. 
	 *  So the document is still in its original state.
	 * @exception BadLocationException is thrown if one of the edits in the 
	 *  tree can't be executed. The state of the document is undefined if this 
	 *  exception is thrown.
	 */
	public UndoMemento performEdits() throws MalformedTreeException, BadLocationException {
		return execute();
	}

	/* non Java-doc
	 * Class isn't intended to be sublcassed
	 */	
	protected boolean considerEdit(TextEdit edit) {
		return true;
	}
		
	/* protected */ void checkIntegrity() throws MalformedTreeException {
		TextEdit failure= checkBufferLength(fRoot, fDocument.getLength());
		if (failure != null) {
			throw new MalformedTreeException(failure.getParent(), failure, TextEditMessages.getString("TextEditProcessor.invalid_length")); //$NON-NLS-1$
		}
	}
	
	//---- Helper methods ------------------------------------------------------------------------
		
	private static TextEdit checkBufferLength(TextEdit root, int bufferLength) {
		if (root.getExclusiveEnd() > bufferLength)
			return root;
		List children= root.internalGetChildren();
		if (children != null) {
			for (Iterator iter= children.iterator(); iter.hasNext();) {
				TextEdit edit= (TextEdit)iter.next();
				TextEdit failure= null;
				if ((failure= checkBufferLength(edit, bufferLength)) != null)
					return failure;
			}
		}
		return null;
	}
	
	private static void checkIntegrity(TextEdit root, IDocument document) {
		root.checkIntegrity();
		List children= root.internalGetChildren();
		if (children != null) {
			for (Iterator iter= children.iterator(); iter.hasNext();) {
				TextEdit edit= (TextEdit)iter.next();
				checkIntegrity(edit, document);
			}
		}
	}
	
	private UndoMemento execute() throws BadLocationException {
		Updater.DoUpdater updater= null;
		try {
			updater= Updater.createDoUpdater();
			fDocument.addDocumentListener(updater);
			updater.push(new TextEdit[] { fRoot });
			updater.setIndex(0);
			execute(fRoot, updater);
			return updater.undo;
		} finally {
			if (updater != null)
				fDocument.removeDocumentListener(updater);
		}
	}
	
	private void execute(TextEdit edit, Updater.DoUpdater updater) throws BadLocationException {
		if (edit.hasChildren()) {
			TextEdit[] children= edit.getChildren();
			updater.push(children);
			for (int i= children.length - 1; i >= 0; i--) {
				updater.setIndex(i);
				execute(children[i], updater);
			}
			updater.pop();
		}
		if (considerEdit(edit)) {
			updater.setActiveEdit(edit);
			edit.perform(fDocument);
		}
	}
}
