/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.statushandlers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;

/**
 * This is a default workbench error handler.
 * 
 * @see WorkbenchAdvisor#getWorkbenchErrorHandler()
 * @since 3.3
 */
public class WorkbenchErrorHandler extends AbstractStatusHandler {

	private WorkbenchStatusDialogManager statusDialog;

	/**
	 * For testing purposes only. This method must not be used by any other
	 * clients.
	 * 
	 * @param dialog
	 *            a new WorkbenchStatusDialog to be set.
	 */
	void setStatusDialog(WorkbenchStatusDialogManager dialog) {
		statusDialog = dialog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.statushandlers.AbstractStatusHandler#handle(org.eclipse.ui.statushandlers.StatusAdapter,
	 *      int)
	 */
	public void handle(final StatusAdapter statusAdapter, int style) {
		if (((style & StatusManager.SHOW) == StatusManager.SHOW)
				|| ((style & StatusManager.BLOCK) == StatusManager.BLOCK)) {

			// INFO status is set in the adapter when the passed adapter has OK
			// or CANCEL status
			if (statusAdapter.getStatus().getSeverity() == IStatus.OK
					|| statusAdapter.getStatus().getSeverity() == IStatus.CANCEL) {
				IStatus status = statusAdapter.getStatus();
				statusAdapter.setStatus(new Status(IStatus.INFO, status
						.getPlugin(), status.getMessage(), status
						.getException()));
			}

			boolean modal = ((style & StatusManager.BLOCK) == StatusManager.BLOCK);
			getStatusDialogManager().addStatusAdapter(statusAdapter, modal);
		}

		if ((style & StatusManager.LOG) == StatusManager.LOG) {
			StatusManager.getManager().addLoggedStatus(
					statusAdapter.getStatus());
			WorkbenchPlugin.getDefault().getLog()
					.log(statusAdapter.getStatus());
		}
	}

	/**
	 * This method returns current {@link WorkbenchStatusDialogManager}.
	 * 
	 * @return current {@link WorkbenchStatusDialogManager}
	 */
	private WorkbenchStatusDialogManager getStatusDialogManager() {
		if (statusDialog == null) {
			initStatusDialogManager();
		}
		return statusDialog;
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

	/**
	 * This method initializes {@link WorkbenchStatusDialogManager} and is called only
	 * once.
	 */
	private void initStatusDialogManager() {
		// this class must be instantiated in UI thread
		// (temporary solution, under investigation)
		if (Display.getCurrent() != null) {
			statusDialog = new WorkbenchStatusDialogManager(ProgressManagerUtil
					.getDefaultParent(), null);
			configureStatusDialog(statusDialog);
		} else {
			// if not ui than sync exec
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					statusDialog = new WorkbenchStatusDialogManager(
							ProgressManagerUtil.getDefaultParent(), null);
					configureStatusDialog(statusDialog);
				}
			});
		}
	}
}
