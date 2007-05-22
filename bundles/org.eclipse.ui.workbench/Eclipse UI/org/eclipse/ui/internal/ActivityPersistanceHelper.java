/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IActivityRequirementBinding;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.NotDefinedException;

/**
 * Utility class that manages the persistance of enabled activities.
 * 
 * @since 3.0
 */
final class ActivityPersistanceHelper {

    /**
     * Prefix for all activity preferences
     */
    protected final static String PREFIX = "UIActivities."; //$NON-NLS-1$    

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
            //process newly defined activities.
            if (activityManagerEvent.haveDefinedActivityIdsChanged()) {
                Set delta = new HashSet(activityManagerEvent
                        .getActivityManager().getDefinedActivityIds());
                delta.removeAll(activityManagerEvent
                        .getPreviouslyDefinedActivityIds());
                // whatever is still in delta are new activities - restore their
                // state
                loadEnabledStates(activityManagerEvent
                        .getActivityManager().getEnabledActivityIds(), delta);
            }
            if (activityManagerEvent.haveEnabledActivityIdsChanged()) {
				saveEnabledStates();
			}
        }
    };
    
    /**
     * The listener that responds to preference changes
     */
    private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent event) {
            // dont process property events if we're in the process of
            // serializing state.
            if (!saving && event.getProperty().startsWith(PREFIX)) {
                String activityId = event.getProperty().substring(PREFIX.length());
                IWorkbenchActivitySupport support = PlatformUI.getWorkbench().getActivitySupport();
                IActivityManager activityManager = support.getActivityManager();
                
                boolean enabled = Boolean.valueOf(event.getNewValue().toString()).booleanValue();
                // if we're turning an activity off we'll need to create its dependency tree to ensuure that all dependencies are also disabled.
                Set set = new HashSet(activityManager.getEnabledActivityIds());
                if (enabled == false) {
                    Set dependencies = buildDependencies(activityManager, activityId);
                    set.removeAll(dependencies);
                }
                else {
                    set.add(activityId);
                }
                support.setEnabledActivityIds(set);
            }
        }
    };

    /**
     * Whether we are currently saving the state of activities to the preference
     * store.
     */
    protected boolean saving = false;

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
     * Returns a set of activity IDs that depend on the provided ID in order to be enabled.
     * 
     * @param activityManager the activity manager to query
     * @param activityId the activity whos dependencies should be added
     * @return a set of activity IDs
     */
    protected Set buildDependencies(IActivityManager activityManager, String activityId) {
        Set set = new HashSet();
        for (Iterator i = activityManager.getDefinedActivityIds().iterator(); i.hasNext(); ) {
            IActivity activity = activityManager.getActivity((String) i.next());
            for (Iterator j = activity.getActivityRequirementBindings().iterator(); j.hasNext(); ) {
                IActivityRequirementBinding binding = (IActivityRequirementBinding) j.next();
                if (activityId.equals(binding.getRequiredActivityId())) {
                    set.addAll(buildDependencies(activityManager, activity.getId()));
                }
            }
        }
        set.add(activityId);
        return set;
    }

    /**
     * Create a new <code>ActivityPersistanceHelper</code> which will restore
     * previously enabled activity states.
     */
    private ActivityPersistanceHelper() {
        loadEnabledStates();
        hookListeners();
    }

    /**
     * Hook the listener that will respond to any activity state changes.
     */
    private void hookListeners() {
        IWorkbenchActivitySupport support = PlatformUI.getWorkbench()
                .getActivitySupport();

        IActivityManager activityManager = support.getActivityManager();

        activityManager.addActivityManagerListener(activityManagerListener);

        IPreferenceStore store = WorkbenchPlugin.getDefault()
                .getPreferenceStore();
        
        store.addPropertyChangeListener(propertyChangeListener);        
    }

    /**
     * Hook the listener that will respond to any activity state changes.
     */
    private void unhookListeners() {
        IWorkbenchActivitySupport support = PlatformUI.getWorkbench()
                .getActivitySupport();

        IActivityManager activityManager = support.getActivityManager();

        activityManager.removeActivityManagerListener(activityManagerListener); 
        
        IPreferenceStore store = WorkbenchPlugin.getDefault()
                .getPreferenceStore();
        
        store.removePropertyChangeListener(propertyChangeListener);                
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
        loadEnabledStates(Collections.EMPTY_SET, PlatformUI.getWorkbench()
                .getActivitySupport().getActivityManager()
                .getDefinedActivityIds());
    }

    /**
     * Load the enabled states for the given activity IDs.
     * 
     * @param previouslyEnabledActivities the activity states to maintain.  This set must be writabe.
     * @param activityIdsToProcess the activity ids to process
     */
    protected void loadEnabledStates(Set previouslyEnabledActivities, Set activityIdsToProcess) {
        if (activityIdsToProcess.isEmpty()) {
			return;
		}
        
        Set enabledActivities = new HashSet(previouslyEnabledActivities);
        IPreferenceStore store = WorkbenchPlugin.getDefault()
                .getPreferenceStore();

        IWorkbenchActivitySupport support = PlatformUI.getWorkbench()
                .getActivitySupport();

        IActivityManager activityManager = support.getActivityManager();

        for (Iterator i = activityIdsToProcess.iterator(); i
                .hasNext();) {
            String activityId = (String) i.next();
            String preferenceKey = createPreferenceKey(activityId);
			try {
                IActivity activity = activityManager.getActivity(activityId);
                if ("".equals(store.getDefaultString(preferenceKey))) { //$NON-NLS-1$ // no override has been provided in the customization file
                	store // the default should be whatever the XML specifies
					.setDefault(preferenceKey, activity
							.isDefaultEnabled());
                	
                }				

            } catch (NotDefinedException e) {
                // can't happen - we're iterating over defined activities
            }

            if (store.getBoolean(preferenceKey)) {
				enabledActivities.add(activityId);
			} else {
				enabledActivities.remove(activityId);
			}
        }

        support.setEnabledActivityIds(enabledActivities);
    }

    /**
     * Save the enabled states in the preference store.
     */
    protected void saveEnabledStates() {
        try {
            saving = true;
	        
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
        finally {
            saving = false;
        }
    }

    /**
     * Save the enabled state of all activities.
     */
    public void shutdown() {
        unhookListeners();
        saveEnabledStates();        
    }
}
