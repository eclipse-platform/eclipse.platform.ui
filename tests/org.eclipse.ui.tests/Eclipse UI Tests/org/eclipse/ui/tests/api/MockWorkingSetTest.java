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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the WorkingSetDescriptor and WorkingSetRegistry.
 */
public class MockWorkingSetTest {
	static final String WORKING_SET_ID = "org.eclipse.ui.tests.api.MockWorkingSet";

	static final String WORKING_SET_NAME = "Mock Working Set";

	static final String WORKING_SET_PAGE_CLASS_NAME = "org.eclipse.ui.tests.api.MockWorkingSetPage";

	WorkingSetRegistry fRegistry;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		fRegistry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
	}

	@Test
	public void testWorkingSetDescriptor() throws Throwable {
		WorkingSetDescriptor workingSetDescriptor = fRegistry
				.getWorkingSetDescriptor(WORKING_SET_ID);

		assertNotNull(workingSetDescriptor.getIcon());
		assertEquals(WORKING_SET_ID, workingSetDescriptor.getId());
		assertEquals(WORKING_SET_NAME, workingSetDescriptor.getName());
		assertEquals(WORKING_SET_PAGE_CLASS_NAME, workingSetDescriptor
				.getPageClassName());
	}

	@Test
	public void testWorkingSetRegistry() throws Throwable {
		WorkingSetDescriptor[] workingSetDescriptors = fRegistry
				.getWorkingSetDescriptors();
		/*
		 * Should have at least resourceWorkingSetPage and MockWorkingSet
		 */
		assertTrue(workingSetDescriptors.length >= 2);

		assertEquals(Class.forName(WORKING_SET_PAGE_CLASS_NAME), fRegistry
				.getWorkingSetPage(WORKING_SET_ID).getClass());
	}

}

