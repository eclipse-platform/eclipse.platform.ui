/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.statushandlers;

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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.statushandlers.AbstractStatusHandler#supportsNotification(int)
	 */
	public boolean supportsNotification(int type) {
		if (type == INotificationTypes.HANDLED) {
			return true;
		}
		return super.supportsNotification(type);
	}

	private WorkbenchStatusDialogManager statusDialogManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.statushandlers.AbstractStatusHandler#handle(org.eclipse.ui.statushandlers.StatusAdapter,
	 *      int)
	 */
	public void handle(final StatusAdapter statusAdapter, int style) {
		statusAdapter.setProperty(WorkbenchStatusDialogManager.HINT,
				new Integer(style));
		if (((style & StatusManager.SHOW) == StatusManager.SHOW)
				|| ((style & StatusManager.BLOCK) == StatusManager.BLOCK)) {

			final boolean block = ((style & StatusManager.BLOCK) == StatusManager.BLOCK);
			
			if (Display.getCurrent() != null) {
				showStatusAdapter(statusAdapter, block);
			} else {
				if (block) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							showStatusAdapter(statusAdapter, true);
						}
					});

				} else {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							showStatusAdapter(statusAdapter, false);
						}
					});
				}
			}
		}

		if ((style & StatusManager.LOG) == StatusManager.LOG) {
			StatusManager.getManager().addLoggedStatus(
					statusAdapter.getStatus());
			WorkbenchPlugin.getDefault().getLog()
					.log(statusAdapter.getStatus());
		}
	}
	
	/**
	 * Requests the status dialog manager to show the status adapter.
	 * 
	 * @param statusAdapter
	 *            the status adapter to show
	 * @param block
	 *            <code>true</code> to request a modal dialog and suspend the
	 *            calling thread till the dialog is closed, <code>false</code>
	 *            otherwise.
	 */
	private void showStatusAdapter(StatusAdapter statusAdapter, boolean block) {
		if (!PlatformUI.isWorkbenchRunning()) {
			// we are shutting down, so just log
			WorkbenchPlugin.log(statusAdapter.getStatus());
			return;
		}

		getStatusDialogManager().addStatusAdapter(statusAdapter, block);

		if (block) {
			Shell shell;
			while ((shell = getStatusDialogShell()) != null
					&& !shell.isDisposed()) {
				if (!shell.getDisplay().readAndDispatch()) {
					Display.getDefault().sleep();
				}
			}
		}
	}
	
	private Shell getStatusDialogShell() {
		return (Shell) getStatusDialogManager().getProperty(
				IStatusDialogConstants.SHELL);
	}

	/**
	 * This method returns current {@link WorkbenchStatusDialogManager}.
	 * 
	 * @return current {@link WorkbenchStatusDialogManager}
	 */
	private WorkbenchStatusDialogManager getStatusDialogManager() {
		if (statusDialogManager == null) {
			synchronized (this) {
				if (statusDialogManager == null) {
					statusDialogManager = new WorkbenchStatusDialogManager(null);
					statusDialogManager.setProperty(
							IStatusDialogConstants.SHOW_SUPPORT, Boolean.TRUE);
					statusDialogManager.setProperty(
							IStatusDialogConstants.HANDLE_OK_STATUSES,
							Boolean.TRUE);
					statusDialogManager.setProperty(
							IStatusDialogConstants.ERRORLOG_LINK, Boolean.TRUE);
					configureStatusDialog(statusDialogManager);
				}
			}
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
	 * @param statusDialog
	 *            a status dialog to be configured.
	 * @since 3.4
	 */
	protected void configureStatusDialog(
			final WorkbenchStatusDialogManager statusDialog) {
		// default configuration does nothing
	}
}
