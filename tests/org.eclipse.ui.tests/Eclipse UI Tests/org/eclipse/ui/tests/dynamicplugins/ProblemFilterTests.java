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

import java.util.Iterator;

import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.views.markers.internal.ProblemFilter;
import org.eclipse.ui.views.markers.internal.ProblemFilterRegistry;

/**
 * Test the loading and unloading of problem filters.
 * 
 * @since 3.1
 */
public class ProblemFilterTests extends DynamicTestCase {

	public static final String FILTER1 = "filter1";

	public static final String FILTER2 = "filter2";

	public static final String FILTER3 = "filter3";

	/**
	 * @param testName
	 */
	public ProblemFilterTests(String testName) {
		super(testName);
	}

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

	public boolean hasFilter(String id) {
		Iterator filters = ProblemFilterRegistry.getInstance()
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
		return ProblemFilterRegistry.MARKER_SUPPORT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newProblemFilter";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getDeclaringNamespace()
	 */
	protected String getDeclaringNamespace() {
		return IDEWorkbenchPlugin.IDE_WORKBENCH;
	}

}
