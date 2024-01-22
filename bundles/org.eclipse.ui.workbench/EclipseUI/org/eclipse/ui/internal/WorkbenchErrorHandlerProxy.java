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

package org.eclipse.ui.internal;

import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusAdapter;

/**
 * A proxy handler which passes all statuses to handler assigned to current
 * application workbench advisor.
 *
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 *
 * @since 3.3
 */
public class WorkbenchErrorHandlerProxy extends AbstractStatusHandler {

	@Override
	public void handle(final StatusAdapter statusAdapter, int style) {
		Workbench.getInstance().getAdvisor().getWorkbenchErrorHandler().handle(statusAdapter, style);
	}

	@Override
	public boolean supportsNotification(int type) {
		return Workbench.getInstance().getAdvisor().getWorkbenchErrorHandler().supportsNotification(type);
	}

}
