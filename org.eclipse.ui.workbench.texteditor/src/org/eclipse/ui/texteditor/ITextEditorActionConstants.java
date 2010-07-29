/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Genady Beryozkin, me@genady.org - https://bugs.eclipse.org/bugs/show_bug.cgi?id=11668
 *     Benjamin Muskalla <b.muskalla@gmx.net> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=41573
 *     Tom Eicher (Avaloq Evolution AG) - block selection mode
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.jface.text.information.IInformationProvider;

import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;


/**
 * Defines the names of those actions which are pre-registered with the
 * <code>AbstractTextEditor</code>. <code>RULER_DOUBLE_CLICK</code> defines
 * the action which is registered as being executed when the editor's ruler has
 * been double clicked. This interface extends the set of names available from
 * <code>IWorkbenchActionConstants</code>. It also defines the names of the
 * menu groups in a text editor's context menu.
 * <p>
 * This interface must not be implemented by clients.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITextEditorActionConstants extends IWorkbenchActionConstants {

	/**
	 * Context menu group for undo/redo related actions.
	 * Value: <code>"group.undo"</code>
	 */
	String GROUP_UNDO= "group.undo"; //$NON-NLS-1$

	/**
	 * Context menu group for copy/paste related actions.
	 * Value: <code>"group.copy"</code>
	 */
	String GROUP_COPY= "group.copy"; //$NON-NLS-1$

	/**
	 * Context menu group for text manipulation actions.
	 * Value: <code>"group.edit"</code>
	 */
	String GROUP_EDIT= "group.edit"; //$NON-NLS-1$

	/**
	 * Context menu group for print related actions.
	 * Value: <code>"group.print"</code>
	 */
	String GROUP_PRINT= "group.print"; //$NON-NLS-1$

	/**
	 * Context menu group for find/replace related actions.
	 * Value: <code>"group.find"</code>
	 */
	String GROUP_FIND= "group.find"; //$NON-NLS-1$

	/**
	 * Context menu group for save related actions.
	 * Value: <code>"group.save"</code>
	 */
	String GROUP_SAVE= "group.save"; //$NON-NLS-1$

	/**
	 * Context menu group for actions which do not fit in one of the other categories.
	 * Value: <code>"group.rest"</code>
	 */
	String GROUP_REST= "group.rest"; //$NON-NLS-1$

	/**
	 * Menu group for open actions.
	 * Value <code>"group.open"</code>
	 * @since 3.1
	 */
	String GROUP_OPEN= "group.open"; //$NON-NLS-1$

	/**
	 * Menu group for code generation and content assist actions.
	 * Value <code>"group.generate"</code>).
	 * @since 3.1
	 */
	String GROUP_GENERATE=	"group.generate"; //$NON-NLS-1$

	/**
	 * Name of the action for shifting text blocks to the right.
	 * Value: <code>"ShiftRight"</code>
	 */
	String SHIFT_RIGHT= "ShiftRight"; //$NON-NLS-1$

	/**
	 * Name of the action for shifting text blocks to the right, triggered by the TAB key.
	 * Value: <code>"ShiftRightTab"</code>
	 * @since 3.0
	 */
	String SHIFT_RIGHT_TAB= "ShiftRightTab"; //$NON-NLS-1$

	/**
	 * Name of the action for shifting text blocks to the left.
	 * Value: <code>"ShiftLeft"</code>
	 */
	String SHIFT_LEFT= "ShiftLeft"; //$NON-NLS-1$

	/**
	 * Name of the action to delete the current line.
	 * Value: <code>"DeleteLine"</code>
	 * @since 2.0
	 */
	String DELETE_LINE= "DeleteLine"; //$NON-NLS-1$

	/**
	 * Name of the action to join the current lines.
	 * Value: <code>"JoinLine"</code>
	 * @since 3.3
	 */
	String JOIN_LINES= "JoinLines"; //$NON-NLS-1$

	/**
	 * Name of the action to cut the current line.
	 * Value: <code>"CutLine"</code>
	 * @since 2.1
	 */
	String CUT_LINE= "CutLine"; //$NON-NLS-1$

	/**
	 * Name of the action to delete line to beginning.
	 * Value: <code>"DeleteLineToBeginning"</code>
	 * @since 2.0
	 */
	String DELETE_LINE_TO_BEGINNING= "DeleteLineToBeginning"; //$NON-NLS-1$

	/**
	 * Name of the action to cut line to beginning.
	 * Value: <code>"CutLineToBeginning"</code>
	 * @since 2.1
	 */
	String CUT_LINE_TO_BEGINNING= "CutLineToBeginning"; //$NON-NLS-1$

	/**
	 * Name of the action to delete line to end.
	 * Value: <code>"DeleteLineToEnd"</code>
	 * @since 2.0
	 */
	String DELETE_LINE_TO_END= "DeleteLineToEnd"; //$NON-NLS-1$

	/**
	 * Name of the action to cut line to end.
	 * Value: <code>"CutLineToEnd"</code>
	 * @since 2.1
	 */
	String CUT_LINE_TO_END= "CutLineToEnd"; //$NON-NLS-1$

	/**
	 * Name of the action to set the mark.
	 * Value: <code>"SetMark"</code>
	 * @since 2.0
	 */
	String SET_MARK= "SetMark"; //$NON-NLS-1$

	/**
	 * Name of the action to set the mark.
	 * Value: <code>"ClearMark"</code>
	 * @since 2.0
	 */
	String CLEAR_MARK= "ClearMark"; //$NON-NLS-1$

	/**
	 * Name of the action to swap the mark with the cursor position.
	 * Value: <code>"SwapMark"</code>
	 * @since 2.0
	 */
	String SWAP_MARK= "SwapMark"; //$NON-NLS-1$

	/**
	 * Name of the action to jump to a certain text line.
	 * Value: <code>"GotoLine"</code>
	 */
	String GOTO_LINE= "GotoLine"; //$NON-NLS-1$

	/**
	 * Name of the action to insert a new line below the current position.
	 * Value: <code>"SmartEnter"</code>
	 * @since 3.0
	 */
	String SMART_ENTER= "SmartEnter"; //$NON-NLS-1$

	/**
	 * Name of the action to insert a new line above the current position.
	 * Value: <code>"SmartEnterInverse"</code>
	 * @since 3.0
	 */
	String SMART_ENTER_INVERSE= "SmartEnterInverse"; //$NON-NLS-1$

	/**
	 * Name of the action to move lines upwards
	 * Value: <code>"MoveLineUp"</code>
	 * @since 3.0
	 */
	String MOVE_LINE_UP= "MoveLineUp"; //$NON-NLS-1$

	/**
	 * Name of the action to move lines downwards
	 * Value: <code>"MoveLineDown"</code>
	 * @since 3.0
	 */
	String MOVE_LINE_DOWN= "MoveLineDown"; //$NON-NLS-1$

	/**
	 * Name of the action to copy lines upwards
	 * Value: <code>"CopyLineUp"</code>
	 * @since 3.0
	 */
	String COPY_LINE_UP= "CopyLineUp"; //$NON-NLS-1$;

	/**
	 * Name of the action to copy lines downwards
	 * Value: <code>"CopyLineDown"</code>
	 * @since 3.0
	 */
	String COPY_LINE_DOWN= "CopyLineDown"; //$NON-NLS-1$;

	/**
	 * Name of the action to turn a selection to upper case
	 * Value: <code>"UpperCase"</code>
	 * @since 3.0
	 */
	String UPPER_CASE= "UpperCase"; //$NON-NLS-1$

	/**
	 * Name of the action to turn a selection to lower case
	 * Value: <code>"LowerCase"</code>
	 * @since 3.0
	 */
	String LOWER_CASE= "LowerCase"; //$NON-NLS-1$

	/**
	 * Name of the action to find next.
	 * Value: <code>"FindNext"</code>
	 * @since 2.0
	 */
	String FIND_NEXT= "FindNext"; //$NON-NLS-1$

	/**
	 * Name of the action to find previous.
	 * Value: <code>"FindPrevious"</code>
	 * @since 2.0
	 */
	String FIND_PREVIOUS= "FindPrevious"; //$NON-NLS-1$

	/**
	 * Name of the action to incremental find.
	 * Value: <code>"FindIncremental"</code>
	 * @since 2.0
	 */
	String FIND_INCREMENTAL= "FindIncremental"; //$NON-NLS-1$
	/**
	 * Name of the action to incremental find reverse.
	 * Value: <code>"FindIncrementalReverse"</code>
	 * @since 2.1
	 */
	String FIND_INCREMENTAL_REVERSE= "FindIncrementalReverse"; //$NON-NLS-1$

	/**
	 * Name of the action to convert line delimiters to Windows.
	 * Value: <code>"ConvertLineDelimitersToWindows"</code>
	 * @since 2.0
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	String CONVERT_LINE_DELIMITERS_TO_WINDOWS= "ConvertLineDelimitersToWindows"; //$NON-NLS-1$

	/**
	 * Name of the action to convert line delimiters to UNIX.
	 * Value: <code>"ConvertLineDelimitersToUNIX"</code>
	 * @since 2.0
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	String CONVERT_LINE_DELIMITERS_TO_UNIX= "ConvertLineDelimitersToUNIX"; //$NON-NLS-1$

	/**
	 * Name of the action to convert line delimiters to MAC.
	 * Value: <code>"ConvertLineDelimitersToMAC"</code>
	 * @since 2.0
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	String CONVERT_LINE_DELIMITERS_TO_MAC= "ConvertLineDelimitersToMAC"; //$NON-NLS-1$

	/**
	 * Name of the change encoding action.
	 * Value: <code>"ChangeEncoding"</code>
	 * @since 3.1
	 */
	String CHANGE_ENCODING= "ChangeEncoding"; //$NON-NLS-1$

	/**
	 * Name of the ruler action performed when double clicking the editor's vertical ruler.
	 * Value: <code>"RulerDoubleClick"</code>
	 */
	String RULER_DOUBLE_CLICK= "RulerDoubleClick"; //$NON-NLS-1$

	/**
	 * Name of the ruler action performed when clicking the editor's vertical ruler.
	 * Value: <code>"RulerClick"</code>
	 * @since 2.0
	 */
	String RULER_CLICK= "RulerClick"; //$NON-NLS-1$

	/**
	 * Name of the ruler action to manage tasks.
	 * Value: <code>"ManageTasks"</code>
	 */
	String RULER_MANAGE_TASKS= "ManageTasks"; //$NON-NLS-1$

	/**
	 * Name of the ruler action to manage bookmarks.
	 * Value: <code>"ManageBookmarks"</code>
	 */
	String RULER_MANAGE_BOOKMARKS= "ManageBookmarks"; //$NON-NLS-1$


	/**
	 * Status line category "input position".
	 * Value: <code>"InputPosition"</code>
	 * @since 2.0
	 */
	String STATUS_CATEGORY_INPUT_POSITION= "InputPosition"; //$NON-NLS-1$

	/**
	 * Status line category "input mode".
	 * Value: <code>"InputMode"</code>
	 * @since 2.0
	 */
	String STATUS_CATEGORY_INPUT_MODE= "InputMode"; //$NON-NLS-1$

	/**
	 * Status line category "element state".
	 * Value: <code>"ElementState"</code>
	 * @since 2.0
	 */
	String STATUS_CATEGORY_ELEMENT_STATE= "ElementState"; //$NON-NLS-1$

	/**
	 * Status line category "findField".
	 * Value: <code>"findField"</code>
	 * @since 3.0
	 */
	String STATUS_CATEGORY_FIND_FIELD= "findField"; //$NON-NLS-1$

	/**
	 * Name of standard Copy global action in the Edit menu.
	 * Value <code>"copy"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#COPY
	 */
	String COPY= ActionFactory.COPY.getId();

	/**
	 * Name of standard Cut global action in the Edit menu.
	 * Value <code>"cut"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#CUT
	 */
	String CUT= ActionFactory.CUT.getId();

	/**
	 * Name of standard Delete global action in the Edit menu.
	 * Value <code>"delete"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#DELETE
	 */
	String DELETE= ActionFactory.DELETE.getId();

	/**
	 * Name of standard Find global action in the Edit menu.
	 * Value <code>"find"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#FIND
	 */
	String FIND= ActionFactory.FIND.getId();

	/**
	 * Name of standard Paste global action in the Edit menu.
	 * Value <code>"paste"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#PASTE
	 */
	String PASTE= ActionFactory.PASTE.getId();

	/**
	 * Name of standard Print global action in the File menu.
	 * Value <code>"print"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#PRINT
	 */
	String PRINT= ActionFactory.PRINT.getId();

	/**
	 * Name of standard Properties global action in the File menu.
	 * Value <code>"properties"</code>
	 * @since 3.1
	 * @see org.eclipse.ui.actions.ActionFactory#PROPERTIES
	 */
	String PROPERTIES= ActionFactory.PROPERTIES.getId();

	/**
	 * Name of standard Redo global action in the Edit menu.
	 * Value <code>"redo"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#REDO
	 */
	String REDO= ActionFactory.REDO.getId();

	/**
	 * Name of standard Undo global action in the Edit menu.
	 * Value <code>"undo"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#UNDO
	 */
	String UNDO= ActionFactory.UNDO.getId();

	/**
	 * Name of standard Save global action in the File menu.
	 * Value <code>"save"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#SAVE
	 */
	String SAVE= ActionFactory.SAVE.getId();

	/**
	 * Name of standard Select All global action in the Edit menu.
	 * Value <code>"selectAll"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#SELECT_ALL
	 */
	String SELECT_ALL= ActionFactory.SELECT_ALL.getId();

	/**
	 * Name of standard Revert global action in the File menu.
	 * Value <code>"revert"</code>
	 * @since 3.0
	 * @see org.eclipse.ui.actions.ActionFactory#REVERT
	 */
	String REVERT= ActionFactory.REVERT.getId();

	/**
	 * Name of standard Next global action in the Navigate menu.
	 * Value <code>"next"</code>
	 * @since 3.2
	 * @see org.eclipse.ui.actions.ActionFactory#NEXT
	 */
	String NEXT= ActionFactory.NEXT.getId();

	/**
	 * Name of standard Previous global action in the Navigate menu.
	 * Value <code>"previous"</code>
	 * @since 3.2
	 * @see org.eclipse.ui.actions.ActionFactory#PREVIOUS
	 */
	String PREVIOUS= ActionFactory.PREVIOUS.getId();

	/**
	 * Name of standard Refresh global action in the File menu.
	 * Value <code>"refresh"</code>
	 * @since 3.4
	 * @see org.eclipse.ui.actions.ActionFactory#REFRESH
	 */
	String REFRESH= ActionFactory.REFRESH.getId();

	/**
	 * Name of the action for re-establishing the state after the
	 * most recent save operation.
	 * Value: <code>"ITextEditorActionConstants.REVERT"</code>
	 */
	String REVERT_TO_SAVED= REVERT;

	/**
	 * Name of the action for toggling the smart insert mode.
	 * Value: <code>"ToggleInsertMode"</code>
	 * @since 3.0
	 */
	String TOGGLE_INSERT_MODE= "TOGGLE_INSERT_MODE"; //$NON-NLS-1$

	/**
	 * Context menu group for preference related actions.
	 * Value: <code>"settings"</code>
	 * @since 3.1
	 */
	String GROUP_SETTINGS= "settings"; //$NON-NLS-1$

	/**
	 * Context menu group for ruler column related actions.
	 * Value: <code>"rulers"</code>
	 * @since 3.1
	 */
	String GROUP_RULERS= "rulers"; //$NON-NLS-1$

	/**
	 * Context menu group for quick diff revert related actions.
	 * Value: <code>"restore"</code>
	 * @since 3.1
	 */
	String GROUP_RESTORE= "restore"; //$NON-NLS-1$

	/**
	 * Context menu group for actions that display additional information. Value:
	 * <code>"group.information"</code>.
	 * @since 3.2
	 */
	String GROUP_INFORMATION= "group.information"; //$NON-NLS-1$

	/**
	 * Context menu group for typing aid actions such as content assist. Value:
	 * <code>"group.assist"</code>.
	 * @since 3.2
	 */
	String GROUP_ASSIST= "group.assist"; //$NON-NLS-1$

	/**
	 * Name of the action for showing the preferences from the editor context
	 * menu. Value: <code>"Preferences.ContextAction"</code>
	 * @since 3.1
	 */
	String CONTEXT_PREFERENCES= "Preferences.ContextAction"; //$NON-NLS-1$

	/**
	 * Name of the action for showing the preferences from the editor ruler
	 * context menu. Value: <code>"Preferences.RulerAction"</code>
	 * @since 3.1
	 */
	String RULER_PREFERENCES= "Preferences.RulerAction"; //$NON-NLS-1$

	/**
	 * Name of the action for toggling line number display.
	 * Value: <code>"Linenumbers.Toggle"</code>
	 * @since 3.1
	 */
	String LINENUMBERS_TOGGLE= "Linenumbers.Toggle"; //$NON-NLS-1$

	/**
	 * Name of the action for reverting deleted lines at the current selection.
	 * Value: <code>"QuickDiff.RevertDeletion"</code>
	 * @since 3.1
	 */
	String QUICKDIFF_REVERTDELETION= "QuickDiff.RevertDeletion"; //$NON-NLS-1$

	/**
	 * Name of the action for reverting the line at the current selection.
	 * Value: <code>"QuickDiff.RevertLine"</code>
	 * @since 3.1
	 */
	String QUICKDIFF_REVERTLINE= "QuickDiff.RevertLine"; //$NON-NLS-1$

	/**
	 * Name of the action for reverting the selection or the block at the
	 * current selection. Value: <code>"QuickDiff.Revert"</code>
	 * @since 3.1
	 */
	String QUICKDIFF_REVERT= "QuickDiff.Revert"; //$NON-NLS-1$

	/**
	 * Name of the action for reverting the block at the current selection.
	 * Value: <code>"QuickDiff.RevertBlock"</code>
	 * @since 3.1
	 */
	String QUICKDIFF_REVERTBLOCK= "QuickDiff.RevertBlock"; //$NON-NLS-1$

	/**
	 * Name of the action for reverting the current selection.
	 * Value: <code>"QuickDiff.RevertBlock"</code>
	 * @since 3.1
	 */
	String QUICKDIFF_REVERTSELECTION= "QuickDiff.RevertSelection"; //$NON-NLS-1$

	/**
	 * Name of the action for toggling quick diff display.
	 * Value: <code>"QuickDiff.Toggle"</code>
	 * @since 3.1
	 */
	String QUICKDIFF_TOGGLE= "QuickDiff.Toggle"; //$NON-NLS-1$

	/**
	 * Name of the action for emacs style word completion.
	 * Value: <code>"HIPPIE_COMPLETION"</code>
	 * @since 3.1
	 */
	String HIPPIE_COMPLETION= "HIPPIE_COMPLETION"; //$NON-NLS-1$

	/**
	 * Name of the action for hiding the revision info
	 * Value: <code>"Revision.HideInfo"</code>
	 * @since 3.2
	 */
	String REVISION_HIDE_INFO= "Revision.HideInfo"; //$NON-NLS-1$

	/**
	 * Name of the content assist action.
	 * Value: <code>"ContentAssistProposal"</code>
	 * 
	 * @since 3.5
	 */
	String CONTENT_ASSIST= "ContentAssistProposal"; //$NON-NLS-1$

	/**
	 * Name of the content assist context information action.
	 * Value: <code>"ContentAssistContextInformation"</code>
	 * 
	 * @since 3.5
	 */
	String CONTENT_ASSIST_CONTEXT_INFORMATION= "ContentAssistContextInformation"; //$NON-NLS-1$

	/**
	 * Name of the quick assist action
	 * Value: <code>"QuickAssist"</code>
	 * @since 3.2
	 */
	String QUICK_ASSIST= "QuickAssist"; //$NON-NLS-1$

	/**
	 * Name of the action for cycling through the revision rendering modes.
	 * Value: <code>"Revision.Rendering.Cycle"</code>
	 * @since 3.3
	 */
	String REVISION_RENDERING_CYCLE= "Revision.Rendering.Cycle"; //$NON-NLS-1$

	/**
	 * Name of the action for toggling the display of the revision author.
	 * Value: <code>"Revision.ShowAuthor.Toggle"</code>
	 * @since 3.3
	 */
	String REVISION_SHOW_AUTHOR_TOGGLE= "Revision.ShowAuthor.Toggle"; //$NON-NLS-1$

	/**
	 * Name of the action for toggling the display of the revision id.
	 * Value: <code>"Revision.ShowId.Toggle"</code>
	 * @since 3.3
	 */
	String REVISION_SHOW_ID_TOGGLE= "Revision.ShowId.Toggle"; //$NON-NLS-1$
	/**
	 * Name of the action for emacs recenter.
	 * Value: <code>"RECENTER"</code>
	 * @since 3.3
	 */
	String RECENTER= "Recenter"; //$NON-NLS-1$

	/**
	 * Name of the action for toggling the display of whitespace characters.
	 * Value: <code>"ShowWhitespaceCharacters"</code>
	 * @since 3.3
	 */
	String SHOW_WHITESPACE_CHARACTERS= "ShowWhitespaceCharacters"; //$NON-NLS-1$

	/**
	 * Name of the action displaying information for the
	 * current caret location in a sticky hover.
	 * Value: <code>"ShowInformation"</code>
	 * @see IInformationProvider
	 * @since 3.3
	 */
	String SHOW_INFORMATION= "ShowInformation"; //$NON-NLS-1$

	/**
	 * Name of the action for toggling block selection mode. Value:
	 * <code>"BlockSelectionMode"</code>
	 * @since 3.5
	 */
	String BLOCK_SELECTION_MODE= "BlockSelectionMode"; //$NON-NLS-1$

	/**
	 * Name of the action displaying a sticky ruler hover for the current caret location.
	 * Value: <code>"ShowChangeRulerInformation"</code>
	 * @since 3.6
	 */
	public static final String SHOW_CHANGE_RULER_INFORMATION= "ShowChangeRulerInformation"; //$NON-NLS-1$

	/**
	 * Name of the action displaying a sticky ruler annotation hover for the current caret location.
	 * Value: <code>"ShowRulerAnnotationInformation"</code>
	 * @since 3.6
	 */
	public static final String SHOW_RULER_ANNOTATION_INFORMATION= "ShowRulerAnnotationInformation"; //$NON-NLS-1$

	/**
	 * Name of the action to open the hyperlink at the caret location or to display a chooser
	 * if more than one hyperlink is available.
	 * Value: <code>"OpenHyperlink"</code> 
	 * @since 3.7
	 */
	public static String OPEN_HYPERLINK= "OpenHyperlink"; //$NON-NLS-1$
}
