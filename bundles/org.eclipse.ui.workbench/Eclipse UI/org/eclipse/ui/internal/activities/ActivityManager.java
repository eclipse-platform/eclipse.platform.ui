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

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.DisposedException;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityEvent;
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

	private Map activitiesById = new WeakHashMap();
	private Set activitiesWithListeners = new HashSet();
	private Map activityDefinitionsById = new HashMap();
	private List activityManagerListeners;
	private IActivityRegistry activityRegistry;	
	private Set definedActivityIds = new HashSet();
	private Set enabledActivityIds = new HashSet();	
	private Map patternBindingsByActivityId = new HashMap();

	public ActivityManager() {
		this(new ExtensionActivityRegistry(Platform.getExtensionRegistry()));
	}

	public ActivityManager(IActivityRegistry activityRegistry) {
		if (activityRegistry == null)
			throw new NullPointerException();

		this.activityRegistry = activityRegistry;
		
		this.activityRegistry.addActivityRegistryListener(new IActivityRegistryListener() {
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

	public IActivity getActivity(String activityId) {
		if (activityId == null)
			throw new NullPointerException();
			
		Activity activity = (Activity) activitiesById.get(activityId);
		
		if (activity == null) {
			activity = new Activity(this, activityId);
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
			String activityId = (String) iterator.next();
			IActivity activity = getActivity(activityId);			
						
			if (activity.match(string))
				return true;
		}
			
		return false;
	}	

	public Set matches(String string, Set activityIds) {
		Set matches = new HashSet();
		activityIds = Util.safeCopy(activityIds, String.class);
		
		for (Iterator iterator = activityIds.iterator(); iterator.hasNext();) {			
			String activityId = (String) iterator.next();
			IActivity activity = getActivity(activityId);
			
			if (activity.match(string))
				matches.add(activityId);
		}
		
		return Collections.unmodifiableSet(matches);	
	}

	public void removeActivityManagerListener(IActivityManagerListener activityManagerListener) {
		if (activityManagerListener == null)
			throw new NullPointerException();
			
		if (activityManagerListeners != null)
			activityManagerListeners.remove(activityManagerListener);
	}
	
	public void setEnabledActivityIds(Set enabledActivityIds) {	
		enabledActivityIds = Util.safeCopy(enabledActivityIds, String.class);
		boolean activityManagerChanged = false;
		Map activityEventsByActivityId = null;

		if (!this.enabledActivityIds.equals(enabledActivityIds)) {
			this.enabledActivityIds = enabledActivityIds;
			activityManagerChanged = true;	
			activityEventsByActivityId = updateActivities(this.definedActivityIds);	
		}
		
		if (activityManagerChanged)
			fireActivityManagerChanged(new ActivityManagerEvent(this, false, true));

		if (activityEventsByActivityId != null)
			notifyActivities(activityEventsByActivityId);	
	}	
	
	Set getActivitiesWithListeners() {
		return activitiesWithListeners;
	}

	private void fireActivityManagerChanged(IActivityManagerEvent activityManagerEvent) {
		if (activityManagerEvent == null)
			throw new NullPointerException();
		
		if (activityManagerListeners != null)
			for (int i = 0; i < activityManagerListeners.size(); i++)
				((IActivityManagerListener) activityManagerListeners.get(i)).activityManagerChanged(activityManagerEvent);
	}

	private void notifyActivities(Map activityEventsByActivityId) {	
		for (Iterator iterator = activityEventsByActivityId.entrySet().iterator(); iterator.hasNext();) {	
			Map.Entry entry = (Map.Entry) iterator.next();			
			String activityId = (String) entry.getKey();
			IActivityEvent activityEvent = (IActivityEvent) entry.getValue();
			Activity activity = (Activity) activitiesById.get(activityId);
			
			if (activity != null)
				activity.fireActivityChanged(activityEvent);
		}
	}

	private void readRegistry() {
		Collection activityDefinitions = new ArrayList();
		activityDefinitions.addAll(activityRegistry.getActivityDefinitions());				
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

		Map patternBindingDefinitionsByActivityId = PatternBindingDefinition.patternBindingDefinitionsByActivityId(activityRegistry.getPatternBindingDefinitions());
		Map patternBindingsByActivityId = new HashMap();		

		for (Iterator iterator = patternBindingDefinitionsByActivityId.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String activityId = (String) entry.getKey();
			
			if (activityDefinitionsById.containsKey(activityId)) {			
				Collection patternBindingDefinitions = (Collection) entry.getValue();
				
				if (patternBindingDefinitions != null)
					for (Iterator iterator2 = patternBindingDefinitions.iterator(); iterator2.hasNext();) {
						IPatternBindingDefinition patternBindingDefinition = (IPatternBindingDefinition) iterator2.next();
						String pattern = patternBindingDefinition.getPattern();
					
						if (pattern != null && pattern.length() != 0) {
							IPatternBinding patternBinding = new PatternBinding(patternBindingDefinition.isInclusive(), Pattern.compile(pattern));	
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
		Set definedActivityIds = new HashSet(activityDefinitionsById.keySet());		

		if (!definedActivityIds.equals(this.definedActivityIds)) {
			this.definedActivityIds = definedActivityIds;
			activityManagerChanged = true;	
		}

		Map activityEventsByActivityId = updateActivities(activitiesById.keySet());	
		
		if (activityManagerChanged)
			fireActivityManagerChanged(new ActivityManagerEvent(this, true, false));

		if (activityEventsByActivityId != null)
			notifyActivities(activityEventsByActivityId);		
	}

	private IActivityEvent updateActivity(Activity activity) {
		IActivityDefinition activityDefinition = (IActivityDefinition) activityDefinitionsById.get(activity.getId());
		boolean definedChanged = activity.setDefined(activityDefinition != null);
		boolean descriptionChanged = activity.setDescription(activityDefinition != null ? activityDefinition.getDescription() : null);		
		boolean enabledChanged = activity.setEnabled(enabledActivityIds.contains(activity.getId()));
		boolean nameChanged = activity.setName(activityDefinition != null ? activityDefinition.getName() : null);
		boolean parentIdChanged = activity.setParentId(activityDefinition != null ? activityDefinition.getParentId() : null);				
		List patternBindings = (List) patternBindingsByActivityId.get(activity.getId());
		boolean patternBindingsChanged = activity.setPatternBindings(patternBindings != null ? patternBindings : Collections.EMPTY_LIST);

		if (definedChanged || descriptionChanged || enabledChanged || nameChanged || parentIdChanged || patternBindingsChanged)
			return new ActivityEvent(activity, definedChanged, descriptionChanged, enabledChanged, nameChanged, parentIdChanged, patternBindingsChanged); 
		else 
			return null;
	}

	private Map updateActivities(Collection activityIds) {
		Map activityEventsByActivityId = new TreeMap();
		
		for (Iterator iterator = activityIds.iterator(); iterator.hasNext();) {		
			String activityId = (String) iterator.next();					
			Activity activity = (Activity) activitiesById.get(activityId);
			
			if (activity != null) {
				IActivityEvent activityEvent = updateActivity(activity);
				
				if (activityEvent != null)
					activityEventsByActivityId.put(activityId, activityEvent);
			}
		}
		
		return activityEventsByActivityId;			
	}
}
