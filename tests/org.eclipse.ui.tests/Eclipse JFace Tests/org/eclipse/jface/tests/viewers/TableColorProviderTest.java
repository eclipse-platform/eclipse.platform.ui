/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Initial implementation - Gunnar Ahlberg - www.gunnarahlberg.com
 *     IBM Corporation - further revisions
 *******************************************************************************/

package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.tests.viewers.TableViewerTest.TableTestLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
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
     *  
     */
    public void testColorProviderForeground() {
        TableViewer viewer = (TableViewer) fViewer;
        TableColorViewLabelProvider provider = (TableColorViewLabelProvider) viewer.getLabelProvider();

        //refresh so that the colors are set
        fViewer.refresh();

    	
    	TestElement first = fRootElement.getFirstChild();
    	Color providerFG = provider.getForeground(first, 0);
    	assertEquals("foreground green", providerFG, green);//$NON-NLS-1$
    	assertTrue("foreground green", providerFG.equals(red)== false);//$NON-NLS-1$
    	
    	
    	provider.fExtended = false;

    } 
    
    /**
     * Test that the backgrounds are being set.
     */
    public void testColorProviderBackground() {
        TableViewer viewer = (TableViewer) fViewer;
        TableColorViewLabelProvider provider = (TableColorViewLabelProvider) viewer.getLabelProvider();

        fViewer.refresh();

    	
    	TestElement first = fRootElement.getFirstChild();
    	Color providerBG = provider.getBackground(first, 0);
    	assertEquals("background red", providerBG, red); //$NON-NLS-1$
    	assertTrue("background red", providerBG.equals(green) == false); //$NON-NLS-1$
    	
    	
    	provider.fExtended = false;

    }
    
    /**
     * Test that the foregrounds are being set.
     *
     */
    public void testTableItemsColorProviderForeground() {
        TableViewer viewer = (TableViewer) fViewer;
        TableColorViewLabelProvider provider = (TableColorViewLabelProvider) viewer.getLabelProvider();
        Table table = viewer.getTable();
      
        fViewer.refresh();
    	
    	Color tableItemFG = table.getItem(0).getForeground(0);
    	assertEquals("table item green", tableItemFG, green);//$NON-NLS-1$
    	assertTrue("table item green", tableItemFG.equals(red) == false);//$NON-NLS-1$
    	provider.fExtended = false;

    }
    
    /**
     * Test the table item colours.
     *
     */
    public void testTableItemsColorProviderBackground() {
        TableViewer viewer = (TableViewer) fViewer;
        TableColorViewLabelProvider provider = (TableColorViewLabelProvider) viewer.getLabelProvider();
        Table table = viewer.getTable();
    	fViewer.refresh();

    	
    	Color tableItemBG = table.getItem(0).getBackground(0);
    	assertEquals("table item background red", tableItemBG, red);//$NON-NLS-1$
    	assertTrue("table item background red", tableItemBG.equals(green) == false);//$NON-NLS-1$
    	provider.fExtended = false;

    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#tearDown()
     */
    public void tearDown() {
        super.tearDown();
        red.dispose();
        green.dispose();
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#setUp()
     */
    public void setUp() {
        super.setUp();
        red = new Color(Display.getCurrent(), 255, 0 ,0);
        green = new Color(Display.getCurrent(), 0, 255,0);
    }

    /**
     * Run as a stand alone test
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TableColorProviderTest.class);
    }
    

    /* (non-Javadoc)
     * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#createViewer(org.eclipse.swt.widgets.Composite)
     */
    protected StructuredViewer createViewer(Composite parent) {
    	TableViewer viewer = new TableViewer(parent);
    	viewer.setContentProvider(new TestModelContentProvider());
    	viewer.setLabelProvider(new TableColorViewLabelProvider());
    	viewer.getTable().setLinesVisible(true);

    	TableLayout layout = new TableLayout();
    	viewer.getTable().setLayout(layout);
    	viewer.getTable().setHeaderVisible(true);
    	String headers[] = { "column 1 header", "column 2 header" };//$NON-NLS-1$ //$NON-NLS-2$

    	ColumnLayoutData layouts[] =
    		{ new ColumnWeightData(100), new ColumnWeightData(100)};

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

    protected int getItemCount() {
    	TestElement first = fRootElement.getFirstChild();
    	TableItem ti = (TableItem) fViewer.testFindItem(first);
    	Table table = ti.getParent();
    	return table.getItemCount();
    }
    protected String getItemText(int at) {
    	Table table = (Table) fViewer.getControl();
    	return table.getItem(at).getText();
    }
 
    class TableColorViewLabelProvider extends TableTestLabelProvider implements ITableColorProvider{
       
    
    	public Image getColumnImage(Object obj, int index) {
    		return null;
    	}
    	
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
         */
        public Color getForeground(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return green;
                
            default:
                return red;
            }    	
        }
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
         */
        public Color getBackground(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return red;
            default:
                return green;
            }    	
        }
  
    }

}