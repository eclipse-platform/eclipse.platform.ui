/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 118919
 *     Lucas Bullen (Red Hat Inc.) - Bug 493357
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

/**
 * Tests TreeViewer's VIRTUAL support with a lazy content provider.
 *
 * @since 3.2
 */
public class SimpleVirtualLazyTreeViewerTest extends ViewerTestCase {
	private static final int NUM_ROOTS = 100;
	private static final int NUM_CHILDREN = 10;

	private boolean callbacksEnabled = true;
	private boolean printCallbacks = false;
	private int offset = 0;

	private int updateElementCallCount = 0;

	private class LazyTreeContentProvider implements ILazyTreeContentProvider {
		/**
		 *
		 */
		private Object input;

		@Override
		public void updateElement(Object parent, int index) {
			updateElementCallCount++;
			String parentString = (String) parent;
			Object childElement = parentString + "-" + (index + offset);
			if (printCallbacks) {
				System.out.println("updateElement called for " + parent
						+ " at " + index);
			}
			if (callbacksEnabled) {
				getTreeViewer().replace(parent, index, childElement);
				getTreeViewer().setChildCount(childElement, NUM_CHILDREN);
			}
		}

		@Override
		public void dispose() {
			// do nothing
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.input = newInput;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public void updateChildCount(Object element, int currentChildCount) {
			if (printCallbacks) {
				System.out.println("updateChildCount called for " + element
						+ " with " + currentChildCount);
			}
			if (callbacksEnabled) {
				getTreeViewer().setChildCount(element,
						element == input ? NUM_ROOTS : NUM_CHILDREN);
			}
		}
	}

	public SimpleVirtualLazyTreeViewerTest(String name) {
		super(name);
	}

	public TreeViewer getTreeViewer() {
		return (TreeViewer) fViewer;
	}

	/**
	 * Checks if the virtual tree / table functionality can be tested in the
	 * current settings. The virtual trees and tables rely on SWT.SetData event
	 * which is only sent if OS requests information about the tree / table. If
	 * the window is not visible (obscured by another window, outside of visible
	 * area, or OS determined that it can skip drawing), then OS request won't
	 * be send, causing automated tests to fail. See
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=118919 .
	 */
	protected boolean setDataCalled = false;

	@Override
	public void setUp() {
		super.setUp();
		processEvents(); // run events for SetData precondition test
	}

	@Override
	protected void setInput() {
		String letterR = "R";
		getTreeViewer().setInput(letterR);
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		Tree tree = new Tree(fShell, SWT.VIRTUAL | SWT.MULTI);
		TreeViewer treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new LazyTreeContentProvider());
		tree.addListener(SWT.SetData, event -> setDataCalled = true);
		return treeViewer;
	}

	public void testCreation() {
		if (disableTestsBug347491) {
			System.out.println(getName() + " disabled due to Bug 347491");
			return;
		}
		assertTrue("SWT.SetData not received", setDataCalled);
		processEvents();
		assertTrue("tree should have items", getTreeViewer().getTree()
				.getItemCount() > 0);
		assertTrue("call to updateElement expected", updateElementCallCount > 0);
		assertTrue(
				"expected calls to updateElement for less than half of the items",
				updateElementCallCount < NUM_ROOTS / 2);
		assertEquals("R-0", getTreeViewer().getTree().getItem(0).getText());
	}

	public void testExpand() {
		processEvents();
		Tree tree = getTreeViewer().getTree();
		getTreeViewer().expandToLevel("R-0", 1);
		// redraw the tree - this will trigger the SetData event
		processEvents();
		assertEquals(NUM_CHILDREN, tree.getItem(0).getItemCount());
		TreeItem treeItem = tree.getItem(0).getItem(3);
		expandAndNotify(treeItem);
		// force redrawing the tree - this will trigger the SetData event
		tree.update();
		assertEquals(NUM_CHILDREN, treeItem.getItemCount());
		assertEquals(NUM_CHILDREN, treeItem.getItems().length);
		// interact();
	}

	private void expandAndNotify(TreeItem treeItem) {
		// callbacksEnabled = false;
		Tree tree = treeItem.getParent();
		tree.setRedraw(false);
		treeItem.setExpanded(true);
		try {
			Event event = new Event();
			event.item = treeItem;
			event.type = SWT.Expand;
			tree.notifyListeners(SWT.Expand, event);
		} finally {
			// callbacksEnabled = true;
			tree.setRedraw(true);
		}
	}

	public void testSetSorterOnNullInput() {
		fViewer.setInput(null);
		fViewer.setSorter(new ViewerSorter());
	}

	public void testSetComparatorOnNullInput() {
		fViewer.setInput(null);
		fViewer.setComparator(new ViewerComparator());
	}

	/* test TreeViewer.remove(parent, index) */
	public void testRemoveAt() {
		assertTrue("expected less than " + (NUM_ROOTS / 2) + " but got " + updateElementCallCount,
				updateElementCallCount < NUM_ROOTS / 2);
		if (disableTestsBug347491) {
			System.out.println(getName() + " disabled due to Bug 347491");
			return;
		}
		assertTrue("SWT.SetData not received", setDataCalled);
		TreeViewer treeViewer = (TreeViewer) fViewer;
		// correct what the content provider is answering with
		treeViewer.getTree().update();
		offset = 1;
		treeViewer.remove(treeViewer.getInput(), 3);
		assertEquals(NUM_ROOTS - 1, treeViewer.getTree().getItemCount());
		treeViewer.setSelection(new StructuredSelection(new Object[] { "R-0",
				"R-1" }));
		assertEquals(2, treeViewer.getStructuredSelection().size());
		processEventsUntilElementUpdated();
		// printCallbacks = true;
		// correct what the content provider is answering with
		offset = 2;
		treeViewer.remove(treeViewer.getInput(), 1);
		assertEquals(NUM_ROOTS - 2, treeViewer.getTree().getItemCount());
		processEventsUntilElementUpdated();
		assertEquals(1, treeViewer.getStructuredSelection().size());
		// printCallbacks = false;
	}

	private void processEventsUntilElementUpdated() {
		updateElementCallCount = 0;
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				// Keep processing events until the element updated event is queued and run
				processEvents();
				if (eventLoopAdjustmentBug531048) {
					return updateElementCallCount <= 2;
				}
				return updateElementCallCount == 1;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000);
		if (eventLoopAdjustmentBug531048) {
			assertTrue(updateElementCallCount <= 2);
		} else {
			assertEquals(1, updateElementCallCount);
		}
	}
}
