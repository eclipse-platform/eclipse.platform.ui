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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Provides a registry of id-&gt;object mappings (likely derived from extension
 * point contributions), id-&gt;activity mappings, and a means of filtering the
 * object registry based on the currently enabled activities.
 * 
 * This functionality is currently implemented via calculating the filtered set
 * at every call.  When the ContextManager can provide incremental change 
 * infomation in its events we can calculate the up-to-date list in the 
 * background. 
 */
public class ObjectActivityManager {

    /**
     * The map of all known managers.
     */
    private static Map fManagers = new HashMap(17);    

    /**
     * Get the manager for a given id, optionally creating it if it doesn't 
     * exist.
     * 
     * @param id
     * @param create
     * @return
     */
    public static ObjectActivityManager getManager(String id, boolean create) {
        ObjectActivityManager manager = (ObjectActivityManager) fManagers.get(id);
        if (manager == null && create) {
            manager = new ObjectActivityManager(id);
            fManagers.put(id, manager);
        }
        return manager;
    }
    
    /**
     * Map of id-&gt;list&lt;activity&gt;.
     */
    private Map fActivityMap = new HashMap();

    /**
     * Unique ID for this manager.  
     */
    private String fId;

    /**
     * Map of id-&gt;object.
     */
    private Map fObjectMap = new HashMap();

    /**
     * Create an instance with the given id.
     * 
     * @param id
     */
    private ObjectActivityManager(String id) {
        if (id == null) {
            throw new IllegalArgumentException();
        }
        
        fId = id;
    }
    
    /**
     * Adds a binding between object-&gt;activity.  If the given activity is not
     * defined in the RoleManager registry then no action is taken.
     * TBD: should the binding be added if the object doesnt exist?
     * 
     * @param objectId
     * @param activityId
     */
    public void addActivityBinding(ObjectContributionRecord record, String activityId) {
        if (record == null || activityId == null) {
            throw new IllegalArgumentException();
        }    
        
        if (RoleManager.getInstance().getActivity(activityId) == null) {
            return;
        }
            
        Set bindings = getActivityIdsFor(record, true);
        bindings.add(activityId);
    }

    /**
     * Add a given id-&gt;object mapping.  A given object should be added to 
     * the reciever only once.
     * 
     * @param id
     * @param o
     */
    public void addObject(ObjectContributionRecord record, Object o) {
        if (record == null || o == null) {
            throw new IllegalArgumentException();
        }
        fObjectMap.put(record, o);
    }
    
    /**
     * Find the (first) ObjectContributionRecord that maps to the given object, 
     * or null.     
     * 
     * @param objectOfInterest
     * @return
     */
    private ObjectContributionRecord findObjectContributionRecord(Object objectOfInterest) {
        for (Iterator i = fObjectMap.entrySet().iterator(); i.hasNext(); ) {    
            Map.Entry entry = (Entry) i.next();
            if (entry.getValue() == objectOfInterest) {
                return (ObjectContributionRecord) entry.getKey();
            }
        }
        return null;
    }

    /**
     * Return a set of objects that are currently valid based on the active
     * activities, or all objects if role filtering is currently disabled.  
     * 
     * @return
     */
    public Set getActiveObjects() {
        if (RoleManager.getInstance().isFiltering()) {
            Set activeObjects = new HashSet(17);      
            Set activeActivities = getActivityIds();
            for (Iterator iter = fObjectMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Entry) iter.next();
                ObjectContributionRecord record = (ObjectContributionRecord) entry.getKey();
                Set activitiesForId = getActivityIdsFor(record, false);
                if (activitiesForId == null) {
                    activeObjects.add(entry.getValue());
                }
                else {
                    Set activitiesForIdCopy = new HashSet(activitiesForId);                
                    activitiesForIdCopy.retainAll(activeActivities);
                    if (!activitiesForIdCopy.isEmpty()) {
                        activeObjects.add(entry.getValue());
                    }
                }
            }
            
            return activeObjects;
        }
        else {
            return new HashSet(fObjectMap.values());            
        }
    }

    /**
     * Return the list of enabled activities as provided by the RoleManager.
     * 
     * @return
     */
    private Set getActivityIds() {
        Collection activities = RoleManager.getInstance().getActivities();
        Set activityIds = new HashSet();
        for(Iterator i = activities.iterator(); i.hasNext(); ) {
            Activity activity = (Activity) i.next();
            if (activity.isEnabled()) {
                activityIds.add(activity.getId());
            }
        }
        return activityIds;       
    }

    /**
     * Return the activity set for the given record, creating and inserting one 
     * if requested.
     * 
     * @param record
     * @param create
     * @return
     */
    private Set getActivityIdsFor(ObjectContributionRecord record, boolean create) {
        Set set = (Set) fActivityMap.get(record);
        if (set == null && create) {
            set = new HashSet();
            fActivityMap.put(record, set);
        }
        return set;
    }
    
    /**
     * Get the unique identifier for this manager.
     * 
     * @return
     */
    public String getId() {
        return fId;
    }
    
    /**
     * Get the Set of ObjectContributionRecord keys from the object store.  This
     * Set is read only.
     * 
     * @return
     */
    Set getObjectIds() {
        return Collections.unmodifiableSet(fObjectMap.keySet());
    }
    
    /**
     * Remove all bindings that map to the given id.  This does not impact the 
     * id-&gt;object mappings.
     * 
     * TBD: should remove be supported?
     * 
     * @param id the id of the object to remove bindings for.  
     */
    public void removeActivityBinding(String id) {
        fActivityMap.remove(id);
    }
    
	/**
     * Clear both the id-&gt;object and id-&gt;list&lt;activity&gt; mappings.
     * 
     * TBD : should remove be supported?
     */
    public void removeAll() {
        removeAllObjectMap();
        removeAllActivityMap();
    }

    /**
     * Clear the id-&gt;list&lt;activity&gt; map. 
     * 
     * TBD : should remove be supported?
     */
    public void removeAllActivityMap() {
        fActivityMap.clear();        
    }

    /**
     * Clear the id-&gt;object map.
     * 
     * TBD: should remove be supported?
     */
    public void removeAllObjectMap() {
       fObjectMap.clear();      
    }
    
    /**
     * Remove the object with the given id.  This does not impact the 
     * id-&gt;list&lt;activity&gt; mappings.
     * 
     * TBD: should remove be supported?
     * 
     * @param id the id of the object to remove.  
     */
    public void removeObject(String id) {
        fObjectMap.remove(id);
    }
    
    /**
     * Set the enablement state for all activities bound to the given object.
     * Useful for "turning on" activities based on key-points in the UI (ie: 
     * the New wizard).
     * 
     * @param objectOfInterest
     * @param enablement
     */
    public void setEnablementFor(Object objectOfInterest, boolean enablement) {
        ObjectContributionRecord record = findObjectContributionRecord(objectOfInterest);
        if (record != null) {
            Set activities = getActivityIdsFor(record, false);
            for(Iterator i = activities.iterator(); i.hasNext(); ) {
                Activity activity = RoleManager.getInstance().getActivity((String) i.next());
                if (activity != null) {
                    activity.setEnabled(enablement);
                }
            }            
        }
    }
}
