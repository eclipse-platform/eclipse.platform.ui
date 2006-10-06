/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide.undo;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * WorkspaceUndoSupport defines common utility methods and constants used by
 * clients who create undoable workspace operations.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public class WorkspaceUndoSupport {

	private static ObjectUndoContext tasksUndoContext;

	private static ObjectUndoContext bookmarksUndoContext;

	/**
	 * Return the undo context that should be used for workspace-wide operations
	 * 
	 * @return the undo context suitable for workspace-level operations.
	 */
	public static IUndoContext getWorkspaceUndoContext() {
		return WorkbenchPlugin.getDefault().getOperationSupport()
				.getUndoContext();
	}

	/**
	 * Return the workspace.
	 * 
	 * @return the current workspace.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Return the workspace root.
	 * 
	 * @return the current workspace root.
	 */
	public static IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}

	/**
	 * Return the workspace rule factory.
	 * 
	 * @return the current workspace root.
	 */
	public static IResourceRuleFactory getWorkspaceRuleFactory() {
		return getWorkspace().getRuleFactory();
	}

	/**
	 * Return the undo context that should be used for operations involving
	 * tasks.
	 * 
	 * @return the tasks undo context
	 */
	public static IUndoContext getTasksUndoContext() {
		if (tasksUndoContext == null) {
			tasksUndoContext = new ObjectUndoContext(new Object(),
					"Tasks Context"); //$NON-NLS-1$
			tasksUndoContext.addMatch(getWorkspaceUndoContext());
		}
		return tasksUndoContext;
	}

	/**
	 * Return the undo context that should be used for operations involving
	 * bookmarks.
	 * 
	 * @return the bookmarks undo context
	 */
	public static IUndoContext getBookmarksUndoContext() {
		if (bookmarksUndoContext == null) {
			bookmarksUndoContext = new ObjectUndoContext(new Object(),
					"Bookmarks Context"); //$NON-NLS-1$
			bookmarksUndoContext.addMatch(getWorkspaceUndoContext());
		}
		return bookmarksUndoContext;
	}

	/**
	 * Make an <code>IAdaptable</code> that adapts to the specified shell,
	 * suitable for passing for passing to any {@link IUndoableOperation} or
	 * {@link IOperationHistory} method that requires an {@link IAdaptable}
	 * <code>uiInfo</code> parameter.
	 * 
	 * @param shell
	 *            the shell that should be returned by the IAdaptable when asked
	 *            to adapt a shell. If this parameter is <code>null</code>,
	 *            the returned shell will also be <code>null</code>.
	 * 
	 * @return an IAdaptable that will return the specified shell.
	 */
	public static IAdaptable getUiInfoAdapter(final Shell shell) {
		return new IAdaptable() {
			public Object getAdapter(Class clazz) {
				if (clazz == Shell.class) {
					return shell;
				}
				return null;
			}
		};
	}

	/**
	 * This class should never be constructed.
	 */
	private WorkspaceUndoSupport() {
		// Not allowed.
	}
}
