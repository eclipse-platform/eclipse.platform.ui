/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Initial implementation - Gunnar Ahlberg (IBS AB, www.ibs.net)
 *     IBM Corporation - further revisions
 *******************************************************************************/

package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.tests.viewers.TableViewerTest.TableTestLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * The TableColorProviderTest is a test suite designed to test
 * ITableColorProviders.
 */
public class TableColorProviderTest extends StructuredViewerTest {
    Color red = null;

    Color green = null;

    /**
     * Create a new instance of the receiver
     * @param name
     */
    public TableColorProviderTest(String name) {
        super(name);
    }

    /**
     *  Test with a standard color provider.
     */
    public void testColorProviderForeground() {
        TableViewer viewer = (TableViewer) fViewer;
        ColorViewLabelProvider provider = new ColorViewLabelProvider();

        viewer.setLabelProvider(provider);

        //refresh so that the colors are set
        fViewer.refresh();

        assertEquals("foreground 1 green", viewer.getTable().getItem(0).getForeground(0), green);//$NON-NLS-1$
        assertEquals("foreground 2 green", viewer.getTable().getItem(0).getForeground(1), green);//$NON-NLS-1$

        provider.fExtended = false;

    }

    /**
     * Test that the backgrounds are being set.
     */
    public void testColorProviderBackground() {
        TableViewer viewer = (TableViewer) fViewer;
        ColorViewLabelProvider provider = new ColorViewLabelProvider();

        viewer.setLabelProvider(provider);

        fViewer.refresh();

        assertEquals("background 1 red", viewer.getTable().getItem(0).getBackground(0), red);//$NON-NLS-1$
        assertEquals("background 2 red", viewer.getTable().getItem(1).getBackground(1), red);//$NON-NLS-1$

        provider.fExtended = false;

    }

    /**
     * Test that the foregrounds are being set.
     *
     */
    public void testTableItemsColorProviderForeground() {
        TableViewer viewer = (TableViewer) fViewer;
        TableColorViewLabelProvider provider = new TableColorViewLabelProvider();

        viewer.setLabelProvider(provider);
        Table table = viewer.getTable();

        fViewer.refresh();

        assertEquals("table item 1 green", table.getItem(0).getForeground(0), green);//$NON-NLS-1$
        assertEquals("table item 2 red", table.getItem(0).getForeground(1), red);//$NON-NLS-1$
        provider.fExtended = false;

    }

    /**
     * Test the table item colours.
     *
     */
    public void testTableItemsColorProviderBackground() {
        TableViewer viewer = (TableViewer) fViewer;
        TableColorViewLabelProvider provider = new TableColorViewLabelProvider();

        viewer.setLabelProvider(provider);

        Table table = viewer.getTable();
        fViewer.refresh();

        assertEquals("table item 1 background red", table.getItem(0).getBackground(0), red);//$NON-NLS-1$
        assertEquals("table item 2 background green", table.getItem(0).getBackground(1), green);//$NON-NLS-1$
        provider.fExtended = false;

    }

    @Override
	public void tearDown() {
        super.tearDown();
        red.dispose();
        green.dispose();
    }

    @Override
	public void setUp() {
        super.setUp();
        red = new Color(Display.getCurrent(), 255, 0, 0);
        green = new Color(Display.getCurrent(), 0, 255, 0);
    }

    /**
     * Run as a stand alone test
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TableColorProviderTest.class);
    }

    @Override
	protected StructuredViewer createViewer(Composite parent) {
        TableViewer viewer = new TableViewer(parent);
        viewer.setContentProvider(new TestModelContentProvider());

        viewer.getTable().setLinesVisible(true);

        TableLayout layout = new TableLayout();
        viewer.getTable().setLayout(layout);
        viewer.getTable().setHeaderVisible(true);
        String headers[] = { "column 1 header", "column 2 header" };//$NON-NLS-1$ //$NON-NLS-2$

        ColumnLayoutData layouts[] = { new ColumnWeightData(100),
                new ColumnWeightData(100) };

        final TableColumn columns[] = new TableColumn[headers.length];

        for (int i = 0; i < headers.length; i++) {
            layout.addColumnData(layouts[i]);
            TableColumn tc = new TableColumn(viewer.getTable(), SWT.NONE, i);
            tc.setResizable(layouts[i].resizable);
            tc.setText(headers[i]);
            columns[i] = tc;
        }
        return viewer;
    }

    @Override
	protected int getItemCount() {
        TestElement first = fRootElement.getFirstChild();
        TableItem ti = (TableItem) fViewer.testFindItem(first);
        Table table = ti.getParent();
        return table.getItemCount();
    }

    @Override
	protected String getItemText(int at) {
        Table table = (Table) fViewer.getControl();
        return table.getItem(at).getText();
    }

    class TableColorViewLabelProvider extends TableTestLabelProvider implements
            ITableColorProvider {

        @Override
		public Image getColumnImage(Object obj, int index) {
            return null;
        }

        @Override
		public Color getForeground(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return green;

            default:
                return red;
            }
        }



        @Override
		public Color getBackground(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return red;
            default:
                return green;
            }
        }

    }

    /**
     * A class to test color providing without coloured columns.
     */
    class ColorViewLabelProvider extends TableTestLabelProvider implements IColorProvider{
		@Override
		public Color getBackground(Object element) {
			return red;
		}

		@Override
		public Color getForeground(Object element) {
			return green;
		}
    }
}
