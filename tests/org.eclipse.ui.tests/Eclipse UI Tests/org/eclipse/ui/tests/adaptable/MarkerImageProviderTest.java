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
package org.eclipse.ui.tests.adaptable;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests the markerImageProviders extension point.
 */
public class MarkerImageProviderTest extends UITestCase {

    public MarkerImageProviderTest(String testName) {
        super(testName);
    }

    /**
     * Tests the static form of the extension, where just a file path is given.
     */
    public void testStatic() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IMarker marker = null;
        try {
            marker = workspace.getRoot().createMarker(
                    "org.eclipse.ui.tests.testmarker"); //$NON-NLS-1$
        } catch (CoreException e) {
            fail(e.getMessage());
        }
        IWorkbenchAdapter adapter = (IWorkbenchAdapter) marker
                .getAdapter(IWorkbenchAdapter.class);
        ImageDescriptor imageDesc = adapter.getImageDescriptor(marker);
        assertNotNull(imageDesc);
        assertTrue(imageDesc.toString().indexOf("anything") != -1); //$NON-NLS-1$
    }

    /**
     * Tests the dynamic form of the extension, where an IMarkerImageProvider class is given.
     */
    public void testDynamic() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IMarker marker = null;
        try {
            marker = workspace.getRoot().createMarker(
                    "org.eclipse.ui.tests.testmarker2"); //$NON-NLS-1$
        } catch (CoreException e) {
            fail(e.getMessage());
        }
        IWorkbenchAdapter adapter = (IWorkbenchAdapter) marker
                .getAdapter(IWorkbenchAdapter.class);
        ImageDescriptor imageDesc = adapter.getImageDescriptor(marker);
        assertNotNull(imageDesc);
        assertTrue(imageDesc.toString().indexOf("anything") != -1); //$NON-NLS-1$
    }

}
