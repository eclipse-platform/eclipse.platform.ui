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
public class PreferencesSettingsTransfer extends WorkbenchSettingsTransfer{

	@Override
	public IStatus transferSettings(IPath newWorkspaceRoot) {
		File srcFolder = new File(getOldPath().toOSString());
		File destFolder = new File(getNewPath(newWorkspaceRoot).toOSString());

		if (!destFolder.exists()) {
			destFolder.mkdirs();
		}

		if (srcFolder.isDirectory()) {
			for (String file : srcFolder.list()) {
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
		FileInputStream fis = new FileInputStream(src);
		FileOutputStream fos = new FileOutputStream(dest);

		byte[] buffer = new byte[1024];

		int length;
		while ((length = fis.read(buffer)) > 0) {
			fos.write(buffer, 0, length);
		}

		fis.close();
		fos.close();
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