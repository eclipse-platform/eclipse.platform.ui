package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Defines the definitions ids for workbench actions. 
 * This is not yet API.
 */
public interface IWorkbenchActionDefinitionIds {
	
	// workbench file actions
	public static final String PRINT= "org.eclipse.ui.file.print";
	public static final String SAVE= "org.eclipse.file.save";
		
	// workbench edit actions
	public static final String CUT= "org.eclipse.ui.edit.cut";
	public static final String COPY= "org.eclipse.ui.edit.copy";
	public static final String PASTE= "org.eclipse.ui.edit.paste";
	public static final String UNDO= "org.eclipse.ui.edit.undo";
	public static final String REDO= "org.eclipse.ui.edit.redo";
	public static final String DELETE= "org.eclipse.ui.edit.delete";
	public static final String SELECT_ALL= "org.eclipse.ui.edit.selectAll";
	public static final String FIND_REPLACE= "org.eclipse.ui.edit.findReplace";
	public static final String ADD_BOOKMARK= "org.eclipse.ui.edit.addBookmark";
	
	// future workbench edit actions
	public static final String FIND_NEXT= "org.eclipse.ui.edit.findNext";
	public static final String FIND_PREVIOUS= "org.eclipse.ui.edit.findPrevious";
	public static final String FIND_INCREMENTAL= "org.eclipse.ui.edit.findIncremental";
	public static final String REVERT_TO_SAVED= "org.eclipse.ui.edit.revertToSaved";
	public static final String ADD_TASK= "org.eclipse.ui.edit.addTask";
}
