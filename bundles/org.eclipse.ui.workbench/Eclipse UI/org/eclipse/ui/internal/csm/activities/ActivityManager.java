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

package org.eclipse.ui.internal.csm.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.csm.activities.api.IActivity;
import org.eclipse.ui.internal.csm.activities.api.IActivityManager;
import org.eclipse.ui.internal.csm.activities.api.IActivityManagerEvent;
import org.eclipse.ui.internal.csm.activities.api.IActivityManagerListener;
import org.eclipse.ui.internal.util.Util;

public final class ActivityManager implements IActivityManager {

	public static boolean isActivityDefinitionChildOf(String ancestor, String id, Map activityDefinitionsById) {
		Set visited = new HashSet();

		while (id != null && !visited.contains(id)) {
			IActivityDefinition activityDefinition = (IActivityDefinition) activityDefinitionsById.get(id);				
			visited.add(id);

			if (activityDefinition != null && Util.equals(id = activityDefinition.getParentId(), ancestor))
				return true;
		}

		return false;
	}	

	private Set activeActivityIds = new TreeSet();
	private IActivityManagerEvent activityManagerEvent;
	private List activityManagerListeners;
	private SortedMap activityDefinitionsById = new TreeMap();
	private SortedMap activitiesById = new TreeMap();
	private SortedSet definedActivityIds = new TreeSet();
	private PluginActivityRegistry pluginActivityRegistry;
	private PreferenceActivityRegistry preferenceActivityRegistry;

	public ActivityManager() {
		if (pluginActivityRegistry == null)
			pluginActivityRegistry = new PluginActivityRegistry(Platform.getPluginRegistry());
			
		loadPluginActivityRegistry();		

		pluginActivityRegistry.addActivityRegistryListener(new IActivityRegistryListener() {
			public void activityRegistryChanged(IActivityRegistryEvent activityRegistryEvent) {
				readRegistry();
			}
		});

		if (preferenceActivityRegistry == null)
			preferenceActivityRegistry = new PreferenceActivityRegistry(WorkbenchPlugin.getDefault().getPreferenceStore());	

		loadPreferenceActivityRegistry();

		preferenceActivityRegistry.addActivityRegistryListener(new IActivityRegistryListener() {
			public void activityRegistryChanged(IActivityRegistryEvent activityRegistryEvent) {
				readRegistry();
			}
		});
		
		readRegistry();
	}

	public void addActivityManagerListener(IActivityManagerListener activityManagerListener) {
		if (activityManagerListener == null)
			throw new NullPointerException();
			
		if (activityManagerListeners == null)
			activityManagerListeners = new ArrayList();
		
		if (!activityManagerListeners.contains(activityManagerListener))
			activityManagerListeners.add(activityManagerListener);
	}

	public Set getActiveActivityIds() {
		return Collections.unmodifiableSet(activeActivityIds);
	}

	public IActivity getActivity(String activityId) {
		if (activityId == null)
			throw new NullPointerException();
			
		Activity activity = (Activity) activitiesById.get(activityId);
		
		if (activity == null) {
			activity = new Activity(activityId);
			updateActivity(activity);
			activitiesById.put(activityId, activity);
		}
		
		return activity;
	}
	
	public SortedSet getDefinedActivityIds() {
		return Collections.unmodifiableSortedSet(definedActivityIds);
	}

	public void removeActivityManagerListener(IActivityManagerListener activityManagerListener) {
		if (activityManagerListener == null)
			throw new NullPointerException();
			
		if (activityManagerListeners != null)
			activityManagerListeners.remove(activityManagerListener);
	}

	public void setActiveActivityIds(Set activeActivityIds) {
		activeActivityIds = Util.safeCopy(activeActivityIds, String.class);
		boolean activityManagerChanged = false;
		SortedSet updatedActivityIds = null;

		if (!this.activeActivityIds.equals(activeActivityIds)) {
			this.activeActivityIds = activeActivityIds;
			activityManagerChanged = true;	
			updatedActivityIds = updateActivitys(this.definedActivityIds);	
		}
		
		if (activityManagerChanged)
			fireActivityManagerChanged();

		if (updatedActivityIds != null)
			notifyActivitys(updatedActivityIds);	
	}

	// TODO private
	public IActivityRegistry getPluginActivityRegistry() {
		return pluginActivityRegistry;
	}

	// TODO private
	public IActivityRegistry getPreferenceActivityRegistry() {
		return preferenceActivityRegistry;
	}

	private void loadPluginActivityRegistry() {
		try {
			pluginActivityRegistry.load();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}
	}
	
	private void loadPreferenceActivityRegistry() {
		try {
			preferenceActivityRegistry.load();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}		
	}

	private void fireActivityManagerChanged() {
		if (activityManagerListeners != null) {
			for (int i = 0; i < activityManagerListeners.size(); i++) {
				if (activityManagerEvent == null)
					activityManagerEvent = new ActivityManagerEvent(this);
								
				((IActivityManagerListener) activityManagerListeners.get(i)).activityManagerChanged(activityManagerEvent);
			}				
		}			
	}

	private void notifyActivitys(Collection activityIds) {	
		Iterator iterator = activityIds.iterator();
		
		while (iterator.hasNext()) {
			String activityId = (String) iterator.next();					
			Activity activity = (Activity) activitiesById.get(activityId);
			
			if (activity != null)
				activity.fireActivityChanged();
		}
	}

	private void readRegistry() {
		List activityDefinitions = new ArrayList();
		activityDefinitions.addAll(pluginActivityRegistry.getActivityDefinitions());
		activityDefinitions.addAll(preferenceActivityRegistry.getActivityDefinitions());
		SortedMap activityDefinitionsById = new TreeMap(ActivityDefinition.activityDefinitionsById(activityDefinitions, false));

		for (Iterator iterator = activityDefinitionsById.values().iterator(); iterator.hasNext();) {
			IActivityDefinition activityDefinition = (IActivityDefinition) iterator.next();
			String name = activityDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}

		for (Iterator iterator = activityDefinitionsById.keySet().iterator(); iterator.hasNext();)
			if (!isActivityDefinitionChildOf(null, (String) iterator.next(), activityDefinitionsById))
				iterator.remove();

		SortedSet definedActivityIds = new TreeSet(activityDefinitionsById.keySet());		
		boolean activityManagerChanged = false;

		if (!this.definedActivityIds.equals(definedActivityIds)) {
			this.definedActivityIds = definedActivityIds;
			activityManagerChanged = true;	
		}

		this.activityDefinitionsById = activityDefinitionsById;
		SortedSet updatedActivityIds = updateActivitys(this.definedActivityIds);	
		
		if (activityManagerChanged)
			fireActivityManagerChanged();

		if (updatedActivityIds != null)
			notifyActivitys(updatedActivityIds);		
	}

	private boolean updateActivity(Activity activity) {
		boolean updated = false;
		updated |= activity.setActive(activeActivityIds.contains(activity.getId()));
		IActivityDefinition activityDefinition = (IActivityDefinition) activityDefinitionsById.get(activity.getId());
		updated |= activity.setDefined(activityDefinition != null);
		updated |= activity.setDescription(activityDefinition != null ? activityDefinition.getDescription() : null);
		updated |= activity.setName(activityDefinition != null ? activityDefinition.getName() : null);
		return updated;
	}

	private SortedSet updateActivitys(Collection activityIds) {
		SortedSet updatedIds = new TreeSet();
		Iterator iterator = activityIds.iterator();
		
		while (iterator.hasNext()) {
			String activityId = (String) iterator.next();					
			Activity activity = (Activity) activitiesById.get(activityId);
			
			if (activity != null && updateActivity(activity))
				updatedIds.add(activityId);			
		}
		
		return updatedIds;			
	}
}