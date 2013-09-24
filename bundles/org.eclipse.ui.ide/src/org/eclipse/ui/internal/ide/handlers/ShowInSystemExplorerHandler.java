/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.handlers;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * @since 3.106
 */
public class ShowInSystemExplorerHandler extends AbstractHandler {

	/**
	 * Command id
	 */
	public static final String ID = "org.eclipse.ui.showIn.systemExplorer"; //$NON-NLS-1$

	private static final String VARIABLE_RE = "\\$\\{selected_resource_loc\\}"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ILog log = IDEWorkbenchPlugin.getDefault().getLog();

		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if ((selection == null) || (selection.isEmpty())
				|| (!(selection instanceof IStructuredSelection))) {
			return null;
		}

		Object selectedObject = ((IStructuredSelection) selection)
				.getFirstElement();
		IResource item = (IResource) org.eclipse.ui.internal.util.Util
				.getAdapter(selectedObject, IResource.class);
		if (item == null) {
			return null;
		}

		String logMsgPrefix;
		try {
			logMsgPrefix = event.getCommand().getName() + ": "; //$NON-NLS-1$
		} catch (NotDefinedException e) {
			// will used id instead...
			logMsgPrefix = event.getCommand().getId() + ": "; //$NON-NLS-1$
		}

		try {
			String canonicalPath = getSystemExplorerPath(item);
			if (canonicalPath == null) {
				ErrorDialog
						.openError(
								Display.getDefault().getActiveShell(),
								"Could not determine resource location", //$NON-NLS-1$
								"The system could not determine the selected resource's location.", //$NON-NLS-1$
								new Status(
										IStatus.ERROR,
										IDEWorkbenchPlugin.getDefault()
												.getBundle().getSymbolicName(),
										logMsgPrefix
												+ "Could not determine resource's location.")); //$NON-NLS-1$
				return null;
			}
			String launchCmd = formShowInSytemExplorerCommand(canonicalPath);

			if ("".equals(launchCmd)) { //$NON-NLS-1$
				ErrorDialog
						.openError(
								Display.getDefault().getActiveShell(),
								"System explorer is not set", //$NON-NLS-1$
								"Please set the system explorer command in the workbench preferences.", //$NON-NLS-1$
								new Status(IStatus.ERROR, IDEWorkbenchPlugin
										.getDefault().getBundle()
										.getSymbolicName(), logMsgPrefix
										+ "Command for launching is not set.")); //$NON-NLS-1$
				return null;
			}

			log.log(new Status(IStatus.INFO, IDEWorkbenchPlugin.getDefault()
					.getBundle().getSymbolicName(), launchCmd));

			Process p = Runtime.getRuntime().exec(launchCmd, null,
					item.getWorkspace().getRoot().getLocation().toFile());
			int retCode = p.waitFor();
			if (retCode != 0) {
				log.log(new Status(
						IStatus.ERROR,
						IDEWorkbenchPlugin.getDefault().getBundle()
								.getSymbolicName(),
						logMsgPrefix
								+ "Execution of launch command failed with return code: " + retCode)); //$NON-NLS-1$
			}
		} catch (Exception e) {
			log.log(new Status(IStatus.ERROR, IDEWorkbenchPlugin.getDefault()
					.getBundle().getSymbolicName(), logMsgPrefix
					+ "Unhandled failure.", e)); //$NON-NLS-1$
			throw new ExecutionException("Show in Explorer command failed.", e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Prepare command for launching system explorer to show a path
	 * 
	 * @param path
	 *            the path to show
	 * @return the command that shows the path
	 */
	private String formShowInSytemExplorerCommand(String path) {
		String command = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getString(IDEInternalPreferences.WORKBENCH_SYSTEM_EXPLORER);
		return command.replaceAll(VARIABLE_RE, path);
	}

	/**
	 * Returns the path used for a resource when showing it in the system
	 * explorer
	 * 
	 * @see File#getCanonicalPath()
	 * @param resource
	 *            the {@link IResource} object to be used
	 * @return the canonical path to show in the system explorer for this
	 *         resource, or null if it cannot be determined
	 * @throws IOException
	 *             if an I/O error occurs while trying to determine the path
	 */
	private String getSystemExplorerPath(IResource resource) throws IOException {
		IPath location = resource.getLocation();
		if (location == null)
			return null;
		File f = location.toFile();
		return f.getCanonicalPath();
	}

	/**
	 * The default command for launching the system explorer on this platform.
	 * 
	 * @return The default command which launches the system explorer on this
	 *         system, or an empty string if no default exists.
	 */
	public static String getDefaultCommand() {
		if (Util.isGtk()) {
			return IDEWorkbenchMessages.ShowInSystemExplorerHandler_LinuxDefaultCommand;
		} else if (Util.isWindows()) {
			return IDEWorkbenchMessages.ShowInSystemExplorerHandler_Win32DefaultCommand;
		} else if (Util.isMac()) {
			return IDEWorkbenchMessages.ShowInSystemExplorerHandler_MacOSXDefaultCommand;
		}

		// if all else fails, return empty default
		return ""; //$NON-NLS-1$
	}
}
