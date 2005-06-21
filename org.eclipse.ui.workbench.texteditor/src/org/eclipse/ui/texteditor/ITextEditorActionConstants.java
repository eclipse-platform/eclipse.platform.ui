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
import org.eclipse.ui.actions.ActionFactory;


/**
 * Defines the names of those actions which are pre-registered with the
 * <code>AbstractTextEditor</code>. <code>RULER_DOUBLE_CLICK</code> defines
 * the action which is registered as being executed when the editor's
 * ruler has been double clicked. This interface extends the set of names
 * available from <code>IWorkbenchActionConstants</code>. It also defines the
 * names of the menu groups in a text editor's context menu.
 */
public interface ITextEditorActionConstants extends IWorkbenchActionConstants {

	/**
	 * Context menu group for undo/redo related actions.
	 * Value: <code>"group.undo"</code>
	 */
	static final String GROUP_UNDO= "group.undo"; //$NON-NLS-1$

	/**
	 * Context menu group for copy/paste related actions.
	 * Value: <code>"group.copy"</code>
	 */
	static final String GROUP_COPY= "group.copy"; //$NON-NLS-1$

	/**
	 * Context menu group for text manipulation actions.
	 * Value: <code>"group.edit"</code>
	 */
	static final String GROUP_EDIT= "group.edit"; //$NON-NLS-1$

	/**
	 * Context menu group for print related actions.
	 * Value: <code>"group.print"</code>
	 */
	static final String GROUP_PRINT= "group.print"; //$NON-NLS-1$

	/**
	 * Context menu group for find/replace related actions.
	 * Value: <code>"group.find"</code>
	 */
	static final String GROUP_FIND= "group.find"; //$NON-NLS-1$

	/**
	 * Context menu group for save related actions.
	 * Value: <code>"group.save"</code>
	 */
	static final String GROUP_SAVE= "group.save"; //$NON-NLS-1$

	/**
	 * Context menu group for actions which do not fit in one of the other categories.
	 * Value: <code>"group.rest"</code>
	 */
	static final String GROUP_REST= "group.rest"; //$NON-NLS-1$

	/**
	 * Menu group for open actions.
	 * Value <code>"group.open"</code>
	 * @since 3.1
	 */
	static final String GROUP_OPEN= "group.open"; //$NON-NLS-1$

	/**
	 * Menu group for code generation and code assist actions.
	 * Value <code>"group.generate"</code>).
	 * @since 3.1
	 */
	static final String GROUP_GENERATE=	"group.generate"; //$NON-NLS-1$

	/**
	 * Name of the action for shifting text blocks to the right.
	 * Value: <code>"ShiftRight"</code>
	 */
	static final String SHIFT_RIGHT= "ShiftRight"; //$NON-NLS-1$

	/**
	 * Name of the action for shifting text blocks to the right, triggered by the TAB key.
	 * Value: <code>"ShiftRightTab"</code>
	 * @since 3.0
	 */
	static final String SHIFT_RIGHT_TAB= "ShiftRightTab"; //$NON-NLS-1$

	/**
	 * Name of the action for shifting text blocks to the left.
	 * Value: <code>"ShiftLeft"</code>
	 */
	static final String SHIFT_LEFT= "ShiftLeft"; //$NON-NLS-1$

	/**
	 * Name of the action to delete the current line.
	 * Value: <code>"DeleteLine"</code>
	 * @since 2.0
	 */
	static final String DELETE_LINE= "DeleteLine"; //$NON-NLS-1$

	/**
	 * Name of the action to cut the current line.
	 * Value: <code>"CutLine"</code>
	 * @since 2.1
	 */
	static final String CUT_LINE= "CutLine"; //$NON-NLS-1$

	/**
	 * Name of the action to delete line to beginning.
	 * Value: <code>"DeleteLineToBeginning"</code>
	 * @since 2.0
	 */
	static final String DELETE_LINE_TO_BEGINNING= "DeleteLineToBeginning"; //$NON-NLS-1$

	/**
	 * Name of the action to cut line to beginning.
	 * Value: <code>"CutLineToBeginning"</code>
	 * @since 2.1
	 */
	static final String CUT_LINE_TO_BEGINNING= "CutLineToBeginning"; //$NON-NLS-1$

	/**
	 * Name of the action to delete line to end.
	 * Value: <code>"DeleteLineToEnd"</code>
	 * @since 2.0
	 */
	static final String DELETE_LINE_TO_END= "DeleteLineToEnd"; //$NON-NLS-1$

	/**
	 * Name of the action to cut line to end.
	 * Value: <code>"CutLineToEnd"</code>
	 * @since 2.1
	 */
	static final String CUT_LINE_TO_END= "CutLineToEnd"; //$NON-NLS-1$

	/**
	 * Name of the action to set the mark.
	 * Value: <code>"SetMark"</code>
	 * @since 2.0
	 */
	static final String SET_MARK= "SetMark"; //$NON-NLS-1$

	/**
	 * Name of the action to set the mark.
	 * Value: <code>"ClearMark"</code>
	 * @since 2.0
	 */
	static final String CLEAR_MARK= "ClearMark"; //$NON-NLS-1$

	/**
	 * Name of the action to swap the mark with the cursor position.
	 * Value: <code>"SwapMark"</code>
	 * @since 2.0
	 */
	static final String SWAP_MARK= "SwapMark"; //$NON-NLS-1$

	/**
	 * Name of the action to jump to a certain text line.
	 * Value: <code>"GotoLine"</code>
	 */
	static final String GOTO_LINE= "GotoLine"; //$NON-NLS-1$

	/**
	 * Name of the action to insert a new line below the current position.
	 * Value: <code>"SmartEnter"</code>
	 * @since 3.0
	 */
	static final String SMART_ENTER= "SmartEnter"; //$NON-NLS-1$

	/**
	 * Name of the action to insert a new line above the current position.
	 * Value: <code>"SmartEnterInverse"</code>
	 * @since 3.0
	 */
	static final String SMART_ENTER_INVERSE= "SmartEnterInverse"; //$NON-NLS-1$

	/**
	 * Name of the action to move lines upwards
	 * Value: <code>"MoveLineUp"</code>
	 * @since 3.0
	 */
	static final String MOVE_LINE_UP= "MoveLineUp"; //$NON-NLS-1$

	/**
	 * Name of the action to move lines downwards
	 * Value: <code>"MoveLineDown"</code>
	 * @since 3.0
	 */
	static final String MOVE_LINE_DOWN= "MoveLineDown"; //$NON-NLS-1$

	/**
	 * Name of the action to copy lines upwards
	 * Value: <code>"CopyLineUp"</code>
	 * @since 3.0
	 */
	static final String COPY_LINE_UP= "CopyLineUp"; //$NON-NLS-1$;

	/**
	 * Name of the action to copy lines downwards
	 * Value: <code>"CopyLineDown"</code>
	 * @since 3.0
	 */
	static final String COPY_LINE_DOWN= "CopyLineDown"; //$NON-NLS-1$;

	/**
	 * Name of the action to turn a selection to upper case
	 * Value: <code>"UpperCase"</code>
	 * @since 3.0
	 */
	static final String UPPER_CASE= "UpperCase"; //$NON-NLS-1$

	/**
	 * Name of the action to turn a selection to lower case
	 * Value: <code>"LowerCase"</code>
	 * @since 3.0
	 */
	static final String LOWER_CASE= "LowerCase"; //$NON-NLS-1$

	/**
	 * Name of the action to find next.
	 * Value: <code>"FindNext"</code>
	 * @since 2.0
	 */
	static final String FIND_NEXT= "FindNext"; //$NON-NLS-1$

	/**
	 * Name of the action to find previous.
	 * Value: <code>"FindPrevious"</code>
	 * @since 2.0
	 */
	static final String FIND_PREVIOUS= "FindPrevious"; //$NON-NLS-1$

	/**
	 * Name of the action to incremental find.
	 * Value: <code>"FindIncremental"</code>
	 * @since 2.0
	 */
	static final String FIND_INCREMENTAL= "FindIncremental"; //$NON-NLS-1$
	/**
	 * Name of the action to incremental find reverse.
	 * Value: <code>"FindIncrementalReverse"</code>
	 * @since 2.1
	 */
	static final String FIND_INCREMENTAL_REVERSE= "FindIncrementalReverse"; //$NON-NLS-1$

	/**
	 * Name of the action to convert line delimiters to Windows.
	 * Value: <code>"ConvertLineDelimitersToWindows"</code>
	 * @since 2.0
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	static final String CONVERT_LINE_DELIMITERS_TO_WINDOWS= "ConvertLineDelimitersToWindows"; //$NON-NLS-1$

	/**
	 * Name of the action to convert line delimiters to UNIX.
	 * Value: <code>"ConvertLineDelimitersToUNIX"</code>
	 * @since 2.0
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	static final String CONVERT_LINE_DELIMITERS_TO_UNIX= "ConvertLineDelimitersToUNIX"; //$NON-NLS-1$

	/**
	 * Name of the action to convert line delimiters to MAC.
	 * Value: <code>"ConvertLineDelimitersToMAC"</code>
	 * @since 2.0
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	static final String CONVERT_LINE_DELIMITERS_TO_MAC= "ConvertLineDelimitersToMAC"; //$NON-NLS-1$

	/**
	 * Name of the change encoding action.
	 * Value: <code>"ChangeEncoding"</code>
	 * @since 3.1
	 */
	static final String CHANGE_ENCODING= "ChangeEncoding"; //$NON-NLS-1$

	/**
	 * Name of the ruler action performed when double clicking the editor's vertical ruler.
	 * Value: <code>"RulerDoubleClick"</code>
	 */
	static final String RULER_DOUBLE_CLICK= "RulerDoubleClick"; //$NON-NLS-1$

	/**
	 * Name of the ruler action performed when clicking the editor's vertical ruler.
	 * Value: <code>"RulerClick"</code>
	 * @since 2.0
	 */
	static final String RULER_CLICK= "RulerClick"; //$NON-NLS-1$

	/**
	 * Name of the ruler action to manage tasks.
	 * Value: <code>"ManageTasks"</code>
	 */
	static final String RULER_MANAGE_TASKS= "ManageTasks"; //$NON-NLS-1$

	/**
	 * Name of the ruler action to manage bookmarks.
	 * Value: <code>"ManageBookmarks"</code>
	 */
	static final String RULER_MANAGE_BOOKMARKS= "ManageBookmarks"; //$NON-NLS-1$


	/**
	 * Status line category "input position".
	 * Value: <code>"InputPosition"</code>
	 * @since 2.0
	 */
	static final String STATUS_CATEGORY_INPUT_POSITION= "InputPosition"; //$NON-NLS-1$

	/**
	 * Status line category "input mode".
	 * Value: <code>"InputMode"</code>
	 * @since 2.0
	 */
	static final String STATUS_CATEGORY_INPUT_MODE= "InputMode"; //$NON-NLS-1$

	/**
	 * Status line category "element state".
	 * Value: <code>"ElementState"</code>
	 * @since 2.0
	 */
	static final String STATUS_CATEGORY_ELEMENT_STATE= "ElementState"; //$NON-NLS-1$

	/**
	 * Status line category "findField".
	 * Value: <code>"findField"</code>
	 * @since 3.0
	 */
	static final String STATUS_CATEGORY_FIND_FIELD= "findField"; //$NON-NLS-1$

	/**
	 * Name of standard Copy global action in the Edit menu.
	 * Value <code>"copy"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#COPY
	 */
	static final String COPY= ActionFactory.COPY.getId();

	/**
	 * Name of standard Cut global action in the Edit menu.
	 * Value <code>"cut"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#CUT
	 */
	static final String CUT= ActionFactory.CUT.getId();

	/**
	 * Name of standard Delete global action in the Edit menu.
	 * Value <code>"delete"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#DELETE
	 */
	static final String DELETE= ActionFactory.DELETE.getId();

	/**
	 * Name of standard Find global action in the Edit menu.
	 * Value <code>"find"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#FIND
	 */
	static final String FIND= ActionFactory.FIND.getId();

	/**
	 * Name of standard Paste global action in the Edit menu.
	 * Value <code>"paste"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#PASTE
	 */
	static final String PASTE= ActionFactory.PASTE.getId();

	/**
	 * Name of standard Print global action in the File menu.
	 * Value <code>"print"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#PRINT
	 */
	static final String PRINT= ActionFactory.PRINT.getId();

	/**
	 * Name of standard Properties global action in the File menu.
	 * Value <code>"properties"</code>
	 * @since 3.1
	 * @see org.eclipse.ui.actions.ActionFactory#PROPERTIES
	 */
	static final String PROPERTIES= ActionFactory.PROPERTIES.getId();

	/**
	 * Name of standard Redo global action in the Edit menu.
	 * Value <code>"redo"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#REDO
	 */
	static final String REDO= ActionFactory.REDO.getId();

	/**
	 * Name of standard Undo global action in the Edit menu.
	 * Value <code>"undo"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#UNDO
	 */
	static final String UNDO= ActionFactory.UNDO.getId();

	/**
	 * Name of standard Save global action in the File menu.
	 * Value <code>"save"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#SAVE
	 */
	static final String SAVE= ActionFactory.SAVE.getId();

	/**
	 * Name of standard Select All global action in the Edit menu.
	 * Value <code>"selectAll"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#SELECT_ALL
	 */
	static final String SELECT_ALL= ActionFactory.SELECT_ALL.getId();

	/**
	 * Name of standard Revert global action in the File menu.
	 * Value <code>"revert"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#REVERT
	 */
	static final String REVERT= ActionFactory.REVERT.getId();

	/**
	 * Name of the action for re-establishing the state after the
	 * most recent save operation.
	 * Value: <code>"ITextEditorActionConstants.REVERT"</code>
	 */
	static final String REVERT_TO_SAVED= REVERT;

	/**
	 * Name of the action for toggling the smart insert mode.
	 * Value: <code>"ToggleInsertMode"</code>
	 * @since 3.0
	 */
	static final String TOGGLE_INSERT_MODE= "TOGGLE_INSERT_MODE"; //$NON-NLS-1$

	/**
	 * Context menu group for preference related actions.
	 * Value: <code>"settings"</code>
	 * @since 3.1
	 */
	static final String GROUP_SETTINGS= "settings"; //$NON-NLS-1$

	/**
	 * Context menu group for ruler column related actions.
	 * Value: <code>"rulers"</code>
	 * @since 3.1
	 */
	static final String GROUP_RULERS= "rulers"; //$NON-NLS-1$

	/**
	 * Context menu group for quick diff revert related actions.
	 * Value: <code>"restore"</code>
	 * @since 3.1
	 */
	static final String GROUP_RESTORE= "restore"; //$NON-NLS-1$

	/**
	 * Name of the action for showing the preferences from the editor context
	 * menu. Value: <code>"Preferences.ContextAction"</code>
	 * @since 3.1
	 */
	static final String CONTEXT_PREFERENCES= "Preferences.ContextAction"; //$NON-NLS-1$

	/**
	 * Name of the action for showing the preferences from the editor ruler
	 * context menu. Value: <code>"Preferences.RulerAction"</code>
	 * @since 3.1
	 */
	static final String RULER_PREFERENCES= "Preferences.RulerAction"; //$NON-NLS-1$

	/**
	 * Name of the action for toggling line number display.
	 * Value: <code>"Linenumbers.Toggle"</code>
	 * @since 3.1
	 */
	static final String LINENUMBERS_TOGGLE= "Linenumbers.Toggle"; //$NON-NLS-1$

	/**
	 * Name of the action for reverting deleted lines at the current selection.
	 * Value: <code>"QuickDiff.RevertDeletion"</code>
	 * @since 3.1
	 */
	static final String QUICKDIFF_REVERTDELETION= "QuickDiff.RevertDeletion"; //$NON-NLS-1$

	/**
	 * Name of the action for reverting the line at the current selection.
	 * Value: <code>"QuickDiff.RevertLine"</code>
	 * @since 3.1
	 */
	static final String QUICKDIFF_REVERTLINE= "QuickDiff.RevertLine"; //$NON-NLS-1$

	/**
	 * Name of the action for reverting the selection or the block at the
	 * current selection. Value: <code>"QuickDiff.Revert"</code>
	 * @since 3.1
	 */
	static final String QUICKDIFF_REVERT= "QuickDiff.Revert"; //$NON-NLS-1$

	/**
	 * Name of the action for reverting the block at the current selection.
	 * Value: <code>"QuickDiff.RevertBlock"</code>
	 * @since 3.1
	 */
	static final String QUICKDIFF_REVERTBLOCK= "QuickDiff.RevertBlock"; //$NON-NLS-1$

	/**
	 * Name of the action for reverting the current selection.
	 * Value: <code>"QuickDiff.RevertBlock"</code>
	 * @since 3.1
	 */
	static final String QUICKDIFF_REVERTSELECTION= "QuickDiff.RevertSelection"; //$NON-NLS-1$

	/**
	 * Name of the action for toggling quick diff display.
	 * Value: <code>"QuickDiff.Toggle"</code>
	 * @since 3.1
	 */
	static final String QUICKDIFF_TOGGLE= "QuickDiff.Toggle"; //$NON-NLS-1$

	/**
	 * Name of the action for emacs style word completion.
	 * Value: <code>"HIPPIE_COMPLETION"</code>
	 * @since 3.1
	 */
	static final String HIPPIE_COMPLETION= "HIPPIE_COMPLETION"; //$NON-NLS-1$
}
