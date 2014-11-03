/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.markers;
import java.util.List;

import org.eclipse.ui.views.markers.internal.MarkerFilter;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.ProblemFilter;

public class DeclarativeFilterDeclarationTest extends DeclarativeFilterTest {

	/**
	 * The DeclarativeFilterActivityTest is a test that the
	 * declarative filters are removed by activities
	 *
	 * @param testName
	 */
	public DeclarativeFilterDeclarationTest(String testName) {
		super(testName);
	}

	/**
	 * Test the filter on any error.
	 */
	public void testAnyErrorFilter() {
		String filterName = PROBLEM_TEST_ON_ANY_ERROR;
		ProblemFilter filter = getFilter(filterName);
		assertTrue(filterName + " not found ", filter != null);
		assertTrue(filterName + " is enabled ", !filter.isEnabled());
		assertTrue(filterName + "not selecting by severity", filter
				.getSelectBySeverity());
		assertTrue(filterName + "should be on error",
				filter.getSeverity() == ProblemFilter.SEVERITY_ERROR);
		assertTrue(filterName + "should be on any",
				filter.getOnResource() == MarkerFilter.ON_ANY);
	}

	/**
	 * Test the filter on selected warning.
	 */
	public void testSelectedWarning() {
		String filterName = PROBLEM_TEST_ON_SELECTED_WARNING;
		ProblemFilter filter = getFilter(filterName);
		assertTrue(filterName + " not found ", filter != null);
		assertTrue(filterName + " is enabled ", !filter.isEnabled());
		assertTrue(filterName + "not selecting by severity", filter
				.getSelectBySeverity());
		assertTrue(filterName + "should be on warning",
				filter.getSeverity() == ProblemFilter.SEVERITY_WARNING);
		assertTrue(filterName + "should be on selected only", filter
				.getOnResource() == MarkerFilter.ON_SELECTED_ONLY);
	}

	/**
	 * Test the filter on selected and children infos.
	 */
	public void testInfoAndChildren() {
		String filterName = PROBLEM_TEST_INFO_AND_CHILDREN;
		ProblemFilter filter = getFilter(filterName);
		assertTrue(filterName + " not found ", filter != null);
		assertTrue(filterName + " is enabled ", !filter.isEnabled());
		assertTrue(filterName + "not selecting by severity", filter
				.getSelectBySeverity());
		assertTrue(filterName + "should be on info",
				filter.getSeverity() == ProblemFilter.SEVERITY_INFO);
		assertTrue(filterName + "should be on selected and children", filter
				.getOnResource() == MarkerFilter.ON_SELECTED_AND_CHILDREN);
	}

	/**
	 * Test the filter on same container.
	 */
	public void testSameContainer() {
		String filterName = PROBLEM_TEST_SAME_CONTAINER_NO_SEVERITY;
		ProblemFilter filter = getFilter(filterName);
		assertTrue(filterName + " not found ", filter != null);
		assertTrue(filterName + " is enabled ", !filter.isEnabled());
		assertFalse(filterName + "selecting by severity", filter
				.getSelectBySeverity());
		assertTrue(filterName + "should be on on any in same container", filter
				.getOnResource() == MarkerFilter.ON_ANY_IN_SAME_CONTAINER);
	}

	/**
	 * Test the filter on description.
	 */
	public void testDescription() {
		String filterName = PROBLEM_TEST_ON_METHOD;
		ProblemFilter filter = getFilter(filterName);
		assertTrue(filterName + " not found ", filter != null);
		assertTrue(filterName + " is enabled ", !filter.isEnabled());
		assertTrue(filterName + "does not have description", filter
				.getDescription().length() > 0);
		assertTrue(filterName + "not checking contains", filter.getContains());
	}

	/**
	 * Test the filter not on description.
	 */
	public void testNotOnDescription() {
		String filterName = PROBLEM_TEST_NOT_ON_METHOD;
		ProblemFilter filter = getFilter(filterName);
		assertTrue(filterName + " not found ", filter != null);
		assertTrue(filterName + " is enabled ", !filter.isEnabled());
		assertTrue(filterName + "does not have description", filter
				.getDescription().length() > 0);
		assertFalse(filterName + "checking contains", filter.getContains());
	}

	/**
	 * Test the filter on problem types.
	 */
	public void testProblemTypes() {
		String filterName = PROBLEM_TEST_ON_PROBLEM;
		ProblemFilter filter = getFilter(filterName);
		assertTrue(filterName + " not found ", filter != null);
		assertTrue(filterName + " is enabled ", !filter.isEnabled());
		List types = filter.getSelectedTypes();
		assertTrue(
				filterName + "should only have one type has " + types.size(),
				types.size() == 1);
		assertTrue(filterName + "should be enabled for category test",
				((MarkerType) types.get(0)).getId().equals(
						"org.eclipse.ui.tests.categoryTestMarker"));
	}

}
