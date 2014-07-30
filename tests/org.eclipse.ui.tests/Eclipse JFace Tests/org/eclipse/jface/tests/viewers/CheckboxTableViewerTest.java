/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.tests.viewers.CheckStateProviderTestsUtil.TestCheckStateProvider;
import org.eclipse.jface.tests.viewers.CheckStateProviderTestsUtil.TestMethodsInvokedCheckStateProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class CheckboxTableViewerTest extends TableViewerTest {
    public static class CheckboxTableTestLabelProvider extends
            TestLabelProvider implements ITableLabelProvider {

        public boolean fExtended = false;

        @Override
		public String getText(Object element) {
            if (fExtended)
                return providedString((String) element);
            return element.toString();
        }

        @Override
		public String getColumnText(Object element, int index) {
            if (fExtended)
                return providedString((TestElement) element);
            return element.toString();
        }

        @Override
		public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    public static class DeprecatedConstructor extends CheckboxTableViewerTest {
		public DeprecatedConstructor(String name) {
			super(name);
		}
    	
	    @Override
		protected StructuredViewer createViewer(Composite parent) {
	    	TableViewer viewer = new CheckboxTableViewer(parent);
	    	
	        Table table = viewer.getTable();
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

	        viewer.setContentProvider(new TestModelContentProvider());
	        viewer.setLabelProvider(new TableTestLabelProvider());
	        return viewer;
	    }
	    
		@Override
		public void testViewerColumn() {
	    	assertNull(getViewerColumn((TableViewer) fViewer, -1));
			assertNotNull(getViewerColumn((TableViewer) fViewer, 0));
			assertNotNull(getViewerColumn((TableViewer) fViewer, 1));
				//due to CheckboxTableViewer.createTable, there is an
				//extra column, so the next test looks for column 3
				//instead of 2 -- a result of using deprecated code
			assertNull(getViewerColumn((TableViewer) fViewer, 3));
	    }
    }
    
    public static class FactoryMethod extends CheckboxTableViewerTest {
		public FactoryMethod(String name) {
			super(name);
		}
    	
	    @Override
		protected StructuredViewer createViewer(Composite parent) {
	    	TableViewer viewer = CheckboxTableViewer.newCheckList(parent, SWT.NONE);
	    	
	        Table table = viewer.getTable();
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

	        viewer.setContentProvider(new TestModelContentProvider());
	        viewer.setLabelProvider(new TableTestLabelProvider());
	        return viewer;
	    }
    }


    public CheckboxTableViewerTest(String name) {
        super(name);
    }

    @Override
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
    
    public void testWithoutCheckProvider() {
    	//Check that without a provider, no exceptions are thrown
    	CheckboxTableViewer ctv = (CheckboxTableViewer)fViewer;
    	ctv.refresh();
    }
    
    public void testCheckProviderInvoked() {
    	//Check that a refresh successfully causes the provider's
    	//setChecked and setGrayed methods to be invoked.
    	CheckboxTableViewer ctv = (CheckboxTableViewer)fViewer;

    	TestMethodsInvokedCheckStateProvider provider = new TestMethodsInvokedCheckStateProvider();
    	
    	ctv.setCheckStateProvider(provider);
    	assertTrue("isChecked should be invoked on a refresh", (!provider.isCheckedInvokedOn.isEmpty()));
    	assertTrue("isGrayed should be invoked on a refresh", (!provider.isGrayedInvokedOn.isEmpty()));

    	provider.reset();
    	ctv.refresh();
    	assertTrue("isChecked should be invoked on a refresh", (!provider.isCheckedInvokedOn.isEmpty()));
    	assertTrue("isGrayed should be invoked on a refresh", (!provider.isGrayedInvokedOn.isEmpty()));
    }
    
    public void testCheckedFalseGrayedFalse() {
    	testSpecificState(false, false);
    }
    
    public void testCheckedFalseGrayedTrue() {
    	testSpecificState(false, true);
    }
    
    public void testCheckedTrueGrayedFalse() {
    	testSpecificState(true, false);
    }
    
    public void testCheckedTrueGrayedTrue() {
    	testSpecificState(true, true);
    }
    
    private void testSpecificState(final boolean isChecked, final boolean isGrayed) {
    	CheckboxTableViewer ctv = (CheckboxTableViewer)fViewer;
    	
    	ctv.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isChecked(Object element)	{	return isChecked;	}
			@Override
			public boolean isGrayed(Object element) 	{	return isGrayed;	}
    	});
    	
    	TableItem item = ctv.getTable().getItem(0);
    	
    	assertEquals(item.getChecked(), isChecked);
    	assertEquals(item.getGrayed(), isGrayed);
    }
    
    public void testSetCheckProviderRefreshesItems() {
    	CheckboxTableViewer ctv = (CheckboxTableViewer)fViewer;
    	
    	//First provider
    	//Should cause visible items' check state to adhere to provider
    	ctv.setCheckStateProvider(new TestCheckStateProvider(0));
    	
    	//Check that all states are properly set
    	checkAllStates("Testing checkbox state after refresh", ctv, 0);
    	
    	//Remove the check state provider
    	ctv.setCheckStateProvider(null);
    	
    	//Test that an update doesn't fail
    	TestElement update = fRootElement.getChildAt(5);
    	ctv.update(update, null);
    	
    	//Test that a refresh doesn't fail
    	ctv.refresh();
    }
    
    public void testCheckProviderWithSorter() {
    	CheckboxTableViewer ctv = (CheckboxTableViewer) fViewer;
    	
    	ctv.setSorter(new CheckStateProviderTestsUtil.Sorter());
    	
    	//First provider
    	//Should cause visible items' check state adhere to provider
    	ctv.setCheckStateProvider(new TestCheckStateProvider(0));
    	
    	//Check that all states are properly set
    	checkAllStates("Testing checkbox state with a sorter", ctv, 0);
    }
    
    public void testCheckProviderWithFilter() {
    	CheckboxTableViewer ctv = (CheckboxTableViewer) fViewer;
    	
    	final CheckStateProviderTestsUtil.Filter filter = new CheckStateProviderTestsUtil.Filter();
		ctv.addFilter(filter);
    	
    	//First provider
		//Should cause visible items' check state adhere to provider
    	final TestCheckStateProvider checkStateProvider = new TestCheckStateProvider(0);
		ctv.setCheckStateProvider(checkStateProvider);
    	
    	//Check that all states are properly set
    	checkAllStates("Testing checkbox state with a sorter", ctv, 0);
    	
    	//Check that the provider is only invoked on elements which pass the filter
    	for (Iterator i = checkStateProvider.isCheckedInvokedOn.iterator(); i.hasNext();) {
			TestElement element = (TestElement) i.next();
			assertTrue("The check provider should not be invoked on elements which did not get through the filter", filter.select(ctv, null, element));
		}
    	
    	for (Iterator i = checkStateProvider.isGrayedInvokedOn.iterator(); i.hasNext();) {
			TestElement element = (TestElement) i.next();
			assertTrue("The check provider should not be invoked on elements which did not get through the filter", filter.select(ctv, null, element));
		}
    }
    
    public void testCheckProviderUpdate() {
    	CheckboxTableViewer ctv = (CheckboxTableViewer)fViewer;
    	
    	//First provider
    	//Should cause visible items' check state to adhere to provider
    	ctv.setCheckStateProvider(new TestCheckStateProvider(0));
    	
    	checkAllStates("Testing checkbox state after refresh", ctv, 0);
    	
    	//Put in a new check state provider
    	ctv.setCheckStateProvider(new TestCheckStateProvider(1));
    	
    	//Check that setting a new check provider caused a refresh,
    	//and thus all the items have their new appropriate check
    	//states.
    	checkAllStates("Testing checkbox state after refresh", ctv, 1);
    }

	private void checkAllStates(String comment, CheckboxTableViewer ctv, int shift) {
		TableItem[] items = ctv.getTable().getItems();
    	
    	//Check that actual states were set properly
    	for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
    		TestElement element = (TestElement)items[i].getData();
    		
    		checkState(comment, element, item, shift);	//check in Table
    		checkState(comment, element, ctv, shift);	//check in Viewer
		}
	}
    
	/**
	 * Invokes the appropriate asserts to verify the state
	 * of a TestElement.
	 * @param te
	 * @param viewer	the viewer <code>te</code> is in.
	 * @param shift 	the shift parameter being used
	 */
	private void checkState(String comment, TestElement te, CheckboxTableViewer viewer, int shift) {
		assertEquals(comment, CheckStateProviderTestsUtil.shouldBeChecked(te, shift), viewer.getChecked(te));
		assertEquals(comment, CheckStateProviderTestsUtil.shouldBeGrayed(te, shift), viewer.getGrayed(te));
	}
	
	/**
	 * Invokes the appropriate asserts to verify the state
	 * of a TestElement's associated TableItem
	 * @param te
	 * @param item	the item representing <code>te</code>
	 * @param shift	the shift parameter being used
	 */
	private void checkState(String comment, TestElement te, TableItem item, int shift) {
		assertEquals("Wrong checkstate: " + comment, CheckStateProviderTestsUtil.shouldBeChecked(te, shift), item.getChecked());
		assertEquals("Wrong checkstate: " + comment, CheckStateProviderTestsUtil.shouldBeGrayed(te, shift), item.getGrayed());
	}
	
	public void testGetCheckedElements() {
		CheckboxTableViewer ctv = (CheckboxTableViewer) fViewer;
		
		TestElement[] children = fRootElement.getChildren();
		
		List checked = new ArrayList((children.length + 1) / 2);
		
		for (int i = 0; i < children.length; i+=2) {
			ctv.setChecked(children[i], true);
			checked.add(children[i]);
		}
		
		Object[] actuallyChecked = ctv.getCheckedElements();
		
		for (int i = 0; i < actuallyChecked.length; i++) {
			assertTrue("getCheckedElements should include all checked elements", checked.remove(actuallyChecked[i]));
		}
		
		assertTrue("getCheckedElements should not include any unchecked elements", checked.isEmpty());
	}
	
	public void testSetCheckedElements() {
		CheckboxTableViewer ctv = (CheckboxTableViewer) fViewer;
		
		TestElement[] children = fRootElement.getChildren();
		
		List toCheck = new ArrayList((children.length + 1) / 2);
		
		for (int i = 0; i < children.length; i+=2) {
			toCheck.add(children[i]);
		}
		
		ctv.setCheckedElements(toCheck.toArray());
		
		for (int i = 0; i < children.length; i++) {
			if(i % 2 == 0) {
				assertTrue("an element passed through setCheckedElements should be checked", ctv.getChecked(children[i]));
			} else {
				assertFalse("an element not passed through setCheckedElements should be unchecked", ctv.getChecked(children[i]));
			}
		}
	}
	
	public void testSetGrayedElements() {
		CheckboxTableViewer ctv = (CheckboxTableViewer) fViewer;
		
		TestElement[] children = fRootElement.getChildren();
		
		List toGray = new ArrayList((children.length + 1) / 2);
		
		for (int i = 0; i < children.length; i+=2) {
			toGray.add(children[i]);
		}
		
		ctv.setGrayedElements(toGray.toArray());
		
		for (int i = 0; i < children.length; i++) {
			if(i % 2 == 0) {
				assertTrue("an element passed through setGrayedElements should be grayed", ctv.getGrayed(children[i]));
			} else {
				assertFalse("an element not passed through setGrayedElements should not be grayed", ctv.getGrayed(children[i]));
			}
		}
	}
}
