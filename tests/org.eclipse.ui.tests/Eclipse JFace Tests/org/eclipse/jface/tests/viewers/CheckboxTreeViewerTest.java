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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.tests.viewers.CheckStateProviderTestsUtil.TestCheckStateProvider;
import org.eclipse.jface.tests.viewers.CheckStateProviderTestsUtil.TestMethodsInvokedCheckStateProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

public class CheckboxTreeViewerTest extends TreeViewerTest {
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

    public CheckboxTreeViewerTest(String name) {
        super(name);
    }

    protected StructuredViewer createViewer(Composite parent) {
        fTreeViewer = new CheckboxTreeViewer(parent);
        fTreeViewer.setContentProvider(new TestModelContentProvider());
        return fTreeViewer;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(CheckboxTreeViewerTest.class);
    }

    public void testCheckSubtree() {
        CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
        TestElement first = fRootElement.getFirstChild();
        TestElement firstfirst = first.getFirstChild();
        TestElement firstfirstfirst = firstfirst.getFirstChild();
        fTreeViewer.expandToLevel(firstfirst, 0);

        ctv.setSubtreeChecked(first, true);
        assertTrue(ctv.getChecked(firstfirst));
        ctv.setSubtreeChecked(first, false);
        assertTrue(!ctv.getChecked(firstfirst));

        // uncheck invisible subtree
        assertTrue(ctv.setSubtreeChecked(firstfirstfirst, false));
        assertTrue(!ctv.getChecked(firstfirstfirst));
    }

    public void testGrayed() {
        CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
        TestElement element = fRootElement.getFirstChild();

        assertTrue(ctv.getGrayedElements().length == 0);
        assertTrue(!ctv.getGrayed(element));

        ctv.setGrayed(element, true);
        assertTrue(ctv.getGrayedElements().length == 1);
        assertTrue(ctv.getGrayed(element));

        ctv.setGrayed(element, false);
        assertTrue(ctv.getGrayedElements().length == 0);
        assertTrue(!ctv.getGrayed(element));
    }

    public void testParentGrayed() {
        CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
        TestElement first = fRootElement.getFirstChild();
        TestElement firstfirst = first.getFirstChild();
        TestElement firstfirstfirst = firstfirst.getFirstChild();
        ctv.expandToLevel(firstfirstfirst, 0);

        ctv.setParentsGrayed(firstfirstfirst, true);
        Object[] elements = ctv.getGrayedElements();
        assertTrue(elements.length == 3);
        for (int i = 0; i < elements.length; ++i) {
            assertTrue(ctv.getGrayed(elements[i]));
        }

        assertTrue(elements[0] == first);
        assertTrue(elements[1] == firstfirst);
        assertTrue(elements[2] == firstfirstfirst);
        ctv.setParentsGrayed(firstfirstfirst, false);
    }
    
    public void testWithoutCheckProvider() {
    	//Check that without a provider, no exceptions are thrown
    	CheckboxTreeViewer ctv = (CheckboxTreeViewer)fViewer;
    	ctv.expandAll();
    	ctv.refresh();
    }
    
    public void testCheckProviderInvoked() {
    	//Check that a refresh successfully causes the provider's
    	//setChecked and setGrayed methods to be invoked.
    	CheckboxTreeViewer ctv = (CheckboxTreeViewer)fViewer;
    	
    	TestMethodsInvokedCheckStateProvider provider = new TestMethodsInvokedCheckStateProvider();
    	
    	ctv.setCheckStateProvider(provider);
    	assertTrue("isChecked should be invoked on a refresh", (!provider.isCheckedInvokedOn.isEmpty()));
    	assertTrue("isGrayed should be invoked on a refresh", (!provider.isGrayedInvokedOn.isEmpty()));
    	
    	provider.reset();
    	ctv.refresh();
    	assertTrue("isChecked should be invoked on a refresh", (!provider.isCheckedInvokedOn.isEmpty()));
    	assertTrue("isGrayed should be invoked on a refresh", (!provider.isGrayedInvokedOn.isEmpty()));

    }
    
    public void testCheckProviderLazilyInvoked() {
    	//Check that a refresh successfully causes the provider's
    	//setChecked and setGrayed methods to be invoked.
    	CheckboxTreeViewer ctv = (CheckboxTreeViewer)fViewer;
    	
    	TestMethodsInvokedCheckStateProvider provider = new TestMethodsInvokedCheckStateProvider();
    	
    	ctv.setCheckStateProvider(provider);
    	ctv.refresh();
    	
    	TestElement[] expected = fRootElement.getChildren();
    	
    	for (Iterator i = provider.isCheckedInvokedOn.iterator(); i.hasNext();) {
			TestElement element = (TestElement) i.next();
			boolean firstLevelElement = false;
			for (int j = 0; j < expected.length && !firstLevelElement; j++) {
				firstLevelElement = element.equals(expected[j]);
			}
			assertTrue("The check provider should only be invoked with visible elements", firstLevelElement);
		}
    	
    	for (Iterator i = provider.isGrayedInvokedOn.iterator(); i.hasNext();) {
			TestElement element = (TestElement) i.next();
			boolean firstLevelElement = false;
			for (int j = 0; j < expected.length && !firstLevelElement; j++) {
				firstLevelElement = element.equals(expected[j]);
			}
			assertTrue("The check provider should only be invoked with visible elements", firstLevelElement);
		}
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
    	CheckboxTreeViewer ctv = (CheckboxTreeViewer)fViewer;
    	
    	ctv.setCheckStateProvider(new ICheckStateProvider() {
			public boolean isChecked(Object element)	{	return isChecked;	}
			public boolean isGrayed(Object element) 	{	return isGrayed;	}
    	});
    	
    	TreeItem item = ctv.getTree().getItem(0);
    	
    	assertEquals(item.getChecked(), isChecked);
    	assertEquals(item.getGrayed(), isGrayed);
    }
    
    public void testSetCheckProviderRefreshesItems() {
    	CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
    	
    	//First provider
    	//Should cause visible items' check state adhere to provider
    	ctv.setCheckStateProvider(new TestCheckStateProvider(0));
    	
    	ctv.expandAll();
    	
    	//Check that all states are properly set
    	checkAllStates("Testing checkbox state after refresh", ctv, 0);
    	
    	//Remove the check state provider
    	ctv.setCheckStateProvider(null);
    	
    	//Test that an update doesn't fail
    	TestElement update = fRootElement.getFirstChild().getChildAt(5);
    	ctv.update(update, null);
    	
    	//Test that a refresh doesn't fail
    	ctv.refresh();
    }
    
    public void testCheckProviderWithSorter() {
    	CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
    	
    	ctv.setSorter(new CheckStateProviderTestsUtil.Sorter());
    	
    	//First provider
    	//Should cause visible items' check state adhere to provider
    	ctv.setCheckStateProvider(new TestCheckStateProvider(0));
    	ctv.expandAll();
    	
    	//Check that all states are properly set
    	checkAllStates("Testing checkbox state with a sorter", ctv, 0);
    }
    
    public void testCheckProviderWithFilter() {
    	CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
    	
    	final CheckStateProviderTestsUtil.Filter filter = new CheckStateProviderTestsUtil.Filter();
		ctv.addFilter(filter);
    	
    	//First provider
		//Should cause visible items' check state adhere to provider
    	final TestCheckStateProvider checkStateProvider = new TestCheckStateProvider(0);
		ctv.setCheckStateProvider(checkStateProvider);
    	ctv.expandAll();
    	
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

    public void testSetNewCheckProvider() {
    	CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
    	
    	//First provider
    	//Should cause visible items' check state to adhere to provider
    	ctv.setCheckStateProvider(new TestCheckStateProvider(0));
    	ctv.expandAll();
    	
    	checkAllStates("Testing checkbox state after first refresh", ctv, 0);
    	
    	//Put in a new check state provider
    	ctv.setCheckStateProvider(new TestCheckStateProvider(1));
    	
    	//Check that setting a new check provider caused a refresh,
    	//and thus all the items have their new appropriate check
    	//states.
    	checkAllStates("Testing checkbox state after setting new check provider", ctv, 1);
    }
    
    private void collectElementsInBranch(TreeItem item, Collection treeItems, Collection testElements) {
    	treeItems.add(item);
    	testElements.add(item.getData());
    	TreeItem[] children = item.getItems();
    	for (int i = 0; i < children.length; i++) {
    		collectElementsInBranch(children[i], treeItems, testElements);
    	}
    }
    
    private void checkAllStates(String comment, CheckboxTreeViewer ctv, int shift) {
    	List items = new ArrayList();
    	List elements = new ArrayList();
    	collectElementsInBranch(ctv.getTree().getItem(0), items, elements);
    	
    	//Check that actual states were set properly
    	for (Iterator i = items.iterator(), j = elements.iterator(); i.hasNext();) {
    		TreeItem item = (TreeItem)i.next();
    		TestElement element = (TestElement)j.next();
    		
    		checkState(comment, element, item, shift);	//check in Tree
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
	private void checkState(String comment, TestElement te, CheckboxTreeViewer viewer, int shift) {
		assertEquals(comment, CheckStateProviderTestsUtil.shouldBeChecked(te, shift), viewer.getChecked(te));
		assertEquals(comment, CheckStateProviderTestsUtil.shouldBeGrayed(te, shift), viewer.getGrayed(te));
	}
	
	/**
	 * Invokes the appropriate asserts to verify the state
	 * of a TestElement's associated TreeItem
	 * @param te
	 * @param item	the item representing <code>te</code>
	 * @param shift	the shift parameter being used
	 */
	private void checkState(String comment, TestElement te, TreeItem item, int shift) {
		assertEquals("Wrong checkstate: " + comment, CheckStateProviderTestsUtil.shouldBeChecked(te, shift), item.getChecked());
		assertEquals("Wrong checkstate: " + comment, CheckStateProviderTestsUtil.shouldBeGrayed(te, shift), item.getGrayed());
	}
	
	public void testGetCheckedElements() {
		CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
		
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
		CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
		
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
		CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
		
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
	
	public void testSetAllChecked() {
		CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
		
		ctv.expandToLevel(2);
		
		ctv.setAllChecked(true);
		Object[] expandedElements = ctv.getExpandedElements();
		
		for (int i = 0; i < expandedElements.length; i++) {
			assertTrue("all expanded items should be checked", ctv.getChecked(expandedElements[i]));
		}
		
		ctv.setAllChecked(false);
		
		for (int i = 0; i < expandedElements.length; i++) {
			assertFalse("all expanded items should be unchecked", ctv.getChecked(expandedElements[i]));
		}
	}
	
	public void testSetGrayChecked() {
		CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
		
		TestElement[] children = fRootElement.getChildren();
		
		ctv.setGrayChecked(children[0], true);
		ctv.setGrayChecked(children[1], false);
	
		assertTrue("an item invoked with setGrayChecked(true) should be checked", ctv.getChecked(children[0]));
		assertTrue("an item invoked with setGrayChecked(true) should be grayed", ctv.getGrayed(children[0]));
		
		assertFalse("an item invoked with setGrayChecked(false) should be unchecked", ctv.getChecked(children[1]));
		assertFalse("an item invoked with setGrayChecked(false) should not be grayed", ctv.getGrayed(children[1]));
	}
}
