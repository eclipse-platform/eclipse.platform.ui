/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.IIdentifier;

public final class ProxyActivityManager extends AbstractActivityManager {
    private IActivityManager activityManager;

    public ProxyActivityManager(IActivityManager activityManager) {
        if (activityManager == null) {
			throw new NullPointerException();
		}

        this.activityManager = activityManager;

        this.activityManager
                .addActivityManagerListener(new IActivityManagerListener() {
                    @Override
					public void activityManagerChanged(
                            ActivityManagerEvent activityManagerEvent) {
                        ActivityManagerEvent proxyActivityManagerEvent = new ActivityManagerEvent(
                                ProxyActivityManager.this, activityManagerEvent
                                        .haveDefinedActivityIdsChanged(),
                                activityManagerEvent
                                        .haveDefinedCategoryIdsChanged(),
                                activityManagerEvent
                                        .haveEnabledActivityIdsChanged(),
                                activityManagerEvent
                                        .getPreviouslyDefinedActivityIds(),
                                activityManagerEvent
                                        .getPreviouslyDefinedCategoryIds(),
                                activityManagerEvent
                                        .getPreviouslyEnabledActivityIds());
                        fireActivityManagerChanged(proxyActivityManagerEvent);
                    }
                });
    }

    @Override
	public IActivity getActivity(String activityId) {
        return activityManager.getActivity(activityId);
    }

    @Override
	public ICategory getCategory(String categoryId) {
        return activityManager.getCategory(categoryId);
    }

    @Override
	public Set getDefinedActivityIds() {
        return activityManager.getDefinedActivityIds();
    }

    @Override
	public Set getDefinedCategoryIds() {
        return activityManager.getDefinedCategoryIds();
    }

    @Override
	public Set getEnabledActivityIds() {
        return activityManager.getEnabledActivityIds();
    }

    @Override
	public IIdentifier getIdentifier(String identifierId) {
        return activityManager.getIdentifier(identifierId);
    }
}
