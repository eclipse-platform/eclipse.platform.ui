package org.eclipse.jface.tests.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.viewers.AbstractTreeViewer;

public abstract class AbstractTreeViewerTest extends StructuredItemViewerTest {
	
	AbstractTreeViewer fTreeViewer;
	
	public AbstractTreeViewerTest(String name) {
		super(name);
	}
	protected void assertEqualsArray(String s, Object[] a1, Object[] a2) {
		int s1= a1.length;
		int s2= a2.length;
		assertTrue(s, s1 == s2);
		for (int i= 0; i < s1; i++) {
			assertEquals(s, a1[i], a2[i]);
		}
	}
	protected abstract int getItemCount(TestElement element);   //was IElement
public void testBulkExpand() {
	// navigate
	TestElement first = fRootElement.getFirstChild();
	TestElement first2 = first.getFirstChild();
	TestElement last = fRootElement.getLastChild();

	// expand a few nodes
	fTreeViewer.expandToLevel(first, 2);
	fTreeViewer.expandToLevel(first2, 2);
	fTreeViewer.expandToLevel(last, 2);

	// get expand state
	Object[] list1 = fTreeViewer.getExpandedElements();

	// flush viewer
	fViewer.setInput(fRootElement);

	// restore old expand state
	fTreeViewer.collapseAll();
	fTreeViewer.expandToLevel(first, 2);
	fTreeViewer.expandToLevel(first2, 2);
	fTreeViewer.expandToLevel(last, 2);
	
	Object[] list2 = fTreeViewer.getExpandedElements();

	assertEqualsArray("old and new expand state are the same", list1, list2);
}
	public void testDeleteChildExpanded() {
		TestElement first= fRootElement.getFirstChild();
		TestElement first2= first.getFirstChild();
		fTreeViewer.expandToLevel(first2,0);
		
		assertNotNull("first child is visible", fViewer.testFindItem(first2));		
		first.deleteChild(first2);
		assertNull("first child is not visible", fViewer.testFindItem(first2));
	}
	public void testDeleteChildren() {
		TestElement first= fRootElement.getFirstChild();		
		first.deleteChildren();
		assertTrue("no children", getItemCount(first) == 0);
	}
	public void testDeleteChildrenExpanded() {
		TestElement first= fRootElement.getFirstChild();
		TestElement first2= first.getFirstChild();
		fTreeViewer.expandToLevel(first2,0);
		assertNotNull("first child is visible", fViewer.testFindItem(first2));
		
		first.deleteChildren();
		assertTrue("no children", getItemCount(first) == 0);
	}
	public void testExpand() {
		TestElement first= fRootElement.getFirstChild();
		TestElement first2= first.getFirstChild();
		assertNull("first child is not visible", fViewer.testFindItem(first2));		
		fTreeViewer.expandToLevel(first2,0);	
		assertNotNull("first child is visible", fViewer.testFindItem(first2));		
	}
public void testExpandElement() {
	TestElement first = fRootElement.getFirstChild();
	TestElement first2 = first.getFirstChild();
	TestElement first3 = first2.getFirstChild();
	fTreeViewer.expandToLevel(first3, 0);
	assertNotNull("first3 is visible", fViewer.testFindItem(first3));
	assertNotNull("first2 is visible", fViewer.testFindItem(first2));
}
	public void testExpandToLevel() {
		TestElement first= fRootElement.getFirstChild();
		TestElement first2= first.getFirstChild();
		fTreeViewer.expandToLevel(2);	
		assertNotNull("first2 is visible", fViewer.testFindItem(first2));		
	}
	public void testFilterExpanded() {
		TestElement first= fRootElement.getFirstChild();
		TestElement first2= first.getFirstChild();
		fTreeViewer.expandToLevel(first2,0);  

		fTreeViewer.addFilter(new TestLabelFilter());
		assertTrue("filtered count", getItemCount() == 5);
	}
	public void testInsertChildReveal() {
		TestElement first= fRootElement.getFirstChild();		
		TestElement newElement= first.addChild(TestModelChange.INSERT | TestModelChange.REVEAL);
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
	}
	public void testInsertChildRevealSelect() {
		TestElement last= fRootElement.getLastChild();
		TestElement newElement= last.addChild(TestModelChange.INSERT | TestModelChange.REVEAL | TestModelChange.SELECT);
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
		assertSelectionEquals("new element is selected", newElement);
	}
	public void testInsertChildRevealSelectExpanded() {
		TestElement first= fRootElement.getFirstChild();
		TestElement newElement= first.addChild(TestModelChange.INSERT | TestModelChange.REVEAL | TestModelChange.SELECT);
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
		assertSelectionEquals("new element is selected", newElement);
	}
	/**
	 * Regression test for 1GDN0PX: ITPUI:WIN2000 - SEVERE  - AssertionFailure when expanding Navigator
	 * Problem was:
	 *   - before addition, parent item had no children, and was expanded
	 *   - after addition, during refresh(), updatePlus() added dummy node even though parent item was expanded
	 *   - in updateChildren, it wasn't handling a dummy node
	 */
	public void testRefreshWithAddedChildren() {
		TestElement parent = fRootElement.addChild(TestModelChange.INSERT);
		TestElement child = parent.addChild(TestModelChange.INSERT);
		((AbstractTreeViewer) fViewer).setExpandedState(parent, true);
		parent.deleteChild(child);
		child = parent.addChild(TestModelChange.STRUCTURE_CHANGE);
		assertNotNull("new child is visible", fViewer.testFindItem(child));
	}
	/**
	 * Regression test for 1GBDB5A: ITPUI:WINNT - Exception in AbstractTreeViewer update.
	 * Problem was:
	 *   node has child A
	 *   node gets duplicate child A
	 *   viewer is refreshed rather than using add for new A
	 *   AbstractTreeViewer.updateChildren(...) was not properly handling it
	 */
	public void testRefreshWithDuplicateChild() {
		TestElement first= fRootElement.getFirstChild();
		TestElement newElement= (TestElement) first.clone();
		fRootElement.addChild(newElement, new TestModelChange(TestModelChange.STRUCTURE_CHANGE, fRootElement));
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
	}
	public void testRenameChildElement() {
		TestElement first= fRootElement.getFirstChild();
		TestElement first2= first.getFirstChild();
		fTreeViewer.expandToLevel(first2,0);
		assertNotNull("first child is visible", fViewer.testFindItem(first2));
				
		String newLabel= first2.getLabel()+" changed";
		first2.setLabel(newLabel);
		Widget widget= fViewer.testFindItem(first2);
		assertTrue(widget instanceof Item);
		assertEquals("changed label", first2.getID()+" "+newLabel, ((Item)widget).getText());
	}
}
