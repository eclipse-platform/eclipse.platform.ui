/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
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
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

/**
 * Utility class that manages the persistance of enabled activities.
 * 
 * @since 3.0
 */
class ActivityPersistanceHelper {

    /**
     * Prefix for all activity preferences
     */
    private static String PREFIX = "UIActivities."; //$NON-NLS-1$    

    /**
     * Singleton instance.
     */
    private static ActivityPersistanceHelper singleton;

    /**
     * The listener that responds to changes in the <code>IActivityManager</code>
     */
    private final IActivityManagerListener activityManagerListener = new IActivityManagerListener() {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.activities.IActivityManagerListener#activityManagerChanged(org.eclipse.ui.activities.ActivityManagerEvent)
         */
        public void activityManagerChanged(
                ActivityManagerEvent activityManagerEvent) {
            if (activityManagerEvent.haveEnabledActivityIdsChanged())
                saveEnabledStates();
        }
    };

    /**
     * Get the singleton instance of this class.
     * 
     * @return the singleton instance of this class.
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
        hookActivityListener();
    }

    /**
     * Hook the listener that will respond to any activity state changes.
     */
    private void hookActivityListener() {
        IWorkbenchActivitySupport support = PlatformUI.getWorkbench()
                .getActivitySupport();

        IActivityManager activityManager = support.getActivityManager();

        activityManager.addActivityManagerListener(activityManagerListener);
    }

    /**
     * Hook the listener that will respond to any activity state changes.
     */
    private void unhookActivityListener() {
        IWorkbenchActivitySupport support = PlatformUI.getWorkbench()
                .getActivitySupport();

        IActivityManager activityManager = support.getActivityManager();

        activityManager.removeActivityManagerListener(activityManagerListener);        
    }
    
    /**
     * Create the preference key for the activity.
     * 
     * @param activityId the activity id.
     * @return String a preference key representing the activity.
     */
    private String createPreferenceKey(String activityId) {
        return PREFIX + activityId;
    }

    /**
     * Loads the enabled states from the preference store.
     */
    void loadEnabledStates() {
        IPreferenceStore store = WorkbenchPlugin.getDefault()
                .getPreferenceStore();

        IWorkbenchActivitySupport support = PlatformUI.getWorkbench()
                .getActivitySupport();

        IActivityManager activityManager = support.getActivityManager();

        for (Iterator i = activityManager.getEnabledActivityIds().iterator(); i
                .hasNext();) { // default enabled IDs		    
            store.setDefault(createPreferenceKey((String) i.next()), true);
        }

        Set enabledActivities = new HashSet();
        for (Iterator i = activityManager.getDefinedActivityIds().iterator(); i
                .hasNext();) {
            String activityId = (String) i.next();

            if (store.getBoolean(createPreferenceKey(activityId)))
                enabledActivities.add(activityId);
        }

        support.setEnabledActivityIds(enabledActivities);
    }

    /**
     * Save the enabled states in the preference store.
     */
    protected void saveEnabledStates() {
        IPreferenceStore store = WorkbenchPlugin.getDefault()
                .getPreferenceStore();

        IWorkbenchActivitySupport support = PlatformUI.getWorkbench()
                .getActivitySupport();
        IActivityManager activityManager = support.getActivityManager();
        Iterator values = activityManager.getDefinedActivityIds().iterator();
        while (values.hasNext()) {
            IActivity activity = activityManager.getActivity((String) values
                    .next());

            store.setValue(createPreferenceKey(activity.getId()), activity
                    .isEnabled());
        }
        WorkbenchPlugin.getDefault().savePluginPreferences();
    }

    /**
     * Save the enabled state of all activities.
     */
    public void shutdown() {
        unhookActivityListener();
        saveEnabledStates();        
    }
}