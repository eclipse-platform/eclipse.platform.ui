/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.activities.ws.WorkbenchActivitySupport;

/**
 * A utility class that contains helpful methods for interacting with the
 * activities API.
 * 
 * @since 3.0
 */
public final class WorkbenchActivityHelper {
	/**
	 * The ID of the trigger point that only returns activities with core
	 * expressions.
	 * 
	 * @since 3.4
	 */
	public static final String TRIGGER_PRE_UI_POINT = "org.eclipse.ui.workbenchModel"; //$NON-NLS-1$
	
	private static ITriggerPoint getTriggerPoint(String id) {
		return PlatformUI.getWorkbench().getActivitySupport()
				.getTriggerPointManager().getTriggerPoint(id);
	}

	/**
	 * Return the identifier that maps to the given contribution.
	 * 
	 * @param contribution
	 *            the contribution
	 * @return the identifier
	 * @since 3.1
	 */
	public static IIdentifier getIdentifier(IPluginContribution contribution) {
		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
				.getWorkbench().getActivitySupport();
		IIdentifier identifier = workbenchActivitySupport.getActivityManager()
				.getIdentifier(createUnifiedId(contribution));
		return identifier;
	}

	/**
	 * Answers whether a given contribution is allowed to be used based on
	 * activity enablement. If it is currently disabled, then a dialog is opened
	 * and the user is prompted to activate the requried activities. If the user
	 * declines their activation then false is returned. In all other cases
	 * <code>true</code> is returned.
	 * 
	 * @param object
	 *            the contribution to test.
	 * @return whether the contribution is allowed to be used based on activity
	 *         enablement.
	 * @deprecated
	 * @see #allowUseOf(ITriggerPoint, Object)
	 */
	public static boolean allowUseOf(Object object) {
		return allowUseOf(PlatformUI.getWorkbench().getActivitySupport()
				.getTriggerPointManager().getTriggerPoint(
						ITriggerPointManager.UNKNOWN_TRIGGER_POINT_ID), object);
	}

	/**
	 * Answers whether a given contribution is allowed to be used based on
	 * activity enablement. If it is currently disabled, then a dialog is opened
	 * and the user is prompted to activate the requried activities. If the user
	 * declines their activation then false is returned. In all other cases
	 * <code>true</code> is returned.
	 * 
	 * @param triggerPoint
	 *            the trigger point being hit
	 * @param object
	 *            the contribution to test.
	 * @return whether the contribution is allowed to be used based on activity
	 *         enablement.
	 */
	public static boolean allowUseOf(ITriggerPoint triggerPoint, Object object) {
		if (!isFiltering()) {
			return true;
		}
		if (triggerPoint == null) {
			return true;
		}
		if (object instanceof IPluginContribution) {
			IPluginContribution contribution = (IPluginContribution) object;
			IIdentifier identifier = getIdentifier(contribution);
			return allow(triggerPoint, identifier);
		}
		return true;
	}
	
	/**
	 * Restrict the use of the object only if it is matched by an activity with
	 * a core expression. A normal disabled activity will not restrict the use
	 * of this object.
	 * 
	 * @param object
	 *            the object to restrict
	 * @return <code>true</code> if this object is matched by a disabled
	 *         activity with an expression.
	 * @since 3.4
	 */
	public static boolean restrictUseOf(Object object) {
		return !allowUseOf(getTriggerPoint(TRIGGER_PRE_UI_POINT), object);
	}

	/**
	 * Answers whether a given identifier is enabled. If it is not enabled, then
	 * a dialog is opened and the user is prompted to enable the associated
	 * activities.
	 * 
	 * @param triggerPoint
	 *            the trigger point to test
	 * @param identifier
	 *            the identifier to test.
	 * @return whether the identifier is enabled.
	 */
	private static boolean allow(ITriggerPoint triggerPoint,
			IIdentifier identifier) {
		if (identifier.isEnabled()) {
			return true;
		}

		ITriggerPointAdvisor advisor = ((WorkbenchActivitySupport) PlatformUI
				.getWorkbench().getActivitySupport()).getTriggerPointAdvisor();
		Set activitiesToEnable = advisor.allow(triggerPoint, identifier);
		
		if (activitiesToEnable == null) {
			return false;
		}
		
		if (activitiesToEnable.isEmpty()) {
			// no activities required to be enabled for this trigger point -
			// allow use unconditionally.
			return true;
		}

		enableActivities(activitiesToEnable);
		// only allow the operation if all the activities we needed to enabled
		// are now enabled. this means if something has an expression bound
		// activity that is not currently enabled this call will always return
		// false - trying to manually set such an activity will always fail.
		Set newEnabled = PlatformUI.getWorkbench().getActivitySupport()
				.getActivityManager().getEnabledActivityIds();
		return newEnabled.containsAll(activitiesToEnable);
	}

	/**
	 * Utility method to create a <code>String</code> containing the plugin
	 * and extension ids of a contribution. This will have the form
	 * 
	 * <pre>
	 * pluginId / extensionId
	 * </pre>. If the IPluginContribution does not define a plugin id then the
	 * extension id alone is returned.
	 * 
	 * @param contribution
	 *            the contribution to use
	 * @return the unified id
	 */
	public static final String createUnifiedId(IPluginContribution contribution) {
		if (contribution.getPluginId() != null) {
			return contribution.getPluginId() + '/' + contribution.getLocalId();
		}
		return contribution.getLocalId();
	}

	/**
	 * Enables the set of activities.
	 * 
	 * @param activities
	 *            the activities to enable
	 */
	private static void enableActivities(Collection activities) {
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench()
				.getActivitySupport();
		Set newSet = new HashSet(activitySupport.getActivityManager()
				.getEnabledActivityIds());
		newSet.addAll(activities);
		activitySupport.setEnabledActivityIds(newSet);
	}

	/**
	 * Answers whether the provided object should be filtered from the UI based
	 * on activity state. Returns <code>false</code> except when the object is
	 * an instance of <code>IPluginContribution</code> whos unified id matches
	 * an <code>IIdentifier</code> that is currently disabled.
	 * 
	 * @param object
	 *            the object to test
	 * @return whether the object should be filtered
	 * @see #createUnifiedId(IPluginContribution)
	 */
	public static final boolean filterItem(Object object) {
		if (object instanceof IPluginContribution) {
			IPluginContribution contribution = (IPluginContribution) object;
			IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
					.getWorkbench().getActivitySupport();
			IIdentifier identifier = workbenchActivitySupport
					.getActivityManager().getIdentifier(
							createUnifiedId(contribution));
			if (!identifier.isEnabled()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the UI is set up to filter contributions. This is the
	 * case if there are defined activities.
	 * 
	 * @return whether the UI is set up to filter contributions
	 */
	public static final boolean isFiltering() {
		return !PlatformUI.getWorkbench().getActivitySupport()
				.getActivityManager().getDefinedActivityIds().isEmpty();
	}

	/**
	 * Return a list of category ids that will become implicity enabled if the
	 * given category becomes enabled Note that the set returned by this set
	 * represents the delta of categories that would be enabled - if the
	 * category is already enabled then it is omitted.
	 * 
	 * @param activityManager
	 *            the activity manager to test against
	 * @param categoryId
	 *            the category to be enabled
	 * @return a list of category ids that will become implicity enabled if the
	 *         given category becomes enabled
	 * @since 3.1
	 */
	public static Set getEnabledCategories(IActivityManager activityManager,
			String categoryId) {
		ICategory category = activityManager.getCategory(categoryId);
		if (!category.isDefined()) {
			return Collections.EMPTY_SET;
		}

		Set activities = expandActivityDependencies(getActivityIdsForCategory(category));
		Set otherEnabledCategories = new HashSet();
		Set definedCategoryIds = activityManager.getDefinedCategoryIds();
		for (Iterator i = definedCategoryIds.iterator(); i.hasNext();) {
			String otherCategoryId = (String) i.next();
			if (otherCategoryId.equals(categoryId)) {
				continue;
			}
			ICategory otherCategory = activityManager
					.getCategory(otherCategoryId);
			Set otherCategoryActivityIds = expandActivityDependencies(getActivityIdsForCategory(otherCategory));
			if (activityManager.getEnabledActivityIds().containsAll(
					otherCategoryActivityIds)) {
				continue;
			}
			if (activities.containsAll(otherCategoryActivityIds)) {
				otherEnabledCategories.add(otherCategoryId);
			}

		}
		return otherEnabledCategories;
	}

	/**
	 * Return the expanded activities for the given activity set. This will
	 * resolve all activity requirement bindings.
	 * 
	 * @param baseActivities
	 *            the set of activities to expand
	 * @return the expanded activities
	 * @since 3.1
	 */
	public static Set expandActivityDependencies(Set baseActivities) {
		Set extendedActivities = new HashSet();
		for (Iterator i = baseActivities.iterator(); i.hasNext();) {
			String activityId = (String) i.next();
			Set requiredActivities = getRequiredActivityIds(activityId);
			extendedActivities.addAll(requiredActivities);
		}
		extendedActivities.addAll(baseActivities);
		return extendedActivities;
	}

	/**
	 * Return the activities required for this activity.
	 * 
	 * @param activityId
	 *            the activity id
	 * @return the activities required for this activity
	 * @since 3.1
	 */
	public static Set getRequiredActivityIds(String activityId) {
		IActivityManager manager = PlatformUI.getWorkbench()
				.getActivitySupport().getActivityManager();
		IActivity activity = manager.getActivity(activityId);
		if (!activity.isDefined()) {
			return Collections.EMPTY_SET;
		}
		Set requirementBindings = activity.getActivityRequirementBindings();
		if (requirementBindings.isEmpty()) {
			return Collections.EMPTY_SET;
		}

		Set requiredActivities = new HashSet(3);
		for (Iterator i = requirementBindings.iterator(); i.hasNext();) {
			IActivityRequirementBinding binding = (IActivityRequirementBinding) i
					.next();
			requiredActivities.add(binding.getRequiredActivityId());
			requiredActivities.addAll(getRequiredActivityIds(binding
					.getRequiredActivityId()));
		}
		return requiredActivities;
	}

	/**
	 * Return the activities directly required by a given category.
	 * 
	 * @param category
	 *            the category
	 * @return the activities directly required by a given category
	 * @since 3.1
	 */
	public static Set getActivityIdsForCategory(ICategory category) {
		Set bindings = category.getCategoryActivityBindings();
		Set activityIds = new HashSet();
		for (Iterator i = bindings.iterator(); i.hasNext();) {
			ICategoryActivityBinding binding = (ICategoryActivityBinding) i
					.next();
			activityIds.add(binding.getActivityId());
		}
		return activityIds;
	}

	/**
	 * Return a list of category ids that will become implicity disabled if the
	 * given category becomes disabled Note that the set returned by this set
	 * represents the delta of categories that would be enabled - if the
	 * category is already enabled then it is omitted.
	 * 
	 * @param activityManager
	 *            the activity manager to test against
	 * @param categoryId
	 *            the category to be enabled
	 * @return a list of category ids that will become implicity enabled if the
	 *         given category becomes enabled
	 * @since 3.1
	 */
	public static Set getDisabledCategories(IActivityManager activityManager,
			String categoryId) {
		ICategory category = activityManager.getCategory(categoryId);
		if (!category.isDefined()) {
			return Collections.EMPTY_SET;
		}

		Set activities = expandActivityDependencies(getActivityIdsForCategory(category));
		Set otherDisabledCategories = new HashSet();
		Set definedCategoryIds = activityManager.getDefinedCategoryIds();
		for (Iterator i = definedCategoryIds.iterator(); i.hasNext();) {
			String otherCategoryId = (String) i.next();
			if (otherCategoryId.equals(categoryId)) {
				continue;
			}
			ICategory otherCategory = activityManager
					.getCategory(otherCategoryId);
			Set otherCategoryActivityIds = expandActivityDependencies(getActivityIdsForCategory(otherCategory));

			if (otherCategoryActivityIds.isEmpty()) {
				continue;
			}

			if (!activities.containsAll(otherCategoryActivityIds)) {
				continue;
			}

			if (activityManager.getEnabledActivityIds().containsAll(
					otherCategoryActivityIds)) {
				otherDisabledCategories.add(otherCategoryId);
			}

		}
		return otherDisabledCategories;
	}

	/**
	 * Return a list of category ids that are implicitly contained within the
	 * given category.
	 * 
	 * @param activityManager
	 *            the activity manager to test agaisnt
	 * @param categoryId
	 *            the category to be enabled
	 * @return a list of category ids that will become implicity enabled if the
	 *         given category becomes enabled
	 * @since 3.1
	 */
	public static final Set getContainedCategories(
			IActivityManager activityManager, String categoryId) {
		ICategory category = activityManager.getCategory(categoryId);
		if (!category.isDefined()) {
			return Collections.EMPTY_SET;
		}

		Set activities = expandActivityDependencies(getActivityIdsForCategory(category));
		Set containedCategories = new HashSet();
		Set definedCategoryIds = activityManager.getDefinedCategoryIds();
		for (Iterator i = definedCategoryIds.iterator(); i.hasNext();) {
			String otherCategoryId = (String) i.next();
			if (otherCategoryId.equals(categoryId)) {
				continue;
			}
			ICategory otherCategory = activityManager
					.getCategory(otherCategoryId);
			Set otherCategoryActivityIds = expandActivityDependencies(getActivityIdsForCategory(otherCategory));

			if (otherCategoryActivityIds.isEmpty()) {
				continue;
			}

			if (activities.containsAll(otherCategoryActivityIds)) {
				containedCategories.add(otherCategoryId);
			}

		}
		return containedCategories;

	}

	/**
	 * Return the set of enabled categories. An enabled category is one in which
	 * all contained activities are enabled.
	 * 
	 * @param activityManager
	 *            the activity manager to test against
	 * @return the set of enabled categories.
	 * @since 3.1
	 */
	public static Set getEnabledCategories(IActivityManager activityManager) {

		Set definedCategoryIds = activityManager.getDefinedCategoryIds();
		Set enabledCategories = new HashSet();
		for (Iterator i = definedCategoryIds.iterator(); i.hasNext();) {
			String categoryId = (String) i.next();
			if (isEnabled(activityManager, categoryId)) {
				enabledCategories.add(categoryId);
			}
		}
		return enabledCategories;
	}
	
	/**
	 * Return the set of partially enabled categories.
	 * 
	 * @param activityManager
	 *            the activity manager to test against
	 * @return the set of partially enabled categories
	 * @since 3.2
	 */
	public static Set getPartiallyEnabledCategories(
			IActivityManager activityManager) {
		Set definedCategoryIds = activityManager.getDefinedCategoryIds();
		Set partialCategories = new HashSet();
		for (Iterator i = definedCategoryIds.iterator(); i.hasNext();) {
			String categoryId = (String) i.next();
			if (isPartiallyEnabled(activityManager, categoryId)) {
				partialCategories.add(categoryId);
			}
		}

		return partialCategories;
	}

	/**
	 * Returns whether the given category is partially enabled. A partially
	 * enabled category is one in which the number of enabled activites is both
	 * non-zero and less than the total number of activities in the category.
	 * 
	 * @param activityManager
	 *            the activity manager to test against
	 * @param categoryId
	 *            the category id
	 * @return whether the category is enabled
	 * @since 3.2
	 */
	public static boolean isPartiallyEnabled(IActivityManager activityManager,
			String categoryId) {
		Set activityIds = getActivityIdsForCategory(activityManager
				.getCategory(categoryId));
		int foundCount = 0;
		for (Iterator i = activityIds.iterator(); i.hasNext();) {
			String activityId = (String) i.next();
			if (activityManager.getEnabledActivityIds().contains(activityId)) {
				foundCount++;
			}
		}

		return foundCount > 0 && foundCount != activityIds.size();
	}

	/**
	 * Return the number of enabled categories that this activity belongs to.
	 * 
	 * @param activityManager
	 *            the activity manager to test against *
	 * @param activityId
	 *            the activity id to query on
	 * @return the set of enabled category ids that this activity belongs to
	 * @since 3.1
	 */
	public static Set getEnabledCategoriesForActivity(
			IActivityManager activityManager, String activityId) {
		Set enabledCategoriesForActivity = new HashSet();
		Set enabledCategories = getEnabledCategories(activityManager);
		for (Iterator i = enabledCategories.iterator(); i.hasNext();) {
			String categoryId = (String) i.next();
			if (getActivityIdsForCategory(
					activityManager.getCategory(categoryId)).contains(
					activityId)) {
				enabledCategoriesForActivity.add(categoryId);
			}
		}
		return enabledCategoriesForActivity;
	}

	/**
	 * Returns whether the given category is enabled. A category is enabled if
	 * all of its activities are enabled.
	 * 
	 * @param activityManager
	 *            the activity manager to test against
	 * @param categoryId
	 *            the category id
	 * @return whether the category is enabled
	 * @since 3.1
	 */
	public static boolean isEnabled(IActivityManager activityManager,
			String categoryId) {

		ICategory category = activityManager.getCategory(categoryId);
		if (category.isDefined()) {
			Set activityIds = getActivityIdsForCategory(category);
			if (activityManager.getEnabledActivityIds().containsAll(activityIds)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Resolve the collection of category ids to an array of
	 * <code>ICategory</code> objects.
	 * 
	 * @param activityManager
	 *            the activity manager to test against
	 * @param categoryIds
	 *            the category ids
	 * @return the array of category ids resolved to <code>ICategory</code>
	 *         objects
	 * @since 3.1
	 */
	public static ICategory[] resolveCategories(
			IMutableActivityManager activityManager, Set categoryIds) {
		ICategory[] categories = new ICategory[categoryIds.size()];
		String[] categoryIdArray = (String[]) categoryIds
				.toArray(new String[categoryIds.size()]);
		for (int i = 0; i < categoryIdArray.length; i++) {
			categories[i] = activityManager.getCategory(categoryIdArray[i]);
		}
		return categories;
	}
	
	/**
	 * Fills and returns the second argument with those objects of the first
	 * argument that pass the {@link #restrictUseOf(Object)} test.
	 * 
	 * @param toBeFiltered the input collection
	 * @param result the collection to which objects passing the test should be added
	 * @return the <code>result</code> collection for convenience
	 * 
	 * @since 3.4
	 */
	public static Collection restrictCollection(Collection toBeFiltered, Collection result) {
		for (Iterator iterator = toBeFiltered.iterator(); iterator.hasNext();) {
			Object item = iterator.next();
			if (!restrictUseOf(item)) {
				result.add(item);
			}
		}
		return result;
	}
	
	/**
	 * Returns an array with those objects of the argument array that pass the
	 * {@link #restrictUseOf(Object)} test.
	 * 
	 * @param array
	 *            the input array
	 * @return a new array of the same type as the argument array, containing
	 *         objects that pass the test
	 *         
	 * @since 3.4
	 */
	public static Object[] restrictArray(Object[] array) {
		ArrayList list = new ArrayList(array.length);
		for (int i = 0; i < array.length; i++) {
			if (!restrictUseOf(array[i])) {
				list.add(array[i]);
			}
		}
		return list.toArray((Object[]) Array.newInstance(array.getClass().getComponentType(), array.length));
	}
	
	/**
	 * Fills and returns the second argument with those objects of the first
	 * argument that pass the {@link #filterItem(Object)} test.
	 * 
	 * @param toBeFiltered the input collection
	 * @param result the collection to which objects passing the test should be added
	 * @return the <code>result</code> collection for convenience
	 * 
	 * @since 3.4
	 */
	public static Collection filterCollection(Collection toBeFiltered, Collection result) {
		for (Iterator iterator = toBeFiltered.iterator(); iterator.hasNext();) {
			Object item = iterator.next();
			if (!filterItem(item)) {
				result.add(item);
			}
		}
		return result;
	}
	
	/**
	 * Returns an array with those objects of the argument array that pass the
	 * {@link #filterItem(Object)} test.
	 * 
	 * @param array
	 *            the input array
	 * @return a new array of the same type as the argument array, containing
	 *         objects that pass the test
	 *         
	 * @since 3.4
	 */
	public static Object[] filterArray(Object[] array) {
		ArrayList list = new ArrayList(array.length);
		for (int i = 0; i < array.length; i++) {
			if (!filterItem(array[i])) {
				list.add(array[i]);
			}
		}
		return list.toArray((Object[]) Array.newInstance(array.getClass().getComponentType(), array.length));
	}

	/**
	 * Not intended to be instantiated.
	 */
	private WorkbenchActivityHelper() {
		// no-op
	}
}
