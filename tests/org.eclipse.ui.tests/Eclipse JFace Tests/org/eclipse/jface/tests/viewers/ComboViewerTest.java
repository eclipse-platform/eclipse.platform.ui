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
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.0
 */
public class ComboViewerTest extends StructuredViewerTest {
    public ComboViewerTest(String name) {
        super(name);
    }

    protected StructuredViewer createViewer(Composite parent) {
        ComboViewer viewer = new ComboViewer(parent);
        viewer.setContentProvider(new TestModelContentProvider());
        return viewer;
    }

    protected int getItemCount() {
        TestElement first = fRootElement.getFirstChild();
        Combo list = (Combo) fViewer.testFindItem(first);
        return list.getItemCount();
    }

    protected String getItemText(int at) {
        Combo list = (Combo) fViewer.getControl();
        return list.getItem(at);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(ComboViewerTest.class);
    }

    /**
     * TODO: Determine if this test is applicable to ComboViewer 
     */
    public void testInsertChild() {

    }
}
