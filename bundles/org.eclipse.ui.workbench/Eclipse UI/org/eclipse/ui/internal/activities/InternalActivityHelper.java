/*******************************************************************************
 * Copyright (c) 2008, 2018 IBM Corporation and others.
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

	public static Set<String> getActivityIdsForCategory(IActivityManager activityManager, ICategory category) {
		Set<ICategoryActivityBinding> bindings = category.getCategoryActivityBindings();
		Set<String> activityIds = new HashSet<>();
		for (Iterator<ICategoryActivityBinding> i = bindings.iterator(); i.hasNext();) {
			ICategoryActivityBinding binding = i.next();
			String id = binding.getActivityId();
			if (activityManager.getActivity(id).getExpression() == null)
				activityIds.add(id);
		}
		return activityIds;
	}

	private static boolean isEnabled(IActivityManager activityManager, String categoryId) {

		ICategory category = activityManager.getCategory(categoryId);
		if (category.isDefined()) {
			Set<String> activityIds = getActivityIdsForCategory(activityManager, category);
			if (activityManager.getEnabledActivityIds().containsAll(activityIds)) {
				return true;
			}
		}

		return false;
	}

	public static Set<String> getEnabledCategories(IActivityManager activityManager) {

		Set<String> definedCategoryIds = activityManager.getDefinedCategoryIds();
		Set<String> enabledCategories = new HashSet<>();
		for (Iterator<String> i = definedCategoryIds.iterator(); i.hasNext();) {
			String categoryId = i.next();
			if (isEnabled(activityManager, categoryId)) {
				enabledCategories.add(categoryId);
			}
		}
		return enabledCategories;
	}

	public static Set<String> getPartiallyEnabledCategories(IActivityManager activityManager) {
		Set<String> definedCategoryIds = activityManager.getDefinedCategoryIds();
		Set<String> partialCategories = new HashSet<>();
		for (Iterator<String> i = definedCategoryIds.iterator(); i.hasNext();) {
			String categoryId = i.next();
			if (isPartiallyEnabled(activityManager, categoryId)) {
				partialCategories.add(categoryId);
			}
		}

		return partialCategories;
	}

	private static boolean isPartiallyEnabled(IActivityManager activityManager, String categoryId) {
		Set<String> activityIds = getActivityIdsForCategory(activityManager, activityManager.getCategory(categoryId));
		int foundCount = 0;
		for (Iterator<String> i = activityIds.iterator(); i.hasNext();) {
			String activityId = i.next();
			if (activityManager.getEnabledActivityIds().contains(activityId)) {
				foundCount++;
			}
		}

		return foundCount > 0 && foundCount != activityIds.size();
	}
}