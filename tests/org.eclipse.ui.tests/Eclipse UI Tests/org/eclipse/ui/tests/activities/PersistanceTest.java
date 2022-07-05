/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests that the Persistance class is catching malformed registry entries.
 *
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class PersistanceTest extends UITestCase {

	public PersistanceTest() {
		super(PersistanceTest.class.getSimpleName());
	}

	@Test
	public void testCategoryPermutations() {
		try {
			IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
			ICategory category = manager.getCategory("org.eclipse.ui.PT.C1"); // should not be defined - missing name
			assertFalse(category.isDefined());

			category = manager.getCategory("org.eclipse.ui.PT.C2"); // should be defined - missing desc
			assertTrue(category.isDefined());
			assertNotNull(category.getDescription());

			for (String string : manager.getDefinedCategoryIds()) {
				if (manager.getCategory(string).getName().equals("org.eclipse.ui.PT.C3")) {
					Assert.fail("Found category that should not be.");
				}
			}
		}
		catch (NotDefinedException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testActivityRequirementBindings() {
		IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
		IActivity activity  = manager.getActivity("org.eclipse.ui.PT.A2");
		assertTrue(activity.getActivityRequirementBindings().isEmpty());
	}

	@Test
	public void testActivityPatternBindings() {
		IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
		IActivity activity  = manager.getActivity("org.eclipse.ui.PT.A2");
		assertTrue(activity.getActivityPatternBindings().isEmpty());
	}

	@Test
	public void testCategoryActivityBindings() {
		IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
		ICategory category  = manager.getCategory("org.eclipse.ui.PT.C2");
		assertTrue(category.getCategoryActivityBindings().isEmpty());
	}

	@Test
	public void testActivityPermutations() {
		try {
			IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
			IActivity activity = manager.getActivity("org.eclipse.ui.PT.A1"); // should not be defined - missing name
			assertFalse(activity.isDefined());

			activity = manager.getActivity("org.eclipse.ui.PT.A2"); // should be defined - missing desc
			assertTrue(activity.isDefined());
			assertNotNull(activity.getDescription());

			for (String string : manager.getDefinedActivityIds()) {
				if (manager.getActivity(string).getName().equals("org.eclipse.ui.PT.A3")) {
					Assert.fail("Found activity that should not be.");
				}
			}
		}
		catch (NotDefinedException e) {
			Assert.fail(e.getMessage());
		}
	}
}
