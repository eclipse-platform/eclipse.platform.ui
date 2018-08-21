/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.debug.tests.stepfilters;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IStepFilter;
import org.eclipse.debug.tests.AbstractDebugTest;

/**
 * Tests step filters
 */
public class StepFiltersTests extends AbstractDebugTest {

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
