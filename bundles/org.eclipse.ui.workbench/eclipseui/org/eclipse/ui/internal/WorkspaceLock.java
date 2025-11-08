/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation.
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

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

/**
 * Utility class for reading and presenting workspace lock information.
 *
 * <p>
 * This class is used during two different phases of the Eclipse application
 * lifecycle:
 * </p>
 * <ul>
 * <li>before the Workbench is created (no workbench windows exist, such as when
 * the Workspace Launcher dialog is displayed)</li>
 * <li>after the Workbench has been created and workbench windows are
 * available</li>
 * </ul>
 *
 * <p>
 * To support both environments, this class does not rely on workbench-specific
 * APIs such as {@code PlatformUI.getWorkbench()} or {@code IWorkbenchWindow},
 * nor on any API that requires an initialized workbench window. Only SWT-level
 * constructs (for example, {@link org.eclipse.swt.widgets.Display} and
 * {@link org.eclipse.swt.widgets.Shell}) and core/runtime APIs are used.
 * </p>
 *
 */
public class WorkspaceLock {

	public static final String PROCESS_ID = "process-id"; //$NON-NLS-1$

	public static final String DISPLAY = "display"; //$NON-NLS-1$

	public static final String HOST = "host"; //$NON-NLS-1$

	public static final String USER = "user"; //$NON-NLS-1$

	/**
	 * Read workspace lock file of the selected workspace if it is locked by another
	 * Eclipse application and parse all the properties present. Based on the
	 * Eclipse version and operating system some or all the properties may be not
	 * present. In such scenario empty string may be returned.
	 *
	 * @param workspaceUrl the <code>URL</code> of workspace to check for lock
	 *                     details
	 * @return <code>null</code> if workspace is not locked, empty string if
	 *         workspace is locked but no details are available (or lock file is not
	 *         accessible), or a formatted string with lock details
	 */
	public static String getWorkspaceLockDetails(URL workspaceUrl) {
		Path lockFile = getLockInfoFile(workspaceUrl);
		if (lockFile == null || !Files.exists(lockFile)) {
			return null;
		}
		StringBuilder lockDetails = new StringBuilder();
		try (InputStream is = Files.newInputStream(lockFile)) {
			Properties properties = new Properties();
			properties.load(is);
			String prop = properties.getProperty(USER);
			if (prop != null) {
				lockDetails.append(NLS.bind(WorkbenchMessages.IDEApplication_workspaceLockOwner, prop));
			}
			prop = properties.getProperty(HOST);
			if (prop != null) {
				lockDetails.append(NLS.bind(WorkbenchMessages.IDEApplication_workspaceLockHost, prop));
			}
			prop = properties.getProperty(DISPLAY);
			if (prop != null) {
				lockDetails.append(NLS.bind(WorkbenchMessages.IDEApplication_workspaceLockDisplay, prop));
			}
			prop = properties.getProperty(PROCESS_ID);
			if (prop != null) {
				lockDetails.append(NLS.bind(WorkbenchMessages.IDEApplication_workspaceLockPID, prop));
			}
		} catch (Exception e) {
			WorkbenchPlugin.log("Could not read lock info file: " + lockFile, e); //$NON-NLS-1$
		}
		return lockDetails.toString();
	}

	/**
	 * Returns the lock file.
	 *
	 * @param workspaceUrl the <code>URL</code> of selected workspace
	 * @return the path to the <code>.lock_info</code> file within the specified
	 *         workspace, or <code> null</code> if the workspace URL cannot be
	 *         converted to a valid URI
	 */
	public static Path getLockInfoFile(URL workspaceUrl) {
		Path lockFile = Path.of(".metadata", ".lock_info"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			return Path.of(URIUtil.toURI(workspaceUrl)).resolve(lockFile);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * Opens an error dialog indicating that the selected workspace is locked by
	 * another Eclipse instance.
	 * <p>
	 * This method works in both early startup (before the Workbench is created) and
	 * in normal runtime (after Workbench windows exist).
	 * </p>
	 *
	 * @param shell              the parent {@link Shell} for the dialog, or
	 *                           {@code null} if no workbench window is available
	 * @param workspacePath      the absolute path of the workspace that could not
	 *                           be locked
	 * @param workspaceLockOwner a formatted description of the existing lock owner
	 */
	public static void showWorkspaceLockedDialog(Shell shell, String workspacePath, String workspaceLockOwner) {
		String lockMessage = NLS.bind(WorkbenchMessages.IDEApplication_workspaceCannotLockMessage2, workspacePath);
		String wsLockedError = lockMessage + System.lineSeparator() + System.lineSeparator()
				+ NLS.bind(WorkbenchMessages.IDEApplication_workspaceLockMessage, workspaceLockOwner);

		MessageDialog.openError(shell,
				WorkbenchMessages.IDEApplication_workspaceCannotLockTitle, wsLockedError);
	}

}
