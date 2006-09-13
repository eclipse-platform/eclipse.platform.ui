/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide.undo;

import org.eclipse.osgi.util.NLS;

/**
 * UndoMessages is the class that handles the messages for performing workspace
 * undo and redo.
 * 
 */
public class UndoMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.ide.undo.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, UndoMessages.class);
	}

	public static String AbstractWorkspaceOperation_ExecuteErrorTitle;
	public static String AbstractWorkspaceOperation_RedoErrorTitle;
	public static String AbstractWorkspaceOperation_UndoErrorTitle;
	public static String AbstractWorkspaceOperation_SideEffectsWarningTitle;
	public static String AbstractWorkspaceOperation_ExecuteSideEffectsWarningMessage;
	public static String AbstractWorkspaceOperation_UndoSideEffectsWarningMessage;
	public static String AbstractWorkspaceOperation_RedoSideEffectsWarningMessage;
	public static String AbstractWorkspaceOperation_ErrorInvalidMessage;
	public static String AbstractWorkspaceOperation_GenericWarningMessage;

	public static String MarkerOperation_ResourceDoesNotExist;
	public static String MarkerOperation_MarkerDoesNotExist;
	public static String MarkerOperation_NotEnoughInfo;
	
	public static String RenameResourceOperation_SameName;
	public static String RenameResourceOperation_NameAlreadyExists;
	public static String RenameResourceOperation_ResourceDoesNotExist;
	public static String RenameResourceOperation_ReadOnly;
	public static String RenameResourceOperation_ResourceAlreadyExists;
}
