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
package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityEvent;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityListener;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.activities.IObjectActivityManager;
import org.eclipse.ui.activities.IObjectContributionRecord;

/**
 * Provides a registry of id-&gt;object mappings (likely derived from extension
 * point contributions), id-&gt;activity mappings, and a means of filtering the
 * object registry based on the currently enabled activities.
 * 
 * This functionality is currently implemented by calculating the filtered set
 * only when activity changes dictate that the cache is invalid. In a stable
 * system (one in which activities of interest are not enabling and disabling
 * themselves with any great rate and in which new objects and bindings are not
 * being added often) then this calculation should need to be performed
 * infrequently.
 * 
 * Some behaviour of this class assumes that when an object is added to the
 * cache the bindings will be added either immediately or shortly (after a
 * batch of objects is added, for instance). It is not possible to reliably use
 * this class with only a subset of activity bindings - the full set of
 * bindings may be applied at any time based on certain activity changes.
 * 
 * This object will respond to changes in defined activities (required by
 * dynamic plugin support). When a change event is provided by the activity
 * manager this object will call <code>applyPatternBindings</code> to remove
 * bindings to stale activities. This will <strong>NOT</strong> remove stale
 * objects from the collection managed by this manager. Anyone who makes use of
 * this system should monitor for plugin changes and update the objects in this
 * manager as required.
 * 
 * @since 3.0
 */
public class ObjectActivityManager implements IObjectActivityManager {

	/**
	 * The map of all known managers.
	 */
	private static Map managersMap = new HashMap(17);

	/**
	 * Get the manager for a given id, optionally creating it if it doesn't
	 * exist.
	 * 
	 * @param id
	 *            the unique ID of the manager that is being sought.
	 * @param create
	 *            force creation if the manager does not yet exist.
	 */
	public static ObjectActivityManager getManager(String id, boolean create) {
		ObjectActivityManager manager =
			(ObjectActivityManager) managersMap.get(id);
		if (manager == null && create) {
			// TODO cast
			manager =
				new ObjectActivityManager(
					id,
					(IMutableActivityManager) PlatformUI
						.getWorkbench()
						.getActivityManager());
			managersMap.put(id, manager);
		}
		return manager;
	}

	/**
	 * The cache of currently active objects. This is also the synchronization
	 * lock for all cache operations.
	 */
	private Collection activeObjects = new HashSet(17);

	/**
	 * Listener that is responsible for invalidating the cache on messages from
	 * <code>IActivity</code> objects.
	 */
	private IActivityListener activityListener = new IActivityListener() {
		public void activityChanged(ActivityEvent activityEvent) {
			invalidateCache();
		}
	};

	/**
	 * The <code>IMutableActivityManager</code> to which this manager is
	 * bound.
	 */
	private IMutableActivityManager activityManager;

	/**
	 * The <code>IActivityManagerListener</code> that monitors for defined
	 * activities entering/leaving the system. Any change will result in
	 * recalculating the pattern bindings. It would be possible to selectivly
	 * clear and re-apply bindings but it's probably more effort than it's
	 * worth. Just clear and re-create them all.
	 */
	private IActivityManagerListener activityManagerListener =
		new IActivityManagerListener() {
		public void activityManagerChanged(ActivityManagerEvent activityManagerEvent) {
			if (activityManagerEvent.haveDefinedActivityIdsChanged()) {
				// if the defined set has changed, then reapply all patterns
				// and dirty the cache.
				removePatternBindings();
				applyPatternBindings();
			}
		}
	};

	/**
	 * Map of id-&gt;list&lt;activity&gt;.
	 */
	private Map activityMap = new HashMap();

	/**
	 * Whether the active objects set is stale due to Activity enablement
	 * changes or object/binding additions.
	 */
	private boolean dirty = true;

	/**
	 * Unique ID for this manager.
	 */
	private String managerId;

	/**
	 * Map of id-&gt;object.
	 */
	private Map objectMap = new HashMap();

	/**
	 * Create an instance with the given id that is bound to the provided
	 * managers.
	 * 
	 * @param id
	 *            the unique identifier for this manager.
	 * @param activityManager
	 *            the <code>IMutableActivityManager</code> to bind to.
	 * @param roleManager
	 *            the <code>IRoleManager</code> to bind to.
	 */
	public ObjectActivityManager(
		String id,
		IMutableActivityManager activityManager) {
		if (id == null) {
			throw new IllegalArgumentException();
		}

		managerId = id;

		this.activityManager = activityManager;

		activityManager.addActivityManagerListener(activityManagerListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.IObjectActivityManager#addActivityBinding(org.eclipse.ui.activities.IObjectContributionRecord,
	 *      java.lang.String)
	 */
	public void addActivityBinding(
		IObjectContributionRecord record,
		String activityId) {
		if (record == null || activityId == null) {
			throw new IllegalArgumentException();
		}

		IActivity activity = activityManager.getActivity(activityId);
		if (activity == null || !activity.isDefined()) {
			return;
		}

		Collection bindings = getActivityIdsFor(record, true);
		if (bindings.add(activityId)) {
			// if we havn't already bound this activity do so and invalidate
			// the cache
			activity.addActivityListener(activityListener);
			invalidateCache();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.IObjectActivityManager#addObject(java.lang.String,
	 *      java.lang.String, java.lang.Object)
	 */
	public IObjectContributionRecord addObject(
		String pluginId,
		String localId,
		Object object) {
		if (pluginId == null || localId == null || object == null) {
			throw new IllegalArgumentException();
		}

		IObjectContributionRecord record =
			new ObjectContributionRecord(pluginId, localId);
		Object oldObject = objectMap.put(record, object);

		if (!object.equals(oldObject)) {
			// dirty the cache if the old entry is not the same as the new one.
			invalidateCache();
		}
		return record;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.IObjectActivityManager#applyPatternBindings()
	 */
	public void applyPatternBindings() {
		applyPatternBindings(getObjectIds());
	}

	/**
	 * Apply pattern bindings to a collection of objects within this manager.
	 * 
	 * @param objectIds
	 *            a collection containing <code>IObjectContributionRecords</code>
	 * @since 3.0
	 */
	void applyPatternBindings(Collection objectIds) {
		Collection activities = activityManager.getDefinedActivityIds();

		for (Iterator actItr = activities.iterator(); actItr.hasNext();) {
			IActivity activity =
				activityManager.getActivity((String) actItr.next());

			for (Iterator objItr = objectIds.iterator(); objItr.hasNext();) {
				IObjectContributionRecord objectId =
					(IObjectContributionRecord) objItr.next();

				if (activity.match(objectId.toString())) {
					addActivityBinding(objectId, activity.getId());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.IObjectActivityManager#applyPatternBindings(org.eclipse.ui.activities.IObjectContributionRecord)
	 */
	public void applyPatternBindings(IObjectContributionRecord record) {
		applyPatternBindings(Collections.singleton(record));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.IObjectActivityManager#findObjectContributionRecords(java.lang.Object)
	 */
	public Collection findObjectContributionRecords(Object objectOfInterest) {
		ArrayList collection = new ArrayList();
		for (Iterator i = objectMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Entry) i.next();
			if (entry.getValue().equals(objectOfInterest)) {
				collection.add(entry.getKey());
			}
		}
		return collection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.IObjectActivityManager#getActiveObjects()
	 */
	public Collection getActiveObjects() {
		synchronized (activeObjects) {
			if (!activityManager.getDefinedCategoryIds().isEmpty()) {
				if (dirty) {
					activeObjects.clear();
					Collection activeActivities =
						activityManager.getEnabledActivityIds();
					for (Iterator iter = objectMap.entrySet().iterator();
						iter.hasNext();
						) {
						Map.Entry entry = (Entry) iter.next();
						IObjectContributionRecord record =
							(IObjectContributionRecord) entry.getKey();
						Collection activitiesForId =
							getActivityIdsFor(record, false);
						if (activitiesForId == null) {
							activeObjects.add(entry.getValue());
						} else {
							Set activitiesForIdCopy =
								new HashSet(activitiesForId);
							activitiesForIdCopy.retainAll(activeActivities);
							if (!activitiesForIdCopy.isEmpty()) {
								activeObjects.add(entry.getValue());
							}
						}
					}

					dirty = false;
				}
				return Collections.unmodifiableCollection(activeObjects);
			} else {
				return Collections.unmodifiableCollection(objectMap.values());
			}
		}
	}

	/**
	 * Return the activity set for the given record, creating and inserting one
	 * if requested.
	 * 
	 * @param record
	 *            the record to search for.
	 * @param create
	 *            create the activity set if none is found.
	 * @return the set of activities bound to the given record.
	 */
	private Set getActivityIdsFor(
		IObjectContributionRecord record,
		boolean create) {
		Set set = (Set) activityMap.get(record);
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
	 */
	public String getId() {
		return managerId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.IObjectActivityManager#getObjectIds()
	 */
	public Set getObjectIds() {
		return Collections.unmodifiableSet(objectMap.keySet());
	}

	/**
	 * Mark the cache for recalculation.
	 */
	protected void invalidateCache() {
		synchronized (activeObjects) {
			dirty = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.IObjectActivityManager#removeObject(org.eclipse.ui.activities.IObjectContributionRecord)
	 */
	public void removeObject(IObjectContributionRecord record) {
		synchronized (activeObjects) {
			objectMap.remove(record);
			activityMap.remove(record);
			invalidateCache();
		}
	}

	/**
	 * Remove all activity bindings.
	 */
	protected void removePatternBindings() {
		for (Iterator i = activityMap.values().iterator(); i.hasNext();) {
			i.remove();
		}
		invalidateCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.IObjectActivityManager#setEnablementFor(java.lang.Object,
	 *      boolean)
	 */
	public void setEnablementFor(Object objectOfInterest, boolean enablement) {
		Collection records = findObjectContributionRecords(objectOfInterest);
		for (Iterator i = records.iterator(); i.hasNext();) {
			IObjectContributionRecord record =
				(IObjectContributionRecord) i.next();
			Set activities = getActivityIdsFor(record, false);
			if (activities != null && activities.size() > 0) {
				Set oldActivities = activityManager.getEnabledActivityIds();
				Set newActivities = new HashSet(oldActivities);
				if (enablement) {
					newActivities.addAll(activities);
				} else {
					newActivities.removeAll(activities);
				}
				activityManager.setEnabledActivityIds(newActivities);
			}
		}
	}
}
