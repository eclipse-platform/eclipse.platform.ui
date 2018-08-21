/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
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
package org.eclipse.debug.tests.launching;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.tests.AbstractDebugTest;

/**
 * Tests accelerator adjustments for DBCS languages.
 * See bug 186921.
 *
 * @since 3.3
 */
public class AcceleratorSubstitutionTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name the name of the test
	 */
	public AcceleratorSubstitutionTests(String name) {
		super(name);
	}

	/**
	 * tests a string with "..."
	 */
	public void testWithEllipses() {
		assertEquals("incorrect DBCS accelerator substitution", //$NON-NLS-1$
				"Open Run Dialog(&R)...", //$NON-NLS-1$
				DebugUIPlugin.adjustDBCSAccelerator("Open Run(&R) Dialog...")); //$NON-NLS-1$
	}

	/**
	 * tests a string without "..."
	 */
	public void testWithoutEllipses() {
		assertEquals("incorrect DBCS accelerator substitution", //$NON-NLS-1$
				"Open Run Dialog(&R)", //$NON-NLS-1$
				DebugUIPlugin.adjustDBCSAccelerator("Open Run(&R) Dialog")); //$NON-NLS-1$
	}

	/**
	 * tests a string that should not change (no DBCS style accelerator).
	 */
	public void testWithoutDBCSAcclerator() {
		assertEquals("incorrect DBCS accelerator substitution", //$NON-NLS-1$
				"Open &Run Dialog...", //$NON-NLS-1$
				DebugUIPlugin.adjustDBCSAccelerator("Open &Run Dialog...")); //$NON-NLS-1$
	}
}
