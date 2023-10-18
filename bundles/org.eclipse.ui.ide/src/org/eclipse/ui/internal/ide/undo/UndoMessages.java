/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * @since 3.3
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

	public static String AbstractResourcesOperation_ResourcesDoNotExist;
	public static String AbstractResourcesOperation_ResourcesAlreadyExist;
	public static String AbstractResourcesOperation_NotEnoughInfo;
	public static String AbstractResourcesOperation_InvalidRestoreInfo;
	public static String AbstractResourcesOperation_DeleteResourcesProgress;
	public static String AbstractResourcesOperation_CreateResourcesProgress;
	public static String AbstractResourcesOperation_CopyingResourcesProgress;
	public static String AbstractResourcesOperation_MovingResources;
	public static String AbstractResourcesOperation_outOfSyncError;
	public static String AbstractResourcesOperation_outOfSyncQuestion;
	public static String AbstractResourcesOperation_deletionMessageTitle;
	public static String AbstractResourcesOperation_deletionExceptionMessage;

	public static String AbstractCopyOrMoveResourcesOperation_SameNameOrLocation;
	public static String AbstractCopyOrMoveResourcesOperation_ResourceDoesNotExist;
	public static String AbstractCopyOrMoveResourcesOperation_copyProjectProgress;
	public static String AbstractCopyOrMoveResourcesOperation_moveProjectProgress;

	public static String CopyResourcesOperation_NotAllowedDueToDataLoss;

	public static String MarkerOperation_ResourceDoesNotExist;
	public static String MarkerOperation_MarkerDoesNotExist;
	public static String MarkerOperation_NotEnoughInfo;
	public static String MarkerOperation_CreateProgress;
	public static String MarkerOperation_DeleteProgress;
	public static String MarkerOperation_UpdateProgress;
}
