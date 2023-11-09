/*******************************************************************************
 * Copyright (c) 2003, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityPatternBinding;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.internal.activities.ActivityRequirementBinding;
import org.eclipse.ui.internal.activities.CategoryActivityBinding;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 * The static test reads activity definitions from the plugin.xml (in
 * org.eclipse.ui.tests) file and valides its content.
 */
@RunWith(JUnit4.class)
public class StaticTest extends UITestCase {
	private final IActivityManager activityManager;

	private List<String> categoryIds;

	private List<String> activityIds;

	private List<Object> patternActivityIds;

	public StaticTest() {
		super(StaticTest.class.getSimpleName());
		activityManager = PlatformUI.getWorkbench().getActivitySupport()
				.getActivityManager();
		populateIds();
	}

	/**
	 * Populate the id arrays.
	 *
	 */
	private void populateIds() {
		int index = 0;
		categoryIds = new ArrayList<>();
		for (index = 1; index <= 6; index++)
		 {
			categoryIds.add("org.eclipse.category" + index); //$NON-NLS-1$
		}
		activityIds = new ArrayList<>();
		for (index = 1; index <= 18; index++)
		 {
			activityIds.add("org.eclipse.activity" + index); //$NON-NLS-1$
		}
		patternActivityIds = new ArrayList<>();
		for (index = 0; index < 3; index++) {
			patternActivityIds.add(activityIds.toArray()[index]);
		}
	}

	/**
	 * Test the activity manager's content.
	 *
	 */
	@Test
	public void testActivityManager() {
		// Check the defined category Ids
		assertTrue(activityManager.getDefinedCategoryIds().containsAll(
				categoryIds));
		// Check the defined activity Ids
		assertTrue(activityManager.getDefinedActivityIds().containsAll(
				activityIds));
		// Check enabled activity Ids
		for (int index = 1; index <= 4; index++) {
			assertTrue(activityManager.getEnabledActivityIds().contains(
					"org.eclipse.activity" + index));
		}
		// Check identifier
		IIdentifier activityIdentifier = activityManager
				.getIdentifier("org.eclipse.pattern1");
		Set<?> activityIds = activityIdentifier.getActivityIds();
		assertTrue(activityIds.containsAll(patternActivityIds));
		assertTrue(activityIdentifier.getId().equals("org.eclipse.pattern1"));
	}

	/**
	 * Test an activitie's content.
	 *
	 */
	@Test
	public void testActivity() {
		IActivity first_activity = activityManager
				.getActivity((String) activityIds.toArray()[0]);
		// Check activity activity bindings for parent activity
		Set<?> activityRequirementBindings = first_activity
				.getActivityRequirementBindings();
		for (int index = 2; index <= 7; index++) {
			assertTrue(activityRequirementBindings
					.contains(new ActivityRequirementBinding(
							"org.eclipse.activity" + index,
							"org.eclipse.activity1")));
		}
		// Check activity pattern bindings
		Set<?> activityPatternBindings = first_activity
				.getActivityPatternBindings();
		assertTrue(!activityPatternBindings.isEmpty());
		IActivityPatternBinding activityPatternBinding = (IActivityPatternBinding) activityPatternBindings
				.toArray()[0];
		assertTrue(activityPatternBinding.getActivityId().equals(
				first_activity.getId()));
		assertTrue(activityPatternBinding.getPattern().pattern().equals(
				"org.eclipse.pattern1"));
		// Check description
		try {
			assertTrue(first_activity.getDescription().equals("description"));
		} catch (NotDefinedException e) {
			e.printStackTrace();
		}
		// Check activity id
		assertTrue(first_activity.getId().equals("org.eclipse.activity1"));
		// Check activity name
		try {
			assertTrue(first_activity.getName().equals("Activity 1"));
		} catch (NotDefinedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test a category's content.
	 *
	 */
	@Test
	public void testCategory() {
		ICategory first_category = activityManager
				.getCategory((String) categoryIds.toArray()[0]);
		// Check category activity bindings
		Set<?> categoryActivityBindings = first_category
				.getCategoryActivityBindings();
		for (int index = 1; index <= 4; index++) {
			assertTrue(categoryActivityBindings
					.contains(new CategoryActivityBinding(
							"org.eclipse.activity" + index,
							first_category.getId())));
		}
		try {
			// Check category description
			assertTrue(first_category.getDescription().equals("description"));
		} catch (NotDefinedException e) {
			e.printStackTrace();
		}
		// Check category id
		assertTrue(first_category.getId().equals("org.eclipse.category1"));
		try {
			// Check category name
			assertTrue(first_category.getName().equals("Category 1"));
		} catch (NotDefinedException e) {
			e.printStackTrace();
		}
	}
}
