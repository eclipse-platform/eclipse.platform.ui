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

package org.eclipse.jface.text;
 
 /**
  * A target publishing the required functions to modify a document that is displayed
  * in the ui, such as in a text viewer. It provides access to the document and control 
  * over the redraw behavior and the batching of document changes into undo commands.
  * 
  * @see org.eclipse.jface.text.ITextViewer
  * @see org.eclipse.jface.text.IDocument
  * @see org.eclipse.jface.text.IUndoManager
  * @since 2.0
  */
public interface IRewriteTarget {
	
	/**
	 * Returns the document of this target.
	 * 
	 * @return the document of this target
	 */
	IDocument getDocument();
	
	/**
	 * Disables/enables redrawing of the ui while modifying the target's document.
	 * 
	 * @param redraw <code>true</code> if the document's ui presentation should 
	 * 	be updated, <code>false</code> otherwise
	 */
	void setRedraw(boolean redraw);
	
	/**
	 * If an undo manager is connected to the document's ui presentation, this
	 * method tells the undo manager to fold all subsequent changes into
	 * one single undo command until <code>endCompoundChange</code> is called.
	 */
	void beginCompoundChange();
	
	/**
	 * If an undo manager is connected to the document's ui presentation, this method
	 * tells the undo manager to stop the folding of changes into a single undo command. 
	 * After this call, all subsequent changes are considered to be individually undoable.
	 */
	void endCompoundChange();
}

