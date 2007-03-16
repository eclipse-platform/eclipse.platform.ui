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
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ICheckable;

public abstract class StructuredItemViewerTest extends StructuredViewerTest {

    public StructuredItemViewerTest(String name) {
        super(name);
    }

    public void testCheckElement() {

        if (fViewer instanceof ICheckable) {
            TestElement first = fRootElement.getFirstChild();
            TestElement firstfirst = first.getFirstChild();

            ICheckable ctv = (ICheckable) fViewer;
            ctv.setChecked(first, true);
            assertTrue(ctv.getChecked(first));

            // checking an invisible element
            if (fViewer instanceof AbstractTreeViewer) {
                // The first child of the first child can only be resolved in a tree
                assertTrue(ctv.setChecked(firstfirst, true));
                assertTrue(ctv.getChecked(firstfirst));
            } else {
                assertTrue(!ctv.setChecked(firstfirst, true));
                assertTrue(!ctv.getChecked(firstfirst));
            }

            ctv.setChecked(first, false);
            assertTrue(!ctv.getChecked(first));
        }
    }
}
