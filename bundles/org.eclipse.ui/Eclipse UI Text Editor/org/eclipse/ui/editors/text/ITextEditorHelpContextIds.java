package org.eclipse.ui.editors.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;

/**
 * Help context ids for the text editor.
 * <p>
 * This interface contains constants only; 
 * it is not intended to be implemented.
 * </p>
 */
public interface ITextEditorHelpContextIds extends IAbstractTextEditorHelpContextIds {
	
	/* Preference pages */
	public static final String TEXT_EDITOR_PREFERENCE_PAGE= PREFIX + "text_editor_preference_page_context";

	/* Editors */
	public static final String TEXT_EDITOR= PREFIX + "text_editor_context";
}
