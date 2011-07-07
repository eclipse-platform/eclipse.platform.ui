/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.preferences.WorkbenchSettingsTransfer;

/**
 * The WorkbenchSettings handles the recording and restoring of workbench
 * settings.
 * 
 * @since 3.3
 * 
 */
public class WorkbenchLayoutSettingsTransfer extends WorkbenchSettingsTransfer {

	/**
	 * Create a new instance of the receiver.
	 */
	public WorkbenchLayoutSettingsTransfer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.SettingsTransfer#transferSettings(org.eclipse.core.runtime.IPath)
	 */
	public IStatus transferSettings(IPath newWorkspaceRoot) {
		try {
			IPath currentLocation = getNewWorkbenchStateLocation(Platform.getLocation());
			File workspaceFile = createFileAndDirectories(newWorkspaceRoot);

			if (workspaceFile == null)
				return new Status(
						IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH,
						WorkbenchMessages.WorkbenchSettings_CouldNotCreateDirectories);

			File deltas = new File(currentLocation.toOSString(), "deltas.xml"); //$NON-NLS-1$
			if (deltas.exists()) {
				byte[] bytes = new byte[8192];
				FileInputStream inputStream = new FileInputStream(deltas);
				FileOutputStream outputStream = new FileOutputStream(new File(workspaceFile,
						"deltas.xml")); //$NON-NLS-1$
				int read = inputStream.read(bytes, 0, 8192);
				while (read != -1) {
					outputStream.write(bytes, 0, read);
					read = inputStream.read(bytes, 0, 8192);
				}
				inputStream.close();
				outputStream.close();
			}

			File workbenchModel = new File(currentLocation.toOSString(), "workbench.xmi"); //$NON-NLS-1$
			if (workbenchModel.exists()) {
				byte[] bytes = new byte[8192];
				FileInputStream inputStream = new FileInputStream(workbenchModel);
				FileOutputStream outputStream = new FileOutputStream(new File(workspaceFile,
						"workbench.xmi")); //$NON-NLS-1$
				int read = inputStream.read(bytes, 0, 8192);
				while (read != -1) {
					outputStream.write(bytes, 0, read);
					read = inputStream.read(bytes, 0, 8192);
				}
				inputStream.close();
				outputStream.close();
			}
		} catch (IOException e) {
			return new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					WorkbenchMessages.Workbench_problemsSavingMsg, e);

		}

		return Status.OK_STATUS;
	}

	/**
	 * Create the parent directories for the workbench layout file and then
	 * return the File.
	 * 
	 * @param newWorkspaceRoot
	 * @return File the new layout file. Return <code>null</code> if the file
	 *         cannot be created.
	 */
	private File createFileAndDirectories(IPath newWorkspaceRoot) {
		IPath newWorkspaceLocation = getNewWorkbenchStateLocation(newWorkspaceRoot);
		File workspaceFile = new File(newWorkspaceLocation.toOSString());
		if (!workspaceFile.exists()) {
			if (!workspaceFile.mkdirs())
				return null;
		}

		return workspaceFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.SettingsTransfer#getName()
	 */
	public String getName() {
		return WorkbenchMessages.WorkbenchLayoutSettings_Name;
	}

	/**
	 * Return the workbench settings location for the new root
	 * 
	 * @param newWorkspaceRoot
	 * @return IPath or <code>null</code> if it can't be determined.
	 */
	protected IPath getNewWorkbenchStateLocation(IPath newWorkspaceRoot) {
		return newWorkspaceRoot.append(new Path(".metadata/.plugins/org.eclipse.e4.workbench")); //$NON-NLS-1$
	}

}
