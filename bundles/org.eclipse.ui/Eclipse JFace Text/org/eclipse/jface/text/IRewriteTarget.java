package org.eclipse.jface.text;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

 
 /**
  * Rewrite target.
  */
public interface IRewriteTarget {
	
	/**
	 * Returns the document of this target.
	 */
	IDocument getDocument();
	
	/**
	 * Disables/Enables redrawing.
	 */
	void setRedraw(boolean redraw);
	
	/**
	 * Signals the undo manager that all subsequent changes until
	 * <code>endCompoundChange</code> is called are to be
	 * undone in one piece.
	 */
	void beginCompoundChange();
	
	/**
	 * Signals the undo manager that the sequence of changes which started
	 * with  <code>beginCompoundChange</code> has been finished. All 
	 * subsequent changes are considered to be individually undoable.
	 */
	void endCompoundChange();
}

