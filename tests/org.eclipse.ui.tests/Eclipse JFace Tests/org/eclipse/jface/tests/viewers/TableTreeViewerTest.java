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

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

public class TableTreeViewerTest extends AbstractTreeViewerTest {

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

    public TableTreeViewerTest(String name) {
        super(name);
    }

    protected StructuredViewer createViewer(Composite parent) {
        TableTreeViewer viewer = new TableTreeViewer(parent);
        viewer.setContentProvider(new TestModelContentProvider());
        viewer.setLabelProvider(new TableTreeTestLabelProvider());
        viewer.getTableTree().getTable().setLinesVisible(true);

        TableLayout layout = new TableLayout();
        viewer.getTableTree().getTable().setLayout(layout);
        viewer.getTableTree().getTable().setHeaderVisible(true);
        String headers[] = { "column 1 header", "column 2 header" };

        ColumnLayoutData layouts[] = { new ColumnWeightData(100),
                new ColumnWeightData(100) };

        final TableColumn columns[] = new TableColumn[headers.length];

        for (int i = 0; i < headers.length; i++) {
            layout.addColumnData(layouts[i]);
            TableColumn tc = new TableColumn(viewer.getTableTree().getTable(),
                    SWT.NONE, i);
            tc.setResizable(layouts[i].resizable);
            tc.setText(headers[i]);
            columns[i] = tc;
        }
        fTreeViewer = viewer;
        return viewer;
    }

    protected int getItemCount() {
        TestElement first = fRootElement.getFirstChild();
        TableTreeItem ti = (TableTreeItem) fViewer.testFindItem(first);
        TableTree table = ti.getParent();
        return table.getItemCount();
    }

    protected int getItemCount(TestElement element) {
        TableTreeItem ti = (TableTreeItem) fViewer.testFindItem(element);
        return ti.getItemCount();
    }

    protected String getItemText(int at) {
        TableTree table = (TableTree) fViewer.getControl();
        return table.getItems()[at].getText();
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(TableTreeViewerTest.class);
    }

    public void testLabelProvider() {
        TableTreeViewer viewer = (TableTreeViewer) fViewer;
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
        TableTreeViewer viewer = (TableTreeViewer) fViewer;
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
