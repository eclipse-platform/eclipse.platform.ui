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

import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.eclipse.ui.views.markers.internal.ProblemFilter;

public abstract class DeclarativeFilterTest {

	public static final String PROBLEM_TEST_ON_PROBLEM = "problemTest.onProblem";

	public static final String PROBLEM_TEST_NOT_ON_METHOD = "problemTest.notOnMethod";

	public static final String PROBLEM_TEST_ON_METHOD = "problemTest.onMethod";

	public static final String PROBLEM_TEST_SAME_CONTAINER_NO_SEVERITY = "problemTest.sameContainerNoSeverity";

	public static final String PROBLEM_TEST_INFO_AND_CHILDREN = "problemTest.infoAndChildren";

	public static final String PROBLEM_TEST_ON_SELECTED_WARNING = "problemTest.onSelectedWarning";

	protected static final String PROBLEM_TEST_ON_ANY_ERROR = "problemTest.onAnyError";

	/**
	 * Get the filter with id.
	 *
	 * @return ProblemFilter
	 */
	protected ProblemFilter getFilter(String id) {
		for (ProblemFilter filter : MarkerSupportRegistry.getInstance()
				.getRegisteredFilters()) {
			if (filter.getId().equals(id)) {
				return filter;
			}
		}
		return null;

	}

	/**
	 * Get the names of all of the filters we are testing.
	 * @return String[]
	 */
	String[] getAllFilterNames() {
		return new String[] { PROBLEM_TEST_ON_PROBLEM,
				PROBLEM_TEST_NOT_ON_METHOD, PROBLEM_TEST_ON_METHOD,
				PROBLEM_TEST_SAME_CONTAINER_NO_SEVERITY,
				PROBLEM_TEST_INFO_AND_CHILDREN,
				PROBLEM_TEST_ON_SELECTED_WARNING, PROBLEM_TEST_ON_ANY_ERROR };
	}

}
