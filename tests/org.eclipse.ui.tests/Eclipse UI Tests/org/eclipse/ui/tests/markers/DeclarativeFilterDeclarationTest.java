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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.ui.views.markers.internal.MarkerFilter;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.ProblemFilter;
import org.junit.Test;

/**
 * The DeclarativeFilterActivityTest is a test that the declarative filters are
 * removed by activities
 */
public class DeclarativeFilterDeclarationTest extends DeclarativeFilterTest {

	/**
	 * Test the filter on any error.
	 */
	@Test
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
	@Test
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
	@Test
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
	@Test
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
	@Test
	public void testDescription() {
		String filterName = PROBLEM_TEST_ON_METHOD;
		ProblemFilter filter = getFilter(filterName);
		assertTrue(filterName + " not found ", filter != null);
		assertTrue(filterName + " is enabled ", !filter.isEnabled());
		assertTrue(filterName + "does not have description", !filter
				.getDescription().isEmpty());
		assertTrue(filterName + "not checking contains", filter.getContains());
	}

	/**
	 * Test the filter not on description.
	 */
	@Test
	public void testNotOnDescription() {
		String filterName = PROBLEM_TEST_NOT_ON_METHOD;
		ProblemFilter filter = getFilter(filterName);
		assertTrue(filterName + " not found ", filter != null);
		assertTrue(filterName + " is enabled ", !filter.isEnabled());
		assertTrue(filterName + "does not have description", !filter
				.getDescription().isEmpty());
		assertFalse(filterName + "checking contains", filter.getContains());
	}

	/**
	 * Test the filter on problem types.
	 */
	@Test
	public void testProblemTypes() {
		String filterName = PROBLEM_TEST_ON_PROBLEM;
		ProblemFilter filter = getFilter(filterName);
		assertTrue(filterName + " not found ", filter != null);
		assertTrue(filterName + " is enabled ", !filter.isEnabled());
		List<MarkerType> types = filter.getSelectedTypes();
		assertTrue(
				filterName + "should only have one type has " + types.size(),
				types.size() == 1);
		assertTrue(filterName + "should be enabled for category test",
				types.get(0).getId().equals(
						"org.eclipse.ui.tests.categoryTestMarker"));
	}

}
