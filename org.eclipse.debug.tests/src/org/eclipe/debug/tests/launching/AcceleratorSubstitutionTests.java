/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipe.debug.tests.launching;

import junit.framework.TestCase;

import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * Tests accelerator adjustments for DBCS languages.
 * See bug 186921.
 * 
 * @since 3.3
 */
public class AcceleratorSubstitutionTests extends TestCase {

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
