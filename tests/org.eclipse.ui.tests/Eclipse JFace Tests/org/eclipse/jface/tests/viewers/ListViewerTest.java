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
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class ListViewerTest extends StructuredViewerTest {

    public ListViewerTest(String name) {
        super(name);
    }

    protected StructuredViewer createViewer(Composite parent) {
        ListViewer viewer = new ListViewer(parent);
        viewer.setContentProvider(new TestModelContentProvider());
        return viewer;
    }

    protected int getItemCount() {
        TestElement first = fRootElement.getFirstChild();
        List list = (List) fViewer.testFindItem(first);
        return list.getItemCount();
    }

    protected String getItemText(int at) {
        List list = (List) fViewer.getControl();
        return list.getItem(at);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(ListViewerTest.class);
    }
}