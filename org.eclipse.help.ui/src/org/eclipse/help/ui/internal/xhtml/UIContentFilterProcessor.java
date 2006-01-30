/***************************************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.ui.internal.xhtml;

import org.eclipse.help.internal.xhtml.UAContentFilterProcessor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Handles content manipulation. Filters filter content in as opposed to filtering out.
 * 
 */
public class UIContentFilterProcessor extends UAContentFilterProcessor {

	public boolean isFilteredIn(String filter, String value) {
		boolean filtered_in = false;
		if (filter.equals("category")) { //$NON-NLS-1$
			filtered_in = filterByCategory(value);
		} else if (filter.equals("activity")) { //$NON-NLS-1$
			filtered_in = filterByActivity(value);
		} else
			filtered_in = super.isFilteredIn(filter, value);

		return filtered_in;
	}


	/**
	 * evaluates Role (aka category) filter.
	 */
	private static boolean filterByCategory(String categoryId) {
		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
		IActivityManager activityManager = workbenchActivitySupport.getActivityManager();
		if (!activityManager.getCategory(categoryId).isDefined())
			return false;

		boolean categoryIsEnabled = WorkbenchActivityHelper.isEnabled(activityManager, categoryId);
		if (categoryIsEnabled)
			return true;
		return false;
	}


	/**
	 * evaluates Activity filter.
	 */
	private static boolean filterByActivity(String activityId) {
		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
		IActivityManager activityManager = workbenchActivitySupport.getActivityManager();
		if (!activityManager.getActivity(activityId).isDefined())
			return false;

		boolean activityIsEnabled = activityManager.getActivity(activityId).isEnabled();
		if (activityIsEnabled)
			return true;
		return false;
	}



}
