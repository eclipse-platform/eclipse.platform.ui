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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ui.internal.util.Util;

public final class ActivityActivationService implements IActivityActivationService {

	private SortedSet activeActivityIds;
	private IActivityActivationServiceEvent activityActivationServiceEvent;
	private List activityActivationServiceListeners;

	public ActivityActivationService() {
	}

	public void activateActivity(String activityId) {
		if (activityId == null)
			throw new NullPointerException();

		if (activeActivityIds == null)
			activeActivityIds = new TreeSet();
			
		if (activeActivityIds.add(activityId))
			fireActivityActivationServiceChanged();
	}

	public void addActivityActivationServiceListener(IActivityActivationServiceListener activityActivationServiceListener) {
		if (activityActivationServiceListener == null)
			throw new NullPointerException();
			
		if (activityActivationServiceListeners == null)
			activityActivationServiceListeners = new ArrayList();
		
		if (!activityActivationServiceListeners.contains(activityActivationServiceListener))
			activityActivationServiceListeners.add(activityActivationServiceListener);
	}

	public void deactivateActivity(String activityId) {
		if (activityId == null)
			throw new NullPointerException();

		if (activeActivityIds != null && activeActivityIds.remove(activityId)) {			
			if (activeActivityIds.isEmpty())
				activeActivityIds = null;

			fireActivityActivationServiceChanged();
		}			
	}

	public SortedSet getActiveActivityIds() {
		return activeActivityIds != null ? Collections.unmodifiableSortedSet(activeActivityIds) : Util.EMPTY_SORTED_SET;
	}
	
	public void removeActivityActivationServiceListener(IActivityActivationServiceListener activityActivationServiceListener) {
		if (activityActivationServiceListener == null)
			throw new NullPointerException();
			
		if (activityActivationServiceListeners != null)
			activityActivationServiceListeners.remove(activityActivationServiceListener);
	}
	
	private void fireActivityActivationServiceChanged() {
		if (activityActivationServiceListeners != null) {
			for (int i = 0; i < activityActivationServiceListeners.size(); i++) {
				if (activityActivationServiceEvent == null)
					activityActivationServiceEvent = new ActivityActivationServiceEvent(this);
							
				((IActivityActivationServiceListener) activityActivationServiceListeners.get(i)).activityActivationServiceChanged(activityActivationServiceEvent);
			}				
		}	
	}	
}
