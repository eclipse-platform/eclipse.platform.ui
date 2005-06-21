/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Genady Beryozkin, me@genady.org - https://bugs.eclipse.org/bugs/show_bug.cgi?id=11668
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;


/**
 * Help context ids for the text editor.
 * <p>
 * This interface contains constants only; it is not intended to be implemented.
 * </p>
 */
public interface IAbstractTextEditorHelpContextIds {

	/**
	 * The string with which all other defined ids are prefixed to construct help context ids.
	 * Value: <code>"org.eclipse.ui."</code>
	 */
	public static final String PREFIX= PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

	/**
	 * The string which is appended to action ids to construct help context ids.
	 * Value: <code>"_action_context"</code>
	 */
	public static final String ACTION_POSTFIX= "_action_context"; //$NON-NLS-1$

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.undo_action_context"</code>
	 */
	public static final String UNDO_ACTION= PREFIX + ITextEditorActionConstants.UNDO + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.redo_action_context"</code>
	 */
	public static final String REDO_ACTION= PREFIX + ITextEditorActionConstants.REDO + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.cut_action_context"</code>
	 */
	public static final String CUT_ACTION= PREFIX + ITextEditorActionConstants.CUT + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.copy_action_context"</code>
	 */
	public static final String COPY_ACTION= PREFIX + ITextEditorActionConstants.COPY + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.paste_action_context"</code>
	 */
	public static final String PASTE_ACTION= PREFIX + ITextEditorActionConstants.PASTE + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.delete_action_context"</code>
	 */
	public static final String DELETE_ACTION= PREFIX + ITextEditorActionConstants.DELETE + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.DeleteLine_action_context"</code>
	 * @since 2.0
	 */
	public static final String DELETE_LINE_ACTION= PREFIX + ITextEditorActionConstants.DELETE_LINE + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.CutLine_action_context"</code>
	 * @since 2.1
	 */
	public static final String CUT_LINE_ACTION= PREFIX + ITextEditorActionConstants.CUT_LINE + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.DeleteLineToBeginning_action_context"</code>
	 * @since 2.0
	 */
	public static final String DELETE_LINE_TO_BEGINNING_ACTION= PREFIX + ITextEditorActionConstants.DELETE_LINE_TO_BEGINNING + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.CutLineToBeginning_action_context"</code>
	 * @since 2.1
	 */
	public static final String CUT_LINE_TO_BEGINNING_ACTION= PREFIX + ITextEditorActionConstants.CUT_LINE_TO_BEGINNING + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.DeleteLineToEnd_action_context"</code>
	 * @since 2.0
	 */
	public static final String DELETE_LINE_TO_END_ACTION= PREFIX + ITextEditorActionConstants.DELETE_LINE_TO_END + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.CutLineToEnd_action_context"</code>
	 * @since 2.1
	 */
	public static final String CUT_LINE_TO_END_ACTION= PREFIX + ITextEditorActionConstants.CUT_LINE_TO_END + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.SetMark_action_context"</code>
	 * @since 2.0
	 */
	public static final String SET_MARK_ACTION= PREFIX + ITextEditorActionConstants.SET_MARK + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.ClearMark_action_context"</code>
	 * @since 2.0
	 */
	public static final String CLEAR_MARK_ACTION= PREFIX + ITextEditorActionConstants.CLEAR_MARK + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.SwapMark_action_context"</code>
	 * @since 2.0
	 */
	public static final String SWAP_MARK_ACTION= PREFIX + ITextEditorActionConstants.SWAP_MARK + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.selectAll_action_context"</code>
	 */
	public static final String SELECT_ALL_ACTION= PREFIX + ITextEditorActionConstants.SELECT_ALL + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.ShiftRight_action_context"</code>
	 */
	public static final String SHIFT_RIGHT_ACTION= PREFIX + ITextEditorActionConstants.SHIFT_RIGHT + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.ShiftLeft_action_context"</code>
	 */
	public static final String SHIFT_LEFT_ACTION= PREFIX + ITextEditorActionConstants.SHIFT_LEFT + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.find_action_context"</code>
	 */
	public static final String FIND_ACTION= PREFIX + ITextEditorActionConstants.FIND + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.FindNext_action_context"</code>
	 * @since 2.0
	 */
	public static final String FIND_NEXT_ACTION= PREFIX + ITextEditorActionConstants.FIND_NEXT + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.FindPrevious_action_context"</code>
	 * @since 2.0
	 */
	public static final String FIND_PREVIOUS_ACTION= PREFIX + ITextEditorActionConstants.FIND_PREVIOUS + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.FindIncremental_action_context"</code>
	 * @since 2.0
	 */
	public static final String FIND_INCREMENTAL_ACTION= PREFIX + ITextEditorActionConstants.FIND_INCREMENTAL + ACTION_POSTFIX;
	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.FindIncrementalReverse_action_context"</code>
	 * @since 2.1
	 */
	public static final String FIND_INCREMENTAL_REVERSE_ACTION= PREFIX + ITextEditorActionConstants.FIND_INCREMENTAL_REVERSE + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.bookmark_action_context"</code>
	 * @deprecated in 3.0 Use <code>org.eclipse.ui.texteditor.ITextEditorHelpContextIds.BOOKMARK_ACTION</code> instead.
	 */
	public static final String BOOKMARK_ACTION= PREFIX + IWorkbenchActionConstants.BOOKMARK + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.addTask_action_context"</code>
	 * @deprecated in 3.0 Use <code>org.eclipse.ui.texteditor.ITextEditorHelpContextIds.ADD_TASK_ACTION</code> instead.
	 */
	public static final String ADD_TASK_ACTION= PREFIX + IWorkbenchActionConstants.ADD_TASK + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.save_action_context"</code>
	 */
	public static final String SAVE_ACTION= PREFIX + ITextEditorActionConstants.SAVE + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.revert_action_context"</code>
	 */
	public static final String REVERT_TO_SAVED_ACTION= PREFIX + ITextEditorActionConstants.REVERT_TO_SAVED + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.GotoLine_action_context"</code>
	 */
	public static final String GOTO_LINE_ACTION= PREFIX + ITextEditorActionConstants.GOTO_LINE + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.print_action_context"</code>
	 */
	public static final String PRINT_ACTION= PREFIX + ITextEditorActionConstants.PRINT + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.ConvertLineDelimitersToWindows_action_context"</code>
	 * @since 2.0
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	public static final String CONVERT_LINE_DELIMITERS_TO_WINDOWS= PREFIX + ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_WINDOWS + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.ConvertLineDelimitersToUNIX_action_context"</code>
	 * @since 2.0
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	public static final String CONVERT_LINE_DELIMITERS_TO_UNIX= PREFIX + ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.ConvertLineDelimitersToMAC_action_context"</code>
	 * @since 2.0
	 * @deprecated since 3.1 No longer supported as editor actions.
	 */
	public static final String CONVERT_LINE_DELIMITERS_TO_MAC= PREFIX + ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC + ACTION_POSTFIX;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.find_replace_dialog_context"</code>
	 */
	public static final String FIND_REPLACE_DIALOG= PREFIX + "find_replace_dialog_context"; //$NON-NLS-1$

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.goto_last_edit_position_action_context"</code>
	 * @since 2.1
	 */
	public static final String GOTO_LAST_EDIT_POSITION_ACTION= PREFIX + "goto_last_edit_position" + ACTION_POSTFIX; //$NON-NLS-1$

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.move_lines_action_context"</code>
	 * @since 3.0
	 */
	public static final String MOVE_LINES_ACTION= PREFIX + "move_lines" + ACTION_POSTFIX; //$NON-NLS-1$

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.copy_lines_action_context"</code>
	 * @since 3.0
	 */
	public static final String COPY_LINES_ACTION= PREFIX + "copy_lines" + ACTION_POSTFIX; //$NON-NLS-1$;

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.upper_case_action_context"</code>
	 * @since 3.0
	 */
	public static final String UPPER_CASE_ACTION= PREFIX + "upper_case" + ACTION_POSTFIX; //$NON-NLS-1$

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.lower_case_action_context"</code>
	 * @since 3.0
	 */
	public static final String LOWER_CASE_ACTION= PREFIX + "lower_case" + ACTION_POSTFIX; //$NON-NLS-1$

	/**
	 * Help context id for the action.
	 * Value: <code>"org.eclipse.ui.smart_enter_action_context"</code>
	 * @since 3.0
	 */
	public static final String SMART_ENTER_ACTION= PREFIX + "smart_enter" + ACTION_POSTFIX; //$NON-NLS-1$;

	/**
	 * Help context id for the smart insert mode toggle action.
	 * Value: <code>"org.eclipse.ui.toggle_insert_mode_action_context"</code>
	 * @since 3.0
	 */
	public static final String TOGGLE_INSERT_MODE_ACTION= PREFIX + "toggle_insert_mode" + ACTION_POSTFIX; //$NON-NLS-1$;;

    /**
     * Help context id for the word completion action.
     * Value: <code>"org.eclipse.ui.hippie_completion_action_context"</code>
     * @since 3.1
     */
    public static final String HIPPIE_COMPLETION_ACTION=PREFIX + "hippie_completion" + ACTION_POSTFIX; //$NON-NLS-1$
}
