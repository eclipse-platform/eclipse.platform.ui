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
 * returned from {@link WorkbenchAdvisor#getWorkbenchErrorHandler()} All handled
 * statuses are logged using logging facility.
 * 
 * @since 3.3
 */
public class WorkbenchErrorHandler extends AbstractStatusHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.errors.AbstractErrorHandler#handle(org.eclipse.ui.errors.HandlingStatusState)
	 */
	public void handle(StatusHandlingState handlingState) {
		WorkbenchPlugin.log(handlingState.getStatus());
	}
}
