package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Defines the definitions ids for the text editor actions, 
 * i.e. navigation, selection, and modification.
 * This is not yet API.
 */
public interface ITextEditorActionDefinitionIds extends IWorkbenchActionDefinitionIds {
	
	// navigation
	public static final String LINE_UP= "org.eclipse.ui.edit.text.goto.lineUp";
	public static final String LINE_DOWN= "org.eclipse.ui.edit.text.goto.lineDown";
	public static final String LINE_START= "org.eclipse.ui.edit.text.goto.lineStart";
	public static final String LINE_END= "org.eclipse.ui.edit.text.goto.lineEnd";
	public static final String LINE_GOTO= "org.eclipse.ui.edit.text.goto.line";
	public static final String COLUMN_PREVIOUS= "org.eclipse.ui.edit.text.goto.columnPrevious";
	public static final String COLUMN_NEXT= "org.eclipse.ui.edit.text.goto.columnNext";
	public static final String PAGE_UP= "org.eclipse.ui.edit.text.goto.pageUp";
	public static final String PAGE_DOWN= "org.eclipse.ui.edit.text.goto.pageDown";
	public static final String WORD_PREVIOUS= "org.eclipse.ui.edit.text.goto.wordPrevious";
	public static final String WORD_NEXT= "org.eclipse.ui.edit.text.goto.wordNext";
	public static final String TEXT_START= "org.eclipse.ui.edit.text.goto.textStart";
	public static final String TEXT_END= "org.eclipse.ui.edit.text.goto.textEnd";
	public static final String WINDOW_START= "org.eclipse.ui.edit.text.goto.windowStart";
	public static final String WINDOW_END= "org.eclipse.ui.edit.text.goto.windowEnd";
	
	// selection
	public static final String SELECT_LINE_UP= "org.eclipse.ui.edit.text.select.lineUp";
	public static final String SELECT_LINE_DOWN= "org.eclipse.ui.edit.text.select.lineDown";
	public static final String SELECT_LINE_START= "org.eclipse.ui.edit.text.select.lineStart";
	public static final String SELECT_LINE_END= "org.eclipse.ui.edit.text.select.lineEnd";
	public static final String SELECT_COLUMN_PREVIOUS= "org.eclipse.ui.edit.text.select.columnPrevious";
	public static final String SELECT_COLUMN_NEXT= "org.eclipse.ui.edit.text.select.columnNext";
	public static final String SELECT_PAGE_UP= "org.eclipse.ui.edit.text.select.pageUp";
	public static final String SELECT_PAGE_DOWN= "org.eclipse.ui.edit.text.select.pageDown";
	public static final String SELECT_WORD_PREVIOUS= "org.eclipse.ui.edit.text.select.wordPrevious";
	public static final String SELECT_WORD_NEXT= "org.eclipse.ui.edit.text.select.wordNext";
	public static final String SELECT_TEXT_START= "org.eclipse.ui.edit.text.select.textStart";
	public static final String SELECT_TEXT_END= "org.eclipse.ui.edit.text.select.textEnd";
	public static final String SELECT_WINDOW_START= "org.eclipse.ui.edit.text.select.windowStart";
	public static final String SELECT_WINDOW_END= "org.eclipse.ui.edit.text.select.windowEnd";
	
	// modification
	public static final String DELETE_PREVIOUS= "org.eclipse.ui.edit.text.deletePrevious";
	public static final String DELETE_NEXT= "org.eclipse.ui.edit.text.deleteNext";
	public static final String SHIFT_RIGHT= "org.eclipse.ui.edit.text.shiftRight";
	public static final String SHIFT_LEFT= "org.eclipse.ui.edit.text.shiftLeft";
	
	// miscellaneous
	public static final String TOGGLE_OVERWRITE= "org.eclipse.ui.edit.text.toggleOverwrite";
}
