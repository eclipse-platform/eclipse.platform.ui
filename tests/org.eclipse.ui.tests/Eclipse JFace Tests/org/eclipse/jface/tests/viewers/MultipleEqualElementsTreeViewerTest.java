/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Tests TreeViewer's support for multiple equal elements ().
 * 
 * @since 3.2
 */
public class MultipleEqualElementsTreeViewerTest extends TreeViewerTest {
	
	private TestElement element_2;

	private TestElement element_2_1;

	private TestElement element_2_1_2;

	private TestElement element_1;

	private TestElement element_1_3;

	private TreePath treePath_1_2_21_212;

	private TreePath treePath_1_21_212;

	private TreePath treePath_1_212;

	private TreePath treePath_2_21_212;

	private TreePath treePath_1;

	private TreePath treePath_1_21;

	private TreePath treePath_2_21;

	private TreePath treePath_1_2_21;

	private TreePath treePath_1_2;

	private TreePath treePath_2;

	public MultipleEqualElementsTreeViewerTest(String name) {
		super(name);
	}

	public TreeViewer getTreeViewer() {
		return (TreeViewer) fViewer;
	}

	protected void setUpModel() {
		/*
		 * - fRootElement
		 * |+element_0
		 * |-element_1
		 * ||+element_1_0
		 * ||+element_1_1
		 * ||+element_1_2
		 * ||+element_1_3
		 * ||+element_1_4
		 * ||+element_1_5
		 * ||+element_1_6
		 * ||+element_1_7
		 * ||+element_1_8
		 * ||+element_1_9
		 * ||+element_2
		 * ||+element_2_1
		 * ||+element_2_1_2
		 * |-element_2
		 * ||+element_2_0
		 * ||-element_2_1
		 * |||+element_2_1_0
		 * |||+element_2_1_1
		 * |||+element_2_1_2
		 * |||+element_2_1_3
		 * |||+element_2_1_4
		 * |||+element_2_1_5
		 * |||+element_2_1_6
		 * |||+element_2_1_7
		 * |||+element_2_1_8
		 * |||+element_2_1_9
		 * ||+element_2_2
		 * ||+element_2_3
		 * ||+element_2_4
		 * ||+element_2_5
		 * ||+element_2_6
		 * ||+element_2_7
		 * ||+element_2_8
		 * ||+element_2_9
		 * |+element_3
		 * |+element_4
		 * |+element_5
		 * |+element_6
		 * |+element_7
		 * |+element_8
		 * |+element_9
		 */

		fRootElement = TestElement.createModel(3, 10);
		element_2 = fRootElement.getChildAt(2);
		element_2_1 = element_2.getChildAt(1);
		element_2_1_2 = element_2_1.getChildAt(2);
		element_1 = fRootElement.getChildAt(1);
		element_1_3 = element_1.getChildAt(1);
		element_1.addChild(element_2, null);
		element_1.addChild(element_2_1, null);
		element_1.addChild(element_2_1_2, null);
		treePath_1_2_21_212 = new TreePath(new Object[] { element_1, element_2,
				element_2_1, element_2_1_2 });
		treePath_1_2_21 = new TreePath(new Object[] { element_1, element_2,
				element_2_1 });
		treePath_1 = new TreePath(new Object[] { element_1 });
		treePath_2 = new TreePath(new Object[] { element_2 });
		treePath_1_2 = new TreePath(new Object[] { element_1, element_2 });
		treePath_1_21 = new TreePath(new Object[] { element_1, element_2_1 });
		treePath_1_21_212 = new TreePath(new Object[] { element_1, element_2_1,
				element_2_1_2 });
		treePath_1_212 = new TreePath(new Object[] { element_1, element_2_1_2 });
		treePath_2_21_212 = new TreePath(new Object[] { element_2, element_2_1,
				element_2_1_2 });
		treePath_2_21 = new TreePath(new Object[] { element_2, element_2_1 });
		fModel = fRootElement.getModel();
	}

	public void testElementMap() {
		getTreeViewer().expandToLevel(element_1, AbstractTreeViewer.ALL_LEVELS);
		getTreeViewer().expandToLevel(element_2, AbstractTreeViewer.ALL_LEVELS);
		assertEquals(1, getTreeViewer().testFindItems(element_1).length);
		assertEquals(2, getTreeViewer().testFindItems(element_2).length);
		assertEquals(3, getTreeViewer().testFindItems(element_2_1).length);
		assertEquals(4, getTreeViewer().testFindItems(element_2_1_2).length);
	}

	public void testSelection() {
		getTreeViewer().expandToLevel(element_1, AbstractTreeViewer.ALL_LEVELS);
		getTreeViewer().expandToLevel(element_2, AbstractTreeViewer.ALL_LEVELS);
		getTreeViewer().setSelection(new StructuredSelection(element_1_3));
		ISelection selection = getTreeViewer().getSelection();
		assertTrue(selection instanceof ITreeSelection);
		ITreeSelection treeSelection = (ITreeSelection) selection;
		assertEquals(new TreeSelection(new TreePath(new Object[] { element_1,
				element_1_3 }), null), treeSelection);
		Widget[] items = getTreeViewer().testFindItems(element_2_1_2);
		TreeItem[] treeItems = new TreeItem[items.length];
		System.arraycopy(items, 0, treeItems, 0, items.length);
		assertEquals(4, treeItems.length);
		for (int i = 0; i < treeItems.length; i++) {
			assertNotNull(treeItems[i]);
		}
		getTreeViewer().getTree().setSelection(treeItems);
		treeSelection = (ITreeSelection) getTreeViewer().getSelection();
		List paths = Arrays.asList(treeSelection.getPaths());
		assertEquals(4, paths.size());
		assertTrue(paths.contains(treePath_1_2_21_212));
		assertTrue(paths.contains(treePath_1_21_212));
		assertTrue(paths.contains(treePath_1_212));
		assertTrue(paths.contains(treePath_2_21_212));
		getTreeViewer().setSelection(
				new TreeSelection(new TreePath[] { treePath_2_21_212 }, null));
		assertEquals(1, getTreeViewer().getTree().getSelectionCount());
		assertMatchingPath(treePath_2_21_212, getTreeViewer().getTree()
				.getSelection()[0]);
	}

	public void testExpansion() {
		getTreeViewer().expandToLevel(treePath_1_21_212, 1);
		assertEqualsArray("element expansion", new Object[] { element_1,
				element_2_1 }, getTreeViewer().getExpandedElements());
		assertEqualsArray("path expansion", new Object[] { treePath_1,
				treePath_1_21 }, getTreeViewer().getExpandedTreePaths());
		getTreeViewer().setExpandedTreePaths(
				new TreePath[] { treePath_1, treePath_1_2, treePath_1_2_21, treePath_2, treePath_2_21 });
		assertEqualsArray("path expansion", new Object[] { treePath_1, treePath_1_2, treePath_1_2_21, treePath_2, treePath_2_21 }, getTreeViewer().getExpandedTreePaths());
	}

	private void assertMatchingPath(TreePath expectedPath, TreeItem item) {
		for (int i = expectedPath.getSegmentCount() - 1; i >= 0; i--) {
			assertNotNull(item);
			assertEquals(expectedPath.getSegment(i), item.getData());
			item = item.getParentItem();
		}
		assertNull(item);
	}

	public void testUpdate() {
		// materialize
		getTreeViewer().setExpandedTreePaths(
				new TreePath[] { treePath_1_2_21, treePath_2_21 });
		Widget[] items = getTreeViewer().testFindItems(element_2_1);
		for (int i = 0; i < items.length; i++) {
			assertEquals("0-2-1 name-1", ((TreeItem) items[i]).getText());
		}
		element_2_1.setLabel("billy");
		// the setLabel call fires a change event which results in a call like this:
		// getTreeViewer().update(element_2_1, null);
		for (int i = 0; i < items.length; i++) {
			assertEquals("0-2-1 billy", ((TreeItem) items[i]).getText());
		}
	}
	
	public void testAddWithoutMaterialize() {
		TestElement newElement = element_2.addChild(TestModelChange.INSERT);
		getTreeViewer().setExpandedTreePaths(
				new TreePath[] { treePath_1_2_21, treePath_2_21 });
		Widget[] items = getTreeViewer().testFindItems(newElement);
		assertEquals(2, items.length);
	}
	
	public void testAddAfterMaterialize() {
		// materialize before adding
		getTreeViewer().setExpandedTreePaths(
				new TreePath[] { treePath_1_2_21, treePath_2_21 });
		TestElement newElement = element_2.addChild(TestModelChange.INSERT);
		getTreeViewer().setExpandedTreePaths(
				new TreePath[] { treePath_1_2_21, treePath_2_21 });
		Widget[] items = getTreeViewer().testFindItems(newElement);
		assertEquals(2, items.length);
	}
	
	public void testRemoveWithParentAfterMaterialize() {
		// materialize
		getTreeViewer().expandToLevel(element_1, AbstractTreeViewer.ALL_LEVELS);
		getTreeViewer().expandToLevel(element_2, AbstractTreeViewer.ALL_LEVELS);
		element_2.basicDeleteChild(element_2_1);
		getTreeViewer().remove(element_2, new Object[]{element_2_1});
		assertEquals(2, getTreeViewer().testFindItems(element_2).length);
		assertEquals(1, getTreeViewer().testFindItems(element_2_1).length);
		assertEquals(2, getTreeViewer().testFindItems(element_2_1_2).length);
	}

	public void testRemoveWithParentBeforeMaterialize() {
		element_2.basicDeleteChild(element_2_1);
		getTreeViewer().remove(element_2, new Object[]{element_2_1});
		// materialize
		getTreeViewer().expandToLevel(element_1, AbstractTreeViewer.ALL_LEVELS);
		getTreeViewer().expandToLevel(element_2, AbstractTreeViewer.ALL_LEVELS);
		assertEquals(2, getTreeViewer().testFindItems(element_2).length);
		assertEquals(1, getTreeViewer().testFindItems(element_2_1).length);
		assertEquals(2, getTreeViewer().testFindItems(element_2_1_2).length);
	}
	
}
