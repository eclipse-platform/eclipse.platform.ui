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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractTreeViewerTest extends StructuredItemViewerTest {

	AbstractTreeViewer fTreeViewer;

	@Override
	protected void assertSelectionEquals(String message, TestElement expected) {
		IStructuredSelection selection = fViewer.getStructuredSelection();
		List<TestElement> expectedList = new ArrayList<>();
		expectedList.add(expected);
		assertEquals(expectedList, selection.toList(), "selectionEquals - " + message);
	}

	protected abstract int getItemCount(TestElement element); // was IElement

	@Test
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

		assertArrayEquals(list1, list2, "old and new expand state are the same");
	}

	@Test
	public void testDeleteChildExpanded() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		fTreeViewer.expandToLevel(first2, 0);

		assertNotNull(fViewer.testFindItem(first2), "first child is visible");
		first.deleteChild(first2);
		assertNull(fViewer.testFindItem(first2), "first child is not visible");
	}

	@Test
	public void testDeleteChildren() {
		TestElement first = fRootElement.getFirstChild();
		first.deleteChildren();
		assertEquals(0, getItemCount(first), "no children");
	}

	@Test
	public void testDeleteChildrenExpanded() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		fTreeViewer.expandToLevel(first2, 0);
		assertNotNull(fViewer.testFindItem(first2), "first child is visible");

		first.deleteChildren();
		assertEquals(0, getItemCount(first), "no children");
	}

	@Test
	public void testExpand() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		assertNull(fViewer.testFindItem(first2), "first child is not visible");
		fTreeViewer.expandToLevel(first2, 0);
		assertNotNull(fViewer.testFindItem(first2), "first child is visible");
	}

	@Test
	public void testExpandElement() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		TestElement first3 = first2.getFirstChild();
		fTreeViewer.expandToLevel(first3, 0);
		assertNotNull(fViewer.testFindItem(first3), "first3 is visible");
		assertNotNull(fViewer.testFindItem(first2), "first2 is visible");
	}

	@Test
	public void testExpandElementAgain() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		TestElement first3 = first2.getFirstChild();
		fTreeViewer.expandToLevel(first3, 0);
		assertTrue(fTreeViewer.getExpandedState(first), "first is expanded");
		assertTrue(fTreeViewer.getExpandedState(first2), "first2 is expanded");
		assertNotNull(fViewer.testFindItem(first3), "first3 is visible");

		fTreeViewer.setExpandedState(first, false);
		fTreeViewer.expandToLevel(first3, 0);
		assertTrue(fTreeViewer.getExpandedState(first), "first is expanded"); // bug 54116
		assertTrue(fTreeViewer.getExpandedState(first2), "first2 is expanded");
		assertNotNull(fViewer.testFindItem(first3), "first3 is visible");
	}

	@Test
	public void testExpandToLevel() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		TestElement first3 = first2.getFirstChild();
		fTreeViewer.expandToLevel(3);

		DisplayHelper.waitAndAssertCondition(fShell.getDisplay(), () -> {
			assertNotNull(fViewer.testFindItem(first2));
			assertNotNull(fViewer.testFindItem(first3));
		});

		assertNotNull(fViewer.testFindItem(first2), "first2 is visible");
		assertNotNull(fViewer.testFindItem(first3), "first3 is visible");
	}

	@Test
	public void testAutoExpandOnSingleChild() {
		TestElement modelRoot = TestElement.createModel(5, 1);
		TestElement trivialPathRoot = modelRoot.getFirstChild();
		fViewer.setInput(modelRoot);

		fTreeViewer.setAutoExpandOnSingleChildLevels(2);
		fTreeViewer.setExpandedStateWithAutoExpandOnSingleChild(trivialPathRoot, true);

		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot), "The expanded widget child is not expanded");
		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild()), "The first child of the trivial path was not auto-expanded");
		assertFalse(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild().getFirstChild()), "Trivial path is expanded further than specified depth ");
	}

	@Test
	public void testAutoExpandOnSingleChildDeeperDownPath() {
		TestElement modelRoot = TestElement.createModel(6, 1);
		TestElement trivialPathRoot = modelRoot.getFirstChild();
		fViewer.setInput(modelRoot);

		fTreeViewer.setExpandedState(modelRoot, true); // https://github.com/eclipse-platform/eclipse.platform.ui/pull/1072#discussion_r1431558570
		fTreeViewer.setExpandedState(trivialPathRoot, true); // https://github.com/eclipse-platform/eclipse.platform.ui/pull/1072#discussion_r1431558570
		fTreeViewer.setAutoExpandOnSingleChildLevels(2);
		fTreeViewer.setExpandedStateWithAutoExpandOnSingleChild(trivialPathRoot.getFirstChild(), true);

		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild()), "The first child of the trivial path was not auto-expanded");
		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild().getFirstChild()), "The second child of the trivial path was not auto-expanded");
		assertFalse(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild().getFirstChild().getFirstChild()), "Trivial path is expanded further than specified depth ");
	}

	@Test
	public void testAutoExpandOnSingleChildFromRoot() {
		TestElement modelRoot = TestElement.createModel(5, 5);
		TestElement trivialPathRoot = modelRoot;
		fViewer.setInput(modelRoot);

		fTreeViewer.setAutoExpandOnSingleChildLevels(2);
		fTreeViewer.setExpandedStateWithAutoExpandOnSingleChild(trivialPathRoot, true);

		assertFalse(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild()), "The first child of the trivial path was auto-expanded");
	}

	@Test
	public void testAutoExpandOnSingleChildFromRootWithSensibleTrivialPath() {
		TestElement modelRoot = TestElement.createModel(5, 1);
		TestElement trivialPathRoot = modelRoot;
		fViewer.setInput(modelRoot);

		fTreeViewer.setAutoExpandOnSingleChildLevels(2);
		fTreeViewer.setExpandedStateWithAutoExpandOnSingleChild(trivialPathRoot, true);

		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild()), "The first child of the trivial path was auto-expanded");
	}

	@Test
	public void testAutoExpandOnSingleChildSmallerThanAutoExpandDepth() {
		TestElement modelRoot = TestElement.createModel(2, 1);
		TestElement trivialPathRoot = modelRoot.getFirstChild();
		fViewer.setInput(modelRoot);

		fTreeViewer.setAutoExpandOnSingleChildLevels(10);
		fTreeViewer.setExpandedStateWithAutoExpandOnSingleChild(trivialPathRoot, true);

		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot), "The expanded widget child is not expanded");
		assertFalse(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild()), "The first child of the trivial path was auto-expanded although it contains zero children");
	}

	@Test
	public void testSetAutoExpandOnSingleChildLevels() {
		fTreeViewer.setAutoExpandOnSingleChildLevels(AbstractTreeViewer.NO_EXPAND);
		assertEquals(AbstractTreeViewer.NO_EXPAND,
				fTreeViewer.getAutoExpandOnSingleChildLevels(), "Setting an auto-expansion level of NO_EXPAND works");

		fTreeViewer.setAutoExpandOnSingleChildLevels(4);
		assertEquals(4, fTreeViewer.getAutoExpandOnSingleChildLevels(), "Setting a non-trivial auto-expansion level works");

		fTreeViewer.setAutoExpandOnSingleChildLevels(AbstractTreeViewer.NO_EXPAND);
		assertEquals(AbstractTreeViewer.NO_EXPAND,
				fTreeViewer.getAutoExpandOnSingleChildLevels(), "Setting an auto-expansion level of NO_EXPAND works");
	}

	@Test
	public void testAutoExpandOnSingleChildManualDisable() {
		// We need our own model since some default models do not generate trivial
		// paths
		TestElement modelRoot = TestElement.createModel(5, 1);
		TestElement trivialPathRoot = modelRoot.getFirstChild();
		fViewer.setInput(modelRoot);

		fTreeViewer.setAutoExpandOnSingleChildLevels(AbstractTreeViewer.NO_EXPAND);
		fTreeViewer.setExpandedStateWithAutoExpandOnSingleChild(trivialPathRoot, true);

		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot), "The expanded widget child is not expanded");
		assertFalse(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild()), "The first child of the trivial path was auto-expanded");
	}

	public void testAutoExpandOnSingleChildManualEnableAndThenDisable() {
		TestElement modelRoot = TestElement.createModel(5, 1);
		TestElement trivialPathRoot = modelRoot.getFirstChild();
		fViewer.setInput(modelRoot);

		fTreeViewer.setAutoExpandOnSingleChildLevels(2);
		fTreeViewer.setAutoExpandOnSingleChildLevels(AbstractTreeViewer.NO_EXPAND);
		fTreeViewer.setExpandedStateWithAutoExpandOnSingleChild(trivialPathRoot, true);

		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot), "The expanded widget child is not expanded");
		assertFalse(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild()), "The first child of the trivial path was auto-expanded");
	}

	@Test
	public void testAutoExpandOnSingleChildInfiniteExpand() {
		TestElement modelRoot = TestElement.createModel(5, 1);
		TestElement trivialPathRoot = modelRoot.getFirstChild();
		fViewer.setInput(modelRoot);

		fTreeViewer.setAutoExpandOnSingleChildLevels(AbstractTreeViewer.ALL_LEVELS);
		fTreeViewer.setExpandedStateWithAutoExpandOnSingleChild(trivialPathRoot, true);

		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot), "The expanded widget child is not expanded");
		TestElement child = trivialPathRoot.getFirstChild();
		for (int depth = 1; child != null; depth++) {
			assertTrue(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild()), "The " + depth + ". child of the trivial path was not auto-expanded");
			child = child.getFirstChild();
		}
	}

	public void testFilterExpanded() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		fTreeViewer.expandToLevel(first2, 0);

		fTreeViewer.addFilter(new TestLabelFilter());
		assertEquals(5, getItemCount(), "filtered count");
	}

	@Test
	public void testInsertChildReveal() {
		TestElement first = fRootElement.getFirstChild();
		TestElement newElement = first.addChild(TestModelChange.INSERT | TestModelChange.REVEAL);
		assertNotNull(fViewer.testFindItem(newElement), "new sibling is visible");
	}

	@Test
	public void testInsertChildRevealSelect() {
		TestElement last = fRootElement.getLastChild();
		TestElement newElement = last
				.addChild(TestModelChange.INSERT | TestModelChange.REVEAL | TestModelChange.SELECT);
		assertNotNull(fViewer.testFindItem(newElement), "new sibling is visible");
		assertSelectionEquals("new element is selected", newElement);
	}

	@Test
	public void testInsertChildRevealSelectExpanded() {
		TestElement first = fRootElement.getFirstChild();
		TestElement newElement = first
				.addChild(TestModelChange.INSERT | TestModelChange.REVEAL | TestModelChange.SELECT);
		assertNotNull(fViewer.testFindItem(newElement), "new sibling is visible");
		assertSelectionEquals("new element is selected", newElement);
	}

	/**
	 * Regression test for 1GDN0PX: ITPUI:WIN2000 - SEVERE - AssertionFailure when
	 * expanding Navigator Problem was: - before addition, parent item had no
	 * children, and was expanded - after addition, during refresh(), updatePlus()
	 * added dummy node even though parent item was expanded - in updateChildren, it
	 * wasn't handling a dummy node
	 */
	@Test
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
			assertNotNull(fViewer.testFindItem(child), "new child is visible");
		}
	}

	/**
	 * Regression test for 1GBDB5A: ITPUI:WINNT - Exception in AbstractTreeViewer
	 * update. Problem was: node has child A node gets duplicate child A viewer is
	 * refreshed rather than using add for new A
	 * AbstractTreeViewer.updateChildren(...) was not properly handling it
	 */
	@Test
	public void testRefreshWithDuplicateChild() {
		TestElement first = fRootElement.getFirstChild();
		TestElement newElement = (TestElement) first.clone();
		fRootElement.addChild(newElement, new TestModelChange(TestModelChange.STRUCTURE_CHANGE, fRootElement));
		assertNotNull(fViewer.testFindItem(newElement), "new sibling is visible");
	}

	/**
	 * Regression test for Bug 3840 [Viewers] free expansion of jar happening when
	 * deleting projects (1GEV2FL) Problem was: - node has children A and B - A is
	 * expanded, B is not - A gets deleted - B gets expanded because it reused A's
	 * item
	 */
	@Test
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

	@Test
	public void testRenameChildElement() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		fTreeViewer.expandToLevel(first2, 0);
		assertNotNull(fViewer.testFindItem(first2), "first child is visible");

		String newLabel = first2.getLabel() + " changed";
		first2.setLabel(newLabel);
		Widget widget = fViewer.testFindItem(first2);
		assertTrue(widget instanceof Item);
		assertEquals(((Item) widget).getText(), first2.getID() + " " + newLabel, "changed label");
	}

	/**
	 * Regression test for Bug 26698 [Viewers] stack overflow during debug session,
	 * causing IDE to crash Problem was: - node A has child A - setExpanded with A
	 * in the list caused an infinite recursion
	 */
	@Test
	public void testSetExpandedWithCycle() {
		TestElement first = fRootElement.getFirstChild();
		first.addChild(first, new TestModelChange(TestModelChange.INSERT, first, first));
		fTreeViewer.setExpandedElements(new Object[] { first });

	}

	/**
	 * Test for Bug 41710 - assertion that an object may not be added to a given
	 * TreeItem more than once.
	 */
	@Test
	public void testSetDuplicateChild() {
		// Widget root = fViewer.testFindItem(fRootElement);
		// assertNotNull(root);
		TestElement parent = fRootElement.addChild(TestModelChange.INSERT);
		TestElement child = parent.addChild(TestModelChange.INSERT);
		int initialCount = getItemCount(parent);
		fRootElement.addChild(child, new TestModelChange(TestModelChange.INSERT, fRootElement, child));
		int postCount = getItemCount(parent);
		assertEquals(initialCount, postCount, "Same element added to a parent twice.");
	}

	/**
	 * Test for Bug 571844 - assert that an item is not added twice if the
	 * comparator returns 0 = equal for more than one tree item. Problem was that
	 * the method AbstractTreeViewer#createAddedElements only searched forward but
	 * not backwards for an equal element, if the comparator returned 0. The example
	 * below is a case where the previous implementation would fail.
	 */
	@Test
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
		assertEquals(3, tree.getItems().length, "Same element added to parent twice.");
	}

	@Test
	public void testContains() {
		// some random element.
		assertFalse(fTreeViewer.contains(fRootElement, ""), "element must not be available on the viewer");

		// first child of root.
		assertTrue(fTreeViewer.contains(fRootElement, fRootElement.getFirstChild()), "element must be available on the viewer");

		// last child of the root
		assertTrue(fTreeViewer.contains(fRootElement, fRootElement.getLastChild()), "element must be available on the viewer");
		// child of first element is not expanded
		assertFalse(fTreeViewer.contains(fRootElement.getFirstChild(), fRootElement.getFirstChild().getFirstChild()), "element must not be available on the viewer");
		fTreeViewer.expandAll();
		// child of first element when expanded.
		assertTrue(fTreeViewer.contains(fRootElement.getFirstChild(), fRootElement.getFirstChild().getFirstChild()), "element must be available on the viewer");
	}

	@AfterEach
	@Override
	public void tearDown() {
		super.tearDown();
		fTreeViewer = null;
	}
}
