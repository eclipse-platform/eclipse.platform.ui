/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.csm.activities.api;

import org.eclipse.ui.internal.csm.activities.ActivityManager;

public final class ActivityManagerFactory {

	private static IActivityManager activityManager;

	public static IActivityManager getActivityManager() {
		if (activityManager == null)
			activityManager = new ActivityManager();
		
		return activityManager;
	}
	
	private ActivityManagerFactory() {		
	}
}
