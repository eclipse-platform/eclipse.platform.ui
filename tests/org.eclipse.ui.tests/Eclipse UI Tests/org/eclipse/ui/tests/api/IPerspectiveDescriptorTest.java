/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

public class IPerspectiveDescriptorTest {

	private IPerspectiveDescriptor[] fPerspectives;

	@Before
	public void setUp() {
		fPerspectives = PlatformUI
				.getWorkbench().getPerspectiveRegistry().getPerspectives();
	}

	/**
	 * Tests that the ids for all perspective descriptors are non-null and non-empty.
	 */
	@Test
	public void testGetId() {
		for (IPerspectiveDescriptor fPerspective : fPerspectives) {
			String id = fPerspective.getId();
			assertNotNull(id);
			assertTrue(!id.isEmpty());
		}
	}

	/**
	 * Tests that the labels for all perspective descriptors are non-null and non-empty.
	 */
	@Test
	public void testGetLabel() {
		for (IPerspectiveDescriptor fPerspective : fPerspectives) {
			String label = fPerspective.getLabel();
			assertNotNull(label);
			assertTrue(!label.isEmpty());
		}
	}

	/**
	 * Tests that the image descriptors for all perspective descriptors are non-null.
	 * <p>
	 * Note that some perspective extensions in the test suite do not specify an icon
	 * attribute.  getImageDescriptor should return a default image descriptor in this
	 * case.  This is a regression test for bug 68325.
	 * </p>
	 */
	@Test
	public void testGetImageDescriptor() {
		for (IPerspectiveDescriptor fPerspective : fPerspectives) {
			ImageDescriptor image = fPerspective.getImageDescriptor();
			assertNotNull(image);
		}
	}

}

