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

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class CheckboxTableViewerTest extends TableViewerTest {
    public static class CheckboxTableTestLabelProvider extends
            TestLabelProvider implements ITableLabelProvider {

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

    public CheckboxTableViewerTest(String name) {
        super(name);
    }

    protected StructuredViewer createViewer(Composite parent) {
        Table table = new Table(parent, SWT.CHECK | SWT.BORDER);
        table.setLinesVisible(true);
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setHeaderVisible(true);

        String headers[] = { "column 1 header", "column 2 header" };

        ColumnLayoutData layouts[] = { new ColumnWeightData(100),
                new ColumnWeightData(100) };

        final TableColumn columns[] = new TableColumn[headers.length];

        for (int i = 0; i < headers.length; i++) {
            layout.addColumnData(layouts[i]);
            TableColumn tc = new TableColumn(table, SWT.NONE, i);
            tc.setResizable(layouts[i].resizable);
            tc.setText(headers[i]);
            columns[i] = tc;
        }

        TableViewer viewer = new CheckboxTableViewer(table);
        viewer.setContentProvider(new TestModelContentProvider());
        viewer.setLabelProvider(new TableTestLabelProvider());
        return viewer;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(CheckboxTableViewerTest.class);
    }

    public void testCheckAllElements() {
        CheckboxTableViewer ctv = (CheckboxTableViewer) fViewer;
        ctv.setAllChecked(true);
        assertTrue(ctv.getChecked(fRootElement.getFirstChild()));
        assertTrue(ctv.getChecked(fRootElement.getLastChild()));
        ctv.setAllChecked(false);
        assertTrue(!ctv.getChecked(fRootElement.getFirstChild()));
        assertTrue(!ctv.getChecked(fRootElement.getLastChild()));
    }

    public void testGrayAllElements() {
        CheckboxTableViewer ctv = (CheckboxTableViewer) fViewer;
        ctv.setAllGrayed(true);
        assertTrue(ctv.getGrayed(fRootElement.getFirstChild()));
        assertTrue(ctv.getGrayed(fRootElement.getLastChild()));
        ctv.setAllGrayed(false);
        assertTrue(!ctv.getGrayed(fRootElement.getFirstChild()));
        assertTrue(!ctv.getGrayed(fRootElement.getLastChild()));
    }

    public void testGrayed() {
        CheckboxTableViewer ctv = (CheckboxTableViewer) fViewer;
        TestElement element = fRootElement.getFirstChild();

        assertTrue(ctv.getGrayedElements().length == 0);
        assertTrue(!ctv.getGrayed(element));

        ctv.setGrayed(element, true);
        assertTrue(ctv.getGrayedElements().length == 1);
        assertTrue(ctv.getGrayed(element));

        ctv.setGrayed(element, false);
        assertTrue(ctv.getGrayedElements().length == 0);
        assertTrue(!ctv.getGrayed(element));

        ctv.setAllGrayed(false);
    }

    public void testGrayedElements() {
        CheckboxTableViewer ctv = (CheckboxTableViewer) fViewer;
        TestElement first = fRootElement.getFirstChild();
        TestElement last = fRootElement.getLastChild();

        assertTrue(ctv.getGrayedElements().length == 0);
        assertTrue(!ctv.getGrayed(first));
        assertTrue(!ctv.getGrayed(last));

        ctv.setGrayed(first, true);
        ctv.setGrayed(last, true);
        Object[] elements = ctv.getGrayedElements();
        assertTrue(elements.length == 2);
        assertTrue(elements[0] == first);
        assertTrue(elements[1] == last);

        ctv.setGrayed(first, false);
        ctv.setGrayed(last, false);
        assertTrue(ctv.getGrayedElements().length == 0);

        ctv.setAllGrayed(false);
    }
}
