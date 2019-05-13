/*******************************************************************************
 * Copyright (c) 2017, 2018 David Weiser and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     David Weiser - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * The PreferenceSettingsTransfer is the settings transfer for the workbench
 * preferences.
 *
 * @since 3.110
 *
 */
public class PreferencesSettingsTransfer extends WorkbenchSettingsTransfer {

	@Override
	public IStatus transferSettings(IPath newWorkspaceRoot) {
		File srcFolder = new File(getOldPath().toOSString());
		File destFolder = new File(getNewPath(newWorkspaceRoot).toOSString());

		if (!destFolder.exists()) {
			destFolder.mkdirs();
		}

		if (srcFolder.isDirectory()) {
			String[] files = srcFolder.list();
			if (files == null) {
				return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID,
						"Content from directory '" + srcFolder.getAbsolutePath() + "' can not be listed."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			for (String file : files) {
				File srcFile = new File(srcFolder.getPath().toString(), file);
				File destFile = new File(destFolder.getPath().toString(), file);

				try {
					copyFiles(srcFile, destFile);
				} catch (IOException e) {
					return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage());
				}
			}
		}
		return Status.OK_STATUS;
	}

	private void copyFiles(File src, File dest) throws IOException {

		try (FileOutputStream fos = new FileOutputStream(dest); FileInputStream fis = new FileInputStream(src)) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, length);
			}
		}
	}

	@Override
	public String getName() {
		return WorkbenchMessages.WorkbenchPreferences_Name;
	}

	private IPath getNewPath(IPath newWorkspaceRoot) {
		return newWorkspaceRoot.append(new Path(".metadata/.plugins/org.eclipse.core.runtime/.settings")); //$NON-NLS-1$
	}

	private IPath getOldPath() {
		return Platform.getLocation().append(new Path(".metadata/.plugins/org.eclipse.core.runtime/.settings")); //$NON-NLS-1$
	}
}