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
	public static final String LINE_UP= "org.eclipse.ui.edit.text.goto.lineUp"; //$NON-NLS-1$
	public static final String LINE_DOWN= "org.eclipse.ui.edit.text.goto.lineDown"; //$NON-NLS-1$
	public static final String LINE_START= "org.eclipse.ui.edit.text.goto.lineStart"; //$NON-NLS-1$
	public static final String LINE_END= "org.eclipse.ui.edit.text.goto.lineEnd"; //$NON-NLS-1$
	public static final String LINE_GOTO= "org.eclipse.ui.edit.text.goto.line"; //$NON-NLS-1$
	public static final String COLUMN_PREVIOUS= "org.eclipse.ui.edit.text.goto.columnPrevious"; //$NON-NLS-1$
	public static final String COLUMN_NEXT= "org.eclipse.ui.edit.text.goto.columnNext"; //$NON-NLS-1$
	public static final String PAGE_UP= "org.eclipse.ui.edit.text.goto.pageUp"; //$NON-NLS-1$
	public static final String PAGE_DOWN= "org.eclipse.ui.edit.text.goto.pageDown"; //$NON-NLS-1$
	public static final String WORD_PREVIOUS= "org.eclipse.ui.edit.text.goto.wordPrevious"; //$NON-NLS-1$
	public static final String WORD_NEXT= "org.eclipse.ui.edit.text.goto.wordNext"; //$NON-NLS-1$
	public static final String TEXT_START= "org.eclipse.ui.edit.text.goto.textStart"; //$NON-NLS-1$
	public static final String TEXT_END= "org.eclipse.ui.edit.text.goto.textEnd"; //$NON-NLS-1$
	public static final String WINDOW_START= "org.eclipse.ui.edit.text.goto.windowStart"; //$NON-NLS-1$
	public static final String WINDOW_END= "org.eclipse.ui.edit.text.goto.windowEnd"; //$NON-NLS-1$
	public static final String SCROLL_LINE_UP= "org.eclipse.ui.edit.text.scroll.lineUp"; //$NON-NLS-1$
	public static final String SCROLL_LINE_DOWN= "org.eclipse.ui.edit.text.scroll.lineDown"; //$NON-NLS-1$
	
	// selection
	public static final String SELECT_LINE_UP= "org.eclipse.ui.edit.text.select.lineUp"; //$NON-NLS-1$
	public static final String SELECT_LINE_DOWN= "org.eclipse.ui.edit.text.select.lineDown"; //$NON-NLS-1$
	public static final String SELECT_LINE_START= "org.eclipse.ui.edit.text.select.lineStart"; //$NON-NLS-1$
	public static final String SELECT_LINE_END= "org.eclipse.ui.edit.text.select.lineEnd"; //$NON-NLS-1$
	public static final String SELECT_COLUMN_PREVIOUS= "org.eclipse.ui.edit.text.select.columnPrevious"; //$NON-NLS-1$
	public static final String SELECT_COLUMN_NEXT= "org.eclipse.ui.edit.text.select.columnNext"; //$NON-NLS-1$
	public static final String SELECT_PAGE_UP= "org.eclipse.ui.edit.text.select.pageUp"; //$NON-NLS-1$
	public static final String SELECT_PAGE_DOWN= "org.eclipse.ui.edit.text.select.pageDown"; //$NON-NLS-1$
	public static final String SELECT_WORD_PREVIOUS= "org.eclipse.ui.edit.text.select.wordPrevious"; //$NON-NLS-1$
	public static final String SELECT_WORD_NEXT= "org.eclipse.ui.edit.text.select.wordNext"; //$NON-NLS-1$
	public static final String SELECT_TEXT_START= "org.eclipse.ui.edit.text.select.textStart"; //$NON-NLS-1$
	public static final String SELECT_TEXT_END= "org.eclipse.ui.edit.text.select.textEnd"; //$NON-NLS-1$
	public static final String SELECT_WINDOW_START= "org.eclipse.ui.edit.text.select.windowStart"; //$NON-NLS-1$
	public static final String SELECT_WINDOW_END= "org.eclipse.ui.edit.text.select.windowEnd"; //$NON-NLS-1$
	
	// modification
	public static final String DELETE_PREVIOUS= "org.eclipse.ui.edit.text.deletePrevious"; //$NON-NLS-1$
	public static final String DELETE_NEXT= "org.eclipse.ui.edit.text.deleteNext"; //$NON-NLS-1$
	public static final String SHIFT_RIGHT= "org.eclipse.ui.edit.text.shiftRight"; //$NON-NLS-1$
	public static final String SHIFT_LEFT= "org.eclipse.ui.edit.text.shiftLeft"; //$NON-NLS-1$
	public static final String CONVERT_LINE_DELIMITERS_TO_DOS= "org.eclipse.ui.edit.text.convert.lineDelimiters.toDOS"; //$NON-NLS-1$
	public static final String CONVERT_LINE_DELIMITERS_TO_UNIX= "org.eclipse.ui.edit.text.convert.lineDelimiters.toUNIX"; //$NON-NLS-1$
	public static final String CONVERT_LINE_DELIMITERS_TO_MAC= "org.eclipse.ui.edit.text.convert.lineDelimiters.toMac"; //$NON-NLS-1$
	
	// miscellaneous
	public static final String TOGGLE_OVERWRITE= "org.eclipse.ui.edit.text.toggleOverwrite"; //$NON-NLS-1$
}
