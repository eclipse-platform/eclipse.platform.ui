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

import org.eclipse.ui.activities.AbstractActivityService;
import org.eclipse.ui.activities.IActivityService;

public final class ActivityServiceAdapter extends AbstractActivityService {

	private IActivityService activityService;

	public ActivityServiceAdapter(IActivityService activityService) {
		if (activityService == null)
			throw new NullPointerException();

		this.activityService = activityService;
	}

	public Set getActiveActivityIds() {
		return activityService.getActiveActivityIds();
	}
}
