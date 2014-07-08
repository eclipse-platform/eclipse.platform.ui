/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.debug.tests.stepfilters;

import junit.framework.TestCase;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IStepFilter;

/**
 * Tests step filters
 */
public class StepFiltersTests extends TestCase {

	public void testStepFitlersExtension_01() {
		IStepFilter[] stepFilters = DebugPlugin.getStepFilters("com.example.lalala.model"); //$NON-NLS-1$
		assertNotNull(stepFilters);
		assertEquals(0, stepFilters.length);
	}

	public void testStepFitlersExtension_02() {
		IStepFilter[] stepFilters = DebugPlugin.getStepFilters("com.example.debug.model"); //$NON-NLS-1$
		assertNotNull(stepFilters);
		assertEquals(1, stepFilters.length);

		assertTrue(stepFilters[0].isFiltered(Boolean.TRUE));
		assertFalse(stepFilters[0].isFiltered(Boolean.FALSE));
		assertFalse(stepFilters[0].isFiltered(new Object()));
	}

}
