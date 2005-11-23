/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Tests TreeViewer's VIRTUAL support with a lazy content provider.
 * 
 * @since 3.2
 */
public class SimpleVirtualLazyTreeViewerTest extends ViewerTestCase {
	private static final int NUM_ROOTS = 100;

	private boolean callbacksEnabled = true;

	private class LazyTreeContentProvider implements ILazyTreeContentProvider {
		public void updateElement(Object parent, int index) {
			updateElementCallCount++;
			String parentString = (String) parent;
			Object childElement = parentString + "-" + index;
			// System.out.println(childElement);
			if (callbacksEnabled) {
				getTreeViewer().replace(parent, index, childElement);
				getTreeViewer().setChildCount(childElement, 10);
			}
		}

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}
	}

	private int updateElementCallCount = 0;

	public SimpleVirtualLazyTreeViewerTest(String name) {
		super(name);
	}

	public TreeViewer getTreeViewer() {
		return (TreeViewer) fViewer;
	}

	protected void setInput() {
		String letterR = "R";
		getTreeViewer().setInput(letterR);
		getTreeViewer().setChildCount(letterR, NUM_ROOTS);
	}

	protected StructuredViewer createViewer(Composite parent) {
		Tree tree = new Tree(fShell, SWT.VIRTUAL);
		TreeViewer treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new LazyTreeContentProvider());
		return treeViewer;
	}

	public void testCreation() {
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
		// TODO temporarily commented out - tracked by Bug 117184
//		processEvents();
//		Tree tree = getTreeViewer().getTree();
//		getTreeViewer().expandToLevel("R-0", 1);
//		// force redrawing the tree - this will trigger the SetData event
//		tree.update();
//		assertEquals(10, tree.getItem(0).getItemCount());
//		TreeItem treeItem = tree.getItem(0).getItem(3);
//		expandAndNotify(treeItem);
//		// force redrawing the tree - this will trigger the SetData event
//		tree.update();
//		assertEquals(10, treeItem.getItemCount());
//		assertEquals(10, treeItem.getItems().length);
//		// interact();
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
}
