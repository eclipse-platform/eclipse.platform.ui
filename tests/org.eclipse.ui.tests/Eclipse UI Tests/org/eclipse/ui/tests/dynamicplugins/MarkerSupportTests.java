/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.eclipse.ui.views.markers.internal.ProblemFilter;

/**
 * Test the loading and unloading of problem filters.
 * 
 * @since 3.2
 */
public class MarkerSupportTests extends DynamicTestCase {

	public static final String FILTER1 = "filter1";

	public static final String FILTER2 = "filter2";

	public static final String FILTER3 = "filter3";

	static final String PROBLEM_MARKER = "org.eclipse.core.resources.problemmarker";

	static final String TEST_VIEW = "org.eclipse.ui.tests.components.MissingDependencyView";

	/**
	 * @param testName
	 */
	public MarkerSupportTests(String testName) {
		super(testName);
	}

	public void testFilters() {
		assertFalse(hasFilter(FILTER1));
		assertFalse(hasFilter(FILTER2));
		assertFalse(hasFilter(FILTER3));
		assertFalse(hasTestViewMapping());
		getBundle();
		assertTrue(hasFilter(FILTER1));
		assertTrue(hasFilter(FILTER2));
		assertTrue(hasFilter(FILTER3));
		assertTrue(hasTestViewMapping());
		removeBundle();
		assertFalse(hasFilter(FILTER1));
		assertFalse(hasFilter(FILTER2));
		assertFalse(hasFilter(FILTER3));
		assertFalse(hasTestViewMapping());
	}

	/**
	 * @return
	 */
	private boolean hasTestViewMapping() {

		Collection ids = MarkerSupportRegistry.getInstance().getViews(
				PROBLEM_MARKER);
		if (ids == null)
			return false;
		Iterator views = ids.iterator();

		while (views.hasNext()) {
			String element = (String) views.next();
			if (element.equals(TEST_VIEW))
				return true;
		}
		return false;
	}

	public boolean hasFilter(String id) {
		Iterator filters = MarkerSupportRegistry.getInstance()
				.getRegisteredFilters().iterator();
		while (filters.hasNext()) {
			ProblemFilter filter = (ProblemFilter) filters.next();
			if (id.equals(filter.getId()))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {
		return "newProblemFilter.testDynamicFilterAddition";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return MarkerSupportRegistry.MARKER_SUPPORT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newMarkerSupport";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getDeclaringNamespace()
	 */
	protected String getDeclaringNamespace() {
		return IDEWorkbenchPlugin.IDE_WORKBENCH;
	}

}
