/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import junit.framework.TestCase;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Tests the PlatformUI class.
 */
public class PlatformUITest extends TestCase {

    public PlatformUITest(String testName) {
        super(testName);
    }

    public void testGetWorkbench() throws Throwable {
        // From Javadoc: "Returns the workbench interface."
        IWorkbench wb = PlatformUI.getWorkbench();
        assertNotNull(wb);
    }

    public void testPLUGIN_ID() {
        // From Javadoc: "Identifies the workbench plugin."
        assertNotNull(PlatformUI.PLUGIN_ID);
    }
}