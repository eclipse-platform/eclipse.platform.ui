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

package org.eclipse.help.ui.internal;

import java.util.*;

import org.eclipse.help.internal.*;
import org.eclipse.ui.activities.*;

/**
 * Wrapper for eclipe ui role manager
 */
public class HelpRoleManager implements IHelpRoleManager {
	private IActivityManager activityManager;
	public HelpRoleManager(IActivityManager activityManager) {
		this.activityManager = activityManager;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.IHelpRoleManager#isEnabled()
	 */
	public boolean isEnabled(String href) {
		if (activityManager == null) {
			return true;
		}

		// For the time being, only look at plugin id filtering
		if (href.startsWith("/"))
			href = href.substring(1);
		int i = href.indexOf("/");
		if (i > 0)
			href = href.substring(0, i);

		Set disabledActivities =
			new HashSet(activityManager.getDefinedActivityIds());
		disabledActivities.removeAll(activityManager.getEnabledActivityIds());
		disabledActivities.removeAll(activityManager.getActiveActivityIds());
		return !activityManager.match(href, disabledActivities);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.IHelpRoleManager#enabledActivities(java.lang.String)
	 */
	public void enabledActivities(String href) {
		if (activityManager == null) {
			return;
		}

		// For the time being, only look at plugin id filtering
		if (href.startsWith("/"))
			href = href.substring(1);
		int i = href.indexOf("/");
		if (i > 0)
			href = href.substring(0, i);

		if (!activityManager
			.match(href, activityManager.getEnabledActivityIds())) {
			Set enabledActivities =
				new HashSet(activityManager.getEnabledActivityIds());
			Set definedActivityIds = activityManager.getDefinedActivityIds();
			for (Iterator it = definedActivityIds.iterator(); it.hasNext();) {
				String definedActivityId = (String) it.next();
				IActivity definedActivity =
					activityManager.getActivity(definedActivityId);
				if (definedActivity.match(href))
					enabledActivities.add(definedActivityId);

			}
			activityManager.setEnabledActivityIds(enabledActivities);
		}
	}

}
