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
 * Handles content manipulation. Filters filter content in as opposed to filtering out. ie: if a
 * filter passes, content is displayed. 
 * 
 */
public class UIContentFilterProcessor extends UAContentFilterProcessor {

	protected boolean isFilteredIn(String filter, String value) {
		boolean filtered_in = false;
		if (filter.equals("category")) {
			filtered_in = filterByCategory(value);
		} else if (filter.equals("activity")) {
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
			// if category is not defined, do not filter.
			return false;

		boolean categoryIsEnabled = WorkbenchActivityHelper.isEnabled(activityManager, categoryId);
		if (categoryIsEnabled)
			// category is enabled, filter content in => content is included.
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
			// if activity is not defined, do not filter.
			return false;

		boolean activityIsEnabled = activityManager.getActivity(activityId).isEnabled();
		if (activityIsEnabled)
			// activity is enabled, filter content in => content is included.
			return true;
		return false;
	}



}
