/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.markers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.views.markers.internal.ProblemFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DeclarativeFilterActivityTest extends DeclarativeFilterTest {

	static final String PROBLEM_FILTER_TEST_ACTIVITY = "problemFilterTestActivity";

	/**
	 * The DeclarativeProblemTest is a test of the declarative filters.
	 */
	public DeclarativeFilterActivityTest() {
		super(DeclarativeFilterActivityTest.class.getSimpleName());
	}

	/**
	 * Check that the activities are enabling as expected.
	 */
	@Test
	public void testActivityEnablement() {
		enableFilterActivity();

		checkFilteredOut(false);

		disableFilterActivity();
		checkFilteredOut(true);
		enableFilterActivity();

	}

	/**
	 * Check that all of the filters match filteredOut.
	 */
	private void checkFilteredOut(boolean filteredOut) {
		String[] allFilterNames = getAllFilterNames();
		String failureMessage = filteredOut ? " should be filtered out" : " should not be filtered out";
		for (String allFilterName : allFilterNames) {
			ProblemFilter filter = getFilter(allFilterName);
			if(filteredOut) {
				assertNull("Should filter out " + allFilterName ,filter);
			} else{
			assertNotNull("No filter for " + allFilterName ,filter);
			assertTrue(allFilterName + failureMessage, filter.isFilteredOutByActivity() == filteredOut);
			}
		}

	}

	/**
	 * Enable the activity for the declarative filters.
	 */
	private void enableFilterActivity() {
		IActivity activity = PlatformUI.getWorkbench().getActivitySupport()
				.getActivityManager().getActivity(PROBLEM_FILTER_TEST_ACTIVITY);
		Set<String> enabledActivityIds = new HashSet<>(
				PlatformUI.getWorkbench()
				.getActivitySupport().getActivityManager()
				.getEnabledActivityIds());

		if (!enabledActivityIds.contains(activity.getId())) {
			enabledActivityIds.add(activity.getId());
		}

		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(
				enabledActivityIds);
	}

	/**
	 * Disable the activity for the declarative filters.
	 */
	private void disableFilterActivity() {
		IActivity activity = PlatformUI.getWorkbench().getActivitySupport()
				.getActivityManager().getActivity(PROBLEM_FILTER_TEST_ACTIVITY);
		Set<String> enabledActivityIds = new HashSet<>(
				PlatformUI.getWorkbench()
				.getActivitySupport().getActivityManager()
				.getEnabledActivityIds());

		if (enabledActivityIds.contains(activity.getId())) {
			enabledActivityIds.remove(activity.getId());
		}

		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(
				enabledActivityIds);
	}

}
