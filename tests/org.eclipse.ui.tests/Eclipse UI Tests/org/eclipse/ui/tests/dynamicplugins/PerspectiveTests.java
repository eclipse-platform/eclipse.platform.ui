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
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.util.UITestCase;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Tests to check the addition of a new perspective once the perspective
 * registry is loaded.
 */

public class PerspectiveTests extends UITestCase implements
        IRegistryChangeListener {
    private IPerspectiveRegistry fReg;

    volatile boolean perspectiveRegistryUpdated = false;

    public PerspectiveTests(String testName) {
        super(testName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
     */
    public void registryChanged(IRegistryChangeEvent event) {
        // Just retrieve any changes relating to the extension point
        // org.eclipse.ui.perspectives
        IExtensionDelta delta = event.getExtensionDelta(
                WorkbenchPlugin.PI_WORKBENCH,
                IWorkbenchConstants.PL_PERSPECTIVES,
                "newPerspective1.testDynamicPerspectiveAddition");
        if (delta != null && delta.getKind() == IExtensionDelta.ADDED)
            perspectiveRegistryUpdated = true;
    }

    public void testFindPerspectiveInRegistry() {
        // Just try to find the new perspective.  Don't actually try to
        // do anything with it as the class it refers to does not exist.
        Platform.getExtensionRegistry().addRegistryChangeListener(this);
        perspectiveRegistryUpdated = false;
        Bundle newBundle = null;
        try {
            newBundle = DynamicUtils
                    .installPlugin("data/org.eclipse.newPerspective1");
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
            while (!perspectiveRegistryUpdated && !timeToFail) {
                processEvents();
                timeToFail = System.currentTimeMillis() > potentialEndTime;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            assertEquals("Test failed due to timeout", false, timeToFail);
            fReg = PlatformUI.getWorkbench().getPerspectiveRegistry();

            IPerspectiveDescriptor found = fReg
                    .findPerspectiveWithId("org.eclipse.newPerspective1.newPerspective1");
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