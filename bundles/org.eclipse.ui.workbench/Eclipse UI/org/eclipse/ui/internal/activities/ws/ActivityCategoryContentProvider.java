/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.ICategoryActivityBinding;

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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		manager = null;
	}

	/**
	 * @param category the category to fetch.
	 * @return all activities in the category.
	 */
	private IActivity[] getCategoryActivities(ICategory category) {
		Set activityBindings = category.getCategoryActivityBindings();
		List categoryActivities = new ArrayList(activityBindings.size());
		for (Iterator j = activityBindings.iterator(); j.hasNext();) {
			ICategoryActivityBinding binding = (ICategoryActivityBinding) j.next();
			String activityId = binding.getActivityId();
			IActivity activity = manager.getActivity(activityId);
			if (activity.isDefined()) {
				categoryActivities.add(new CategorizedActivity(category, activity));
			}
		}
		return (IActivity[]) categoryActivities.toArray(new IActivity[categoryActivities.size()]);
	}
	
	/**
	 * Get the duplicate activities found in the other defined categories.
	 * 
	 * @param categorizedActivity
	 *            The categorized activity.
	 * @return the list of duplicate categorized activities.
	 */
	public Object[] getDuplicateCategoryActivities(
			CategorizedActivity categorizedActivity) {
		ArrayList duplicateCategorizedactivities = new ArrayList();
		Set categoryIds = manager.getDefinedCategoryIds();
		ICategory currentCategory = null;
		String currentActivityId = null;
		IActivity[] categoryActivities = null;
		String currentCategoryId = null;
		// find the duplicate activities in the defined categories
		for (Iterator i = categoryIds.iterator(); i.hasNext();) {
			currentCategoryId = (String) i.next();
			if (!currentCategoryId.equals(categorizedActivity.getCategory()
					.getId())) {
				currentCategory = manager.getCategory(currentCategoryId);
				categoryActivities = getCategoryActivities(currentCategory);
				// traverse the category's activities to find a duplicate
				for (int index = 0; index < categoryActivities.length; index++) {
					currentActivityId = categoryActivities[index].getId();
					if (currentActivityId.equals(categorizedActivity
							.getActivity().getId())) {
						duplicateCategorizedactivities
								.add(new CategorizedActivity(currentCategory,
										manager.getActivity(currentActivityId)));
						// Assuming only one unique activity per category -
						// break
						break;
					}
				}

			}

		}
		return duplicateCategorizedactivities.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IActivityManager) {
			Set categoryIds = manager.getDefinedCategoryIds();
			ArrayList categories = new ArrayList(categoryIds.size());
			for (Iterator i = categoryIds.iterator(); i.hasNext();) {
				String categoryId = (String) i.next();
                ICategory category = manager.getCategory(categoryId);
				if (getCategoryActivities(category).length > 0)
                	categories.add(category); 
            }
			return categories.toArray();
		} else if (parentElement instanceof ICategory) {
			return getCategoryActivities((ICategory) parentElement);
		}
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof CategorizedActivity) {
			return ((CategorizedActivity) element).getCategory();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof IActivityManager || element instanceof ICategory)
			return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		manager = (IActivityManager) newInput;
	}
}
