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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.activities.ActivityServiceEvent;
import org.eclipse.ui.activities.IActivityService;
import org.eclipse.ui.activities.IActivityServiceListener;
import org.eclipse.ui.activities.ICompoundActivityService;
import org.eclipse.ui.activities.IMutableActivityService;
import org.eclipse.ui.internal.util.Util;

public final class CompoundActivityService extends AbstractActivityService implements ICompoundActivityService {

	private Set activeActivityIds = new HashSet();
	private final HashSet activityServices = new HashSet();

	private final IActivityServiceListener activityServiceListener = new IActivityServiceListener() {
		public void activityServiceChanged(ActivityServiceEvent activityServiceEvent) {
			Set activeActivityIds = new HashSet();

			for (Iterator iterator = activityServices.iterator(); iterator.hasNext();) {
				IMutableActivityService mutableActivityService = (IMutableActivityService) iterator.next();
				activeActivityIds.addAll(mutableActivityService.getActiveActivityIds());
			}

			setActiveActivityIds(activeActivityIds);
		}
	};

	public CompoundActivityService() {
	}

	public void addActivityService(IActivityService activityService) {
		if (activityService == null)
			throw new NullPointerException();

		activityService.addActivityServiceListener(activityServiceListener);
		activityServices.add(activityService);
	}

	public Set getActiveActivityIds() {
		return Collections.unmodifiableSet(activeActivityIds);
	}

	public void removeActivityService(IActivityService activityService) {
		if (activityService == null)
			throw new NullPointerException();

		activityServices.remove(activityService);
		activityService.removeActivityServiceListener(activityServiceListener);
	}

	private void setActiveActivityIds(Set activeActivityIds) {
		activeActivityIds = Util.safeCopy(activeActivityIds, String.class);
		boolean activityServiceChanged = false;
		Map activityEventsByActivityId = null;

		if (!this.activeActivityIds.equals(activeActivityIds)) {
			this.activeActivityIds = activeActivityIds;
			fireActivityServiceChanged(new ActivityServiceEvent(this, true));
		}
	}
}