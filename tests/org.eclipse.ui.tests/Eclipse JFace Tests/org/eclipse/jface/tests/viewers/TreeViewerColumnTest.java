/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;

public class TreeViewerColumnTest extends AbstractTreeViewerTest {

    public static class TableTreeTestLabelProvider extends TestLabelProvider
            implements ITableLabelProvider {
        public boolean fExtended = false;

        public String getText(Object element) {
            if (fExtended)
                return providedString((String) element);

            return element.toString();
        }

        public String getColumnText(Object element, int index) {
            if (fExtended)
                return providedString((TestElement) element);
            return element.toString();
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    public TreeViewerColumnTest(String name) {
        super(name);
    }

    protected StructuredViewer createViewer(Composite parent) {
        TreeViewer viewer = new TreeViewer(parent);
        viewer.setContentProvider(new TestModelContentProvider());
        viewer.setLabelProvider(new TableTreeTestLabelProvider());
        viewer.getTree().setLinesVisible(true);

        viewer.getTree().setHeaderVisible(true);
        String headers[] = { "column 1 header", "column 2 header" };

        final TreeColumn columns[] = new TreeColumn[headers.length];

        for (int i = 0; i < headers.length; i++) {
             TreeColumn tc = new TreeColumn(viewer.getTree(),
                    SWT.NONE, i);
            tc.setResizable(true);
            tc.setText(headers[i]);
            tc.setWidth(25);
            columns[i] = tc;
        }
        fTreeViewer = viewer;
        return viewer;
    }
    
    protected int getItemCount() {
        TestElement first = fRootElement.getFirstChild();
        TreeItem ti = (TreeItem) fViewer.testFindItem(first);
         return ti.getParent().getItemCount();
    }

    protected int getItemCount(TestElement element) {
        TreeItem ti = (TreeItem) fViewer.testFindItem(element);
        return ti.getItemCount();
    }

    protected String getItemText(int at) {
        return ((Tree) fViewer.getControl()).getItems()[at].getText();
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(TreeViewerColumnTest.class);
    }

    public void testViewerColumn() {
    	assertNull(((TreeViewer)fViewer).getViewerColumn(-1));
    	assertNotNull(((TreeViewer)fViewer).getViewerColumn(0));
    	assertNotNull(((TreeViewer)fViewer).getViewerColumn(1));
    	assertNull(((TreeViewer)fViewer).getViewerColumn(2));
    }
    
    public void testLabelProvider() {
        TreeViewer viewer = (TreeViewer) fViewer;
        TableTreeTestLabelProvider provider = (TableTreeTestLabelProvider) viewer
                .getLabelProvider();
        provider.fExtended = true;
        // BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
        fViewer.refresh();
        TestElement first = fRootElement.getFirstChild();
        String newLabel = providedString(first);
        assertEquals("rendered label", newLabel, getItemText(0));
        provider.fExtended = false;
        // BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
        fViewer.refresh();
    }

    public void testLabelProviderStateChange() {
        TreeViewer viewer = (TreeViewer) fViewer;
        TableTreeTestLabelProvider provider = (TableTreeTestLabelProvider) viewer
                .getLabelProvider();
        provider.fExtended = true;
        provider.setSuffix("added suffix");
        // BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
        fViewer.refresh();
        TestElement first = fRootElement.getFirstChild();
        String newLabel = providedString(first);
        assertEquals("rendered label", newLabel, getItemText(0));
        provider.fExtended = false;
        // BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
        fViewer.refresh();
    }
}
