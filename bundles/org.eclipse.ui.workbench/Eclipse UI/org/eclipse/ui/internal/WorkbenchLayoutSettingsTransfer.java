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

package org.eclipse.ui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.preferences.WorkbenchSettingsTransfer;

/**
 * The WorkbenchSettings handles the recording and restoring of workbench
 * settings.
 *
 * @since 3.3
 */
public class WorkbenchLayoutSettingsTransfer extends WorkbenchSettingsTransfer {

	/**
	 * Create a new instance of the receiver.
	 */
	public WorkbenchLayoutSettingsTransfer() {
		super();
	}

	@Override
	public IStatus transferSettings(IPath newWorkspaceRoot) {
		try {
			IPath currentLocation = getNewWorkbenchStateLocation(Platform.getLocation());
			File workspaceFile = createFileAndDirectories(newWorkspaceRoot);

			if (workspaceFile == null)
				return new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
						WorkbenchMessages.WorkbenchSettings_CouldNotCreateDirectories);

			File deltas = new File(currentLocation.toOSString(), "deltas.xml"); //$NON-NLS-1$
			if (deltas.exists()) {
				byte[] bytes = new byte[8192];
				try (FileInputStream inputStream = new FileInputStream(deltas);
						FileOutputStream outputStream = new FileOutputStream(new File(workspaceFile, "deltas.xml")) //$NON-NLS-1$
				) {
					int read = inputStream.read(bytes, 0, 8192);
					while (read != -1) {
						outputStream.write(bytes, 0, read);
						read = inputStream.read(bytes, 0, 8192);
					}
				}
			}

			File workbenchModel = new File(currentLocation.toOSString(), "workbench.xmi"); //$NON-NLS-1$
			if (workbenchModel.exists()) {
				byte[] bytes = new byte[8192];
				try (FileInputStream inputStream = new FileInputStream(workbenchModel);
						FileOutputStream outputStream = new FileOutputStream(new File(workspaceFile, "workbench.xmi")) //$NON-NLS-1$
				) {
					int read = inputStream.read(bytes, 0, 8192);
					while (read != -1) {
						outputStream.write(bytes, 0, read);
						read = inputStream.read(bytes, 0, 8192);
					}
				}
			}
		} catch (IOException e) {
			return new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					WorkbenchMessages.Workbench_problemsSavingMsg, e);

		}

		return Status.OK_STATUS;
	}

	/**
	 * Create the parent directories for the workbench layout file and then return
	 * the File.
	 *
	 * @return File the new layout file. Return <code>null</code> if the file cannot
	 *         be created.
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

	@Override
	public String getName() {
		return WorkbenchMessages.WorkbenchLayoutSettings_Name;
	}

	/**
	 * Return the workbench settings location for the new root
	 *
	 * @return IPath or <code>null</code> if it can't be determined.
	 */
	@Override
	protected IPath getNewWorkbenchStateLocation(IPath newWorkspaceRoot) {
		return newWorkspaceRoot.append(IPath.fromOSString(".metadata/.plugins/org.eclipse.e4.workbench")); //$NON-NLS-1$
	}

}
