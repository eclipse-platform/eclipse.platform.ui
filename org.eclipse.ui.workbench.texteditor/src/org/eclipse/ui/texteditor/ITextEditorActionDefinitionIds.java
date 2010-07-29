/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris.Dennis@invidi.com - http://bugs.eclipse.org/bugs/show_bug.cgi?id=29027
 *     Genady Beryozkin, me@genady.org - https://bugs.eclipse.org/bugs/show_bug.cgi?id=11668
 *     Benjamin Muskalla <b.muskalla@gmx.net> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=41573
 *     Tom Eicher (Avaloq Evolution AG) - block selection mode
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.jface.text.information.IInformationProvider;


/**
 * Defines the definitions ids for the text editor actions. These actions are navigation, selection,
 * and modification actions.
 * <p>
 * This interface must not be implemented by clients.
 * </p>
 * 
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITextEditorActionDefinitionIds extends IWorkbenchActionDefinitionIds {

	// edit

	/**
	 * Action definition id of the edit delete line action.
	 * Value: <code>"org.eclipse.ui.edit.text.delete.line"</code>
	 */
	String DELETE_LINE= "org.eclipse.ui.edit.text.delete.line"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit join lines action.
	 * Value: <code>"org.eclipse.ui.edit.text.join.line"</code>
	 * @since 3.3
	 */
	String JOIN_LINES= "org.eclipse.ui.edit.text.join.lines"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit cut line action.
	 * Value: <code>"org.eclipse.ui.edit.text.cut.line"</code>
	 * @since 2.1
	 */
	String CUT_LINE= "org.eclipse.ui.edit.text.cut.line"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit delete line to beginning action.
	 * Value: <code>"org.eclipse.ui.edit.text.delete.line.to.beginning"</code>
	 */
	String DELETE_LINE_TO_BEGINNING= "org.eclipse.ui.edit.text.delete.line.to.beginning"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit cut line to beginning action.
	 * Value: <code>"org.eclipse.ui.edit.text.cut.line.to.beginning"</code>
	 * @since 2.1
	 */
	String CUT_LINE_TO_BEGINNING= "org.eclipse.ui.edit.text.cut.line.to.beginning"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit delete line to end action.
	 * Value: <code>"org.eclipse.ui.edit.text.delete.line.to.end"</code>
	 */
	String DELETE_LINE_TO_END= "org.eclipse.ui.edit.text.delete.line.to.end"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit cut line to end action.
	 * Value: <code>"org.eclipse.ui.edit.text.cut.line.to.end"</code>
	 * @since 2.1
	 */
	String CUT_LINE_TO_END= "org.eclipse.ui.edit.text.cut.line.to.end"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit set mark action.
	 * Value: <code>"org.eclipse.ui.edit.text.set.mark"</code>
	 */
	String SET_MARK= "org.eclipse.ui.edit.text.set.mark"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit clear mark action.
	 * Value: <code>"org.eclipse.ui.edit.text.clear.mark"</code>
	 */
	String CLEAR_MARK= "org.eclipse.ui.edit.text.clear.mark"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit swap mark action.
	 * Value: <code>"org.eclipse.ui.edit.text.swap.mark"</code>
	 */
	String SWAP_MARK= "org.eclipse.ui.edit.text.swap.mark"; //$NON-NLS-1$

	/**
	 * Action definition id of the smart enter action.
	 * Value: <code>"org.eclipse.ui.edit.text.smartEnter"</code>
	 * @since 3.0
	 */
	String SMART_ENTER= "org.eclipse.ui.edit.text.smartEnter"; //$NON-NLS-1$

	/**
	 * Action definition id of the smart enter (inverse) action.
	 * Value: <code>"org.eclipse.ui.edit.text.smartEnterInverse"</code>
	 * @since 3.0
	 */
	String SMART_ENTER_INVERSE= "org.eclipse.ui.edit.text.smartEnterInverse"; //$NON-NLS-1$

	/**
	 * Action definition id of the move lines upwards action.
	 * Value: <code>"org.eclipse.ui.edit.text.moveLineUp"</code>
	 * @since 3.0
	 */
	String MOVE_LINES_UP= "org.eclipse.ui.edit.text.moveLineUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the move lines downwards action.
	 * Value: <code>"org.eclipse.ui.edit.text.moveLineDown"</code>
	 * @since 3.0
	 */
	String MOVE_LINES_DOWN= "org.eclipse.ui.edit.text.moveLineDown"; //$NON-NLS-1$

	/**
	 * Action definition id of the copy lines upwards action.
	 * Value: <code>"org.eclipse.ui.edit.text.copyLineUp"</code>
	 * @since 3.0
	 */
	String COPY_LINES_UP= "org.eclipse.ui.edit.text.copyLineUp"; //$NON-NLS-1$;

	/**
	 * Action definition id of the copy lines downwards action.
	 * Value: <code>"org.eclipse.ui.edit.text.copyLineDown"</code>
	 * @since 3.0
	 */
	String COPY_LINES_DOWN= "org.eclipse.ui.edit.text.copyLineDown"; //$NON-NLS-1$;

	/**
	 * Action definition id of the upper case action.
	 * Value: <code>"org.eclipse.ui.edit.text.upperCase"</code>
	 * @since 3.0
	 */
	String UPPER_CASE= "org.eclipse.ui.edit.text.upperCase"; //$NON-NLS-1$

	/**
	 * Action definition id of the lower case action.
	 * Value: <code>"org.eclipse.ui.edit.text.lowerCase"</code>
	 * @since 3.0
	 */
	String LOWER_CASE= "org.eclipse.ui.edit.text.lowerCase"; //$NON-NLS-1$


	// navigation

	/**
	 * Action definition id of the navigate goto previous line action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.lineUp"</code>
	 */
	String LINE_UP= "org.eclipse.ui.edit.text.goto.lineUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto next line action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.lineDown"</code>
	 */
	String LINE_DOWN= "org.eclipse.ui.edit.text.goto.lineDown"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto line start action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.lineStart"</code>
	 */
	String LINE_START= "org.eclipse.ui.edit.text.goto.lineStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto line end action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.lineEnd"</code>
	 */
	String LINE_END= "org.eclipse.ui.edit.text.goto.lineEnd"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto line action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.line"</code>
	 */
	String LINE_GOTO= "org.eclipse.ui.edit.text.goto.line"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto previous column action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.columnPrevious"</code>
	 */
	String COLUMN_PREVIOUS= "org.eclipse.ui.edit.text.goto.columnPrevious"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto next column action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.columnNext"</code>
	 */
	String COLUMN_NEXT= "org.eclipse.ui.edit.text.goto.columnNext"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto previous page action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.pageUp"</code>
	 */
	String PAGE_UP= "org.eclipse.ui.edit.text.goto.pageUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto next page action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.pageDown"</code>
	 */
	String PAGE_DOWN= "org.eclipse.ui.edit.text.goto.pageDown"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto previous word action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.wordPrevious"</code>
	 */
	String WORD_PREVIOUS= "org.eclipse.ui.edit.text.goto.wordPrevious"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto next word action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.wordNext"</code>
	 */
	String WORD_NEXT= "org.eclipse.ui.edit.text.goto.wordNext"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto text start action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.textStart"</code>
	 */
	String TEXT_START= "org.eclipse.ui.edit.text.goto.textStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto text end action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.textEnd"</code>
	 */
	String TEXT_END= "org.eclipse.ui.edit.text.goto.textEnd"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto start of window action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.windowStart"</code>
	 */
	String WINDOW_START= "org.eclipse.ui.edit.text.goto.windowStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate goto end of window action.
	 * Value: <code>"org.eclipse.ui.edit.text.goto.windowEnd"</code>
	 */
	String WINDOW_END= "org.eclipse.ui.edit.text.goto.windowEnd"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate scroll line up action.
	 * Value: <code>"org.eclipse.ui.edit.text.scroll.lineUp"</code>
	 */
	String SCROLL_LINE_UP= "org.eclipse.ui.edit.text.scroll.lineUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the navigate scroll line down action.
	 * Value: <code>"org.eclipse.ui.edit.text.scroll.lineDown"</code>
	 */
	String SCROLL_LINE_DOWN= "org.eclipse.ui.edit.text.scroll.lineDown"; //$NON-NLS-1$


	// selection

	/**
	 * Action definition id of the select line up action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.lineUp"</code>
	 */
	String SELECT_LINE_UP= "org.eclipse.ui.edit.text.select.lineUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the select line down action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.lineDown"</code>
	 */
	String SELECT_LINE_DOWN= "org.eclipse.ui.edit.text.select.lineDown"; //$NON-NLS-1$

	/**
	 * Action definition id of the select line start action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.lineStart"</code>
	 */
	String SELECT_LINE_START= "org.eclipse.ui.edit.text.select.lineStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the select line end action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.lineEnd"</code>
	 */
	String SELECT_LINE_END= "org.eclipse.ui.edit.text.select.lineEnd"; //$NON-NLS-1$

	/**
	 * Action definition id of the select previous column action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.columnPrevious"</code>
	 */
	String SELECT_COLUMN_PREVIOUS= "org.eclipse.ui.edit.text.select.columnPrevious"; //$NON-NLS-1$

	/**
	 * Action definition id of the select next column action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.columnNext"</code>
	 */
	String SELECT_COLUMN_NEXT= "org.eclipse.ui.edit.text.select.columnNext"; //$NON-NLS-1$

	/**
	 * Action definition id of the select page up action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.pageUp"</code>
	 */
	String SELECT_PAGE_UP= "org.eclipse.ui.edit.text.select.pageUp"; //$NON-NLS-1$

	/**
	 * Action definition id of the select page down action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.pageDown"</code>
	 */
	String SELECT_PAGE_DOWN= "org.eclipse.ui.edit.text.select.pageDown"; //$NON-NLS-1$

	/**
	 * Action definition id of the select previous word action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.wordPrevious"</code>
	 */
	String SELECT_WORD_PREVIOUS= "org.eclipse.ui.edit.text.select.wordPrevious"; //$NON-NLS-1$

	/**
	 * Action definition id of the select next word action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.wordNext"</code>
	 */
	String SELECT_WORD_NEXT= "org.eclipse.ui.edit.text.select.wordNext"; //$NON-NLS-1$

	/**
	 * Action definition id of the select text start action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.textStart"</code>
	 */
	String SELECT_TEXT_START= "org.eclipse.ui.edit.text.select.textStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the select text end action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.textEnd"</code>
	 */
	String SELECT_TEXT_END= "org.eclipse.ui.edit.text.select.textEnd"; //$NON-NLS-1$

	/**
	 * Action definition id of the select window start action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.windowStart"</code>
	 */
	String SELECT_WINDOW_START= "org.eclipse.ui.edit.text.select.windowStart"; //$NON-NLS-1$

	/**
	 * Action definition id of the select window end action.
	 * Value: <code>"org.eclipse.ui.edit.text.select.windowEnd"</code>
	 */
	String SELECT_WINDOW_END= "org.eclipse.ui.edit.text.select.windowEnd"; //$NON-NLS-1$


	// modification

	/**
	 * Action definition id of the edit delete previous character action.
	 * Value: <code>"org.eclipse.ui.edit.text.deletePrevious"</code>
	 */
	String DELETE_PREVIOUS= "org.eclipse.ui.edit.text.deletePrevious"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit delete next character action.
	 * Value: <code>"org.eclipse.ui.edit.text.deleteNext"</code>
	 */
	String DELETE_NEXT= "org.eclipse.ui.edit.text.deleteNext"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit delete previous word action.
	 * Value: <code>"org.eclipse.ui.edit.text.deletePreviousWord"</code>
	 * @since 2.1
	 */
	String DELETE_PREVIOUS_WORD= "org.eclipse.ui.edit.text.deletePreviousWord"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit delete next word action.
	 * Value: <code>"org.eclipse.ui.edit.text.deleteNextWord"</code>
	 * @since 2.1
	 */
	String DELETE_NEXT_WORD= "org.eclipse.ui.edit.text.deleteNextWord"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit shift right action.
	 * Value: <code>"org.eclipse.ui.edit.text.shiftRight"</code>
	 */
	String SHIFT_RIGHT= "org.eclipse.ui.edit.text.shiftRight"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit shift left action.
	 * Value: <code>"org.eclipse.ui.edit.text.shiftLeft"</code>
	 */
	String SHIFT_LEFT= "org.eclipse.ui.edit.text.shiftLeft"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit convert to window's line delimiter action.
	 * Value: <code>"org.eclipse.ui.edit.text.convert.lineDelimiters.toWindows"</code>
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	String CONVERT_LINE_DELIMITERS_TO_WINDOWS= "org.eclipse.ui.edit.text.convert.lineDelimiters.toWindows"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit convert to Unix line delimiter action.
	 * Value: <code>"org.eclipse.ui.edit.text.convert.lineDelimiters.toUNIX"</code>
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	String CONVERT_LINE_DELIMITERS_TO_UNIX= "org.eclipse.ui.edit.text.convert.lineDelimiters.toUNIX"; //$NON-NLS-1$

	/**
	 * Action definition id of the edit convert to Mac line delimiter action.
	 * Value: <code>"org.eclipse.ui.edit.text.convert.lineDelimiters.toMac"</code>
	 * @deprecated since 3.1. No longer supported as editor actions.
	 */
	String CONVERT_LINE_DELIMITERS_TO_MAC= "org.eclipse.ui.edit.text.convert.lineDelimiters.toMac"; //$NON-NLS-1$


	// miscellaneous

	/**
	 * Action definition id of the toggle input mode action.
	 * Value: <code>"org.eclipse.ui.edit.text.toggleOverwrite"</code>
	 */
	String TOGGLE_OVERWRITE= "org.eclipse.ui.edit.text.toggleOverwrite"; //$NON-NLS-1$

	/**
	 * Action definition id of toggle show selected element only action.
	 * Value: <code>"org.eclipse.ui.edit.text.toggleShowSelectedElementOnly"</code>
	 * @since 3.0
	 */
	String TOGGLE_SHOW_SELECTED_ELEMENT_ONLY= "org.eclipse.ui.edit.text.toggleShowSelectedElementOnly"; //$NON-NLS-1$

	/**
	 * Action definition id of the show ruler context menu action.
	 * Value: <code>"org.eclipse.ui.edit.text.showRulerContextMenu"</code>
	 */
	String SHOW_RULER_CONTEXT_MENU= "org.eclipse.ui.edit.text.showRulerContextMenu"; //$NON-NLS-1$

	/**
	 * Action definition id of go to last edit position action.
	 * Value: <code>"org.eclipse.ui.edit.text.gotoLastEditPosition"</code>
	 * @since 2.1
	 */
	String GOTO_LAST_EDIT_POSITION= "org.eclipse.ui.edit.text.gotoLastEditPosition"; //$NON-NLS-1$

	/**
	 * Action definition id of go to next annotation action.
	 * Value: <code>"org.eclipse.ui.edit.text.gotoNextAnnotation"</code>
	 * @since 3.0
	 */
	String GOTO_NEXT_ANNOTATION= "org.eclipse.ui.edit.text.gotoNextAnnotation"; //$NON-NLS-1$

	/**
	 * Action definition id of go to previous annotation action.
	 * Value: <code>"org.eclipse.ui.edit.text.gotoPreviousAnnotation"</code>
	 * @since 3.0
	 */
	String GOTO_PREVIOUS_ANNOTATION= "org.eclipse.ui.edit.text.gotoPreviousAnnotation"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> content assist proposal action
	 * Value: <code>"org.eclipse.ui.edit.text.contentAssist.proposals"</code>).
	 * <p>
	 * Note: Since 3.2 the command is defined in <code>org.eclipse.ui</code> and
	 * its ID can also be accessed using {@link org.eclipse.ui.fieldassist.ContentAssistCommandAdapter#CONTENT_PROPOSAL_COMMAND}.
	 * </p>
	 * @since 2.1
	 */
	String CONTENT_ASSIST_PROPOSALS= "org.eclipse.ui.edit.text.contentAssist.proposals"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> content assist context information action
	 * Value: <code>"org.eclipse.ui.edit.text.contentAssist.contextInformation"</code>).
	 * @since 2.1
	 */
	String CONTENT_ASSIST_CONTEXT_INFORMATION= "org.eclipse.ui.edit.text.contentAssist.contextInformation"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> quick assist proposal action
	 * <p>
	 * Note: The constant contains 'jdt' for historical and compatibility reasons.
	 * </p>
	 * (value <code>"org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals"</code>).
	 * @since 3.2
	 */
	String QUICK_ASSIST= "org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> smart insert mode action
	 * Value: <code>"org.eclipse.ui.edit.text.toggleInsertMode"</code>).
	 * @since 3.0
	 */
	String TOGGLE_INSERT_MODE= "org.eclipse.ui.edit.text.toggleInsertMode"; //$NON-NLS-1$

	/**
	 * Value: <code>"org.eclipse.ui.edit.text.changeEncoding"</code>).
	 * @since 3.1
	 */
	String CHANGE_ENCODING= "org.eclipse.ui.edit.text.changeEncoding"; //$NON-NLS-1$

	/**
	 * Command ID of the revert line action
	 * Value: <code>"org.eclipse.ui.editors.quickdiff.revertLine"</code>).
	 * @since 3.1
	 */
	String QUICKDIFF_REVERTLINE= "org.eclipse.ui.editors.quickdiff.revertLine"; //$NON-NLS-1$

	/**
	 * Command ID of the revert selection/block action
	 * Value: <code>"org.eclipse.ui.editors.quickdiff.revert"</code>).
	 * @since 3.1
	 */
	String QUICKDIFF_REVERT= "org.eclipse.ui.editors.quickdiff.revert"; //$NON-NLS-1$

	/**
	 * Command ID of the toggle quick diff action. The name has no proper prefix for
	 * historical reasons.
	 * Value: <code>"org.eclipse.quickdiff.toggle"</code>).
	 * @since 3.1
	 */
	String QUICKDIFF_TOGGLE= "org.eclipse.quickdiff.toggle"; //$NON-NLS-1$

	/**
	 * Command ID of the toggle display of line numbers
	 * Value: <code>"org.eclipse.ui.editors.lineNumberToggle"</code>).
	 * @since 3.1
	 */
	String LINENUMBER_TOGGLE= "org.eclipse.ui.editors.lineNumberToggle"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> text complete action
	 * Value: <code>"org.eclipse.ui.edit.text.hippieCompletion"</code>).
	 * @since 3.1
	 */
	String HIPPIE_COMPLETION= "org.eclipse.ui.edit.text.hippieCompletion"; //$NON-NLS-1$

	/**
	 * Command ID of the command to cycle the revision rendering mode.
	 * Value: <code>"org.eclipse.ui.editors.revisions.rendering.cycle"</code>).
	 * @since 3.3
	 */
	String REVISION_RENDERING_CYCLE= "org.eclipse.ui.editors.revisions.rendering.cycle"; //$NON-NLS-1$

	/**
	 * Command ID of the command to toggle the revision author display.
	 * Value: <code>"org.eclipse.ui.editors.revisions.author.toggle"</code>).
	 * @since 3.3
	 */
	String REVISION_AUTHOR_TOGGLE= "org.eclipse.ui.editors.revisions.author.toggle"; //$NON-NLS-1$

	/**
	 * Command ID of the command to toggle the revision ID display.
	 * Value: <code>"org.eclipse.ui.editors.revisions.id.toggle"</code>).
	 * @since 3.3
	 */
	String REVISION_ID_TOGGLE= "org.eclipse.ui.editors.revisions.id.toggle"; //$NON-NLS-1$

	 /**
	 * Command ID of the recenter command.
	 * Value: <code>"org.eclipse.ui.edit.text.recenter"</code>).
	 * @since 3.3
	 */
	String RECENTER= "org.eclipse.ui.edit.text.recenter"; //$NON-NLS-1$

	 /**
	 * Command ID of the command to toggle the display of whitespace characters.
	 * Value: <code>"org.eclipse.ui.edit.text.toggleShowWhitespaceCharacters"</code>).
	 * @since 3.3
	 */
	String SHOW_WHITESPACE_CHARACTERS = "org.eclipse.ui.edit.text.toggleShowWhitespaceCharacters"; //$NON-NLS-1$

	/**
	 * Command ID of the command to display information for the
	 * current caret location in a sticky hover.
	 * Value <code>"org.eclipse.ui.edit.text.showInformation"</code>).
	 * @see IInformationProvider
	 * @since 3.3
	 */
	String SHOW_INFORMATION= "org.eclipse.ui.edit.text.showInformation"; //$NON-NLS-1$

	/**
	 * Command ID of the command to toggle block selection mode. Value:
	 * <code>"org.eclipse.ui.edit.text.toggleBlockSelectionMode"</code>).
	 * 
	 * @since 3.5
	 */
	String BLOCK_SELECTION_MODE= "org.eclipse.ui.edit.text.toggleBlockSelectionMode"; //$NON-NLS-1$

	/**
	 * Command ID of the command to display a sticky ruler hover for the current caret location.
	 * Value <code>"org.eclipse.ui.edit.text.showChangeRulerInformation"</code>.
	 * 
	 * @since 3.6
	 */
	public static final String SHOW_CHANGE_RULER_INFORMATION_ID= "org.eclipse.ui.edit.text.showChangeRulerInformation"; //$NON-NLS-1$

	/**
	 * Command ID of the command to display a sticky ruler annotation hover for the current caret
	 * location. Value <code>"org.eclipse.ui.edit.text.showRulerAnnotationInformation"</code>.
	 * 
	 * @since 3.6
	 */
	public static final String SHOW_RULER_ANNOTATION_INFORMATION_ID= "org.eclipse.ui.edit.text.showRulerAnnotationInformation"; //$NON-NLS-1$

	/**
	 * Command ID of the command to open the hyperlink at the caret location or to display a chooser
	 * if more than one hyperlink is available.
	 * Value: <code>"org.eclipse.ui.edit.text.open.hyperlink"</code>
	 * @since 3.7
	 */
	public static final String OPEN_HYPERLINK= "org.eclipse.ui.edit.text.open.hyperlink"; //$NON-NLS-1$
}
