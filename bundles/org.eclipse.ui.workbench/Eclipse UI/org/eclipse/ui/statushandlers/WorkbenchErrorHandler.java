/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.internal.statushandlers.StatusNotificationManager;

/**
 * This is a default workbench error handler.
 * 
 * @see WorkbenchAdvisor#getWorkbenchErrorHandler()
 * 
 * @since 3.3
 */
public class WorkbenchErrorHandler extends AbstractStatusHandler {

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

			if (statusAdapter.getStatus().getSeverity() == IStatus.INFO){
				MessageDialog.openInformation(ProgressManagerUtil
						.getDefaultParent(), (String) statusAdapter
						.getProperty(StatusAdapter.TITLE_PROPERTY),
						statusAdapter.getStatus().getMessage());
				return;
			}
			
			if (statusAdapter.getStatus().getSeverity() == IStatus.WARNING){
				MessageDialog.openWarning(ProgressManagerUtil
						.getDefaultParent(), (String) statusAdapter
						.getProperty(StatusAdapter.TITLE_PROPERTY),
						statusAdapter.getStatus().getMessage());
				return;
			}

			boolean modal = ((style & StatusManager.BLOCK) == StatusManager.BLOCK);
			StatusNotificationManager.getInstance().addError(statusAdapter,
					modal);
		}

		if ((style & StatusManager.LOG) == StatusManager.LOG) {
			StatusManager.getManager().addLoggedStatus(
					statusAdapter.getStatus());
			WorkbenchPlugin.getDefault().getLog()
					.log(statusAdapter.getStatus());
		}
	}
}
