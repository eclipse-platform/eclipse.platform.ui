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

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;


/* package */ abstract class Updater implements IDocumentListener {

	protected UndoEdit undo= new UndoEdit();
	
	public static Updater createUndoUpdater(TextEdit root) {
		return new UndoUpdater(root);
	}

	public static DoUpdater createDoUpdater(TextEdit root) {
		return new DoUpdater(root);
	}
		
	public static class DoUpdater extends Updater {
		private TextEdit fRoot;
		private TextEdit fActiveEdit;
		private TreeIterationInfo fIterationInfo= new TreeIterationInfo();
		
		public DoUpdater(TextEdit root) {
			fRoot= root;
		}
		public void push(TextEdit[] edits) {
			fIterationInfo.push(edits);
		}
		public void setIndex(int index) {
			fIterationInfo.setIndex(index);
		}
		public void pop() {
			fIterationInfo.pop();
		}
		public void setActiveEdit(TextEdit edit) {
			fActiveEdit= edit;
		}
		public void documentChanged(DocumentEvent event) {
			fActiveEdit.update(event, fIterationInfo);
		}
		public void storeRegion() {
			undo.defineRegion(fRoot.getOffset(), fRoot.getLength());
		}
	}
	
	public static class UndoUpdater extends Updater {
		private int fOffset;
		private int fLength;
		private UndoUpdater(TextEdit root) {
			fOffset= root.getOffset();
			fLength= root.getLength();
		}
		public void documentChanged(DocumentEvent event) {
			fLength+= TextEdit.getDelta(event);
		}
		public void storeRegion() {
			undo.defineRegion(fOffset, fLength);
		}
	}

	protected Updater() {
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

		undo.add(new ReplaceEdit(offset, event.getText().length(), currentText));
	}
	
	public void documentChanged(DocumentEvent event) {
	}
	
	public abstract void storeRegion();	
}
