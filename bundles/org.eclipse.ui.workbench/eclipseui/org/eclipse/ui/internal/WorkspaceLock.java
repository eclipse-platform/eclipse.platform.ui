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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.e4.ui.workbench.swt.internal.copy.WorkbenchSWTMessages;
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
 * @since 3.5
 */
public class WorkspaceLock {

	public static final String PROCESS_ID = "process-id"; //$NON-NLS-1$

	public static final String DISPLAY = "display"; //$NON-NLS-1$

	public static final String HOST = "host"; //$NON-NLS-1$

	public static final String USER = "user"; //$NON-NLS-1$

	/**
	 * Extract the lock details of the selected workspace if it is locked by another
	 * Eclipse application
	 *
	 * @param workspaceUrl the <code>URL</code> of selected workspace
	 * @return <code>String</code> details of lock owned workspace,
	 *         <code>null or Empty</code> if not locked
	 */
	@SuppressWarnings("restriction")
	public static String getWorkspaceLockDetails(URL workspaceUrl) {
		Path lockFile = getLockInfoFile(workspaceUrl);
		if (lockFile != null && Files.exists(lockFile)) {
			StringBuilder lockDetails = new StringBuilder();
			Properties properties = new Properties();
			try (InputStream is = Files.newInputStream(lockFile)) {
				properties.load(is);
				String prop = properties.getProperty(USER);
				if (prop != null) {
					lockDetails.append(NLS.bind(WorkbenchSWTMessages.IDEApplication_workspaceLockOwner, prop));
				}
				prop = properties.getProperty(HOST);
				if (prop != null) {
					lockDetails.append(NLS.bind(WorkbenchSWTMessages.IDEApplication_workspaceLockHost, prop));
				}
				prop = properties.getProperty(DISPLAY);
				if (prop != null) {
					lockDetails.append(NLS.bind(WorkbenchSWTMessages.IDEApplication_workspaceLockDisplay, prop));
				}
				prop = properties.getProperty(PROCESS_ID);
				if (prop != null) {
					lockDetails.append(NLS.bind(WorkbenchSWTMessages.IDEApplication_workspaceLockPID, prop));
				}

			} catch (IOException e) {
				WorkbenchPlugin.log(e);
			}
			return lockDetails.toString();
		}
		return null;
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
	@SuppressWarnings("restriction")
	public static void showWorkspaceLockedDialog(Shell shell, String workspacePath, String workspaceLockOwner) {
		String lockMessage = NLS.bind(WorkbenchSWTMessages.IDEApplication_workspaceCannotLockMessage2, workspacePath);
		String wsLockedError = lockMessage + System.lineSeparator() + System.lineSeparator()
				+ NLS.bind(WorkbenchSWTMessages.IDEApplication_workspaceLockMessage, workspaceLockOwner);

		MessageDialog.openError(shell,
				WorkbenchSWTMessages.IDEApplication_workspaceCannotLockTitle, wsLockedError);
	}

}
