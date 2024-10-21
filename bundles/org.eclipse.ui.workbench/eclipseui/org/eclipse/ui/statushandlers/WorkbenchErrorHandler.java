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

package org.eclipse.ui.statushandlers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.statushandlers.IStatusDialogConstants;
import org.eclipse.ui.statushandlers.StatusManager.INotificationTypes;

/**
 * This is a default workbench error handler.
 *
 * @see WorkbenchAdvisor#getWorkbenchErrorHandler()
 * @since 3.3
 */
public class WorkbenchErrorHandler extends AbstractStatusHandler {

	@Override
	public boolean supportsNotification(int type) {
		if (type == INotificationTypes.HANDLED) {
			return true;
		}
		return super.supportsNotification(type);
	}

	private WorkbenchStatusDialogManager statusDialogManager;

	@Override
	public void handle(final StatusAdapter statusAdapter, int style) {
		statusAdapter.setProperty(WorkbenchStatusDialogManager.HINT, Integer.valueOf(style));
		if (((style & StatusManager.SHOW) == StatusManager.SHOW)
				|| ((style & StatusManager.BLOCK) == StatusManager.BLOCK)) {

			final boolean block = ((style & StatusManager.BLOCK) == StatusManager.BLOCK);

			Runnable uiRunnable = () -> {
				if (!PlatformUI.isWorkbenchRunning()) {
					// we are shutting down, so just log (if logging is not already requested)
					if ((style & StatusManager.LOG) == 0) {
						log(statusAdapter);
					}
					return;
				}
				showStatusAdapter(statusAdapter, block);
			};

			if (Display.getCurrent() != null) {
				uiRunnable.run();
			} else {
				try {
					// Checking isDisposed() here isn't enough due to race conditions
					// since we are in a worker thread.
					PlatformUI.getWorkbench().getDisplay().asyncExec(uiRunnable);
				} catch (SWTException e) {
					if (e.code == SWT.ERROR_DEVICE_DISPOSED) {
						// platform is being shut down, just log
						log(statusAdapter);
						return;
					}
					throw e;
				}
			}
		}

		if ((style & StatusManager.LOG) == StatusManager.LOG) {
			StatusManager.getManager().addLoggedStatus(statusAdapter.getStatus());
			log(statusAdapter);
		}
	}

	/**
	 * Requests the status dialog manager to show the status adapter.
	 *
	 * @param statusAdapter the status adapter to show
	 * @param block         <code>true</code> to request a modal dialog and suspend
	 *                      the calling thread till the dialog is closed,
	 *                      <code>false</code> otherwise.
	 */
	private void showStatusAdapter(StatusAdapter statusAdapter, boolean block) {
		getStatusDialogManager().addStatusAdapter(statusAdapter, block);

		if (block) {
			Shell shell;
			while ((shell = getStatusDialogShell()) != null && !shell.isDisposed()) {
				if (!shell.getDisplay().readAndDispatch()) {
					shell.getDisplay().sleep();
				}
			}
		}
	}

	private static void log(StatusAdapter statusAdapter) {
		WorkbenchPlugin.log(statusAdapter.getStatus());
	}

	private Shell getStatusDialogShell() {
		return (Shell) getStatusDialogManager().getProperty(IStatusDialogConstants.SHELL);
	}

	/**
	 * This method returns current {@link WorkbenchStatusDialogManager}.
	 *
	 * @return current {@link WorkbenchStatusDialogManager}
	 */
	private synchronized WorkbenchStatusDialogManager getStatusDialogManager() {
		if (statusDialogManager == null) {
			statusDialogManager = new WorkbenchStatusDialogManager(null);
			statusDialogManager.setProperty(IStatusDialogConstants.SHOW_SUPPORT, Boolean.TRUE);
			statusDialogManager.setProperty(IStatusDialogConstants.HANDLE_OK_STATUSES, Boolean.TRUE);
			statusDialogManager.setProperty(IStatusDialogConstants.ERRORLOG_LINK, Boolean.TRUE);
			configureStatusDialog(statusDialogManager);
		}
		return statusDialogManager;
	}

	/**
	 * This methods should be overridden to configure
	 * {@link WorkbenchStatusDialogManager} behavior. It is advised to use only
	 * following methods of {@link WorkbenchStatusDialogManager}:
	 * <ul>
	 * <li>{@link WorkbenchStatusDialogManager#enableDefaultSupportArea(boolean)}</li>
	 * <li>{@link WorkbenchStatusDialogManager#setDetailsAreaProvider(AbstractStatusAreaProvider)}</li>
	 * <li>{@link WorkbenchStatusDialogManager#setSupportAreaProvider(AbstractStatusAreaProvider)}</li>
	 * </ul>
	 * Default configuration does nothing.
	 *
	 * @param statusDialog a status dialog to be configured.
	 * @since 3.4
	 */
	protected void configureStatusDialog(final WorkbenchStatusDialogManager statusDialog) {
		// default configuration does nothing
	}
}
