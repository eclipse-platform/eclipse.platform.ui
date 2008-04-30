/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.ICategoryActivityBinding;

/**
 * Internal modifications to the (regrettably) public API in
 * WorkbenchActivityHelper. Much of the logic for what to display in the
 * preference page is bound to the API contracts in this class and these are no
 * longer suitable now that we have expression bound activities to consider.
 * 
 * <p>
 * These methods are the same as the originals except that activities with
 * expressions are not considered in any calculations.
 * </p>
 * 
 * <p>
 * See bug 229424 for details.
 * </p>
 * 
 * @since 3.4
 * 
 */
public final class InternalActivityHelper {

	public static Set getActivityIdsForCategory(
			IActivityManager activityManager, ICategory category) {
		Set bindings = category.getCategoryActivityBindings();
		Set activityIds = new HashSet();
		for (Iterator i = bindings.iterator(); i.hasNext();) {
			ICategoryActivityBinding binding = (ICategoryActivityBinding) i
					.next();
			String id = binding.getActivityId();
			if (activityManager.getActivity(id).getExpression() == null)
				activityIds.add(id);
		}
		return activityIds;
	}

	private static boolean isEnabled(IActivityManager activityManager,
			String categoryId) {

		ICategory category = activityManager.getCategory(categoryId);
		if (category.isDefined()) {
			Set activityIds = getActivityIdsForCategory(activityManager,
					category);
			if (activityManager.getEnabledActivityIds()
					.containsAll(activityIds)) {
				return true;
			}
		}

		return false;
	}

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

	private static boolean isPartiallyEnabled(IActivityManager activityManager,
			String categoryId) {
		Set activityIds = getActivityIdsForCategory(activityManager,
				activityManager.getCategory(categoryId));
		int foundCount = 0;
		for (Iterator i = activityIds.iterator(); i.hasNext();) {
			String activityId = (String) i.next();
			if (activityManager.getEnabledActivityIds().contains(activityId)) {
				foundCount++;
			}
		}

		return foundCount > 0 && foundCount != activityIds.size();
	}
}