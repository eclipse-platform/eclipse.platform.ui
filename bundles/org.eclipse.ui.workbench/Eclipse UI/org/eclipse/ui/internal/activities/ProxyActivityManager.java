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

import java.util.Set;

import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerListener;

public final class ProxyActivityManager extends AbstractActivityManager {

	private IActivityManager activityManager;

	public ProxyActivityManager(IActivityManager activityManager) {
		if (activityManager == null)
			throw new NullPointerException();

		this.activityManager = activityManager;

		this.activityManager.addActivityManagerListener(new IActivityManagerListener() {
			public void activityManagerChanged(ActivityManagerEvent activityManagerEvent) {
				ActivityManagerEvent proxyActivityManagerEvent =
					new ActivityManagerEvent(
						ProxyActivityManager.this.activityManager,
						activityManagerEvent.haveDefinedActivityIdsChanged(),
						activityManagerEvent.haveEnabledActivityIdsChanged());
				fireActivityManagerChanged(proxyActivityManagerEvent);
			}
		});
	}
	
	public IActivity getActivity(String activityId) {
		return activityManager.getActivity(activityId);
	}

	public Set getDefinedActivityIds() {
		return activityManager.getDefinedActivityIds();
	}

	public Set getEnabledActivityIds() {
		return activityManager.getEnabledActivityIds();	
	}

	public boolean match(String string, Set activityIds) {
		return activityManager.match(string, activityIds);	
	}

	public Set matches(String string, Set activityIds) {
		return activityManager.matches(string, activityIds);	
	}
}
