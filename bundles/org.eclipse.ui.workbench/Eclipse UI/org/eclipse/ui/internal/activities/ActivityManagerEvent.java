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

package org.eclipse.ui.internal.activities;

import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerEvent;

final class ActivityManagerEvent implements IActivityManagerEvent {

	private boolean activeActivityIdsChanged;
	private IActivityManager activityManager;
	private boolean definedActivityIdsChanged;
	private boolean enabledActivityIdsChanged;

	ActivityManagerEvent(IActivityManager activityManager, boolean activeActivityIdsChanged, boolean definedActivityIdsChanged, boolean enabledActivityIdsChanged) {
		if (activityManager == null)
			throw new NullPointerException();
		
		this.activityManager = activityManager;
		this.activeActivityIdsChanged = activeActivityIdsChanged;
		this.definedActivityIdsChanged = definedActivityIdsChanged;
		this.enabledActivityIdsChanged = enabledActivityIdsChanged;
	}

	public IActivityManager getActivityManager() {
		return activityManager;
	}

	public boolean haveActiveActivityIdsChanged() {
		return activeActivityIdsChanged;
	}
	
	public boolean haveDefinedActivityIdsChanged() {
		return definedActivityIdsChanged;
	}

	public boolean haveEnabledActivityIdsChanged() {
		return enabledActivityIdsChanged;
	}
}
