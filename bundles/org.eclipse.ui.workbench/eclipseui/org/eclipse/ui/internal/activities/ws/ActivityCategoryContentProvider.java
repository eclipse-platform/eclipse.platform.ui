/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityRequirementBinding;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.internal.activities.InternalActivityHelper;

/**
 * Tree provider that provides <code>ICategory</code> objects in an
 * <code>IActivityManager</code> at the top level, with <code>IActivity</code>
 * objects as second level children under the <code>ICategory</code>.
 * <p>
 * Note that the <code>IActivity</code> objects are not instances of
 * <code>org.eclipse.ui.internal.activities.Activity</code>, but rather proxies
 * that also have a pointer to the <code>ICategory</code> for which the
 * <code>IActivity</code> should be represented under.
 *
 * @since 3.0
 */
public class ActivityCategoryContentProvider implements ITreeContentProvider {

	/**
	 * The manager to extract content from.
	 */
	private IActivityManager manager;

	@Override
	public void dispose() {
		manager = null;
	}

	/**
	 * @param category the category to fetch.
	 * @return all activities in the category.
	 */
	private IActivity[] getCategoryActivities(ICategory category) {
		Set<String> activityIds = InternalActivityHelper.getActivityIdsForCategory(manager, category);
		List<IActivity> categoryActivities = new ArrayList<>(activityIds.size());
		for (String activityId : activityIds) {
			categoryActivities.add(new CategorizedActivity(category, manager.getActivity(activityId)));

		}
		return categoryActivities.toArray(new IActivity[categoryActivities.size()]);
	}

	/**
	 * Get the duplicate activities found in the other defined categories.
	 *
	 * @param categorizedActivity The categorized activity.
	 * @return the list of duplicate categorized activities.
	 */
	public Object[] getDuplicateCategoryActivities(CategorizedActivity categorizedActivity) {
		ArrayList<CategorizedActivity> duplicateCategorizedactivities = new ArrayList<>();
		Set<String> categoryIds = manager.getDefinedCategoryIds();
		ICategory currentCategory = null;
		String currentActivityId = null;
		IActivity[] categoryActivities = null;
		String currentCategoryId = null;
		// find the duplicate activities in the defined categories
		for (Iterator<String> i = categoryIds.iterator(); i.hasNext();) {
			currentCategoryId = i.next();
			if (!currentCategoryId.equals(categorizedActivity.getCategory().getId())) {
				currentCategory = manager.getCategory(currentCategoryId);
				categoryActivities = getCategoryActivities(currentCategory);
				// traverse the category's activities to find a duplicate
				for (IActivity categoryActivity : categoryActivities) {
					currentActivityId = categoryActivity.getId();
					if (currentActivityId.equals(categorizedActivity.getActivity().getId())) {
						duplicateCategorizedactivities
								.add(new CategorizedActivity(currentCategory, manager.getActivity(currentActivityId)));
						// Assuming only one unique activity per category -
						// break
						break;
					}
				}

			}

		}
		return duplicateCategorizedactivities.toArray();
	}

	/**
	 * Get the child required activities.
	 *
	 * @param activityId The parent activity id.
	 * @return the list of child required activities.
	 */
	public Object[] getChildRequiredActivities(String activityId) {
		ArrayList<CategorizedActivity> childRequiredActivities = new ArrayList<>();
		IActivity activity = manager.getActivity(activityId);
		Set<IActivityRequirementBinding> actvitiyRequirementBindings = activity.getActivityRequirementBindings();
		String requiredActivityId = null;
		IActivityRequirementBinding currentActivityRequirementBinding = null;
		Object[] currentCategoryIds = null;
		for (Iterator<IActivityRequirementBinding> i = actvitiyRequirementBindings.iterator(); i.hasNext();) {
			currentActivityRequirementBinding = i.next();
			requiredActivityId = currentActivityRequirementBinding.getRequiredActivityId();
			currentCategoryIds = getActivityCategories(requiredActivityId);
			for (Object currentCategoryId : currentCategoryIds) {
				childRequiredActivities.add(new CategorizedActivity(manager.getCategory((String) currentCategoryId),
						manager.getActivity(requiredActivityId)));
			}

		}

		return childRequiredActivities.toArray();
	}

	/**
	 * Get the parent required activities.
	 *
	 * @param activityId The child activity id.
	 * @return the list of parent required activities.
	 */
	public Object[] getParentRequiredActivities(String activityId) {
		ArrayList<CategorizedActivity> parentRequiredActivities = new ArrayList<>();
		Set<String> definedActivities = manager.getDefinedActivityIds();
		String currentActivityId = null;
		Set<IActivityRequirementBinding> activityRequirementBindings = null;
		IActivityRequirementBinding currentActivityRequirementBinding = null;
		Object[] currentCategoryIds = null;
		for (Iterator<String> i = definedActivities.iterator(); i.hasNext();) {
			currentActivityId = i.next();
			activityRequirementBindings = manager.getActivity(currentActivityId).getActivityRequirementBindings();
			for (Iterator<IActivityRequirementBinding> j = activityRequirementBindings.iterator(); j.hasNext();) {
				currentActivityRequirementBinding = j.next();
				if (currentActivityRequirementBinding.getRequiredActivityId().equals(activityId)) {
					// We found one - add it to the list
					currentCategoryIds = getActivityCategories(currentActivityId);
					for (Object currentCategoryId : currentCategoryIds) {
						parentRequiredActivities
								.add(new CategorizedActivity(manager.getCategory((String) currentCategoryId),
										manager.getActivity(currentActivityId)));
					}
				}
			}
		}
		return parentRequiredActivities.toArray();
	}

	/**
	 * Get the activity's categories (there could be more than one).
	 *
	 * @param activityId The activity id.
	 * @return the activity's categories.
	 */
	private Object[] getActivityCategories(String activityId) {
		ArrayList<String> activityCategories = new ArrayList<>();
		Set<String> categoryIds = manager.getDefinedCategoryIds();
		String currentCategoryId = null;
		IActivity[] categoryActivities = null;
		for (Iterator<String> i = categoryIds.iterator(); i.hasNext();) {
			currentCategoryId = i.next();
			categoryActivities = getCategoryActivities(manager.getCategory(currentCategoryId));
			for (IActivity categoryActivity : categoryActivities) {
				if (categoryActivity.getId().equals(activityId)) {
					activityCategories.add(currentCategoryId);
					break;
				}
			}
		}
		return activityCategories.toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IActivityManager) {
			Set<String> categoryIds = manager.getDefinedCategoryIds();
			ArrayList<ICategory> categories = new ArrayList<>(categoryIds.size());
			for (String categoryId : categoryIds) {
				ICategory category = manager.getCategory(categoryId);
				if (getCategoryActivities(category).length > 0) {
					categories.add(category);
				}
			}
			return categories.toArray();
		} else if (parentElement instanceof ICategory) {
			return getCategoryActivities((ICategory) parentElement);
		}
		return new Object[0];
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof CategorizedActivity) {
			return ((CategorizedActivity) element).getCategory();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IActivityManager || element instanceof ICategory) {
			return true;
		}
		return false;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IActivityManager) {
			manager = (IActivityManager) newInput;
		}
	}
}
