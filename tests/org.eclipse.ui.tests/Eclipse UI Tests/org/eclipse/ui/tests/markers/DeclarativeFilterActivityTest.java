/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class DeclarativeFilterActivityTest extends DeclarativeFilterTest {

	static final String PROBLEM_FILTER_TEST_ACTIVITY = "problemFilterTestActivity";

	/**
	 * The DeclarativeProblemTest is a test of the declarative filters.
	 * 
	 * @param testName
	 */
	public DeclarativeFilterActivityTest(String testName) {
		super(testName);
	}

	/**
	 * Check that the activities are enabling as expected.
	 */
	public void testActivityEnablement() {
		enableFilterActivity();
		
		checkFilteredOut(false);
		
		disableFilterActivity();
		checkFilteredOut(true);
		enableFilterActivity();

	}

	/**
	 * Check that all of the filters match filteredOut.
	 * @param filteredOut
	 */
	private void checkFilteredOut(boolean filteredOut) {
		String[] allFilterNames = getAllFilterNames();
		String failureMessage = filteredOut ? " should be filtered out" : " should not be filtered out";
		for (int i = 0; i < allFilterNames.length; i++) {
			ProblemFilter filter = getFilter(allFilterNames[i]);
			if(filteredOut)
				assertNull("Should filter out " + allFilterNames[i] ,filter);
			else{
			assertNotNull("No filter for " + allFilterNames[i] ,filter);
			assertTrue(allFilterNames[i] + failureMessage, filter.isFilteredOutByActivity() == filteredOut);
			}
		}
		
	}

	/**
	 * Enable the activity for the declarative filters.
	 */
	private void enableFilterActivity() {
		IActivity activity = PlatformUI.getWorkbench().getActivitySupport()
				.getActivityManager().getActivity(PROBLEM_FILTER_TEST_ACTIVITY);
		Set enabledActivityIds = new HashSet(PlatformUI.getWorkbench()
				.getActivitySupport().getActivityManager()
				.getEnabledActivityIds());
		
		if (!enabledActivityIds.contains(activity.getId()))
			enabledActivityIds.add(activity.getId());
		
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(
				enabledActivityIds);
	}
	
	/**
	 * Disable the activity for the declarative filters.
	 */
	private void disableFilterActivity() {
		IActivity activity = PlatformUI.getWorkbench().getActivitySupport()
				.getActivityManager().getActivity(PROBLEM_FILTER_TEST_ACTIVITY);
		Set enabledActivityIds = new HashSet(PlatformUI.getWorkbench()
				.getActivitySupport().getActivityManager()
				.getEnabledActivityIds());
		
		if (enabledActivityIds.contains(activity.getId()))
			enabledActivityIds.remove(activity.getId());
		
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(
				enabledActivityIds);
	}

}
