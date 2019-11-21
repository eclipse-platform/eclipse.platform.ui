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

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

/**
 * Tests the PlatformUI class.
 */
public class PlatformUITest {

	@Test
	public void testGetWorkbench() throws Throwable {
		// From Javadoc: "Returns the workbench interface."
		IWorkbench wb = PlatformUI.getWorkbench();
		assertNotNull(wb);
	}

	@Test
	public void testPLUGIN_ID() {
		// From Javadoc: "Identifies the workbench plugin."
		assertNotNull(PlatformUI.PLUGIN_ID);
	}
}
