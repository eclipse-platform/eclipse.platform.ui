package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.ui.IWorkbenchActionConstants;
 

/**
 * Defines the names of those actions which are preregistered with the
 * <code>AbstractTextEditor</code>. <code>RULER_DOUBLE_CLICK</code> defines
 * the action which is registered as being executed when the editor's
 * ruler has been double clicked. This interface extends the set of names 
 * available from <code>IWorkbenchActionConstants</code>. It also defines the
 * names of the menu groups in a text editor's context menu.
 */
public interface ITextEditorActionConstants extends IWorkbenchActionConstants {
	
	/** 
	 * Context menu group for undo/redo related actions. 
	 */
	static final String GROUP_UNDO= "group.undo";
	/** 
	 * Context menu group for copy/paste related actions. 
	 */
	static final String GROUP_COPY= "group.copy";
	/** 
	 * Context menu group for text manipulation actions. 
	 */
	static final String GROUP_EDIT= "group.edit";
	/** 
	 * Context menu group for find/replace related actions. 
	 */
	static final String GROUP_FIND= "group.find";
	/** 
	 * Context menu group for save related actions. 
	 */
	static final String GROUP_SAVE= "group.save";
	/** 
	 * Context menu group for actions which do not fit
	 * in one of the other categories. 
	 */
	static final String GROUP_REST= "group.rest";
	
	
	
	/** 
	 * Name of the action for shifting text blocks to the right. 
	 */
	static final String SHIFT_RIGHT= "ShiftRight";
	/** 
	 * Name of the action for shifting text blocks to the left. 
	 */
	static final String SHIFT_LEFT= "ShiftLeft";
	/** 
	 * Name of the action for creating tasks based on the text selection. 
	 */
	static final String ADD_TASK= "AddTask";
	/** 
	 * Name of the action for re-establishing the state after the 
	 * most recent save operation. 
	 */
	static final String REVERT_TO_SAVED= "Revert";
	/** 
	 * Name of the action to jump to a certain text line. 
	 */
	static final String GOTO_LINE= "GotoLine";
	
	
	/** 
	 * Name of the ruler action performed when double clicking the editor's vertical ruler. 
	 */
	static final String RULER_DOUBLE_CLICK= "RulerDoubleClick";
	/** 
	 * Name of the ruler action to manage tasks.
	 */
	static final String RULER_MANAGE_TASKS= "ManageTasks";
	/** 
	 * Name of the ruler action to manage bookmarks. 
	 */
	static final String RULER_MANAGE_BOOKMARKS= "ManageBookmarks";
}
