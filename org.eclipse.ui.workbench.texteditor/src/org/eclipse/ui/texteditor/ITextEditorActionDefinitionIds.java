/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris.Dennis@invidi.com - http://bugs.eclipse.org/bugs/show_bug.cgi?id=29027
 *     Genady Beryozkin, me@genady.org - https://bugs.eclipse.org/bugs/show_bug.cgi?id=11668
 *******************************************************************************/
package org.eclipse.ui.texteditor;

/**
 * Defines the definitions ids for the text editor actions. These actions are
 * navigation, selection, and modification actions.
 * @since 2.0
 */
public interface ITextEditorActionDefinitionIds extends IWorkbenchActionDefinitionIds {

	// edit

	/**
	 * Action definition id of the edit delete line action.
	 * Value: <code>"org.eclipse.ui.edit.text.delete.line"</code>
	 */
	public static final String DELETE_LINE= "org.eclipse.ui.edit.text.delete.line"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit cut line action.
	 * Value: <code>"org.eclipse.ui.edit.text.cut.line"</code>
	 * @since 2.1
	 */
	public static final String CUT_LINE= "org.eclipse.ui.edit.text.cut.line"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit delete line to beginning action.
	 * Value: <code>"org.eclipse.ui.edit.text.delete.line.to.beginning"</code>
	 */
	public static final String DELETE_LINE_TO_BEGINNING= "org.eclipse.ui.edit.text.delete.line.to.beginning"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit cut line to beginning action.
	 * Value: <code>"org.eclipse.ui.edit.text.cut.line.to.beginning"</code>
	 * @since 2.1
	 */
	public static final String CUT_LINE_TO_BEGINNING= "org.eclipse.ui.edit.text.cut.line.to.beginning"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit delete line to end action.
	 * Value: <code>"org.eclipse.ui.edit.text.delete.line.to.end"</code>
	 */
	public static final String DELETE_LINE_TO_END= "org.eclipse.ui.edit.text.delete.line.to.end"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit cut line to end action.
	 * Value: <code>"org.eclipse.ui.edit.text.cut.line.to.end"</code>
	 * @since 2.1
	 */
	public static final String CUT_LINE_TO_END= "org.eclipse.ui.edit.text.cut.line.to.end"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit set mark action.
	 * Value: <code>"org.eclipse.ui.edit.text.set.mark"</code>
	 */
	public static final String SET_MARK= "org.eclipse.ui.edit.text.set.mark"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit clear mark action.
	 * Value: <code>"org.eclipse.ui.edit.text.clear.mark"</code>
	 */
	public static final String CLEAR_MARK= "org.eclipse.ui.edit.text.clear.mark"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit swap mark action.
	 * Value: <code>"org.eclipse.ui.edit.text.swap.mark"</code>
	 */
	public static final String SWAP_MARK= "org.eclipse.ui.edit.text.swap.mark"; //$NON-NLS-1$

	/**
	 * Action definition id of the smart enter action.
	 * Value: <code>"org.eclipse.ui.edit.text.smartEnter"</code>
	 * @since 3.0
	 */
	public static final String SMART_ENTER= "org.eclipse.ui.edit.text.smartEnter"; //$NON-NLS-1$

	/**
	 * Action definition id of the smart enter (inverse) action.
	 * Value: <code>"org.eclipse.ui.edit.text.smartEnterInverse"</code>
	 * @since 3.0
	 */
	public static final String SMART_ENTER_INVERSE= "org.eclipse.ui.edit.text.smartEnterInverse"; //$NON-NLS-1$

	/**
	 * Action definition id of the move lines upwards action.
	 * Value: <code>"org.eclipse.ui.edit.text.moveLineUp"</code>
	 * @since 3.0
	 */
	public static final String MOVE_LINES_UP= "org.eclipse.ui.edit.text.moveLineUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the move lines downwards action.
	 * Value: <code>"org.eclipse.ui.edit.text.moveLineDown"</code>
	 * @since 3.0
	 */
	public static final String MOVE_LINES_DOWN= "org.eclipse.ui.edit.text.moveLineDown"; //$NON-NLS-1$

	/**
	 * Action definition id of the copy lines upwards action.
	 * Value: <code>"org.eclipse.ui.edit.text.copyLineUp"</code>
	 * @since 3.0
	 */
	public static final String COPY_LINES_UP= "org.eclipse.ui.edit.text.copyLineUp"; //$NON-NLS-1$;

	/**
	 * Action definition id of the copy lines downwards action.
	 * Value: <code>"org.eclipse.ui.edit.text.copyLineDown"</code>
	 * @since 3.0
	 */
	public static final String COPY_LINES_DOWN= "org.eclipse.ui.edit.text.copyLineDown"; //$NON-NLS-1$;

	/**
	 * Action definition id of the upper case action.
	 * Value: <code>"org.eclipse.ui.edit.text.upperCase"</code>
	 * @since 3.0
	 */
	public static final String UPPER_CASE= "org.eclipse.ui.edit.text.upperCase"; //$NON-NLS-1$

	/**
	 * Action definition id of the lower case action.
	 * Value: <code>"org.eclipse.ui.edit.text.lowerCase"</code>
	 * @since 3.0
	 */
	public static final String LOWER_CASE= "org.eclipse.ui.edit.text.lowerCase"; //$NON-NLS-1$


	// navigation

	/**
	 * Action definition id of the navigate goto previous line action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.lineUp"</code>
	 */
	public static final String LINE_UP= "org.eclipse.ui.edit.text.goto.lineUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto next line action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.lineDown"</code>
	 */
	public static final String LINE_DOWN= "org.eclipse.ui.edit.text.goto.lineDown"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto line start action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.lineStart"</code>
	 */
	public static final String LINE_START= "org.eclipse.ui.edit.text.goto.lineStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto line end action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.lineEnd"</code>
	 */
	public static final String LINE_END= "org.eclipse.ui.edit.text.goto.lineEnd"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto line action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.line"</code>
	 */
	public static final String LINE_GOTO= "org.eclipse.ui.edit.text.goto.line"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto previous column action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.columnPrevious"</code>
	 */
	public static final String COLUMN_PREVIOUS= "org.eclipse.ui.edit.text.goto.columnPrevious"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto next column action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.columnNext"</code>
	 */
	public static final String COLUMN_NEXT= "org.eclipse.ui.edit.text.goto.columnNext"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto previous page action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.pageUp"</code>
	 */
	public static final String PAGE_UP= "org.eclipse.ui.edit.text.goto.pageUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto next page action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.pageDown"</code>
	 */
	public static final String PAGE_DOWN= "org.eclipse.ui.edit.text.goto.pageDown"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto previous word action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.wordPrevious"</code>
	 */
	public static final String WORD_PREVIOUS= "org.eclipse.ui.edit.text.goto.wordPrevious"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto next word action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.wordNext"</code>
	 */
	public static final String WORD_NEXT= "org.eclipse.ui.edit.text.goto.wordNext"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto text start action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.textStart"</code>
	 */
	public static final String TEXT_START= "org.eclipse.ui.edit.text.goto.textStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto text end action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.textEnd"</code>
	 */
	public static final String TEXT_END= "org.eclipse.ui.edit.text.goto.textEnd"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto start of window action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.windowStart"</code>
	 */
	public static final String WINDOW_START= "org.eclipse.ui.edit.text.goto.windowStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto end of window action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.windowEnd"</code>
	 */
	public static final String WINDOW_END= "org.eclipse.ui.edit.text.goto.windowEnd"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate scroll line up action.
	 * Value: <code>"org.eclipse.ui.edit.text.scroll.lineUp"</code>
	 */
	public static final String SCROLL_LINE_UP= "org.eclipse.ui.edit.text.scroll.lineUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate scroll line down action.
	 * Value: <code>"org.eclipse.ui.edit.text.scroll.lineDown"</code>
	 */
	public static final String SCROLL_LINE_DOWN= "org.eclipse.ui.edit.text.scroll.lineDown"; //$NON-NLS-1$


	// selection

	/**
	 * Action definition id of the select line up action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.lineUp"</code>
	 */
	public static final String SELECT_LINE_UP= "org.eclipse.ui.edit.text.select.lineUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the select line down action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.lineDown"</code>
	 */
	public static final String SELECT_LINE_DOWN= "org.eclipse.ui.edit.text.select.lineDown"; //$NON-NLS-1$

	/**
	 * Action definition id of the select line start action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.lineStart"</code>
	 */
	public static final String SELECT_LINE_START= "org.eclipse.ui.edit.text.select.lineStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the select line end action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.lineEnd"</code>
	 */
	public static final String SELECT_LINE_END= "org.eclipse.ui.edit.text.select.lineEnd"; //$NON-NLS-1$

	/**
	 * Action definition id of the select previous column action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.columnPrevious"</code>
	 */
	public static final String SELECT_COLUMN_PREVIOUS= "org.eclipse.ui.edit.text.select.columnPrevious"; //$NON-NLS-1$

	/**
	 * Action definition id of the select next column action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.columnNext"</code>
	 */
	public static final String SELECT_COLUMN_NEXT= "org.eclipse.ui.edit.text.select.columnNext"; //$NON-NLS-1$

	/**
	 * Action definition id of the select page up action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.pageUp"</code>
	 */
	public static final String SELECT_PAGE_UP= "org.eclipse.ui.edit.text.select.pageUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the select page down action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.pageDown"</code>
	 */
	public static final String SELECT_PAGE_DOWN= "org.eclipse.ui.edit.text.select.pageDown"; //$NON-NLS-1$

	/**
	 * Action definition id of the select previous word action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.wordPrevious"</code>
	 */
	public static final String SELECT_WORD_PREVIOUS= "org.eclipse.ui.edit.text.select.wordPrevious"; //$NON-NLS-1$

	/**
	 * Action definition id of the select next word action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.wordNext"</code>
	 */
	public static final String SELECT_WORD_NEXT= "org.eclipse.ui.edit.text.select.wordNext"; //$NON-NLS-1$

	/**
	 * Action definition id of the select text start action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.textStart"</code>
	 */
	public static final String SELECT_TEXT_START= "org.eclipse.ui.edit.text.select.textStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the select text end action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.textEnd"</code>
	 */
	public static final String SELECT_TEXT_END= "org.eclipse.ui.edit.text.select.textEnd"; //$NON-NLS-1$

	/**
	 * Action definition id of the select window start action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.windowStart"</code>
	 */
	public static final String SELECT_WINDOW_START= "org.eclipse.ui.edit.text.select.windowStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the select window end action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.windowEnd"</code>
	 */
	public static final String SELECT_WINDOW_END= "org.eclipse.ui.edit.text.select.windowEnd"; //$NON-NLS-1$


	// modification

	/**
	 * Action definition id of the edit delete previous character action.
	 * Value: <code>"org.eclipse.ui.edit.text.deletePrevious"</code>
	 */
	public static final String DELETE_PREVIOUS= "org.eclipse.ui.edit.text.deletePrevious"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit delete next character action.
	 * Value: <code>"org.eclipse.ui.edit.text.deleteNext"</code>
	 */
	public static final String DELETE_NEXT= "org.eclipse.ui.edit.text.deleteNext"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit delete previous word action.
	 * Value: <code>"org.eclipse.ui.edit.text.deletePreviousWord"</code>
	 * @since 2.1
	 */
	public static final String DELETE_PREVIOUS_WORD= "org.eclipse.ui.edit.text.deletePreviousWord"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit delete next word action.
	 * Value: <code>"org.eclipse.ui.edit.text.deleteNextWord"</code>
	 * @since 2.1
	 */
	public static final String DELETE_NEXT_WORD= "org.eclipse.ui.edit.text.deleteNextWord"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit shift right action.
	 * Value: <code>"org.eclipse.ui.edit.text.shiftRight"</code>
	 */
	public static final String SHIFT_RIGHT= "org.eclipse.ui.edit.text.shiftRight"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit shift left action.
	 * Value: <code>"org.eclipse.ui.edit.text.shiftLeft"</code>
	 */
	public static final String SHIFT_LEFT= "org.eclipse.ui.edit.text.shiftLeft"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit convert to window's line delimiter action.
	 * Value: <code>"org.eclipse.ui.edit.text.convert.lineDelimiters.toWindows"</code>
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	public static final String CONVERT_LINE_DELIMITERS_TO_WINDOWS= "org.eclipse.ui.edit.text.convert.lineDelimiters.toWindows"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit convert to Unix line delimiter action.
	 * Value: <code>"org.eclipse.ui.edit.text.convert.lineDelimiters.toUNIX"</code>
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	public static final String CONVERT_LINE_DELIMITERS_TO_UNIX= "org.eclipse.ui.edit.text.convert.lineDelimiters.toUNIX"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit convert to Mac line delimiter action.
	 * Value: <code>"org.eclipse.ui.edit.text.convert.lineDelimiters.toMac"</code>
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	public static final String CONVERT_LINE_DELIMITERS_TO_MAC= "org.eclipse.ui.edit.text.convert.lineDelimiters.toMac"; //$NON-NLS-1$


	// miscellaneous

	/**
	 * Action definition id of the toggle input mode action.
	 * Value: <code>"org.eclipse.ui.edit.text.toggleOverwrite"</code>
	 */
	public static final String TOGGLE_OVERWRITE= "org.eclipse.ui.edit.text.toggleOverwrite"; //$NON-NLS-1$

	/**
	 * Action definition id of toggle show selected element only action.
	 * Value: <code>"org.eclipse.ui.edit.text.toggleShowSelectedElementOnly"</code>
	 * @since 3.0
	 */
	public static final String TOGGLE_SHOW_SELECTED_ELEMENT_ONLY= "org.eclipse.ui.edit.text.toggleShowSelectedElementOnly"; //$NON-NLS-1$

	/**
	 * Action definition id of the show ruler context menu action.
	 * Value: <code>"org.eclipse.ui.edit.text.showRulerContextMenu"</code>
	 */
	public static final String SHOW_RULER_CONTEXT_MENU= "org.eclipse.ui.edit.text.showRulerContextMenu"; //$NON-NLS-1$

	/**
	 * Action definition id of go to last edit position action.
	 * Value: <code>"org.eclipse.ui.edit.text.gotoLastEditPosition"</code>
	 * @since 2.1
	 */
	public static final String GOTO_LAST_EDIT_POSITION= "org.eclipse.ui.edit.text.gotoLastEditPosition"; //$NON-NLS-1$

	/**
	 * Action definition id of go to next annotation action.
	 * Value: <code>"org.eclipse.ui.edit.text.gotoNextAnnotation"</code>
	 * @since 3.0
	 */
	public static final String GOTO_NEXT_ANNOTATION= "org.eclipse.ui.edit.text.gotoNextAnnotation"; //$NON-NLS-1$

	/**
	 * Action definition id of go to previous annotation action.
	 * Value: <code>"org.eclipse.ui.edit.text.gotoPreviousAnnotation"</code>
	 * @since 3.0
	 */
	public static final String GOTO_PREVIOUS_ANNOTATION= "org.eclipse.ui.edit.text.gotoPreviousAnnotation"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> content assist proposal action
	 * Value: <code>"org.eclipse.ui.edit.text.contentAssist.proposals"</code>).
	 * @since 2.1
	 */
	public static final String CONTENT_ASSIST_PROPOSALS= "org.eclipse.ui.edit.text.contentAssist.proposals"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> content assist context information action
	 * Value: <code>"org.eclipse.ui.edit.text.contentAssist.contextInformation"</code>).
	 * @since 2.1
	 */
	public static final String CONTENT_ASSIST_CONTEXT_INFORMATION= "org.eclipse.ui.edit.text.contentAssist.contextInformation"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> smart insert mode action
	 * Value: <code>"org.eclipse.ui.edit.text.toggleInsertMode"</code>).
	 * @since 3.0
	 */
	public static final String TOGGLE_INSERT_MODE= "org.eclipse.ui.edit.text.toggleInsertMode"; //$NON-NLS-1$

	/**
	 * Value: <code>"org.eclipse.ui.edit.text.changeEncoding"</code>).
	 * @since 3.1
	 */
	public static final String CHANGE_ENCODING= "org.eclipse.ui.edit.text.changeEncoding"; //$NON-NLS-1$

	/**
	 * Command ID of the revert line action
	 * Value: <code>"org.eclipse.ui.editors.quickdiff.revertLine"</code>).
	 * @since 3.1
	 */
	static final String QUICKDIFF_REVERTLINE= "org.eclipse.ui.editors.quickdiff.revertLine"; //$NON-NLS-1$

	/**
	 * Command ID of the revert selection/block action
	 * Value: <code>"org.eclipse.ui.editors.quickdiff.revert"</code>).
	 * @since 3.1
	 */
	static final String QUICKDIFF_REVERT= "org.eclipse.ui.editors.quickdiff.revert"; //$NON-NLS-1$

	/**
	 * Command ID of the toggle quick diff action. The name has no proper prefix for
	 * historical reasons.
	 * Value: <code>"org.eclipse.quickdiff.toggle"</code>).
	 * @since 3.1
	 */
	static final String QUICKDIFF_TOGGLE= "org.eclipse.quickdiff.toggle"; //$NON-NLS-1$

	/**
	 * Command ID of the toggle display of line numbers
	 * Value: <code>"org.eclipse.ui.editors.lineNumberToggle"</code>).
	 * @since 3.1
	 */
	static final String LINENUMBER_TOGGLE= "org.eclipse.ui.editors.lineNumberToggle"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> text complete action
	 * Value: <code>"org.eclipse.ui.edit.text.hippieCompletion"</code>).
	 * @since 3.1
	 */
	public static final String HIPPIE_COMPLETION= "org.eclipse.ui.edit.text.hippieCompletion"; //$NON-NLS-1$
}
