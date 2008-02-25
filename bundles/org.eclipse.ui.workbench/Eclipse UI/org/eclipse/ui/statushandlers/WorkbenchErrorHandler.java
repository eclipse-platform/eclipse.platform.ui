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

	private WorkbenchStatusDialog statusDialog;

	/**
	 * For testing purposes only. This method must not be used by any other
	 * clients.
	 * 
	 * @param dialog
	 *            a new WorkbenchStatusDialog to be set.
	 */
	void setStatusDialog(WorkbenchStatusDialog dialog) {
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
			getStatusDialog().addError(statusAdapter, modal);
		}

		if ((style & StatusManager.LOG) == StatusManager.LOG) {
			StatusManager.getManager().addLoggedStatus(
					statusAdapter.getStatus());
			WorkbenchPlugin.getDefault().getLog()
					.log(statusAdapter.getStatus());
		}
	}

	/**
	 * This method returns current {@link WorkbenchStatusDialog}.
	 * 
	 * @return current {@link WorkbenchStatusDialog}
	 */
	private WorkbenchStatusDialog getStatusDialog() {
		if (statusDialog == null) {
			initStatusDialog();
		}
		return statusDialog;
	}

	/**
	 * This methods should be overridden to configure
	 * {@link WorkbenchStatusDialog} behavior. It is advised to use only
	 * following methods of {@link WorkbenchStatusDialog}:
	 * <ul>
	 * <li>{@link WorkbenchStatusDialog#enableDefaultSupportArea(boolean)}</li>
	 * <li>{@link WorkbenchStatusDialog#setDetailsAreaProvider(AbstractStatusAreaProvider)}</li>
	 * <li>{@link WorkbenchStatusDialog#setSupportAreaProvider(AbstractStatusAreaProvider)}</li>
	 * </ul>
	 * Default configuration does nothing.
	 * 
	 * @param statusDialog
	 *            a status dialog to be configured.
	 */
	protected void configureStatusDialog(
			final WorkbenchStatusDialog statusDialog) {
		// default configuration does nothing
	}

	/**
	 * This method initializes {@link WorkbenchStatusDialog} and is called only
	 * once.
	 */
	private void initStatusDialog() {
		// this class must be instantiated in UI thread
		// (temporary solution, under investigation)
		if (Display.getCurrent() != null) {
			statusDialog = new WorkbenchStatusDialog(ProgressManagerUtil
					.getDefaultParent(), null);
			configureStatusDialog(statusDialog);
		} else {
			// if not ui than sync exec
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					statusDialog = new WorkbenchStatusDialog(
							ProgressManagerUtil.getDefaultParent(), null);
					configureStatusDialog(statusDialog);
				}
			});
		}
	}
}
