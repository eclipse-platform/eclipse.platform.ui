/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.1
 */
public class BrowserTests extends DynamicTestCase {

    /**
     * @param testName
     */
    public BrowserTests(String testName) {
        super(testName);
    }

    public void testBrowserSupport() {
        WorkbenchBrowserSupport support = (WorkbenchBrowserSupport) WorkbenchBrowserSupport.getInstance();
        try {
            support.setDesiredBrowserSupportId(getExtensionId());
            assertFalse(support.hasNonDefaultBrowser());
            
            getBundle();
            support.setDesiredBrowserSupportId(getExtensionId());
            assertTrue(support.hasNonDefaultBrowser());
            
            removeBundle();
            support.setDesiredBrowserSupportId(getExtensionId());    
            assertFalse(support.hasNonDefaultBrowser());
        }
        finally {
            support.setDesiredBrowserSupportId(null);    
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
     */
    protected String getExtensionId() {
        return "newBrowser1.testDynamicBrowserAddition";
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
     */
    protected String getExtensionPoint() {
        return IWorkbenchRegistryConstants.PL_BROWSER_SUPPORT;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
     */
    protected String getInstallLocation() {
        return "data/org.eclipse.newBrowser1";
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
     */
    protected String getMarkerClass() {
        return "org.eclipse.ui.dynamic.DynamicBrowserSupport";
    }

}
