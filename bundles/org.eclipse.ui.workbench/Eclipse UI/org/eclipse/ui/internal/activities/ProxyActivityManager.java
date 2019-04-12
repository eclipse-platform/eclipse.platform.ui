/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

package org.eclipse.ui.internal.activities;

import java.util.Set;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.IIdentifier;

public final class ProxyActivityManager extends AbstractActivityManager {
	private IActivityManager activityManager;

	public ProxyActivityManager(IActivityManager activityManager) {
		if (activityManager == null) {
			throw new NullPointerException();
		}

		this.activityManager = activityManager;

		this.activityManager.addActivityManagerListener(activityManagerEvent -> {
			ActivityManagerEvent proxyActivityManagerEvent = new ActivityManagerEvent(ProxyActivityManager.this,
					activityManagerEvent.haveDefinedActivityIdsChanged(),
					activityManagerEvent.haveDefinedCategoryIdsChanged(),
					activityManagerEvent.haveEnabledActivityIdsChanged(),
					activityManagerEvent.getPreviouslyDefinedActivityIds(),
					activityManagerEvent.getPreviouslyDefinedCategoryIds(),
					activityManagerEvent.getPreviouslyEnabledActivityIds());
			fireActivityManagerChanged(proxyActivityManagerEvent);
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
	public Set<String> getDefinedActivityIds() {
		return activityManager.getDefinedActivityIds();
	}

	@Override
	public Set<String> getDefinedCategoryIds() {
		return activityManager.getDefinedCategoryIds();
	}

	@Override
	public Set<String> getEnabledActivityIds() {
		return activityManager.getEnabledActivityIds();
	}

	@Override
	public IIdentifier getIdentifier(String identifierId) {
		return activityManager.getIdentifier(identifierId);
	}
}
