package org.eclipse.ui.texteditor;

import org.eclipse.jface.action.IMenuListener;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Extension to <code>ITextEditor</code>. Intention to be integrated with
 * <code>ITextEditor</code>. Should not yet be considered API.
 */
public interface ITextEditorExtension {
	
	/**
	 * Informs the editor about which status field is to be used
	 * when posting information of a given category.
	 * @see ITextEditorActionConstants
	 */
	void setStatusField(IStatusField field, String category);
	
	/**
	 * Returns whether the editor's input is read-only. The semantics of this method is
	 * orthogonal to <code>isEditable</code> as it talks about the editor input and
	 * <b>not</b> about the editor document.
	 */
	boolean isEditorInputReadOnly();

	/**
	 * Adds a ruler context menu listener to the editor.
	 */
	void addRulerContextMenuListener(IMenuListener listener);
	
	/**
	 * Removes a ruler context menu listener from the editor.
	 */
	void removeRulerContextMenuListener(IMenuListener listener);

}

