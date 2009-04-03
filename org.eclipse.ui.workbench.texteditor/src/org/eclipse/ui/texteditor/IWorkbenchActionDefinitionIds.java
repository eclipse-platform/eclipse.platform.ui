/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.ui.IWorkbenchCommandConstants;


/**
 * Defines the definitions ids for workbench actions.
 * <p>
 * This interface must not be implemented by clients.
 * </p>
 * 
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IWorkbenchActionDefinitionIds {

	// workbench file actions

	/**
	 * Action definition id of the file print action.
	 * Value: <code>"org.eclipse.ui.file.print"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#FILE_PRINT}
	 */
	public static final String PRINT= IWorkbenchCommandConstants.FILE_PRINT;

	/**
	 * Action definition id of the file save action.
	 * Value: <code>"org.eclipse.ui.file.save"</code>
	 * 
	 * @deprecated As of 3.5, no longer used
	 */
	public static final String SAVE= IWorkbenchCommandConstants.FILE_SAVE;

	/**
	 * Action definition id of the file revert action.
	 * Value: <code>"org.eclipse.ui.edit.revertToSaved"</code>
	 * 
	 * @deprecated As of 3.4, replaced by {@link #REVERT}
	 */
	public static final String REVERT_TO_SAVED= "org.eclipse.ui.edit.revertToSaved"; //$NON-NLS-1$

	/**
	 * Action definition id of the file revert action.
	 * Value: <code>"org.eclipse.ui.file.revert"</code>
	 * 
	 * @since 3.4
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#FILE_REVERT}
	 */
	public static final String REVERT= IWorkbenchCommandConstants.FILE_REVERT;

	/**
	 * Action definition id of the file properties action.
	 * Value: <code>"org.eclipse.ui.file.properties"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#FILE_PROPERTIES}
	 * @since 3.1
	 */
	public static final String PROPERTIES= IWorkbenchCommandConstants.FILE_PROPERTIES;


	// workbench edit actions

	/**
	 * Action definition id of the edit cut action.
	 * Value: <code>"org.eclipse.ui.edit.cut"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_CUT}
	 * 
	 */
	public static final String CUT= IWorkbenchCommandConstants.EDIT_CUT;

	/**
	 * Action definition id of the edit copy action.
	 * Value: <code>"org.eclipse.ui.edit.copy"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_COPY}
	 */
	public static final String COPY= IWorkbenchCommandConstants.EDIT_COPY;

	/**
	 * Action definition id of the edit past action.
	 * Value: <code>"org.eclipse.ui.edit.paste"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_PASTE}
	 */
	public static final String PASTE= IWorkbenchCommandConstants.EDIT_PASTE;

	/**
	 * Action definition id of the edit undo action.
	 * Value: <code>"org.eclipse.ui.edit.undo"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_UNDO}
	 */
	public static final String UNDO= IWorkbenchCommandConstants.EDIT_UNDO;

	/**
	 * Action definition id of the edit redo action.
	 * Value: <code>"org.eclipse.ui.edit.redo"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_REDO}
	 */
	public static final String REDO= IWorkbenchCommandConstants.EDIT_REDO;

	/**
	 * Action definition id of the edit delete action.
	 * Value: <code>"org.eclipse.ui.edit.delete"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_DELETE}
	 */
	public static final String DELETE= IWorkbenchCommandConstants.EDIT_DELETE;

	/**
	 * Action definition id of the edit select all action.
	 * Value: <code>"org.eclipse.ui.edit.selectAll"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_SELECT_ALL}
	 */
	public static final String SELECT_ALL= IWorkbenchCommandConstants.EDIT_SELECT_ALL;

	/**
	 * Action definition id of the edit find/replace action.
	 * Value: <code>"org.eclipse.ui.edit.findReplace"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_FIND_AND_REPLACE}
	 */
	public static final String FIND_REPLACE= IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE;

	/**
	 * Action definition id of the edit add bookmark action.
	 * Value: <code>"org.eclipse.ui.edit.addBookmark"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_ADD_BOOKMARK}
	 */
	public static final String ADD_BOOKMARK= IWorkbenchCommandConstants.EDIT_ADD_BOOKMARK;

	/**
	 * Action definition id of the edit add task action.
	 * Value: <code>"org.eclipse.ui.edit.addTask"</code>
	 * 
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#EDIT_ADD_TASK}
	 */
	public static final String ADD_TASK= IWorkbenchCommandConstants.EDIT_ADD_TASK;

	/**
	 * The command identifier for the "move" action that typically appears in the file menu.
	 * Value: <code>"org.eclipse.ui.edit.move"</code>
	 * 
	 * @since 3.0
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#FILE_MOVE}
	 */
	public static final String MOVE= IWorkbenchCommandConstants.FILE_MOVE;

	/**
	 * The command identifier for the "rename" action that typically appears in the file menu.
	 * Value: <code>"org.eclipse.ui.edit.rename"</code>
	 * 
	 * @since 3.0
	 * @deprecated As of 3.5, replaced by {@link IWorkbenchCommandConstants#FILE_RENAME}
	 */
	public static final String RENAME= IWorkbenchCommandConstants.FILE_RENAME;


	// future workbench edit actions

	/**
	 * Action definition id of the edit find next action.
	 * Value: <code>"org.eclipse.ui.edit.findNext"</code>
	 */
	public static final String FIND_NEXT= "org.eclipse.ui.edit.findNext"; //$NON-NLS-1$
	/**
	 * Action definition id of the edit find previous action.
	 * Value: <code>"org.eclipse.ui.edit.findPrevious"</code>
	 */
	public static final String FIND_PREVIOUS= "org.eclipse.ui.edit.findPrevious"; //$NON-NLS-1$
	/**
	 * Action definition id of the edit incremental find action.
	 * Value: <code>"org.eclipse.ui.edit.findIncremental"</code>
	 */
	public static final String FIND_INCREMENTAL= "org.eclipse.ui.edit.findIncremental"; //$NON-NLS-1$
	/**
	 * Action definition id of the edit incremental find reverse action.
	 * Value: <code>"org.eclipse.ui.edit.findIncrementalReverse"</code>
	 *
	 * @since 2.1
	 */
	public static final String FIND_INCREMENTAL_REVERSE= "org.eclipse.ui.edit.findIncrementalReverse"; //$NON-NLS-1$

}
