/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

/**
 * Utility class that manages the persistance of enabled activities.
 * 
 * @since 3.0
 */
class ActivityPersistanceHelper {

	/**
	 * Prefix for all role preferences
	 */
	private static String PREFIX = "UIRoles."; //$NON-NLS-1$    

	/**
	 * Singleton instance.
	 */
	private static ActivityPersistanceHelper singleton;

	/**
	 * Get the singleton instance of this class.
	 * 
	 * @return the singleton instance of this class.
	 * @since 3.0
	 */
	public static ActivityPersistanceHelper getInstance() {
		if (singleton == null) {
			singleton = new ActivityPersistanceHelper();
		}
		return singleton;
	}

	/**
	 * Create a new <code>ActivityPersistanceHelper</code> which will restore
	 * previously enabled activity states.
	 */
	private ActivityPersistanceHelper() {
		loadEnabledStates();

		// TODO kim: shouldn't you want to check for any activities (not
		// categories)?

		IWorkbenchActivitySupport support = PlatformUI.getWorkbench().getActivitySupport();

		if (support == null)
			return;

		boolean noRoles =
			support.getActivityManager().getDefinedCategoryIds().isEmpty();

		if (noRoles) {
			IActivityManager activityManager = support.getActivityManager();
			support.setEnabledActivityIds(
				activityManager.getDefinedActivityIds());
		}
	}

	/**
	 * Create the preference key for the activity.
	 * 
	 * @param activity the activity.
	 * @return String a preference key representing the activity.
	 */
	private String createPreferenceKey(IActivity activity) {
		return PREFIX + activity.getId();
	}

	/**
	 * Loads the enabled states from the preference store.
	 */
	void loadEnabledStates() {
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();

		//Do not set it if the store is not set so as to
		//allow for switching off and on of roles
		//        if (!store.isDefault(PREFIX + FILTERING_ENABLED))
		//            setFiltering(store.getBoolean(PREFIX + FILTERING_ENABLED));

		IWorkbenchActivitySupport support = PlatformUI.getWorkbench().getActivitySupport();

		IActivityManager activityManager = support.getActivityManager();

		Iterator values = activityManager.getDefinedActivityIds().iterator();
		Set enabledActivities = new HashSet();
		while (values.hasNext()) {
			IActivity activity =
				activityManager.getActivity((String) values.next());
			if (store.getBoolean(createPreferenceKey(activity))) {
				enabledActivities.add(activity.getId());
			}
		}

		support.setEnabledActivityIds(enabledActivities);
	}

	/**
	 * Save the enabled states in he preference store.
	 */
	private void saveEnabledStates() {
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();

		IWorkbenchActivitySupport support = PlatformUI.getWorkbench().getActivitySupport();
		IActivityManager activityManager = support.getActivityManager();
		Iterator values = activityManager.getDefinedActivityIds().iterator();
		while (values.hasNext()) {
			IActivity activity =
				activityManager.getActivity((String) values.next());

			store.setValue(createPreferenceKey(activity), activity.isEnabled());
		}
	}

	/**
	 * Save the enabled state of all Activities.
	 */
	public void shutdown() {
		saveEnabledStates();
	}
}
