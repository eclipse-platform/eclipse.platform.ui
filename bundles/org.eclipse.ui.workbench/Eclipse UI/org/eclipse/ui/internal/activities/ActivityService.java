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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.activities.IActivityService;
import org.eclipse.ui.activities.IActivityServiceEvent;
import org.eclipse.ui.activities.IActivityServiceListener;
import org.eclipse.ui.internal.util.Util;

public final class ActivityService implements IActivityService {

	private Set activeActivityIds = new HashSet();
	private List activityServiceListeners;

	public ActivityService() {
	}

	public void addActivityServiceListener(IActivityServiceListener activityServiceListener) {
		if (activityServiceListener == null)
			throw new NullPointerException();
			
		if (activityServiceListeners == null)
			activityServiceListeners = new ArrayList();
		
		if (!activityServiceListeners.contains(activityServiceListener))
			activityServiceListeners.add(activityServiceListener);
	}

	public Set getActiveActivityIds() {
		return Collections.unmodifiableSet(activeActivityIds);
	}
	
	public void removeActivityServiceListener(IActivityServiceListener activityServiceListener) {
		if (activityServiceListener == null)
			throw new NullPointerException();
			
		if (activityServiceListeners != null)
			activityServiceListeners.remove(activityServiceListener);
	}
		
	public void setActiveActivityIds(Set activeActivityIds) {
		activeActivityIds = Util.safeCopy(activeActivityIds, String.class);
		boolean activityServiceChanged = false;
		Map activityEventsByActivityId = null;

		if (!this.activeActivityIds.equals(activeActivityIds)) {
			this.activeActivityIds = activeActivityIds;
			fireActivityServiceChanged(new ActivityServiceEvent(this, true));	
		}
	}

	private void fireActivityServiceChanged(IActivityServiceEvent activityServiceEvent) {
		if (activityServiceEvent == null)
			throw new NullPointerException();
		
		if (activityServiceListeners != null)
			for (int i = 0; i < activityServiceListeners.size(); i++)
				((IActivityServiceListener) activityServiceListeners.get(i)).activityServiceChanged(activityServiceEvent);
	}
}
