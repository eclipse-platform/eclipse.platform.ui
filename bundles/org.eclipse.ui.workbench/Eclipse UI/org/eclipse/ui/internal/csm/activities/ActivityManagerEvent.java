/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.csm.activities;

import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerEvent;

final class ActivityManagerEvent implements IActivityManagerEvent {

	private IActivityManager activityManager;

	ActivityManagerEvent(IActivityManager activityManager) {
		if (activityManager == null)
			throw new NullPointerException();
		
		this.activityManager = activityManager;
	}

	public IActivityManager getActivityManager() {
		return activityManager;
	}
}
