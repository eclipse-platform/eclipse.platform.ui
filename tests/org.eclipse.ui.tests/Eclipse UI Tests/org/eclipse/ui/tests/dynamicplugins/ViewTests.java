/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IViewRegistry;

/**
 * Tests to ensure the addition of new views with dynamic plug-ins.
 */
public class ViewTests extends DynamicTestCase {

    public ViewTests(String testName) {
        super(testName);
    }

    public void testFindViewInRegistry() {
    	getBundle();
        IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
        assertNotNull(reg.find("org.eclipse.newView1.newView1"));
        
        
        // Removal does not currently work for views
        /*
        removeBundle();
        assertNull(reg.find("org.eclipse.newView1.newView1"));
        */
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {
		return "newView1.testDynamicViewAddition";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchConstants.PL_VIEWS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newView1";
	}
}