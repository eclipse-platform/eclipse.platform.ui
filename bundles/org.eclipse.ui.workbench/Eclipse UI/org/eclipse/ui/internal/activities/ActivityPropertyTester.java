/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.activities;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * An expressions property tester that tests whether or not an activity or
 * category of activities is enabled. Useful for cross-component links and to
 * control discoverability of functionality.
 * 
 * @since 3.3
 */
public class ActivityPropertyTester extends PropertyTester {

	private static final String PROPERTY_IS_ACTIVITY_ENABLED = "isActivityEnabled"; //$NON-NLS-1$
	private static final String PROPERTY_IS_CATEGORY_ENABLED = "isCategoryEnabled"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.PropertyTester#test(java.lang.Object,
	 *      java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (args.length == 1 && receiver instanceof IWorkbench && args[0] instanceof String) {
			if (PROPERTY_IS_ACTIVITY_ENABLED.equals(property)) {
				return isActivityEnabled((String) args[0], (IWorkbench)receiver);
			} else if (PROPERTY_IS_CATEGORY_ENABLED.equals(property)) {
				return isCategoryEnabled((String) args[0], (IWorkbench)receiver);
			}
		}
		return false;
	}

	private static boolean isActivityEnabled(String activityId, IWorkbench workbench) {
		try {
			IWorkbenchActivitySupport workbenchActivitySupport =
					workbench.getActivitySupport();
			IActivityManager activityManager = workbenchActivitySupport
					.getActivityManager();
			return activityManager.getActivity(activityId).isEnabled();
		} catch (Exception e) {
			// workbench not yet activated; nothing enabled yet
		}
		return false;
	}

	private static boolean isCategoryEnabled(String categoryId, IWorkbench workbench) {
		try {
			IWorkbenchActivitySupport workbenchActivitySupport =
					workbench.getActivitySupport();
			IActivityManager activityManager = workbenchActivitySupport
					.getActivityManager();
			return WorkbenchActivityHelper.isEnabled(activityManager,
					categoryId);
		} catch (Exception e) {
			// workbench not yet activated; nothing enabled yet
		}
		return false;
	}
}
