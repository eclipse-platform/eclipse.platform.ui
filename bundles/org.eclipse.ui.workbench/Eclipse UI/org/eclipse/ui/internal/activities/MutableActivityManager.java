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
import org.eclipse.ui.activities.ActivityEvent;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.CategoryEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityBinding;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.activities.IPatternBinding;
import org.eclipse.ui.internal.util.Util;

public final class MutableActivityManager
	extends AbstractActivityManager
	implements IMutableActivityManager {

	static boolean isActivityDefinitionChildOf(
		String ancestor,
		String id,
		Map activityDefinitionsById) {
		Collection visited = new HashSet();

		while (id != null && !visited.contains(id)) {
			IActivityDefinition activityDefinition =
				(IActivityDefinition) activityDefinitionsById.get(id);
			visited.add(id);

			if (activityDefinition != null
				&& Util.equals(id = activityDefinition.getParentId(), ancestor))
				return true;
		}

		return false;
	}

	private Map activitiesById = new WeakHashMap();
	private Set activitiesWithListeners = new HashSet();
	private Map activityDefinitionsById = new HashMap();
	private IActivityRegistry activityRegistry;
	private Set definedActivityIds = new HashSet();
	private Set enabledActivityIds = new HashSet();
	private Map patternBindingsByActivityId = new HashMap();
	private Map categoriesById = new WeakHashMap();
	private Set categoriesWithListeners = new HashSet();
	private Map categoryDefinitionsById = new HashMap();
	private Set definedCategoryIds = new HashSet();
	private Set enabledCategoryIds = new HashSet();
	private Map activityBindingsByCategoryId = new HashMap();

	public MutableActivityManager() {
		this(new ExtensionActivityRegistry(Platform.getExtensionRegistry()));
	}

	public MutableActivityManager(IActivityRegistry activityRegistry) {
		if (activityRegistry == null)
			throw new NullPointerException();

		this.activityRegistry = activityRegistry;

		this
			.activityRegistry
			.addActivityRegistryListener(new IActivityRegistryListener() {
			public void activityRegistryChanged(ActivityRegistryEvent activityRegistryEvent) {
				readRegistry();
			}
		});

		readRegistry();
	}

	Set getActivitiesWithListeners() {
		return activitiesWithListeners;
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

	Set getCategoriesWithListeners() {
		return categoriesWithListeners;
	}

	public ICategory getCategory(String categoryId) {
		if (categoryId == null)
			throw new NullPointerException();

		Category category = (Category) categoriesById.get(categoryId);

		if (category == null) {
			category = new Category(this, categoryId);
			updateCategory(category);
			categoriesById.put(categoryId, category);
		}

		return category;
	}

	public Set getDefinedCategoryIds() {
		return Collections.unmodifiableSet(definedCategoryIds);
	}

	public Set getEnabledCategoryIds() {
		return Collections.unmodifiableSet(enabledCategoryIds);
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

	private void notifyActivities(Map activityEventsByActivityId) {
		for (Iterator iterator =
			activityEventsByActivityId.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String activityId = (String) entry.getKey();
			ActivityEvent activityEvent = (ActivityEvent) entry.getValue();
			Activity activity = (Activity) activitiesById.get(activityId);

			if (activity != null)
				activity.fireActivityChanged(activityEvent);
		}
	}

	private void notifyCategories(Map categoryEventsByCategoryId) {
		for (Iterator iterator =
			categoryEventsByCategoryId.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String categoryId = (String) entry.getKey();
			CategoryEvent categoryEvent = (CategoryEvent) entry.getValue();
			Category category = (Category) categoriesById.get(categoryId);

			if (category != null)
				category.fireCategoryChanged(categoryEvent);
		}
	}

	private void readRegistry() {
		Collection activityDefinitions = new ArrayList();
		activityDefinitions.addAll(activityRegistry.getActivityDefinitions());
		Map activityDefinitionsById =
			new HashMap(
				ActivityDefinition.activityDefinitionsById(
					activityDefinitions,
					false));

		for (Iterator iterator = activityDefinitionsById.values().iterator();
			iterator.hasNext();
			) {
			IActivityDefinition activityDefinition =
				(IActivityDefinition) iterator.next();
			String name = activityDefinition.getName();

			if (name == null || name.length() == 0)
				iterator.remove();
		}

		for (Iterator iterator = activityDefinitionsById.keySet().iterator();
			iterator.hasNext();
			)
			if (!isActivityDefinitionChildOf(null,
				(String) iterator.next(),
				activityDefinitionsById))
				iterator.remove();

		Collection categoryDefinitions = new ArrayList();
		categoryDefinitions.addAll(activityRegistry.getCategoryDefinitions());
		Map categoryDefinitionsById =
			new HashMap(
				CategoryDefinition.categoryDefinitionsById(
					categoryDefinitions,
					false));

		for (Iterator iterator = categoryDefinitionsById.values().iterator();
			iterator.hasNext();
			) {
			ICategoryDefinition categoryDefinition =
				(ICategoryDefinition) iterator.next();
			String name = categoryDefinition.getName();

			if (name == null || name.length() == 0)
				iterator.remove();
		}

		Map activityBindingDefinitionsByCategoryId =
			ActivityBindingDefinition.activityBindingDefinitionsByRoleId(
				activityRegistry.getActivityBindingDefinitions());
		Map activityBindingsByCategoryId = new HashMap();

		for (Iterator iterator =
			activityBindingDefinitionsByCategoryId.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String categoryId = (String) entry.getKey();

			if (activityBindingsByCategoryId.containsKey(categoryId)) {
				Collection activityBindingDefinitions =
					(Collection) entry.getValue();

				if (activityBindingDefinitions != null)
					for (Iterator iterator2 =
						activityBindingDefinitions.iterator();
						iterator2.hasNext();
						) {
						IActivityBindingDefinition activityBindingDefinition =
							(IActivityBindingDefinition) iterator2.next();
						String activityId =
							activityBindingDefinition.getActivityId();

						if (activityId != null) {
							IActivityBinding activityBinding =
								new ActivityBinding(activityId);
							Set activityBindings =
								(Set) activityBindingsByCategoryId.get(
									categoryId);

							if (activityBindings == null) {
								activityBindings = new HashSet();
								activityBindingsByCategoryId.put(
									categoryId,
									activityBindings);
							}

							activityBindings.add(activityBinding);
						}
					}
			}
		}

		Map patternBindingDefinitionsByActivityId =
			PatternBindingDefinition.patternBindingDefinitionsByActivityId(
				activityRegistry.getPatternBindingDefinitions());
		Map patternBindingsByActivityId = new HashMap();

		for (Iterator iterator =
			patternBindingDefinitionsByActivityId.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String activityId = (String) entry.getKey();

			if (activityDefinitionsById.containsKey(activityId)) {
				Collection patternBindingDefinitions =
					(Collection) entry.getValue();

				if (patternBindingDefinitions != null)
					for (Iterator iterator2 =
						patternBindingDefinitions.iterator();
						iterator2.hasNext();
						) {
						IPatternBindingDefinition patternBindingDefinition =
							(IPatternBindingDefinition) iterator2.next();
						String pattern = patternBindingDefinition.getPattern();

						if (pattern != null && pattern.length() != 0) {
							IPatternBinding patternBinding =
								new PatternBinding(
									patternBindingDefinition.isInclusive(),
									Pattern.compile(pattern));
							List patternBindings =
								(List) patternBindingsByActivityId.get(
									activityId);

							if (patternBindings == null) {
								patternBindings = new ArrayList();
								patternBindingsByActivityId.put(
									activityId,
									patternBindings);
							}

							patternBindings.add(patternBinding);
						}
					}
			}
		}

		this.activityDefinitionsById = activityDefinitionsById;
		this.activityBindingsByCategoryId = activityBindingsByCategoryId;
		this.categoryDefinitionsById = categoryDefinitionsById;
		this.patternBindingsByActivityId = patternBindingsByActivityId;

		boolean definedActivityIdsChanged = false;
		Set definedActivityIds = new HashSet(activityDefinitionsById.keySet());

		if (!definedActivityIds.equals(this.definedActivityIds)) {
			this.definedActivityIds = definedActivityIds;
			definedActivityIdsChanged = true;
		}

		boolean definedCategoryIdsChanged = false;
		Set definedCategoryIds = new HashSet(categoryDefinitionsById.keySet());

		if (!definedCategoryIds.equals(this.definedCategoryIds)) {
			this.definedCategoryIds = definedCategoryIds;
			definedCategoryIdsChanged = true;
		}

		Map activityEventsByActivityId =
			updateActivities(activitiesById.keySet());

		Map categoryEventsByCategoryId =
			updateCategories(categoriesById.keySet());

		if (definedActivityIdsChanged || definedCategoryIdsChanged)
			fireActivityManagerChanged(
				new ActivityManagerEvent(
					this,
					definedActivityIdsChanged,
					false,
					definedCategoryIdsChanged,
					false));

		if (activityEventsByActivityId != null)
			notifyActivities(activityEventsByActivityId);

		if (categoryEventsByCategoryId != null)
			notifyCategories(categoryEventsByCategoryId);
	}

	public void setEnabledActivityIds(Set enabledActivityIds) {
		enabledActivityIds = Util.safeCopy(enabledActivityIds, String.class);
		boolean activityManagerChanged = false;
		Map activityEventsByActivityId = null;

		if (!this.enabledActivityIds.equals(enabledActivityIds)) {
			this.enabledActivityIds = enabledActivityIds;
			activityManagerChanged = true;
			activityEventsByActivityId =
				updateActivities(this.definedActivityIds);
		}

		if (activityManagerChanged)
			fireActivityManagerChanged(
				new ActivityManagerEvent(this, false, true, false, false));

		if (activityEventsByActivityId != null)
			notifyActivities(activityEventsByActivityId);
	}

	private Map updateActivities(Collection activityIds) {
		Map activityEventsByActivityId = new TreeMap();

		for (Iterator iterator = activityIds.iterator(); iterator.hasNext();) {
			String activityId = (String) iterator.next();
			Activity activity = (Activity) activitiesById.get(activityId);

			if (activity != null) {
				ActivityEvent activityEvent = updateActivity(activity);

				if (activityEvent != null)
					activityEventsByActivityId.put(activityId, activityEvent);
			}
		}

		return activityEventsByActivityId;
	}

	private ActivityEvent updateActivity(Activity activity) {
		IActivityDefinition activityDefinition =
			(IActivityDefinition) activityDefinitionsById.get(activity.getId());
		boolean definedChanged =
			activity.setDefined(activityDefinition != null);
		boolean descriptionChanged =
			activity.setDescription(
				activityDefinition != null
					? activityDefinition.getDescription()
					: null);
		boolean enabledChanged =
			activity.setEnabled(enabledActivityIds.contains(activity.getId()));
		boolean nameChanged =
			activity.setName(
				activityDefinition != null
					? activityDefinition.getName()
					: null);
		boolean parentIdChanged =
			activity.setParentId(
				activityDefinition != null
					? activityDefinition.getParentId()
					: null);

		// TODO this should be a Set now that there are no exclusion bindings..
		List patternBindings =
			(List) patternBindingsByActivityId.get(activity.getId());
		boolean patternBindingsChanged =
			activity.setPatternBindings(
				patternBindings != null
					? patternBindings
					: Collections.EMPTY_LIST);

		if (definedChanged
			|| descriptionChanged
			|| enabledChanged
			|| nameChanged
			|| parentIdChanged
			|| patternBindingsChanged)
			return new ActivityEvent(
				activity,
				definedChanged,
				descriptionChanged,
				enabledChanged,
				nameChanged,
				parentIdChanged,
				patternBindingsChanged);
		else
			return null;
	}

	public void setEnabledCategoryIds(Set enabledCategoryIds) {
		enabledCategoryIds = Util.safeCopy(enabledCategoryIds, String.class);
		boolean activityManagerChanged = false;
		Map categoryEventsByCategoryId = null;

		if (!this.enabledCategoryIds.equals(enabledCategoryIds)) {
			this.enabledCategoryIds = enabledCategoryIds;
			activityManagerChanged = true;
			categoryEventsByCategoryId =
				updateCategories(this.definedCategoryIds);
		}

		if (activityManagerChanged)
			fireActivityManagerChanged(
				new ActivityManagerEvent(this, false, false, false, true));

		if (categoryEventsByCategoryId != null)
			notifyCategories(categoryEventsByCategoryId);
	}

	private Map updateCategories(Collection categoryIds) {
		Map categoryEventsByCategoryId = new TreeMap();

		for (Iterator iterator = categoryIds.iterator(); iterator.hasNext();) {
			String categoryId = (String) iterator.next();
			Category category = (Category) categoriesById.get(categoryId);

			if (category != null) {
				CategoryEvent categoryEvent = updateCategory(category);

				if (categoryEvent != null)
					categoryEventsByCategoryId.put(categoryId, categoryEvent);
			}
		}

		return categoryEventsByCategoryId;
	}

	private CategoryEvent updateCategory(Category category) {
		ICategoryDefinition categoryDefinition =
			(ICategoryDefinition) categoryDefinitionsById.get(category.getId());
		boolean definedChanged =
			category.setDefined(categoryDefinition != null);
		boolean descriptionChanged =
			category.setDescription(
				categoryDefinition != null
					? categoryDefinition.getDescription()
					: null);

		// TODO
		//boolean enabledChanged =
		//category.setEnabled(enabledCategoryIds.contains(category.getId()));

		boolean nameChanged =
			category.setName(
				categoryDefinition != null
					? categoryDefinition.getName()
					: null);

		Set activityBindings =
			(Set) activityBindingsByCategoryId.get(category.getId());
		boolean activityBindingsChanged =
			category.setActivityBindings(
				activityBindings != null
					? activityBindings
					: Collections.EMPTY_SET);

		if (definedChanged
			|| descriptionChanged //|| enabledChanged
			|| nameChanged
			|| activityBindingsChanged)
			return new CategoryEvent(
				category,
				activityBindingsChanged,
				definedChanged,
				descriptionChanged,
			//enabledChanged,
			nameChanged);
		else
			return null;
	}
}
