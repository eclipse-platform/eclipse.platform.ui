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
 * This functionality is currently implemented by calculating the filtered set
 * only when activity changes dictate that the cache is invalid.  In a stable
 * system (one in which activities of interest are not enabling and disabling 
 * themselves with any great rate and in which new objects and bindings are not 
 * being added often) then this calculation should need to be performed 
 * infrequently.
 * 
 * TBD: It'd be possible for us to calculate the up-to-date list in a background
 * Job... is it worth the effort?
 */
public class ObjectActivityManager implements IActivityListener{

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
     * The cache of currently active objects.  This is also the synchronization
     * lock for all cache operations.
     */
    private Set fActiveObjects = new HashSet(17);
    
    /**
     * Whether the active objects set is stale due to Activity enablement 
     * changes or object/binding additions.
     */
    private boolean fDirty = true;

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
     * Changes in activity status impact the cache of enabled objects.  Mark the 
     * object cache as being dirty.
     * 
     * @see org.eclipse.ui.internal.roles.IActivityListener#activityChanged(org.eclipse.ui.internal.roles.ActivityEvent)
     */
    public void activityChanged(ActivityEvent event) {
        invalidateCache();      
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
        
        Activity activity = RoleManager.getInstance().getActivity(activityId); 
        if (activity == null) {
            return;
        }               
            
        Set bindings = getActivityIdsFor(record, true);
        if (bindings.add(activityId)) {
            // if we havn't already bound this activity do so and invalidate the
            // cache
            activity.addListener(this);
            invalidateCache();
        }
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

        Object oldObject = fObjectMap.put(record, o);

        if (oldObject != o) {
            // dirty the cache if the old entry is not the same as the new one.
            // TBD: would .equals() be more appropriate?
            invalidateCache();
        }            
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
        synchronized (fActiveObjects) {
            if (RoleManager.getInstance().isFiltering()) {
                if (fDirty) {
                    fActiveObjects.clear();      
                    Set activeActivities = getActivityIds();
                    for (Iterator iter = fObjectMap.entrySet().iterator(); iter.hasNext();) {
                        Map.Entry entry = (Entry) iter.next();
                        ObjectContributionRecord record = (ObjectContributionRecord) entry.getKey();
                        Set activitiesForId = getActivityIdsFor(record, false);
                        if (activitiesForId == null) {
                            fActiveObjects.add(entry.getValue());
                        }
                        else {
                            Set activitiesForIdCopy = new HashSet(activitiesForId);                
                            activitiesForIdCopy.retainAll(activeActivities);
                            if (!activitiesForIdCopy.isEmpty()) {
                                fActiveObjects.add(entry.getValue());
                            }
                        }
                    }
                    
                    fDirty = false;
                }
                return fActiveObjects;
            }
            else {
                // TBD: this logic can probably be moved into the caching logic.  
                return new HashSet(fObjectMap.values());            
            }
        }
    }

    /**
     * Return the list of enabled activities as provided by the RoleManager.
     * TBD: is it worth caching this info?  We can do it with the help of 
     * activityChanged(ActivityEvent).
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
     * Mark the cache for recalculation.
     */
    private void invalidateCache() {
        synchronized (fActiveObjects) {   
            fDirty = true;
        }
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
     */
    public void removeAll() {
        removeAllObjectMap();
        removeAllActivityMap();
    }

    /**
     * Clear the id-&gt;list&lt;activity&gt; map and deregister our 
     * ActivityListeners
     */
    public void removeAllActivityMap() {
        for (Iterator i = fActivityMap.values().iterator(); i.hasNext(); ) {
            String activityId = (String) i.next();
            Activity activity = RoleManager.getInstance().getActivity(activityId);
            if (activity != null) {
                // clean up our listener
                activity.removeListener(this);
            }
        }
        fActivityMap.clear();
        
        // dirty the cache 
        invalidateCache();     
    }

	/**
     * Clear the id-&gt;object map.
     * 
     */
    public void removeAllObjectMap() {
        fObjectMap.clear();

        // dirty the cache    
        invalidateCache();           
    }
    
    /**
     * Remove the object with the given id.  This does not impact the 
     * id-&gt;list&lt;activity&gt; mappings.
     * 
     * @param id the id of the object to remove.  
     */
    public void removeObject(String id) {
        synchronized (fActiveObjects) {
            if (fActiveObjects.contains(fObjectMap.remove(id))) {
                // the cache contained the given object so invalidate it
                invalidateCache();          
            }
        }
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
