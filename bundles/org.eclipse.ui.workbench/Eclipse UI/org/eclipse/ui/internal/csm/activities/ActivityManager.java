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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IPatternBinding;
import org.eclipse.ui.internal.util.Util;

public final class ActivityManager implements IActivityManager {

	static boolean isActivityDefinitionChildOf(String ancestor, String id, Map activityDefinitionsById) {
		Collection visited = new HashSet();

		while (id != null && !visited.contains(id)) {
			IActivityDefinition activityDefinition = (IActivityDefinition) activityDefinitionsById.get(id);				
			visited.add(id);

			if (activityDefinition != null && Util.equals(id = activityDefinition.getParentId(), ancestor))
				return true;
		}

		return false;
	}	

	private Set activeActivityIds = new HashSet();
	private Map activitiesById = new HashMap();
	private Map activityDefinitionsById = new HashMap();
	private IActivityManagerEvent activityManagerEvent;
	private List activityManagerListeners;
	private Set definedActivityIds = new HashSet();
	private Set enabledActivityIds = new HashSet();	
	private ExtensionActivityRegistry extensionActivityRegistry;	
	private Map patternBindingsByActivityId = new HashMap();

	public ActivityManager() {
		if (extensionActivityRegistry == null)
			extensionActivityRegistry = new ExtensionActivityRegistry(Platform.getExtensionRegistry());

		extensionActivityRegistry.addActivityRegistryListener(new IActivityRegistryListener() {
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
	
	public Set getDefinedActivityIds() {
		return Collections.unmodifiableSet(definedActivityIds);
	}

	public Set getEnabledActivityIds() {
		return Collections.unmodifiableSet(enabledActivityIds);
	}	

	public boolean match(String string, Set activityIds) {
		activityIds = Util.safeCopy(activityIds, String.class);
		
		for (Iterator iterator = activityIds.iterator(); iterator.hasNext();) {			
			IActivity activity = getActivity((String) iterator.next());
						
			if (activity.match(string))
				return true;
		}
			
		return false;
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
		Collection updatedActivityIds = null;

		if (!this.activeActivityIds.equals(activeActivityIds)) {
			this.activeActivityIds = activeActivityIds;
			activityManagerChanged = true;	
			updatedActivityIds = updateActivities(activitiesById.keySet());	
		}
		
		if (activityManagerChanged)
			fireActivityManagerChanged();

		if (updatedActivityIds != null)
			notifyActivities(updatedActivityIds);	
	}
	
	public void setEnabledActivityIds(Set enabledActivityIds) {	
		enabledActivityIds = Util.safeCopy(enabledActivityIds, String.class);
		boolean activityManagerChanged = false;
		Collection updatedActivityIds = null;

		if (!this.enabledActivityIds.equals(enabledActivityIds)) {
			this.enabledActivityIds = enabledActivityIds;
			activityManagerChanged = true;	
			updatedActivityIds = updateActivities(this.definedActivityIds);	
		}
		
		if (activityManagerChanged)
			fireActivityManagerChanged();

		if (updatedActivityIds != null)
			notifyActivities(updatedActivityIds);	
	}	
	
	private void fireActivityManagerChanged() {
		if (activityManagerListeners != null)
			for (int i = 0; i < activityManagerListeners.size(); i++) {
				if (activityManagerEvent == null)
					activityManagerEvent = new ActivityManagerEvent(this);
								
				((IActivityManagerListener) activityManagerListeners.get(i)).activityManagerChanged(activityManagerEvent);
			}				
	}
	
	private void notifyActivities(Collection activityIds) {	
		for (Iterator iterator = activityIds.iterator(); iterator.hasNext();) {	
			String activityId = (String) iterator.next();					
			Activity activity = (Activity) activitiesById.get(activityId);
			
			if (activity != null)
				activity.fireActivityChanged();
		}
	}

	private void readRegistry() {
		Collection activityDefinitions = new ArrayList();
		activityDefinitions.addAll(extensionActivityRegistry.getActivityDefinitions());				
		Map activityDefinitionsById = new HashMap(ActivityDefinition.activityDefinitionsById(activityDefinitions, false));

		for (Iterator iterator = activityDefinitionsById.values().iterator(); iterator.hasNext();) {
			IActivityDefinition activityDefinition = (IActivityDefinition) iterator.next();
			String name = activityDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}

		for (Iterator iterator = activityDefinitionsById.keySet().iterator(); iterator.hasNext();)
			if (!isActivityDefinitionChildOf(null, (String) iterator.next(), activityDefinitionsById))
				iterator.remove();

		Map activityPatternBindingDefinitionsByActivityId = ActivityPatternBindingDefinition.activityPatternBindingDefinitionsByActivityId(extensionActivityRegistry.getActivityPatternBindingDefinitions());
		Map patternBindingsByActivityId = new HashMap();		

		for (Iterator iterator = activityPatternBindingDefinitionsByActivityId.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String activityId = (String) entry.getKey();
			
			if (activityDefinitionsById.containsKey(activityId)) {			
				Collection activityPatternBindingDefinitions = (Collection) entry.getValue();
				
				if (activityPatternBindingDefinitions != null)
					for (Iterator iterator2 = activityPatternBindingDefinitions.iterator(); iterator2.hasNext();) {
						IActivityPatternBindingDefinition activityPatternBindingDefinition = (IActivityPatternBindingDefinition) iterator2.next();
						String pattern = activityPatternBindingDefinition.getPattern();
					
						if (pattern != null && pattern.length() != 0) {
							IPatternBinding patternBinding = new PatternBinding(activityPatternBindingDefinition.isInclusive(), pattern);	
							List patternBindings = (List) patternBindingsByActivityId.get(activityId);
							
							if (patternBindings == null) {
								patternBindings = new ArrayList();
								patternBindingsByActivityId.put(activityId, patternBindings);
							}
							
							patternBindings.add(patternBinding);
						}
					}
			}
		}		
		
		this.activityDefinitionsById = activityDefinitionsById;
		this.patternBindingsByActivityId = patternBindingsByActivityId;			
		boolean activityManagerChanged = false;			
		Set definedActivityIds = new TreeSet(activityDefinitionsById.keySet());		

		if (!definedActivityIds.equals(this.definedActivityIds)) {
			this.definedActivityIds = definedActivityIds;
			activityManagerChanged = true;	
		}

		Collection updatedActivityIds = updateActivities(activitiesById.keySet());	
		
		if (activityManagerChanged)
			fireActivityManagerChanged();

		if (updatedActivityIds != null)
			notifyActivities(updatedActivityIds);		
	}

	private boolean updateActivity(Activity activity) {
		boolean updated = false;
		updated |= activity.setActive(activeActivityIds.contains(activity.getId()));		
		IActivityDefinition activityDefinition = (IActivityDefinition) activityDefinitionsById.get(activity.getId());
		updated |= activity.setDefined(activityDefinition != null);
		updated |= activity.setDescription(activityDefinition != null ? activityDefinition.getDescription() : null);		
		updated |= activity.setEnabled(enabledActivityIds.contains(activity.getId()));
		updated |= activity.setName(activityDefinition != null ? activityDefinition.getName() : null);
		updated |= activity.setParentId(activityDefinition != null ? activityDefinition.getParentId() : null);				
		List patternBindings = (List) patternBindingsByActivityId.get(activity.getId());
		updated |= activity.setPatternBindings(patternBindings != null ? patternBindings : Collections.EMPTY_LIST);
		return updated;
	}

	private Collection updateActivities(Collection activityIds) {
		Collection updatedIds = new TreeSet();
		
		for (Iterator iterator = activityIds.iterator(); iterator.hasNext();) {		
			String activityId = (String) iterator.next();					
			Activity activity = (Activity) activitiesById.get(activityId);
			
			if (activity != null && updateActivity(activity))
				updatedIds.add(activityId);			
		}
		
		return updatedIds;			
	}
}
