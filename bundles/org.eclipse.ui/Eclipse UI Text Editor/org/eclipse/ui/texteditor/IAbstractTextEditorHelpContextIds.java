package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.PlatformUI;

/**
 * Help context ids for the text editor.
 * <p>
 * This interface contains constants only; 
 * it is not intended to be implemented.
 * </p>
 */
public interface IAbstractTextEditorHelpContextIds {
	
	/** The string with which action, dialog, etc. ids are prefixed to construct help context ids. */
	public static final String PREFIX= PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$
	
	/** The string which is appended to action ids to construct help context ids. */
	public static final String ACTION_POSTFIX= "_action_context"; //$NON-NLS-1$

	/* Actions */
	public static final String UNDO_ACTION= PREFIX + ITextEditorActionConstants.UNDO + ACTION_POSTFIX;
	public static final String REDO_ACTION= PREFIX + ITextEditorActionConstants.REDO + ACTION_POSTFIX;
	public static final String CUT_ACTION= PREFIX + ITextEditorActionConstants.CUT + ACTION_POSTFIX;
	public static final String COPY_ACTION= PREFIX + ITextEditorActionConstants.COPY + ACTION_POSTFIX;
	public static final String PASTE_ACTION= PREFIX + ITextEditorActionConstants.PASTE + ACTION_POSTFIX;
	public static final String DELETE_ACTION= PREFIX + ITextEditorActionConstants.DELETE + ACTION_POSTFIX;
	public static final String DELETE_LINE_ACTION= PREFIX + ITextEditorActionConstants.DELETE_LINE + ACTION_POSTFIX;
	public static final String DELETE_LINE_TO_BEGINNING_ACTION= PREFIX + ITextEditorActionConstants.DELETE_LINE_TO_BEGINNING + ACTION_POSTFIX;
	public static final String DELETE_LINE_TO_END_ACTION= PREFIX + ITextEditorActionConstants.DELETE_LINE_TO_END + ACTION_POSTFIX;
	public static final String SET_MARK_ACTION= PREFIX + ITextEditorActionConstants.SET_MARK + ACTION_POSTFIX;
	public static final String CLEAR_MARK_ACTION= PREFIX + ITextEditorActionConstants.CLEAR_MARK + ACTION_POSTFIX;
	public static final String SWAP_MARK_ACTION= PREFIX + ITextEditorActionConstants.SWAP_MARK + ACTION_POSTFIX;
	public static final String SELECT_ALL_ACTION= PREFIX + ITextEditorActionConstants.SELECT_ALL + ACTION_POSTFIX;
	public static final String SHIFT_RIGHT_ACTION= PREFIX + ITextEditorActionConstants.SHIFT_RIGHT + ACTION_POSTFIX;
	public static final String SHIFT_LEFT_ACTION= PREFIX + ITextEditorActionConstants.SHIFT_LEFT + ACTION_POSTFIX;
	public static final String FIND_ACTION= PREFIX + ITextEditorActionConstants.FIND + ACTION_POSTFIX;
	public static final String BOOKMARK_ACTION= PREFIX + ITextEditorActionConstants.BOOKMARK + ACTION_POSTFIX;
	public static final String ADD_TASK_ACTION= PREFIX + ITextEditorActionConstants.ADD_TASK + ACTION_POSTFIX;
	public static final String SAVE_ACTION= PREFIX + ITextEditorActionConstants.SAVE + ACTION_POSTFIX;
	public static final String REVERT_TO_SAVED_ACTION= PREFIX + ITextEditorActionConstants.REVERT_TO_SAVED + ACTION_POSTFIX;
	public static final String GOTO_LINE_ACTION= PREFIX + ITextEditorActionConstants.GOTO_LINE + ACTION_POSTFIX;
	public static final String PRINT_ACTION= PREFIX + ITextEditorActionConstants.PRINT + ACTION_POSTFIX;
	public static final String CONVERT_LINE_DELIMITERS_TO_WINDOWS= PREFIX + ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_WINDOWS + ACTION_POSTFIX;
	public static final String CONVERT_LINE_DELIMITERS_TO_UNIX= PREFIX + ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX + ACTION_POSTFIX;
	public static final String CONVERT_LINE_DELIMITERS_TO_MAC= PREFIX + ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC + ACTION_POSTFIX;
	
	/* Dialogs */
	public static final String FIND_REPLACE_DIALOG= PREFIX + "find_replace_dialog_context"; //$NON-NLS-1$
}