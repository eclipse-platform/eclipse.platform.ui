/***************************************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.ui.internal.xhtml;

import org.eclipse.help.internal.FilterableHelpElement;
import org.eclipse.help.internal.FilterableUAElement;
import org.eclipse.help.internal.base.HelpBasePlugin;
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

	/**
	 * Overrides to add category and activity filtering.
	 *  
	 * @see org.eclipse.help.internal.xhtml.UAContentFilterProcessor#isFilteredIn(java.lang.String, java.lang.String)
	 */
	public boolean isFilteredIn(String filter, String value, boolean isPositive) {
		boolean filtered_in = false;
		if (filter.equals("category")) { //$NON-NLS-1$
			filtered_in = filterByCategory(value);
		} else if (filter.equals("activity")) { //$NON-NLS-1$
			filtered_in = filterByActivity(value);
		} else
			return super.isFilteredIn(filter, value, isPositive);

		return isPositive ? filtered_in : !filtered_in;
	}

	/**
	 * Overrides to turn off filtering when it's specifically a help element
	 * and the user has requested to show all content.
	 * 
	 * @see org.eclipse.help.internal.xhtml.UAContentFilterProcessor#isFilteredIn(org.eclipse.help.internal.FilterableUAElement)
	 */
	public boolean isFilteredIn(FilterableUAElement element) {
		// don't filter help elements if user requested show all content
		if (element instanceof FilterableHelpElement && !HelpBasePlugin.getActivitySupport().isFilteringEnabled()) {
			return true;
		}
		return super.isFilteredIn(element);
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
