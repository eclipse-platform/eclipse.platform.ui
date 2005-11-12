/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
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
    
    public void testRevealBug69076() {
    	// TODO remove the Mac OS check when SWT has fixed the bug in List.java
    	// see bug 116105
    	if ("carbon".equals(SWT.getPlatform())) {
    		return;
    	}
		fViewer = null;
		if (fShell != null) {
			fShell.dispose();
			fShell = null;
		}
		openBrowser();
		for (int i = 40; i < 45; i++) {
			fRootElement = TestElement.createModel(1, i);
			fModel = fRootElement.getModel();
			fViewer.setInput(fRootElement);
			for (int j = 30; j < fRootElement.getChildCount(); j++) {
				fViewer.setSelection(new StructuredSelection(fRootElement
						.getFirstChild()), true);
				TestElement child = fRootElement.getChildAt(j);
				fViewer.reveal(child);
				List list = ((ListViewer) fViewer).getList();
				int topIndex = list.getTopIndex();
				// even though we pass in reveal=false, SWT still scrolls to show the selection (since 20020815)
				fViewer.setSelection(new StructuredSelection(child), false);
				assertEquals("topIndex should not change on setSelection", topIndex, list
						.getTopIndex());
				list.showSelection();
				assertEquals("topIndex should not change on showSelection", topIndex, list
						.getTopIndex());
			}
		}
	}
}
