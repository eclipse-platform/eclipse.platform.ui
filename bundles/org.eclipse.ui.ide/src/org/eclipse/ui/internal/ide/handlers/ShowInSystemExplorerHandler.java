/*******************************************************************************
 * Copyright (c) 2013, 2016 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474273
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 487772, 486777
 ******************************************************************************/

package org.eclipse.ui.internal.ide.handlers;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.filesystem.ZipFileUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEPreferenceInitializer;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;


/**
 * @since 3.106
 */
public class ShowInSystemExplorerHandler extends AbstractHandler {

	/**
	 * Command id
	 */
	public static final String ID = "org.eclipse.ui.ide.showInSystemExplorer"; //$NON-NLS-1$

	/**
	 * Parameter, which can optionally be passed to the command.
	 */
	public static final String RESOURCE_PATH_PARAMETER = "org.eclipse.ui.ide.showInSystemExplorer.path"; //$NON-NLS-1$

	private static final String VARIABLE_RESOURCE = "${selected_resource_loc}"; //$NON-NLS-1$
	private static final String VARIABLE_RESOURCE_URI = "${selected_resource_uri}"; //$NON-NLS-1$
	private static final String VARIABLE_FOLDER = "${selected_resource_parent_loc}"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {

		final IResource item = getResource(event);
		if (item == null) {
			return null;
		}

		final StatusReporter statusReporter = HandlerUtil.getActiveWorkbenchWindow(event).getService(
				StatusReporter.class);

		Job job = Job.create(IDEWorkbenchMessages.ShowInSystemExplorerHandler_jobTitle, monitor -> {
			String logMsgPrefix;
			try {
				logMsgPrefix = event.getCommand().getName() + ": "; //$NON-NLS-1$
			} catch (NotDefinedException e1) {
				// will used id instead...
				logMsgPrefix = event.getCommand().getId() + ": "; //$NON-NLS-1$
			}

			try {
				File canonicalPath = getSystemExplorerPath(item);

				if (item instanceof IFile file && ZipFileUtil.isInsideOpenZipFile(file.getLocationURI())) {
					return statusReporter.newStatus(IStatus.ERROR,
							logMsgPrefix + IDEWorkbenchMessages.ShowInSystemExplorerHandler_insideOfZipFile, null);
				}

				if (canonicalPath == null) {
					return statusReporter.newStatus(IStatus.ERROR, logMsgPrefix
							+ IDEWorkbenchMessages.ShowInSystemExplorerHandler_notDetermineLocation, null);
				}
				String launchCmd = formShowInSytemExplorerCommand(canonicalPath);

				if ("".equals(launchCmd)) { //$NON-NLS-1$
					return statusReporter.newStatus(IStatus.ERROR, logMsgPrefix
							+ IDEWorkbenchMessages.ShowInSystemExplorerHandler_commandUnavailable, null);
				}

				File dir = item.getWorkspace().getRoot().getLocation().toFile();
				Process p;
				if (Util.isLinux() || Util.isMac()) {
					p = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", launchCmd }, null, dir); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					p = Runtime.getRuntime().exec(launchCmd, null, dir);
				}
				int retCode = p.waitFor();
				if (retCode != 0 && !Util.isWindows()) {
					return statusReporter.newStatus(IStatus.ERROR, "Execution of '" + launchCmd //$NON-NLS-1$
							+ "' failed with return code: " + retCode, null); //$NON-NLS-1$
				}
			} catch (IOException | InterruptedException e2) {
				return statusReporter.newStatus(IStatus.ERROR, logMsgPrefix + "Unhandled failure.", e2); //$NON-NLS-1$
			}
			return Status.OK_STATUS;
		});
		job.schedule();
		return null;
	}

	private IResource getResource(ExecutionEvent event) {
		IResource resource = getResourceByParameter(event);
		if (resource == null) {
			resource = getSelectionResource(event);
		}
		if (resource == null) {
			resource = getEditorInputResource(event);
		}
		return resource;
	}

	private IResource getResourceByParameter(ExecutionEvent event) {
		String parameter = event.getParameter(RESOURCE_PATH_PARAMETER);
		if (parameter == null) {
			return null;
		}
		IPath path = IPath.fromOSString(parameter);
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
	}

	private IResource getSelectionResource(ExecutionEvent event) {
		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		if ((selection == null) || (selection.isEmpty())) {
			return null;
		}

		Object selectedObject = selection
				.getFirstElement();
		return Adapters.adapt(selectedObject, IResource.class);
	}

	private IResource getEditorInputResource(ExecutionEvent event) {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (!(activePart instanceof IEditorPart)) {
			return null;
		}
		IEditorInput input = ((IEditorPart)activePart).getEditorInput();
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput)input).getFile();
		}
		return Adapters.adapt(input, IResource.class);
	}

	/**
	 * Prepare command for launching system explorer to show a path
	 *
	 * @param path
	 *            the path to show
	 * @return the command that shows the path
	 */
	private String formShowInSytemExplorerCommand(File path) throws IOException {
		String command = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getString(IDEInternalPreferences.WORKBENCH_SYSTEM_EXPLORER);

		command = Util.replaceAll(command, VARIABLE_RESOURCE, quotePath(path.getCanonicalPath()));
		command = Util.replaceAll(command, VARIABLE_RESOURCE_URI, path.getCanonicalFile().toURI().toString());
		File parent = path.getParentFile();
		if (parent != null) {
			command = Util.replaceAll(command, VARIABLE_FOLDER, quotePath(parent.getCanonicalPath()));
		}
		return command;
	}

	private String quotePath(String path) {
		if (Util.isLinux() || Util.isMac()) {
			// Quote for usage inside "", man sh, topic QUOTING:
			path = path.replaceAll("[\"$`]", "\\\\$0"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// Windows: Can't quote, since explorer.exe has a very special command line parsing strategy.
		return path;
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
	private File getSystemExplorerPath(IResource resource) throws IOException {
		IPath location = resource.getLocation();
		if (location == null)
			return null;
		return location.toFile();
	}

	/**
	 * The default command for launching the system explorer on this platform.
	 *
	 * @return The default command which launches the system explorer on this system, or an empty
	 *         string if no default exists
	 */
	public static String getDefaultCommand() {
		// See https://bugs.eclipse.org/419940 why it is implemented in IDEPreferenceInitializer
		return IDEPreferenceInitializer.getShowInSystemExplorerCommand();
	}
}
