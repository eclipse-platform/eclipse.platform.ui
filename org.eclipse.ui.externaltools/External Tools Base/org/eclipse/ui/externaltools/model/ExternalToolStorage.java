package org.eclipse.ui.externaltools.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolRegistry;

/**
 * Provides utility methods to manager the storage
 * of external tools.
 */
public final class ExternalToolStorage {
	private static ListenerList listeners = new ListenerList();

	/**
	 * Allows no instance to be created
	 */
	private ExternalToolStorage() {
		super();
	}
	
	/**
	 * Adds a tool storage listener.
	 * 
	 * @param listener the tool storage listener to add
	 */
	public static void addStorageListener(IStorageListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Deletes the external tool from storage and from
	 * the tools registry.
	 * 
	 * @param tool the external tool to be deleted
	 * @param shell the shell to parent any error dialogs,
	 * 		of <code>null</code> to operate quietly.
	 */
	public static void deleteTool(ExternalTool tool, Shell shell) {
		if (tool == null)
			return;
		
		ExternalToolRegistry registry = ExternalToolsPlugin.getDefault().getToolRegistry(shell);
		IStatus results = registry.deleteTool(tool);
		
		if (handleResults(results, shell, "ExternalToolStorage.deleteErrorTitle", "ExternalToolStorage.deleteErrorMessage")) //$NON-NLS-2$//$NON-NLS-1$
			return;

		Object list[] = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((IStorageListener)list[i]).toolDeleted(tool);
		}
	}

	/**
	 * Handles the display of any error message if the
	 * results were not ok.
	 * 
	 * @return <code>true</code> if an error was displayed, <code>false</code> otherwise
	 */
	private static boolean handleResults(final IStatus results, final Shell shell, final String titleKey, final String msgKey) {
		if (!results.isOK() && shell != null && !shell.isDisposed()) {
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					String title = ToolMessages.getString(titleKey);
					String msg = ToolMessages.getString(msgKey);
					ErrorDialog.openError(shell, title, msg, results);
				}
			});
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Reloads the tools from storage into the registry.
	 * 
	 * @param shell the shell to parent any error dialogs,
	 * 		of <code>null</code> to operate quietly.
	 */
	public static void refreshTools(final Shell shell) {
		final IStatus[] results = new IStatus[1];
		
		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				results[0] = ExternalToolsPlugin.getDefault().getToolRegistry(shell).reloadTools();
			}
		});

		if (handleResults(results[0], shell, "ExternalToolStorage.reloadErrorTitle", "ExternalToolStorage.reloadErrorMessage")) //$NON-NLS-2$//$NON-NLS-1$
			return;

		Object list[] = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((IStorageListener)list[i]).toolsRefreshed();
		}
	}
	
	/**
	 * Removes a tool storage listener.
	 * 
	 * @param listener the tool storage listener to remove
	 */
	public static void removeStorageListener(IStorageListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Saves the external tool to storage and to
	 * the tools registry.
	 * 
	 * @param tool the external tool to be saved
	 * @param shell the shell to parent any error dialogs,
	 * 		of <code>null</code> to operate quietly.
	 * @return <code>true</code> if save successful, <code>false</code> otherwise.
	 */
	public static boolean saveTool(ExternalTool tool, Shell shell) {
		if (tool == null)
			return false;
		
		ExternalToolRegistry registry = ExternalToolsPlugin.getDefault().getToolRegistry(shell);
		boolean exists = registry.hasToolNamed(tool.getName());
		
		IStatus results = registry.saveTool(tool);
		if (handleResults(results, shell, "ExternalToolStorage.saveErrorTitle", "ExternalToolStorage.saveErrorMessage")) //$NON-NLS-2$//$NON-NLS-1$
			return false;

		Object list[] = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			if (exists)
				((IStorageListener)list[i]).toolModified(tool);
			else
				((IStorageListener)list[i]).toolCreated(tool);
		}
		
		return true;
	}
}
