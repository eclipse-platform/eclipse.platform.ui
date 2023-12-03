/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.markers;

import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * MarkerSupportTests are tests for the markerSupport extension
 * point.
 * @since 3.2
 */
@RunWith(JUnit4.class)
public class MarkerSupportRegistryTests extends UITestCase {

	/**
	 * Create an instance of the receiver.
	 */
	public MarkerSupportRegistryTests() {
		super(MarkerSupportRegistryTests.class.getSimpleName());
	}

	/**
	 * Test that the marker categories expected are found.
	 */
	@Test
	public void testMarkerCategories() {
		doTestCategory("org.eclipse.ui.tests.categoryTestMarker");
		doTestCategory("org.eclipse.ui.tests.testmarker");
		doTestCategory("org.eclipse.ui.tests.testmarker2");
	}

	/**
	 * Test that the marker type specified is in a category.
	 */
	private void doTestCategory(String string) {
		String category = MarkerSupportRegistry.getInstance().getCategory(
				string);
		assertFalse("No Category for" + string, category == null);
		assertTrue("Wrong Category for" + string, category.equals("Test Markers"));

	}

}
