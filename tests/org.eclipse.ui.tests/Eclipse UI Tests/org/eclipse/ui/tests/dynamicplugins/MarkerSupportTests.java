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
package org.eclipse.ui.tests.dynamicplugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.eclipse.ui.views.markers.internal.ProblemFilter;
import org.junit.Test;

/**
 * Test the loading and unloading of the marker support components.
 *
 * @since 3.2
 */
public class MarkerSupportTests extends DynamicTestCase {

	public static final String FILTER1 = "filter1";

	public static final String FILTER2 = "filter2";

	public static final String FILTER3 = "filter3";

	public static final String DYNAMIC_CATEGORY = "dynamicCategory";

	static final String DYNAMIC_PROBLEM_MARKER = "org.eclipse.ui.tests.dynamicTestMarker";

	static final String PROBLEM_MARKER = "org.eclipse.core.resources.problemmarker";

	@Test
	public void testFilters() {
		assertFalse(hasFilter(FILTER1));
		assertFalse(hasFilter(FILTER2));
		assertFalse(hasFilter(FILTER3));
		getBundle();
		assertTrue(hasFilter(FILTER1));
		assertTrue(hasFilter(FILTER2));
		assertTrue(hasFilter(FILTER3));
		removeBundle();
		assertFalse(hasFilter(FILTER1));
		assertFalse(hasFilter(FILTER2));
		assertFalse(hasFilter(FILTER3));
	}

	@Test
	public void testMarkerGroup() {
		assertFalse(hasMarkerGroup());
		getBundle();
		assertTrue(hasMarkerGroup());
		removeBundle();
		assertFalse(hasMarkerGroup());
	}

	@Test
	public void testCategories() {
		assertFalse(hasCategory());
		getBundle();
		assertTrue(hasCategory());
		removeBundle();
		assertFalse(hasCategory());
	}

	@Test
	public void testHierarchies() {
		assertFalse(hasHierarchy());
		getBundle();
		assertTrue(hasHierarchy());
		removeBundle();
		assertFalse(hasHierarchy());
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.markerSupport.DynamicTestsSubCategoryProvider";
	}

	/**
	 * Return whether or not there is a hierarchy for the dynamic type or if it
	 * is using the default.
	 */
	private boolean hasHierarchy() {
		return MarkerSupportRegistry.getInstance().getSorterFor(
				DYNAMIC_PROBLEM_MARKER) != MarkerSupportRegistry.getInstance()
				.getSorterFor(PROBLEM_MARKER);
	}

	private boolean hasMarkerGroup() {
		for (MarkerGroup element : MarkerSupportRegistry.getInstance()
		.getMarkerGroups()) {
			if(element.getField().getDescription().equals("Dynamic Test Grouping")) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return whether or not there is a filter for the dynamic category
	 */
	private boolean hasCategory() {
		return MarkerSupportRegistry.getInstance().getCategory(
				DYNAMIC_PROBLEM_MARKER) != null;
	}

	/**
	 * Return whether or not there is a filter for id.
	 */
	private boolean hasFilter(String id) {
		for (ProblemFilter filter : MarkerSupportRegistry.getInstance()
				.getRegisteredFilters()) {
			if (id.equals(filter.getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String getExtensionId() {
		return "newProblemFilter.testDynamicFilterAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return MarkerSupportRegistry.MARKER_SUPPORT;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newMarkerSupport";
	}

	@Override
	protected String getDeclaringNamespace() {
		return IDEWorkbenchPlugin.IDE_WORKBENCH;
	}

}
