/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.statushandling;

import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This is a default workbench error handler. The instance of this handler is
 * returned from {@link WorkbenchAdvisor#getWorkbenchErrorHandler()}. All
 * handled statuses are logged using logging facility.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 */
public class WorkbenchErrorHandler extends AbstractStatusHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.statushandling.AbstractStatusHandler#handle(org.eclipse.ui.statushandling.StatusHandlingState)
	 */
	public boolean handle(final StatusHandlingState handlingState) {
		if ((handlingState.getHandlingHint() & StatusManager.SHOW) == StatusManager.SHOW) {
			StatusNotificationManager.getInstance().addError(
					handlingState.getStatus(), null);
		}

		if ((handlingState.getHandlingHint() & StatusManager.LOG) == StatusManager.LOG) {
			addLoggedStatus(handlingState.getStatus());
			WorkbenchPlugin.getDefault().getLog()
					.log(handlingState.getStatus());
		}
		return true;
	}
}
