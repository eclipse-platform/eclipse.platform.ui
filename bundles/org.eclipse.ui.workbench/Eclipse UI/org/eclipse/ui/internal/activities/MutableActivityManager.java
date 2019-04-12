/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474273
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityEvent;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.CategoryEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityPatternBinding;
import org.eclipse.ui.activities.IActivityRequirementBinding;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.ICategoryActivityBinding;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.activities.ITriggerPointAdvisor;
import org.eclipse.ui.activities.IdentifierEvent;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.services.IEvaluationReference;
import org.eclipse.ui.services.IEvaluationService;

/**
 * An activity registry that may be altered.
 *
 * @since 3.0
 */
public final class MutableActivityManager extends AbstractActivityManager
		implements IMutableActivityManager, Cloneable {

	private Map<String, Activity> activitiesById = new HashMap<>();

	private Map<String, Set<IActivityRequirementBinding>> activityRequirementBindingsByActivityId = new HashMap<>();

	private Map<String, ActivityDefinition> activityDefinitionsById = new HashMap<>();

	private Map<String, Set<IActivityPatternBinding>> activityPatternBindingsByActivityId = new HashMap<>();

	private IActivityRegistry activityRegistry;

	private Map<String, Category> categoriesById = new HashMap<>();

	private Map<String, Set<ICategoryActivityBinding>> categoryActivityBindingsByCategoryId = new HashMap<>();

	private Map<String, CategoryDefinition> categoryDefinitionsById = new HashMap<>();

	private Set<String> definedActivityIds = new HashSet<>();

	private Set<String> definedCategoryIds = new HashSet<>();

	private Set<String> enabledActivityIds = new HashSet<>();

	private Map<String, Identifier> identifiersById = new HashMap<>();

	/**
	 * Avoid endless circular referencing of re-adding activity to evaluation
	 * listener, because of adding it the first time to evaluation listener.
	 */
	private boolean addingEvaluationListener = false;

	/**
	 * A list of identifiers that need to have their activity sets reconciled in the
	 * background job.
	 */
	private List<Identifier> deferredIdentifiers = Collections.synchronizedList(new LinkedList<>());

	/**
	 * The identifier update job. Lazily initialized.
	 */
	private Job deferredIdentifierJob = null;

	private final IActivityRegistryListener activityRegistryListener = activityRegistryEvent -> readRegistry(false);

	private Map<ActivityDefinition, IEvaluationReference> refsByActivityDefinition = new HashMap<>();

	/**
	 * Create a new instance of this class using the platform extension registry.
	 * 
	 * @param triggerPointAdvisor
	 */
	public MutableActivityManager(ITriggerPointAdvisor triggerPointAdvisor) {
		this(triggerPointAdvisor, new ExtensionActivityRegistry(Platform.getExtensionRegistry()));
	}

	/**
	 * Create a new instance of this class using the provided registry.
	 * 
	 * @param triggerPointAdvisor
	 *
	 * @param activityRegistry    the activity registry
	 */
	public MutableActivityManager(ITriggerPointAdvisor triggerPointAdvisor, IActivityRegistry activityRegistry) {
		Assert.isNotNull(activityRegistry);
		Assert.isNotNull(triggerPointAdvisor);

		this.advisor = triggerPointAdvisor;
		this.activityRegistry = activityRegistry;

		this.activityRegistry.addActivityRegistryListener(activityRegistryListener);

		readRegistry(true);
	}

	@Override
	synchronized public IActivity getActivity(String activityId) {
		if (activityId == null) {
			throw new NullPointerException();
		}

		Activity activity = activitiesById.get(activityId);

		if (activity == null) {
			activity = new Activity(activityId);
			updateActivity(activity);
			activitiesById.put(activityId, activity);
		}

		return activity;
	}

	@Override
	synchronized public ICategory getCategory(String categoryId) {
		if (categoryId == null) {
			throw new NullPointerException();
		}

		Category category = categoriesById.get(categoryId);

		if (category == null) {
			category = new Category(categoryId);
			updateCategory(category);
			categoriesById.put(categoryId, category);
		}

		return category;
	}

	@Override
	synchronized public Set<String> getDefinedActivityIds() {
		return Collections.unmodifiableSet(definedActivityIds);
	}

	@Override
	synchronized public Set<String> getDefinedCategoryIds() {
		return Collections.unmodifiableSet(definedCategoryIds);
	}

	@Override
	synchronized public Set<String> getEnabledActivityIds() {
		return Collections.unmodifiableSet(enabledActivityIds);
	}

	@Override
	synchronized public IIdentifier getIdentifier(String identifierId) {
		if (identifierId == null) {
			throw new NullPointerException();
		}

		Identifier identifier = identifiersById.get(identifierId);

		if (identifier == null) {
			identifier = new Identifier(identifierId);
			updateIdentifier(identifier);
			identifiersById.put(identifierId, identifier);
		}

		return identifier;
	}

	private void getRequiredActivityIds(Set<String> activityIds, Set<String> requiredActivityIds) {
		for (Iterator<String> iterator = activityIds.iterator(); iterator.hasNext();) {
			String activityId = iterator.next();
			IActivity activity = getActivity(activityId);
			Set<String> childActivityIds = new HashSet<>();
			Set<IActivityRequirementBinding> activityRequirementBindings = activity.getActivityRequirementBindings();

			for (Iterator<IActivityRequirementBinding> iterator2 = activityRequirementBindings.iterator(); iterator2
					.hasNext();) {
				IActivityRequirementBinding activityRequirementBinding = iterator2.next();
				childActivityIds.add(activityRequirementBinding.getRequiredActivityId());
			}

			childActivityIds.removeAll(requiredActivityIds);
			requiredActivityIds.addAll(childActivityIds);
			getRequiredActivityIds(childActivityIds, requiredActivityIds);
		}
	}

	private void notifyActivities(Map<String, ActivityEvent> activityEventsByActivityId) {
		for (Iterator<Map.Entry<String, ActivityEvent>> iterator = activityEventsByActivityId.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<String, ActivityEvent> entry = iterator.next();
			String activityId = entry.getKey();
			ActivityEvent activityEvent = entry.getValue();
			Activity activity = activitiesById.get(activityId);

			if (activity != null) {
				activity.fireActivityChanged(activityEvent);
			}
		}
	}

	private void notifyCategories(Map<String, CategoryEvent> categoryEventsByCategoryId) {
		for (Iterator<Entry<String, CategoryEvent>> iterator = categoryEventsByCategoryId.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<String, CategoryEvent> entry = iterator.next();
			String categoryId = entry.getKey();
			CategoryEvent categoryEvent = entry.getValue();
			Category category = categoriesById.get(categoryId);

			if (category != null) {
				category.fireCategoryChanged(categoryEvent);
			}
		}
	}

	private void notifyIdentifiers(Map<String, IdentifierEvent> identifierEventsByIdentifierId) {
		for (Iterator<Entry<String, IdentifierEvent>> iterator = identifierEventsByIdentifierId.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<String, IdentifierEvent> entry = iterator.next();
			String identifierId = entry.getKey();
			IdentifierEvent identifierEvent = entry.getValue();
			Identifier identifier = identifiersById.get(identifierId);

			if (identifier != null) {
				identifier.fireIdentifierChanged(identifierEvent);
			}
		}
	}

	private void readRegistry(boolean setDefaults) {
		clearExpressions();
		Collection<ActivityDefinition> activityDefinitions = new ArrayList<>();
		activityDefinitions.addAll(activityRegistry.getActivityDefinitions());
		Map<String, ActivityDefinition> activityDefinitionsById = new HashMap<>(
				ActivityDefinition.activityDefinitionsById(activityDefinitions, false));

		for (Iterator<ActivityDefinition> iterator = activityDefinitionsById.values().iterator(); iterator.hasNext();) {
			ActivityDefinition activityDefinition = iterator.next();
			String name = activityDefinition.getName();

			if (name == null || name.length() == 0) {
				iterator.remove();
			}
		}

		Collection<CategoryDefinition> categoryDefinitions = new ArrayList<>();
		categoryDefinitions.addAll(activityRegistry.getCategoryDefinitions());
		Map<String, CategoryDefinition> categoryDefinitionsById = new HashMap<>(
				CategoryDefinition.categoryDefinitionsById(categoryDefinitions, false));

		for (Iterator<CategoryDefinition> iterator = categoryDefinitionsById.values().iterator(); iterator.hasNext();) {
			CategoryDefinition categoryDefinition = iterator.next();
			String name = categoryDefinition.getName();

			if (name == null || name.length() == 0) {
				iterator.remove();
			}
		}

		Map<String, Collection<ActivityRequirementBindingDefinition>> activityRequirementBindingDefinitionsByActivityId = ActivityRequirementBindingDefinition
				.activityRequirementBindingDefinitionsByActivityId(
						activityRegistry.getActivityRequirementBindingDefinitions());
		Map<String, Set<IActivityRequirementBinding>> activityRequirementBindingsByActivityId = new HashMap<>();

		for (Iterator<Entry<String, Collection<ActivityRequirementBindingDefinition>>> iterator = activityRequirementBindingDefinitionsByActivityId
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Collection<ActivityRequirementBindingDefinition>> entry = iterator.next();
			String parentActivityId = entry.getKey();

			if (activityDefinitionsById.containsKey(parentActivityId)) {
				Collection<ActivityRequirementBindingDefinition> activityRequirementBindingDefinitions = entry
						.getValue();

				if (activityRequirementBindingDefinitions != null) {
					for (Iterator<ActivityRequirementBindingDefinition> iterator2 = activityRequirementBindingDefinitions
							.iterator(); iterator2.hasNext();) {
						ActivityRequirementBindingDefinition activityRequirementBindingDefinition = iterator2.next();
						String childActivityId = activityRequirementBindingDefinition.getRequiredActivityId();

						if (activityDefinitionsById.containsKey(childActivityId)) {
							IActivityRequirementBinding activityRequirementBinding = new ActivityRequirementBinding(
									childActivityId, parentActivityId);
							Set<IActivityRequirementBinding> activityRequirementBindings = activityRequirementBindingsByActivityId
									.get(parentActivityId);

							if (activityRequirementBindings == null) {
								activityRequirementBindings = new HashSet<>();
								activityRequirementBindingsByActivityId.put(parentActivityId,
										activityRequirementBindings);
							}

							activityRequirementBindings.add(activityRequirementBinding);
						}
					}
				}
			}
		}

		Map<String, Collection<ActivityPatternBindingDefinition>> activityPatternBindingDefinitionsByActivityId = ActivityPatternBindingDefinition
				.activityPatternBindingDefinitionsByActivityId(activityRegistry.getActivityPatternBindingDefinitions());
		Map<String, Set<IActivityPatternBinding>> activityPatternBindingsByActivityId = new HashMap<>();

		for (Iterator<Entry<String, Collection<ActivityPatternBindingDefinition>>> iterator = activityPatternBindingDefinitionsByActivityId
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Collection<ActivityPatternBindingDefinition>> entry = iterator.next();
			String activityId = entry.getKey();

			if (activityDefinitionsById.containsKey(activityId)) {
				Collection<ActivityPatternBindingDefinition> activityPatternBindingDefinitions = entry.getValue();

				if (activityPatternBindingDefinitions != null) {
					for (Iterator<ActivityPatternBindingDefinition> iterator2 = activityPatternBindingDefinitions
							.iterator(); iterator2.hasNext();) {
						ActivityPatternBindingDefinition activityPatternBindingDefinition = iterator2.next();
						String pattern = activityPatternBindingDefinition.getPattern();

						if (pattern != null && pattern.length() != 0) {
							IActivityPatternBinding activityPatternBinding = new ActivityPatternBinding(activityId,
									pattern, activityPatternBindingDefinition.isEqualityPattern());
							Set<IActivityPatternBinding> activityPatternBindings = activityPatternBindingsByActivityId
									.get(activityId);

							if (activityPatternBindings == null) {
								activityPatternBindings = new HashSet<>();
								activityPatternBindingsByActivityId.put(activityId, activityPatternBindings);
							}

							activityPatternBindings.add(activityPatternBinding);
						}
					}
				}
			}
		}

		Map<String, Collection<CategoryActivityBindingDefinition>> categoryActivityBindingDefinitionsByCategoryId = CategoryActivityBindingDefinition
				.categoryActivityBindingDefinitionsByCategoryId(
						activityRegistry.getCategoryActivityBindingDefinitions());
		Map<String, Set<ICategoryActivityBinding>> categoryActivityBindingsByCategoryId = new HashMap<>();

		for (Iterator<Entry<String, Collection<CategoryActivityBindingDefinition>>> iterator = categoryActivityBindingDefinitionsByCategoryId
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Collection<CategoryActivityBindingDefinition>> entry = iterator.next();
			String categoryId = entry.getKey();

			if (categoryDefinitionsById.containsKey(categoryId)) {
				Collection<CategoryActivityBindingDefinition> categoryActivityBindingDefinitions = entry.getValue();

				if (categoryActivityBindingDefinitions != null) {
					for (Iterator<CategoryActivityBindingDefinition> iterator2 = categoryActivityBindingDefinitions
							.iterator(); iterator2.hasNext();) {
						CategoryActivityBindingDefinition categoryActivityBindingDefinition = iterator2.next();
						String activityId = categoryActivityBindingDefinition.getActivityId();

						if (activityDefinitionsById.containsKey(activityId)) {
							ICategoryActivityBinding categoryActivityBinding = new CategoryActivityBinding(activityId,
									categoryId);
							Set<ICategoryActivityBinding> categoryActivityBindings = categoryActivityBindingsByCategoryId
									.get(categoryId);

							if (categoryActivityBindings == null) {
								categoryActivityBindings = new HashSet<>();
								categoryActivityBindingsByCategoryId.put(categoryId, categoryActivityBindings);
							}

							categoryActivityBindings.add(categoryActivityBinding);
						}
					}
				}
			}
		}

		this.activityRequirementBindingsByActivityId = activityRequirementBindingsByActivityId;
		this.activityDefinitionsById = activityDefinitionsById;
		this.activityPatternBindingsByActivityId = activityPatternBindingsByActivityId;
		this.categoryActivityBindingsByCategoryId = categoryActivityBindingsByCategoryId;
		this.categoryDefinitionsById = categoryDefinitionsById;
		boolean definedActivityIdsChanged = false;
		Set<String> definedActivityIds = new HashSet<>(activityDefinitionsById.keySet());

		Set<String> previouslyDefinedActivityIds = null;
		if (!definedActivityIds.equals(this.definedActivityIds)) {
			previouslyDefinedActivityIds = this.definedActivityIds;
			this.definedActivityIds = definedActivityIds;
			definedActivityIdsChanged = true;
		}

		boolean definedCategoryIdsChanged = false;
		Set<String> definedCategoryIds = new HashSet<>(categoryDefinitionsById.keySet());

		Set<String> previouslyDefinedCategoryIds = null;
		if (!definedCategoryIds.equals(this.definedCategoryIds)) {
			previouslyDefinedCategoryIds = this.definedCategoryIds;
			this.definedCategoryIds = definedCategoryIds;
			definedCategoryIdsChanged = true;
		}

		Set<String> enabledActivityIds = new HashSet<>(this.enabledActivityIds);
		getRequiredActivityIds(this.enabledActivityIds, enabledActivityIds);
		boolean enabledActivityIdsChanged = false;

		Set<String> previouslyEnabledActivityIds = null;
		if (!this.enabledActivityIds.equals(enabledActivityIds)) {
			previouslyEnabledActivityIds = this.enabledActivityIds;
			this.enabledActivityIds = enabledActivityIds;
			enabledActivityIdsChanged = true;
		}

		Map<String, ActivityEvent> activityEventsByActivityId = updateActivities(activitiesById.keySet());

		Map<String, CategoryEvent> categoryEventsByCategoryId = updateCategories(categoriesById.keySet());

		Map<String, IdentifierEvent> identifierEventsByIdentifierId = updateIdentifiers(identifiersById.keySet());

		if (definedActivityIdsChanged || definedCategoryIdsChanged || enabledActivityIdsChanged) {
			fireActivityManagerChanged(new ActivityManagerEvent(this, definedActivityIdsChanged,
					definedCategoryIdsChanged, enabledActivityIdsChanged, previouslyDefinedActivityIds,
					previouslyDefinedCategoryIds, previouslyEnabledActivityIds));
		}

		if (activityEventsByActivityId != null) {
			notifyActivities(activityEventsByActivityId);
		}

		if (categoryEventsByCategoryId != null) {
			notifyCategories(categoryEventsByCategoryId);
		}

		if (identifierEventsByIdentifierId != null) {
			notifyIdentifiers(identifierEventsByIdentifierId);
		}

		if (setDefaults) {
			setEnabledActivityIds(new HashSet<>(activityRegistry.getDefaultEnabledActivities()));
		}
	}

	private void clearExpressions() {
		IEvaluationService evaluationService = PlatformUI.getWorkbench().getService(IEvaluationService.class);
		Iterator<IEvaluationReference> i = refsByActivityDefinition.values().iterator();
		while (i.hasNext()) {
			IEvaluationReference ref = i.next();
			evaluationService.removeEvaluationListener(ref);
		}
		refsByActivityDefinition.clear();
	}

	@Override
	synchronized public void setEnabledActivityIds(Set<String> enabledActivityIds) {
		enabledActivityIds = new HashSet<>(enabledActivityIds);
		Set<String> requiredActivityIds = new HashSet<>(enabledActivityIds);
		getRequiredActivityIds(enabledActivityIds, requiredActivityIds);
		enabledActivityIds = requiredActivityIds;
		Set<String> deltaActivityIds = null;
		boolean activityManagerChanged = false;
		Map<String, ActivityEvent> activityEventsByActivityId = null;

		Set<String> previouslyEnabledActivityIds = null;
		// the sets are different so there may be work to do.
		if (!this.enabledActivityIds.equals(enabledActivityIds)) {
			previouslyEnabledActivityIds = this.enabledActivityIds;
			activityManagerChanged = true;

			// break out the additions to the current set
			Set<String> additions = new HashSet<>(enabledActivityIds);
			additions.removeAll(previouslyEnabledActivityIds);

			// and the removals
			Set<String> removals = new HashSet<>(previouslyEnabledActivityIds);
			removals.removeAll(enabledActivityIds);

			// remove from each set the expression-activities
			removeExpressionControlledActivities(additions);
			removeExpressionControlledActivities(removals);

			// merge the two sets into one delta - these are the changes that
			// need to be made after taking expressions into account
			deltaActivityIds = new HashSet<>(additions);
			deltaActivityIds.addAll(removals);

			if (deltaActivityIds.size() > 0) {
				// instead of blowing away the old set with the new we will
				// instead modify it based on the deltas
				// add in all the new activities to the current set
				enabledActivityIds.addAll(additions);
				// and remove the stale ones
				enabledActivityIds.removeAll(removals);
				// finally set the internal set of activities
				this.enabledActivityIds = enabledActivityIds;
				activityEventsByActivityId = updateActivities(deltaActivityIds);
			} else {
				return;
			}
		}

		updateListeners(activityManagerChanged, activityEventsByActivityId, deltaActivityIds,
				previouslyEnabledActivityIds);
	}

	/**
	 * Updates all the listeners to changes in the state.
	 *
	 * @param activityManagerChanged
	 * @param activityEventsByActivityId
	 * @param deltaActivityIds
	 * @param previouslyEnabledActivityIds
	 */
	private void updateListeners(boolean activityManagerChanged, Map<String, ActivityEvent> activityEventsByActivityId,
			Set<String> deltaActivityIds, Set<String> previouslyEnabledActivityIds) {
		// don't update identifiers if the enabled activity set has not changed
		if (activityManagerChanged) {
			Map<String, IdentifierEvent> identifierEventsByIdentifierId = updateIdentifiers(identifiersById.keySet(),
					deltaActivityIds);
			if (identifierEventsByIdentifierId != null) {
				notifyIdentifiers(identifierEventsByIdentifierId);
			}
		}
		if (activityEventsByActivityId != null) {
			notifyActivities(activityEventsByActivityId);
		}

		if (activityManagerChanged) {
			fireActivityManagerChanged(
					new ActivityManagerEvent(this, false, false, true, null, null, previouslyEnabledActivityIds));
		}
	}

	private void addExpressionEnabledActivity(String id) {
		Set<String> previouslyEnabledActivityIds = new HashSet<>(this.enabledActivityIds);
		this.enabledActivityIds.add(id);

		updateExpressionEnabledActivities(id, previouslyEnabledActivityIds);
	}

	private void removeExpressionEnabledActivity(String id) {
		Set<String> previouslyEnabledActivityIds = new HashSet<>(this.enabledActivityIds);
		this.enabledActivityIds.remove(id);

		updateExpressionEnabledActivities(id, previouslyEnabledActivityIds);
	}

	/**
	 * @param id
	 * @param previouslyEnabledActivityIds
	 */
	private void updateExpressionEnabledActivities(String id, Set<String> previouslyEnabledActivityIds) {
		Set<String> deltaActivityIds = new HashSet<>();
		deltaActivityIds.add(id);
		Map<String, ActivityEvent> activityEventsByActivityId = updateActivities(deltaActivityIds);

		updateListeners(true, activityEventsByActivityId, deltaActivityIds, previouslyEnabledActivityIds);
	}

	/**
	 * Removes from a list of activity changes all those that are based on
	 * expressions
	 *
	 * @param delta the set to modify
	 */
	private void removeExpressionControlledActivities(Set<String> delta) {

		for (Iterator<String> i = delta.iterator(); i.hasNext();) {
			String id = i.next();
			IActivity activity = activitiesById.get(id);
			Expression expression = activity.getExpression();

			if (expression != null) {
				i.remove();
			}
		}
	}

	private Map<String, ActivityEvent> updateActivities(Collection<String> activityIds) {
		Map<String, ActivityEvent> activityEventsByActivityId = new TreeMap<>();

		for (Iterator<String> iterator = activityIds.iterator(); iterator.hasNext();) {
			String activityId = iterator.next();
			Activity activity = activitiesById.get(activityId);

			if (activity != null) {
				ActivityEvent activityEvent = updateActivity(activity);

				if (activityEvent != null) {
					activityEventsByActivityId.put(activityId, activityEvent);
				}
			}
		}

		return activityEventsByActivityId;
	}

	private IPropertyChangeListener enabledWhenListener = event -> {
		if (addingEvaluationListener) {
			return;
		}

		Object nv = event.getNewValue();
		boolean enabledWhen = nv == null ? false : ((Boolean) nv).booleanValue();
		String id = event.getProperty();
		IActivity activity = activitiesById.get(id);
		if (activity.isEnabled() != enabledWhen) {
			if (enabledWhen) {
				addExpressionEnabledActivity(id);
			} else {
				removeExpressionEnabledActivity(id);
			}
		}
	};

	private ITriggerPointAdvisor advisor;

	private ActivityEvent updateActivity(Activity activity) {
		Set<IActivityRequirementBinding> activityRequirementBindings = activityRequirementBindingsByActivityId
				.get(activity.getId());
		boolean activityRequirementBindingsChanged = activity.setActivityRequirementBindings(
				activityRequirementBindings != null ? activityRequirementBindings : Collections.emptySet());
		Set<IActivityPatternBinding> activityPatternBindings = activityPatternBindingsByActivityId
				.get(activity.getId());
		boolean activityPatternBindingsChanged = activity.setActivityPatternBindings(
				activityPatternBindings != null ? activityPatternBindings : Collections.emptySet());
		ActivityDefinition activityDefinition = activityDefinitionsById.get(activity.getId());
		boolean definedChanged = activity.setDefined(activityDefinition != null);

		// enabledWhen comes into play
		IEvaluationReference ref = refsByActivityDefinition.get(activityDefinition);
		IEvaluationService evaluationService = PlatformUI.getWorkbench().getService(IEvaluationService.class);
		boolean newRef = false;
		if (activityDefinition != null && evaluationService != null) {
			activity.setExpression(activityDefinition.getEnabledWhen());
			if (ref == null && activityDefinition.getEnabledWhen() != null) {
				addingEvaluationListener = true;
				try {
					ref = evaluationService.addEvaluationListener(activityDefinition.getEnabledWhen(),
							enabledWhenListener, activityDefinition.getId());
					newRef = true;
				} finally {
					addingEvaluationListener = false;
				}
				if (ref != null) {
					refsByActivityDefinition.put(activityDefinition, ref);
				}
			}
		}
		final boolean enabledChanged;
		if (ref != null && evaluationService != null) {
			enabledChanged = activity.setEnabled(ref.evaluate(evaluationService.getCurrentState()));
			if (newRef && activity.isEnabled()) {
				// make sure this activity is in the enabled set for this
				// manager - event firing will be handled by the caller to this
				// method.
				this.enabledActivityIds.add(activity.getId());
			}
		} else {
			enabledChanged = activity.setEnabled(enabledActivityIds.contains(activity.getId()));
		}

		boolean nameChanged = activity.setName(activityDefinition != null ? activityDefinition.getName() : null);
		boolean descriptionChanged = activity
				.setDescription(activityDefinition != null ? activityDefinition.getDescription() : null);
		boolean defaultEnabledChanged = activity
				.setDefaultEnabled(activityRegistry.getDefaultEnabledActivities().contains(activity.getId()));
		if (activityRequirementBindingsChanged || activityPatternBindingsChanged || definedChanged || enabledChanged
				|| nameChanged || descriptionChanged || defaultEnabledChanged) {
			return new ActivityEvent(activity, activityRequirementBindingsChanged, activityPatternBindingsChanged,
					definedChanged, descriptionChanged, enabledChanged, nameChanged, defaultEnabledChanged);
		}

		return null;
	}

	private Map<String, CategoryEvent> updateCategories(Collection<String> categoryIds) {
		Map<String, CategoryEvent> categoryEventsByCategoryId = new TreeMap<>();

		for (Iterator<String> iterator = categoryIds.iterator(); iterator.hasNext();) {
			String categoryId = iterator.next();
			Category category = categoriesById.get(categoryId);

			if (category != null) {
				CategoryEvent categoryEvent = updateCategory(category);

				if (categoryEvent != null) {
					categoryEventsByCategoryId.put(categoryId, categoryEvent);
				}
			}
		}

		return categoryEventsByCategoryId;
	}

	private CategoryEvent updateCategory(Category category) {
		Set<ICategoryActivityBinding> categoryActivityBindings = categoryActivityBindingsByCategoryId
				.get(category.getId());
		boolean categoryActivityBindingsChanged = category.setCategoryActivityBindings(
				categoryActivityBindings != null ? categoryActivityBindings : Collections.emptySet());
		CategoryDefinition categoryDefinition = categoryDefinitionsById.get(category.getId());
		boolean definedChanged = category.setDefined(categoryDefinition != null);
		boolean nameChanged = category.setName(categoryDefinition != null ? categoryDefinition.getName() : null);
		boolean descriptionChanged = category
				.setDescription(categoryDefinition != null ? categoryDefinition.getDescription() : null);

		if (categoryActivityBindingsChanged || definedChanged || nameChanged || descriptionChanged) {
			return new CategoryEvent(category, categoryActivityBindingsChanged, definedChanged, descriptionChanged,
					nameChanged);
		}

		return null;
	}

	private IdentifierEvent updateIdentifier(Identifier identifier) {
		return updateIdentifier(identifier, definedActivityIds);
	}

	private IdentifierEvent updateIdentifier(Identifier identifier, Set<String> changedActivityIds) {
		String id = identifier.getId();
		Set<String> activityIds = new HashSet<>();

		boolean enabled = false;

		boolean activityIdsChanged = false;

		boolean enabledChanged = false;

		// short-circut logic. If all activities are enabled, then the
		// identifier must be as well. Return true and schedule the remainder of
		// the work to run in a background job.
		if (enabledActivityIds.size() == definedActivityIds.size()) {
			enabled = true;
			enabledChanged = identifier.setEnabled(enabled);
			identifier.setActivityIds(Collections.EMPTY_SET);
			deferredIdentifiers.add(identifier);
			getUpdateJob().schedule();
			if (enabledChanged) {
				return new IdentifierEvent(identifier, activityIdsChanged, enabledChanged);
			}
		} else {
			Set<String> activityIdsToUpdate = new HashSet<>(changedActivityIds);
			if (identifier.getActivityIds() != null) {
				activityIdsToUpdate.addAll(identifier.getActivityIds());
			}
			for (Iterator<String> iterator = activityIdsToUpdate.iterator(); iterator.hasNext();) {
				String activityId = iterator.next();
				Activity activity = (Activity) getActivity(activityId);

				if (activity.isMatch(id)) {
					activityIds.add(activityId);
				}
			}

			activityIdsChanged = identifier.setActivityIds(activityIds);

			if (advisor != null) {
				enabled = advisor.computeEnablement(this, identifier);
			}
			enabledChanged = identifier.setEnabled(enabled);

			if (activityIdsChanged || enabledChanged) {
				return new IdentifierEvent(identifier, activityIdsChanged, enabledChanged);
			}
		}
		return null;
	}

	private Map<String, IdentifierEvent> updateIdentifiers(Collection<String> identifierIds) {
		return updateIdentifiers(identifierIds, definedActivityIds);
	}

	private Map<String, IdentifierEvent> updateIdentifiers(Collection<String> identifierIds,
			Set<String> changedActivityIds) {
		Map<String, IdentifierEvent> identifierEventsByIdentifierId = new TreeMap<>();

		for (Iterator<String> iterator = identifierIds.iterator(); iterator.hasNext();) {
			String identifierId = iterator.next();
			Identifier identifier = identifiersById.get(identifierId);

			if (identifier != null) {
				IdentifierEvent identifierEvent = updateIdentifier(identifier, changedActivityIds);

				if (identifierEvent != null) {
					identifierEventsByIdentifierId.put(identifierId, identifierEvent);
				}
			}
		}

		return identifierEventsByIdentifierId;
	}

	/**
	 * Unhook this manager from its registry.
	 *
	 * @since 3.1
	 */
	public void unhookRegistryListeners() {
		activityRegistry.removeActivityRegistryListener(activityRegistryListener);
	}

	@Override
	synchronized public Object clone() {
		MutableActivityManager clone = new MutableActivityManager(advisor, activityRegistry);
		clone.setEnabledActivityIds(getEnabledActivityIds());
		return clone;
	}

	/**
	 * Return the identifier update job.
	 *
	 * @return the job
	 * @since 3.1
	 */
	private Job getUpdateJob() {
		if (deferredIdentifierJob == null) {
			deferredIdentifierJob = Job.create("Activity Identifier Update", (IJobFunction) monitor -> { //$NON-NLS-1$
				final Map<String, IdentifierEvent> identifierEventsByIdentifierId = new HashMap<>();

				while (!deferredIdentifiers.isEmpty()) {
					Identifier identifier = deferredIdentifiers.remove(0);
					Set<String> activityIds = new HashSet<>();
					for (Iterator<String> iterator = definedActivityIds.iterator(); iterator.hasNext();) {
						String activityId = iterator.next();
						Activity activity = (Activity) getActivity(activityId);

						if (activity.isMatch(identifier.getId())) {
							activityIds.add(activityId);
						}
					}

					boolean activityIdsChanged = identifier.setActivityIds(activityIds);
					if (activityIdsChanged) {
						IdentifierEvent identifierEvent = new IdentifierEvent(identifier, activityIdsChanged, false);
						identifierEventsByIdentifierId.put(identifier.getId(), identifierEvent);
					}
				}
				if (!identifierEventsByIdentifierId.isEmpty()) {
					UIJob notifyJob = new UIJob("Activity Identifier Update UI") { //$NON-NLS-1$
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							notifyIdentifiers(identifierEventsByIdentifierId);
							return Status.OK_STATUS;
						}
					};
					notifyJob.setSystem(true);
					notifyJob.schedule();
				}
				return Status.OK_STATUS;
			});
			deferredIdentifierJob.setSystem(true);
		}
		return deferredIdentifierJob;
	}

}
