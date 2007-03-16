/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import junit.framework.TestCase;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;

public class IPerspectiveDescriptorTest extends TestCase {

    private IPerspectiveDescriptor[] fPerspectives;

    public IPerspectiveDescriptorTest(String testName) {
        super(testName);
    }

    public void setUp() {
        fPerspectives = PlatformUI
                .getWorkbench().getPerspectiveRegistry().getPerspectives();
    }

    /**
     * Tests that the ids for all perspective descriptors are non-null and non-empty.
     */
    public void testGetId() {
        for (int i = 0; i < fPerspectives.length; i++) {
            String id = fPerspectives[i].getId();
            assertNotNull(id);
            assertTrue(id.length() > 0);
        }
    }

    /**
     * Tests that the labels for all perspective descriptors are non-null and non-empty.
     */
    public void testGetLabel() {
        for (int i = 0; i < fPerspectives.length; i++) {
            String label = fPerspectives[i].getLabel();
            assertNotNull(label);
            assertTrue(label.length() > 0);
        }
    }

    /**
     * Tests that the image descriptors for all perspective descriptors are non-null.
     * <p>
     * Note that some perspective extensions in the test suite do not specify an icon
     * attribute.  getImageDescriptor should return a default image descriptor in this
     * case.  This is a regression test for bug 68325.
     * </p>
     */
    public void testGetImageDescriptor() {
        for (int i = 0; i < fPerspectives.length; i++) {
            ImageDescriptor image = fPerspectives[i].getImageDescriptor();
            assertNotNull(image);
        }
    }
    
}

