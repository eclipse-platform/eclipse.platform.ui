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

import org.eclipse.ui.activities.IActivityService;
import org.eclipse.ui.activities.IActivityServiceEvent;

final class ActivityServiceEvent implements IActivityServiceEvent {

	private boolean activeActivityIdsChanged;
	private IActivityService activityService;
	
	ActivityServiceEvent(IActivityService activityService, boolean activeActivityIdsChanged) {
		if (activityService == null)
			throw new NullPointerException();
		
		this.activeActivityIdsChanged = activeActivityIdsChanged;
		this.activityService = activityService;
	}

	public IActivityService getActivityService() {
		return activityService;
	}
	
	public boolean haveActiveActivityIdsChanged() {
		return activeActivityIdsChanged;
	}
}
