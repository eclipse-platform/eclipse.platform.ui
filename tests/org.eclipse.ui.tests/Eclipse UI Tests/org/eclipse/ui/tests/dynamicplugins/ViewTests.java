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

import java.io.IOException;

import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;
import org.eclipse.ui.tests.util.UITestCase;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Tests to ensure the addition of new views with dynamic plug-ins.
 */

public class ViewTests extends UITestCase implements IRegistryChangeListener {
    private IViewRegistry fReg;

    volatile boolean viewRegistryUpdated = false;

    public ViewTests(String testName) {
        super(testName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
     */
    public void registryChanged(IRegistryChangeEvent event) {
        // Just retrieve any changes relating to the extension point
        // org.eclipse.ui.views
        IExtensionDelta delta = event.getExtensionDelta(
                WorkbenchPlugin.PI_WORKBENCH, IWorkbenchConstants.PL_VIEWS,
                "newView1.testDynamicViewAddition");
        if (delta != null && delta.getKind() == IExtensionDelta.ADDED)
            viewRegistryUpdated = true;
    }

    public void testFindViewInRegistry() {
        // Just try to find the new view.  Don't actually try to
        // do anything with it as the class it refers to does not exist.
        Platform.getExtensionRegistry().addRegistryChangeListener(this);
        viewRegistryUpdated = false;
        Bundle newBundle = null;
        try {
            newBundle = DynamicUtils.installPlugin("data/org.eclipse.newView1");
        } catch (IOException e1) {
            e1.printStackTrace();
            fail("Dynamic install generated an IOException");
        } catch (BundleException e1) {
            e1.printStackTrace();
            fail("Dynamic install generated a BundleException");
        } catch (IllegalStateException e1) {
            e1.printStackTrace();
            fail("Dynamic install generated an IllegalStateException - this plugin has been installed previously");
        }
        try {
            long startTime = System.currentTimeMillis();
            long potentialEndTime = startTime + 1000;
            boolean timeToFail = false;
            while (!viewRegistryUpdated && !timeToFail) {
                processEvents();
                timeToFail = System.currentTimeMillis() > potentialEndTime;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            assertEquals("Test failed due to timeout", false, timeToFail);
            fReg = WorkbenchPlugin.getDefault().getViewRegistry();
            IViewDescriptor found = fReg.find("org.eclipse.newView1.newView1");
            assertNotNull(found);
        } finally {
            try {
                Platform.getExtensionRegistry().removeRegistryChangeListener(
                        this);
                DynamicUtils.uninstallPlugin(newBundle);
            } catch (BundleException e) {
                // just cleaning up
            }
        }
    }
}