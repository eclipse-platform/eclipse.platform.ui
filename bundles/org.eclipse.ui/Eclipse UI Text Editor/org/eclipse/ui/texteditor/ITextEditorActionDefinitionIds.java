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
public interface ITextEditorActionDefinitionIds {
	// navigation
	public static final String LINE_UP= "org.eclipse.edit.text.goto.line_up";
	public static final String LINE_DOWN= "org.eclipse.edit.text.goto.line_down";
	public static final String LINE_START= "org.eclipse.edit.text.goto.line_start";
	public static final String LINE_END= "org.eclipse.edit.text.goto.line_end";
	public static final String LINE_GOTO= "org.eclipse.edit.text.goto.line";
	public static final String COLUMN_PREVIOUS= "org.eclipse.edit.text.goto.column_previous";
	public static final String COLUMN_NEXT= "org.eclipse.edit.text.goto.column_next";
	public static final String PAGE_UP= "org.eclipse.edit.text.goto.page_up";
	public static final String PAGE_DOWN= "org.eclipse.edit.text.goto.page_down";
	public static final String WORD_PREVIOUS= "org.eclipse.edit.text.goto.word_previous";
	public static final String WORD_NEXT= "org.eclipse.edit.text.goto.word_next";
	public static final String TEXT_START= "org.eclipse.edit.text.goto.text_start";
	public static final String TEXT_END= "org.eclipse.edit.text.goto.text_end";
	public static final String WINDOW_START= "org.eclipse.edit.text.goto.window_start";
	public static final String WINDOW_END= "org.eclipse.edit.text.goto.window_end";
	// selection
	public static final String SELECT_LINE_UP= "org.eclipse.edit.text.select.line_up";
	public static final String SELECT_LINE_DOWN= "org.eclipse.edit.text.select.line_down";
	public static final String SELECT_LINE_START= "org.eclipse.edit.text.select.line_start";
	public static final String SELECT_LINE_END= "org.eclipse.edit.text.select.line_end";
	public static final String SELECT_COLUMN_PREVIOUS= "org.eclipse.edit.text.select.column_previous";
	public static final String SELECT_COLUMN_NEXT= "org.eclipse.edit.text.select.column_next";
	public static final String SELECT_PAGE_UP= "org.eclipse.edit.text.select.page_up";
	public static final String SELECT_PAGE_DOWN= "org.eclipse.edit.text.select.page_down";
	public static final String SELECT_WORD_PREVIOUS= "org.eclipse.edit.text.select.word_previous";
	public static final String SELECT_WORD_NEXT= "org.eclipse.edit.text.select.word_next";
	public static final String SELECT_TEXT_START= "org.eclipse.edit.text.select.text_start";
	public static final String SELECT_TEXT_END= "org.eclipse.edit.text.select.text_end";
	public static final String SELECT_WINDOW_START= "org.eclipse.edit.text.select.window_start";
	public static final String SELECT_WINDOW_END= "org.eclipse.edit.text.select.window_end";
	// modification
	public static final String DELETE_PREVIOUS= "org.eclipse.edit.text.delete_previous";
	public static final String DELETE_NEXT= "org.eclipse.edit.text.delete_next";
	public static final String SHIFT_RIGHT= "org.eclipse.edit.text.shift_right";
	public static final String SHIFT_LEFT= "org.eclipse.edit.text.shift_left";
	// miscellaneous
	public static final String TOGGLE_OVERWRITE= "org.eclipse.edit.text.toggle_overwrite";
	
	// workbench edit actions
	public static final String CUT= "org.eclipse.edit.cut";
	public static final String COPY= "org.eclipse.edit.copy";
	public static final String PASTE= "org.eclipse.edit.paste";
	public static final String UNDO= "org.eclipse.edit.undo";
	public static final String REDO= "org.eclipse.edit.redo";
	public static final String DELETE= "org.eclipse.edit.delete";
	public static final String SELECT_ALL= "org.eclipse.edit.select_all";
	public static final String PRINT= "org.eclipse.edit.print";
	public static final String FIND_REPLACE= "org.eclipse.edit.find_replace";
	public static final String FIND_NEXT= "org.eclipse.edit.find_next";
	public static final String FIND_PREVIOUS= "org.eclipse.edit.find_previous";
	public static final String FIND_INCREMENTAL= "org.eclipse.edit.find_incremental";
	public static final String REVERT_TO_SAVED= "org.eclipse.edit.revert_to_saved";
	public static final String ADD_BOOKMARK= "org.eclipse.edit.add_bookmark";
	public static final String ADD_TASK= "org.eclipse.edit.add_task";
	
	// workbench file actions
	public static final String SAVE= "org.eclipse.file.save";
}
