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
package org.eclipse.ui.internal.roles;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.ui.activities.*;

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
 * @since 3.0
 */
public class ObjectActivityManager implements IActivityListener, IObjectActivityManager{

    /**
     * The map of all known managers.
     */
    private static Map managersMap = new HashMap(17);    

    /**
     * Get the manager for a given id, optionally creating it if it doesn't 
     * exist.
     * 
     * @param id
     * @param create
     * @return
     * @since 3.0
     */
    public static ObjectActivityManager getManager(String id, boolean create) {
        ObjectActivityManager manager = (ObjectActivityManager) managersMap.get(id);
        if (manager == null && create) {
            manager = new ObjectActivityManager(id);
            managersMap.put(id, manager);
        }
        return manager;
    }
    
    /**
     * @param id
     * @return whether an ObjectActivityManager with the given ID has been 
     * created.
     * @since 3.0
     */
    public static boolean managerExists(String id) {
        return managersMap.containsKey(id); 
    }
    
    /**
     * Map of id-&gt;list&lt;activity&gt;.
     */
    private Map activityMap = new HashMap();

    /**
     * Unique ID for this manager.  
     */
    private String managerId;

    /**
     * Map of id-&gt;object.
     */
    private Map objectMap = new HashMap();
    
    /**
     * The cache of currently active objects.  This is also the synchronization
     * lock for all cache operations.
     */
    private Collection activeObjects = new HashSet(17);
    
    /**
     * Whether the active objects set is stale due to Activity enablement 
     * changes or object/binding additions.
     */
    private boolean dirty = true;

    /**
     * Create an instance with the given id.
     * 
     * @param id
     */
    private ObjectActivityManager(String id) {
        if (id == null) {
            throw new IllegalArgumentException();
        }
        
        managerId = id;
    }
    
    /**
     * Changes in activity status impact the cache of enabled objects.  Mark the 
     * object cache as being dirty.
     * 
     * @see org.eclipse.ui.internal.roles.IActivityListener#activityChanged(org.eclipse.ui.internal.roles.ActivityEvent)
     * @since 3.0
     */
    public void activityChanged(ActivityEvent event) {
        invalidateCache();      
    }    
    
    /**
     * Adds a binding between object-&gt;activity.  If the given activity is not
     * defined in the RoleManager registry then no action is taken.
     * 
     * @param objectId
     * @param activityId
     * @since 3.0
     */
    public void addActivityBinding(IObjectContributionRecord record, String activityId) {
        if (record == null || activityId == null) {
            throw new IllegalArgumentException();
        }    
        
        Activity activity = RoleManager.getInstance().getActivity(activityId); 
        if (activity == null) {
            return;
        }               
            
        Collection bindings = getActivityIdsFor(record, true);
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
     * @param pluginId The plugin id
     * @param localId The local id
     * @param object The object being added
     * @return the ObjectContributionRecord that was used as a key to store the 
     * provided object.
     * @since 3.0
     */
    public IObjectContributionRecord addObject(String pluginId, String localId, Object object ) {
    	
        if (pluginId == null || localId == null || object == null) {
            throw new IllegalArgumentException();
        }

        IObjectContributionRecord record = new ObjectContributionRecord(pluginId, localId);
        Object oldObject = objectMap.put(record, object);

        if (!object.equals(oldObject)) {
            // dirty the cache if the old entry is not the same as the new one.
            invalidateCache();
        }  
        return record;          
    }
    
    /**
     * Find the (first) ObjectContributionRecord that maps to the given object, 
     * or null.     
     * 
     * @param objectOfInterest
     * @return ObjectContributionRecord or <code>null</code>
     */
    private IObjectContributionRecord findObjectContributionRecord(Object objectOfInterest) {
        for (Iterator i = objectMap.entrySet().iterator(); i.hasNext(); ) {    
            Map.Entry entry = (Entry) i.next();
            if (entry.getValue().equals(objectOfInterest)) {
                return (IObjectContributionRecord) entry.getKey();
            }
        }
        return null;
    }

    /**
     * Return a set of objects that are currently valid based on the active
     * activities, or all objects if role filtering is currently disabled.  
     * 
     * @return Collection
     * @since 3.0
     */
    public Collection getActiveObjects() {
        synchronized (activeObjects) {
            if (RoleManager.getInstance().isFiltering()) {
                if (dirty) {
                    activeObjects.clear();      
                    Collection activeActivities = getActivityIds();
                    for (Iterator iter = objectMap.entrySet().iterator(); iter.hasNext();) {
                        Map.Entry entry = (Entry) iter.next();
                        IObjectContributionRecord record = (IObjectContributionRecord) entry.getKey();
						Collection activitiesForId = getActivityIdsFor(record, false);
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
                    
                    dirty = false;
                }
                return Collections.unmodifiableCollection(activeObjects);
            }
            else {
                // TBD: this logic can probably be moved into the caching logic.  
                return new HashSet(objectMap.values());            
            }
        }
    }

    /**
     * Return the list of enabled activities as provided by the RoleManager.
     * @return Collection
     */
    private Collection getActivityIds() {
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
     * @return Collection
     */
    private Collection getActivityIdsFor(IObjectContributionRecord record, boolean create) {
        Collection set = (Collection) activityMap.get(record);
        if (set == null && create) {
            set = new HashSet();
            activityMap.put(record, set);
        }
        return set;
    }
    
    /**
     * Get the unique identifier for this manager.
     * 
     * @return the unique identifier for this manager.
     * @since 3.0
     */
    public String getId() {
        return managerId;
    }
    
    /**
     * Get the Set of ObjectContributionRecord keys from the object store.  This
     * Set is read only.
     * 
     * @return
     */
    Set getObjectIds() {
        return Collections.unmodifiableSet(objectMap.keySet());
    }

    /**
     * Mark the cache for recalculation.
     */
    private void invalidateCache() {
        synchronized (activeObjects) {   
            dirty = true;
        }
    }
    
    /**
     * Remove all bindings that map to the given id.  This does not impact the 
     * id-&gt;object mappings.
     * 
     * TBD: should remove be supported?
     * 
     * @param id the id of the object to remove bindings for.
     * @since 3.0  
     */
    public void removeActivityBinding(IObjectContributionRecord id) {
        activityMap.remove(id);
    }
    
	/**
     * Clear both the id-&gt;object and id-&gt;list&lt;activity&gt; mappings.
     * 
     * @since 3.0 
     */
    public void removeAll() {
        removeAllObjectMap();
        removeAllActivityMap();
    }

    /**
     * Clear the id-&gt;list&lt;activity&gt; map and deregister our 
     * ActivityListeners
     * 
     * @since 3.0
     */
    public void removeAllActivityMap() {
        for (Iterator i = activityMap.values().iterator(); i.hasNext(); ) {
            String activityId = (String) i.next();
            Activity activity = RoleManager.getInstance().getActivity(activityId);
            if (activity != null) {
                // clean up our listener
                activity.removeListener(this);
            }
        }
        activityMap.clear();
        
        // dirty the cache 
        invalidateCache();     
    }

	/**
     * Clear the id-&gt;object map.
     * 
     * @since 3.0
     */
    public void removeAllObjectMap() {
        objectMap.clear();

        // dirty the cache    
        invalidateCache();           
    }
    
    /**
     * Remove the object with the given id.  This does not impact the 
     * id-&gt;list&lt;activity&gt; mappings.
     * 
     * @param id the id of the object to remove.
     * @since 3.0  
     */
    public void removeObject(IObjectContributionRecord id) {
        synchronized (activeObjects) {
            if (activeObjects.contains(objectMap.remove(id))) {
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
     * @param objectOfInterest The Object to enable or disable.
     * @param enablement 
     * @since 3.0
     */
    public void setEnablementFor(Object objectOfInterest, boolean enablement) {
        IObjectContributionRecord record = findObjectContributionRecord(objectOfInterest);
        if (record != null) {
			Collection activities = getActivityIdsFor(record, false); 
            if (activities != null && activities.size() > 0) {           
                RoleManager.getInstance().setEnabled(RoleManager.getInstance().toActivityArray((String []) activities.toArray(new String [activities.size()])), enablement);
            }
        }
    }
    
	/**
	 * Apply default pattern bindings to a subset of keys governed by the given 
	 * manager.
	 * 
	 * @param keys a Collection of ObjectContributionRecords on which to apply patterns.
	 * @since 3.0
	 */
    public void applyPatternBindings(Collection keys) {
   	
		Map patternBindings = RoleManager.getInstance().getPatternBindings();
		for (Iterator i = keys.iterator(); i.hasNext();) {
			IObjectContributionRecord record = (IObjectContributionRecord) i.next();
			String objectKey = record.toString();
			for (Iterator j = patternBindings.entrySet().iterator(); j.hasNext();) {
				Map.Entry patternEntry = (Map.Entry) j.next();
				if (objectKey.matches((String) patternEntry.getKey())) {
					Collection activityIds = (Collection) patternEntry.getValue();
					for (Iterator k = activityIds.iterator(); k.hasNext();) {
						String activityId = (String) k.next();
						if (RoleManager.getInstance().getActivity(activityId) != null) {
							addActivityBinding(record, activityId);
						}
					}
				}
			}
		}
	}
   
	/* (non-Javadoc)
	 * @see org.eclipse.ui.activities.IObjectActivityManager#applyPatternBindings()
	 */
	public void applyPatternBindings() {
		applyPatternBindings(getObjectIds());
	}
   
	/* (non-Javadoc)
	 * @see org.eclipse.ui.activities.IObjectActivityManager#applyPatternBindings(org.eclipse.ui.internal.roles.ObjectContributionRecord)
	 */
	public void applyPatternBindings(IObjectContributionRecord record) {
		applyPatternBindings(Collections.singleton(record));
	
	}        
}
