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

package org.eclipse.ui.internal.activities.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.activities.service.AbstractActivityService;
import org.eclipse.ui.activities.service.ActivityServiceEvent;
import org.eclipse.ui.activities.service.IMutableActivityService;
import org.eclipse.ui.internal.util.Util;

public final class MutableActivityService
	extends AbstractActivityService
	implements IMutableActivityService {

	private Set activeActivityIds = new HashSet();

	public MutableActivityService() {
	}

	public Set getActiveActivityIds() {
		return Collections.unmodifiableSet(activeActivityIds);
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
}
