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
package org.eclipse.ui.internal.roles;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility for batching ActivityEvents into one ActivtyManagerEvent.  This 
 * Class is not threadsafe but it is used in RoleManager in a fashion that 
 * guarentees safety (by use of ThreadLocal).
 */
public class ActivityDeltaCalculator {

    private RoleManager fActivityManager;
    private Set enabledActivities = new HashSet(17), 
                disabledActivities = new HashSet(17);
                
    private boolean fInSession = false;

    /**
     * 
     * @param activityManager
     */
    public ActivityDeltaCalculator(RoleManager activityManager) {
        fActivityManager = activityManager;        
    }
    
    /**
     * @return the event based on the current deltas, or null if there are no meaningful deltas.
     */
    private ActivityManagerEvent getEvent() {            
        ActivityManagerEvent event = null;
        if (disabledActivities.size() > 0 || enabledActivities.size() > 0) { 
            event = new ActivityManagerEvent(
                fActivityManager,
                (Activity [])enabledActivities.toArray(new Activity[enabledActivities.size()]),
                (Activity [])disabledActivities.toArray(new Activity[disabledActivities.size()]));
        }
        return event;        
    }
    
    /**
     * Starts a new delta session if one hasn't alreayd been started.
     * @return whether a new session has been started.
     */
    public boolean startDeltaSession() {        
        if (!fInSession) {
            fInSession = true;
            enabledActivities.clear();
            disabledActivities.clear();
            return true;
        }
        else {
            return false;
        }           
    }
    
    /**
     * @return an event that captures the changes described since the last call
     * to startDeltaSession().  If there have been no changes the null is returned.
     */
    public ActivityManagerEvent endDeltaSession() {
        fInSession = false;
        return getEvent();
    } 
    
    /**
     * Adds an activity to this delta.  It will be added either to the enabled
     * or disabled list based on its enablement value.  Note that if you add an 
     * enabled activity, then disable it and add it again it will be registered
     * on the disabled part of the event despite there not being any real change.
     * @param activity
     * @throws IllegalStateException if there is not currently a session in 
     * progress 
     */
    public void addActivity(Activity activity) {
        if (!fInSession) {
            throw new IllegalStateException();
        }
        if (activity.isEnabled()) {
            enabledActivities.add(activity);
            disabledActivities.remove(activity);    
        }
        else {
            enabledActivities.remove(activity);
            disabledActivities.add(activity);            
        }
    }  
}
