/*******************************************************************************
 * Copyright (c) 2024 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/
package org.eclipse.ui.internal.util;

import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activitysupport.IActivityManagerProxy;

public class ActivityManagerProxy implements IActivityManagerProxy {

	private IWorkbenchActivitySupport wbActivitySupport;

	public ActivityManagerProxy(IWorkbenchActivitySupport wbActivitySupport) {
		this.wbActivitySupport = wbActivitySupport;
	}

	@Override
	public boolean isIdentifierEnabled(String identifierId) {
		boolean isEnabled = wbActivitySupport.getActivityManager().getIdentifier(identifierId).isEnabled();
		return isEnabled;
	}
}
