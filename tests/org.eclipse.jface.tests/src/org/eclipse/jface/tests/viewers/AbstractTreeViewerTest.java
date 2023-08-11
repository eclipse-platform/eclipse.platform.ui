/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

public abstract class AbstractTreeViewerTest extends StructuredItemViewerTest {

	AbstractTreeViewer fTreeViewer;

	public AbstractTreeViewerTest(String name) {
		super(name);
	}

	protected void assertEqualsArray(String s, Object[] a1, Object[] a2) {
		int s1 = a1.length;
		int s2 = a2.length;
		assertEquals(s, s1, s2);
		for (int i = 0; i < s1; i++) {
			assertEquals(s, a1[i], a2[i]);
		}
	}

	@Override
	protected void assertSelectionEquals(String message, TestElement expected) {
		IStructuredSelection selection = fViewer.getStructuredSelection();
		List<TestElement> expectedList = new ArrayList<>();
		expectedList.add(expected);
		assertEquals("selectionEquals - " + message, expectedList, selection.toList());
	}

	protected abstract int getItemCount(TestElement element); // was IElement

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
		setInput();
		processEvents();

		// restore old expand state
		fTreeViewer.collapseAll();
		fTreeViewer.expandToLevel(first, 2);
		fTreeViewer.expandToLevel(first2, 2);
		fTreeViewer.expandToLevel(last, 2);

		Object[] list2 = fTreeViewer.getExpandedElements();

		assertEqualsArray("old and new expand state are the same", list1, list2);
	}

	public void testDeleteChildExpanded() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		fTreeViewer.expandToLevel(first2, 0);

		assertNotNull("first child is visible", fViewer.testFindItem(first2));
		first.deleteChild(first2);
		assertNull("first child is not visible", fViewer.testFindItem(first2));
	}

	public void testDeleteChildren() {
		TestElement first = fRootElement.getFirstChild();
		first.deleteChildren();
		assertEquals("no children", 0, getItemCount(first));
	}

	public void testDeleteChildrenExpanded() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		fTreeViewer.expandToLevel(first2, 0);
		assertNotNull("first child is visible", fViewer.testFindItem(first2));

		first.deleteChildren();
		assertEquals("no children", 0, getItemCount(first));
	}

	public void testExpand() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		assertNull("first child is not visible", fViewer.testFindItem(first2));
		fTreeViewer.expandToLevel(first2, 0);
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

	public void testExpandElementAgain() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		TestElement first3 = first2.getFirstChild();
		fTreeViewer.expandToLevel(first3, 0);
		assertTrue("first is expanded", fTreeViewer.getExpandedState(first));
		assertTrue("first2 is expanded", fTreeViewer.getExpandedState(first2));
		assertNotNull("first3 is visible", fViewer.testFindItem(first3));

		fTreeViewer.setExpandedState(first, false);
		fTreeViewer.expandToLevel(first3, 0);
		assertTrue("first is expanded", fTreeViewer.getExpandedState(first)); // bug 54116
		assertTrue("first2 is expanded", fTreeViewer.getExpandedState(first2));
		assertNotNull("first3 is visible", fViewer.testFindItem(first3));
	}

	public void testExpandToLevel() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		TestElement first3 = first2.getFirstChild();
		fTreeViewer.expandToLevel(3);

		DisplayHelper.waitAndAssertCondition(fShell.getDisplay(), () -> {
			assertNotNull(fViewer.testFindItem(first2));
			assertNotNull(fViewer.testFindItem(first3));
		});

		assertNotNull("first2 is visible", fViewer.testFindItem(first2));
		assertNotNull("first3 is visible", fViewer.testFindItem(first3));
	}

	public void testFilterExpanded() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		fTreeViewer.expandToLevel(first2, 0);

		fTreeViewer.addFilter(new TestLabelFilter());
		assertEquals("filtered count", 5, getItemCount());
	}

	public void testInsertChildReveal() {
		TestElement first = fRootElement.getFirstChild();
		TestElement newElement = first.addChild(TestModelChange.INSERT | TestModelChange.REVEAL);
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
	}

	public void testInsertChildRevealSelect() {
		TestElement last = fRootElement.getLastChild();
		TestElement newElement = last
				.addChild(TestModelChange.INSERT | TestModelChange.REVEAL | TestModelChange.SELECT);
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
		assertSelectionEquals("new element is selected", newElement);
	}

	public void testInsertChildRevealSelectExpanded() {
		TestElement first = fRootElement.getFirstChild();
		TestElement newElement = first
				.addChild(TestModelChange.INSERT | TestModelChange.REVEAL | TestModelChange.SELECT);
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
		assertSelectionEquals("new element is selected", newElement);
	}

	/**
	 * Regression test for 1GDN0PX: ITPUI:WIN2000 - SEVERE - AssertionFailure when
	 * expanding Navigator Problem was: - before addition, parent item had no
	 * children, and was expanded - after addition, during refresh(), updatePlus()
	 * added dummy node even though parent item was expanded - in updateChildren, it
	 * wasn't handling a dummy node
	 */
	public void testRefreshWithAddedChildren() {
		TestElement parent = fRootElement.addChild(TestModelChange.INSERT);
		TestElement child = parent.addChild(TestModelChange.INSERT);
		((AbstractTreeViewer) fViewer).setExpandedState(parent, true);
		parent.deleteChild(child);
		child = parent.addChild(TestModelChange.STRUCTURE_CHANGE);
		// On some platforms (namely GTK), removing all children causes the
		// parent to collapse (actually it's worse than that: GTK doesn't
		// allow there to be an empty expanded tree item, even if you do a
		// setExpanded(true)).
		// This behaviour makes it impossible to do this regression test.
		// See bug 40797 for more details. Because GTK 3 takes longer to
		// process, a wait statement is needed so that the assert will be done
		// correctly without failing.
		waitForJobs(300, 1000);
		processEvents();
		if (((AbstractTreeViewer) fViewer).getExpandedState(parent)) {
			assertNotNull("new child is visible", fViewer.testFindItem(child));
		}
	}

	/**
	 * Regression test for 1GBDB5A: ITPUI:WINNT - Exception in AbstractTreeViewer
	 * update. Problem was: node has child A node gets duplicate child A viewer is
	 * refreshed rather than using add for new A
	 * AbstractTreeViewer.updateChildren(...) was not properly handling it
	 */
	public void testRefreshWithDuplicateChild() {
		TestElement first = fRootElement.getFirstChild();
		TestElement newElement = (TestElement) first.clone();
		fRootElement.addChild(newElement, new TestModelChange(TestModelChange.STRUCTURE_CHANGE, fRootElement));
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
	}

	/**
	 * Regression test for Bug 3840 [Viewers] free expansion of jar happening when
	 * deleting projects (1GEV2FL) Problem was: - node has children A and B - A is
	 * expanded, B is not - A gets deleted - B gets expanded because it reused A's
	 * item
	 */
	public void testRefreshWithReusedItems() {
		// TestElement a= fRootElement.getFirstChild();
		// TestElement aa= a.getChildAt(0);
		// TestElement ab= a.getChildAt(1);
		// fTreeViewer.expandToLevel(aa, 1);
		// List expandedBefore = Arrays.asList(fTreeViewer.getExpandedElements());
		// assertTrue(expandedBefore.contains(a));
		// assertTrue(expandedBefore.contains(aa));
		// assertFalse(expandedBefore.contains(ab));
		// a.deleteChild(aa, new TestModelChange(TestModelChange.STRUCTURE_CHANGE, a));
		// List expandedAfter = Arrays.asList(fTreeViewer.getExpandedElements());
		// assertFalse(expandedAfter.contains(ab));
	}

	public void testRenameChildElement() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		fTreeViewer.expandToLevel(first2, 0);
		assertNotNull("first child is visible", fViewer.testFindItem(first2));

		String newLabel = first2.getLabel() + " changed";
		first2.setLabel(newLabel);
		Widget widget = fViewer.testFindItem(first2);
		assertTrue(widget instanceof Item);
		assertEquals("changed label", first2.getID() + " " + newLabel, ((Item) widget).getText());
	}

	/**
	 * Regression test for Bug 26698 [Viewers] stack overflow during debug session,
	 * causing IDE to crash Problem was: - node A has child A - setExpanded with A
	 * in the list caused an infinite recursion
	 */
	public void testSetExpandedWithCycle() {
		TestElement first = fRootElement.getFirstChild();
		first.addChild(first, new TestModelChange(TestModelChange.INSERT, first, first));
		fTreeViewer.setExpandedElements(new Object[] { first });

	}

	/**
	 * Test for Bug 41710 - assertion that an object may not be added to a given
	 * TreeItem more than once.
	 */
	public void testSetDuplicateChild() {
		// Widget root = fViewer.testFindItem(fRootElement);
		// assertNotNull(root);
		TestElement parent = fRootElement.addChild(TestModelChange.INSERT);
		TestElement child = parent.addChild(TestModelChange.INSERT);
		int initialCount = getItemCount(parent);
		fRootElement.addChild(child, new TestModelChange(TestModelChange.INSERT, fRootElement, child));
		int postCount = getItemCount(parent);
		assertEquals("Same element added to a parent twice.", initialCount, postCount);
	}

	/**
	 * Test for Bug 571844 - assert that an item is not added twice if the
	 * comparator returns 0 = equal for more than one tree item. Problem was that
	 * the method AbstractTreeViewer#createAddedElements only searched forward but
	 * not backwards for an equal element, if the comparator returned 0. The example
	 * below is a case where the previous implementation would fail.
	 */
	public void testChildIsNotDuplicatedWhenCompareEquals() {
		fTreeViewer.setComparator(new TestLabelComparator());
		fRootElement.deleteChildren();

		TestElement child1 = fRootElement.addChild(TestModelChange.INSERT);
		child1.setLabel("1");
		TestElement child2 = fRootElement.addChild(TestModelChange.INSERT);
		child2.setLabel("1");
		TestElement child3 = fRootElement.addChild(TestModelChange.INSERT);
		child3.setLabel("0");

		// Every duplicated element must not be added as TreeItem.
		fRootElement.addChild(child1, new TestModelChange(TestModelChange.INSERT, fRootElement, child1));
		fRootElement.addChild(child2, new TestModelChange(TestModelChange.INSERT, fRootElement, child2));
		fRootElement.addChild(child3, new TestModelChange(TestModelChange.INSERT, fRootElement, child3));

		Tree tree = (Tree) fTreeViewer.getControl();
		assertEquals("Same element added to parent twice.", 3, tree.getItems().length);
	}

	public void testContains() {
		// some random element.
		assertFalse("element must not be available on the viewer", fTreeViewer.contains(fRootElement, ""));

		// first child of root.
		assertTrue("element must be available on the viewer",
				fTreeViewer.contains(fRootElement, fRootElement.getFirstChild()));

		// last child of the root
		assertTrue("element must be available on the viewer",
				fTreeViewer.contains(fRootElement, fRootElement.getLastChild()));
		// child of first element is not expanded
		assertFalse("element must not be available on the viewer",
				fTreeViewer.contains(fRootElement.getFirstChild(), fRootElement.getFirstChild().getFirstChild()));
		fTreeViewer.expandAll();
		// child of first element when expanded.
		assertTrue("element must be available on the viewer",
				fTreeViewer.contains(fRootElement.getFirstChild(), fRootElement.getFirstChild().getFirstChild()));
	}

	@Override
	public void tearDown() {
		super.tearDown();
		fTreeViewer = null;
	}
}
