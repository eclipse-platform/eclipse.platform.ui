package org.eclipse.ui.texteditor;

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
}

