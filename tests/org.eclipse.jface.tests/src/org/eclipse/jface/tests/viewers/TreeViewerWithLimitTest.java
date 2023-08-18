/*******************************************************************************
 * Copyright (c) 2023 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/

package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.internal.ExpandableNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

public class TreeViewerWithLimitTest extends BaseLimitBasedViewerTest {

	private TestTreeViewer treeViewer;

	public TreeViewerWithLimitTest(String name) {
		super(name);
	}

	public void testSetSelection() {
		DataModel firstEle = rootModel.get(0);
		assertSetSelection(firstEle);

		DataModel invisible = rootModel.get(rootModel.size() - VIEWER_LIMIT);
		assertSetSelectionExpNode(invisible);
	}

	private void assertSetSelectionExpNode(DataModel invisible) {
		treeViewer.setSelection(new StructuredSelection(invisible), true);
		processEvents();
		IStructuredSelection selection = treeViewer.getStructuredSelection();
		assertFalse("Selection must not be empty", selection.isEmpty());
		Object firstElement = selection.getFirstElement();
		assertTrue("Selection must be expandable node: " + firstElement, treeViewer.isExpandableNode(firstElement));
	}

	private void assertSetSelection(DataModel firstEle) {
		treeViewer.setSelection(new StructuredSelection(firstEle));
		processEvents();
		IStructuredSelection selection = treeViewer.getStructuredSelection();
		assertFalse("Selection must not be empty", selection.isEmpty());
		assertEquals("incorrect element is selected", firstEle, selection.getFirstElement());
	}

	public void testReveal() throws Exception {
		DataModel firstEle = rootModel.get(0);
		DataModel toReveal = firstEle.children.get(2).children.get(2);
		treeViewer.reveal(toReveal);
		// selection works if the element was revealed. selection don't try to create
		// item but reveal does.
		assertSetSelection(toReveal);

		// now try to reveal non expanded item. it should not reveal anything because in
		// case of limit based tree we don't create an item if it hidden.
		DataModel inVisible = firstEle.children.get(2).children.get(VIEWER_LIMIT + 2);
		assertSetSelectionExpNode(inVisible);
		ExpandableNode selected = (ExpandableNode) treeViewer.getStructuredSelection().getFirstElement();
		Object[] remaining = selected.getRemainingElements();
		boolean found = false;
		for (Object object : remaining) {
			if (object == inVisible) {
				found = true;
				break;
			}
		}
		assertTrue("item to select must be inside expandable node", found);
	}

	public void testCollapseAll() {
		treeViewer.expandAll();
		processEvents();
		DataModel l2ThirdEle = rootModel.get(0).children.get(2).children.get(2);
		assertSetSelection(l2ThirdEle);

		treeViewer.collapseAll();
		processEvents();
		TreeItem[] items = assertLimitedItems();
		for (int i = 0; i < VIEWER_LIMIT; i++) {
			TreeItem treeItem = items[i];
			assertFalse("expansion must be false", treeItem.getExpanded());
		}
	}

	public void testCollapseToLevel() {
	}

	public void testExpandAll() {
		treeViewer.expandAll();
		processEvents();
		TreeItem[] rootLevelItems = assertLimitedItems();
		for (int i = 0; i < VIEWER_LIMIT; i++) {
			TreeItem[] items = assertLimitedItems(rootLevelItems[i]);
			for (int j = 0; j < VIEWER_LIMIT; j++) {
				TreeItem treeItem2 = items[j];
				assertLimitedItems(treeItem2);
			}
		}
	}

	private TreeItem[] assertLimitedItems(TreeItem treeItem) {
		TreeItem[] items = treeItem.getItems();
		assertEquals("There should be only limited items", VIEWER_LIMIT + 1, items.length);
		Object data = items[VIEWER_LIMIT].getData();
		assertTrue("last item must be expandable node", treeViewer.isExpandableNode(data));
		return items;
	}

	private TreeItem[] assertLimitedItems() {
		TreeItem[] rootLevelItems = treeViewer.getTree().getItems();
		assertEquals("There should be only limited items", VIEWER_LIMIT + 1, rootLevelItems.length);
		Object data = rootLevelItems[VIEWER_LIMIT].getData();
		assertTrue("last item must be expandable node", treeViewer.isExpandableNode(data));
		return rootLevelItems;
	}

	public void testExpandToLevelInt() {
		treeViewer.expandToLevel(2, true);
		processEvents();
		TreeItem[] rootLevelItems = assertLimitedItems();
		for (int i = 0; i < VIEWER_LIMIT; i++) {
			TreeItem[] items = assertLimitedItems(rootLevelItems[i]);
			for (int j = 0; j < VIEWER_LIMIT; j++) {
				assertDummyItem(items[j]);
			}
		}
	}

	private static void assertDummyItem(TreeItem treeItem) {
		TreeItem[] items = treeItem.getItems();
		assertEquals("Item must not be expanded", 1, items.length);
		assertNull("Dummy tree item data must be null", items[0].getData());
	}

	public void testExpandToLevelObjectInt() {
		DataModel firstEle = rootModel.get(0);
		treeViewer.expandToLevel(firstEle, 3, true);
		processEvents();
		TreeItem[] topLevelItems = assertLimitedItems();
		TreeItem firstItem = topLevelItems[0];
		TreeItem[] children = assertLimitedItems(firstItem);
		for (int i = 1; i < VIEWER_LIMIT; i++) {
			assertLimitedItems(children[i]);
		}
		// no other items are expanded
		for (int i = 1; i < VIEWER_LIMIT; i++) {
			assertDummyItem(topLevelItems[i]);
		}
	}

	public void testRemoveItemsAtParent() {
		treeViewer.expandAll();
		processEvents();
		DataModel firstEle = rootModel.get(0);
		DataModel thirdOfFirst = firstEle.children.get(2).children.remove(2);
		TreeItem visItem = treeViewer.getTree().getItem(0).getItem(2).getItem(2);
		assertEquals("element contains unexpected data", thirdOfFirst, visItem.getData());
		treeViewer.remove(firstEle, new Object[] { thirdOfFirst });
		processEvents();
		thirdOfFirst = firstEle.children.get(2).children.get(2);
		visItem = treeViewer.getTree().getItem(0).getItem(2).getItem(2);
		assertEquals("element contains unexpected data", thirdOfFirst, visItem.getData());
	}

	public void testRemoveItem() {
		processEvents();
		treeViewer.expandAll();
		processEvents();
		DataModel firstEle = rootModel.remove(0);
		TreeItem firstItem = treeViewer.getTree().getItem(0);
		assertEquals("element contains unexpected data", firstEle, firstItem.getData());
		treeViewer.remove(firstEle);
		processEvents();
		firstEle = rootModel.get(0);
		firstItem = treeViewer.getTree().getItem(0);
		assertEquals("element contains unexpected data", firstEle, firstItem.getData());
	}

	public void testSetAutoExpandLevel() {
		treeViewer.setInput(new DataModel(Integer.valueOf(100)));
		treeViewer.setAutoExpandLevel(2);
		treeViewer.setInput(rootModel);
		processEvents();
		TreeItem[] rootLevelItems = assertLimitedItems();
		for (int i = 0; i < VIEWER_LIMIT; i++) {
			TreeItem[] items = assertLimitedItems(rootLevelItems[i]);
			for (int j = 0; j < VIEWER_LIMIT; j++) {
				assertDummyItem(items[j]);
			}
		}
	}

	public void testInsert() {
		TreeItem thirdItem = treeViewer.getTree().getItem(2);
		assertEquals("unexpected element found at position 2", rootModel.get(2), thirdItem.getData());
		DataModel newElement = new DataModel(Integer.valueOf(3));
		rootModel.add(newElement);
		treeViewer.insert(rootModel, newElement, 2);
		processEvents();
		thirdItem = treeViewer.getTree().getItem(2);
		assertEquals("unexpected element found at position 2", newElement, thirdItem.getData());
	}

	public void testRefresh() {
		DataModel firstEle = rootModel.remove(0);
		TreeItem firstItem = treeViewer.getTree().getItem(0);
		assertEquals("element contains unexpected data", firstEle, firstItem.getData());
		treeViewer.refresh();
		processEvents();
		firstEle = rootModel.get(0);
		firstItem = treeViewer.getTree().getItem(0);
		assertEquals("element contains unexpected data", firstEle, firstItem.getData());
	}

	public void testSetFilters() {
		DataModel firstEle = rootModel.get(0);
		TreeItem firstItem = treeViewer.getTree().getItem(0);
		assertEquals("element contains unexpected data", firstEle, firstItem.getData());
		treeViewer.setFilters(new TestViewerFilter());
		processEvents();
		firstEle = rootModel.get(6);
		firstItem = treeViewer.getTree().getItem(0);
		assertEquals("element contains unexpected data", firstEle, firstItem.getData());
	}

	public void testSetInput() {
		List<DataModel> rootModel = new ArrayList<>();
		DataModel rootLevel = new DataModel(Integer.valueOf(100));
		rootModel.add(rootLevel);
		treeViewer.setInput(rootModel);
		processEvents();
		assertEquals("there must be only one item", 1, treeViewer.getTree().getItems().length);
		treeViewer.setInput(createModel(DEFAULT_ELEMENTS_COUNT));
		processEvents();
		assertLimitedItems();
	}

	public void testContains() {
		// some random element.
		assertFalse("element must not be available on the viewer", treeViewer.contains(fRootElement, ""));

		// first child of root.
		assertTrue("element must be available on the viewer", treeViewer.contains(rootModel, rootModel.get(0)));

		// last child of the root
		assertTrue("element must be available on the viewer",
				treeViewer.contains(rootModel, rootModel.get(rootModel.size() - 1)));
		// child of first element is not expanded
		assertFalse("element must not be available on the viewer",
				treeViewer.contains(rootModel, rootModel.get(0).children.get(0)));
		treeViewer.expandAll();
		// child of first element when expanded.
		assertTrue("element must be available on the viewer",
				treeViewer.contains(rootModel, rootModel.get(0).children.get(0)));
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		treeViewer = new TestTreeViewer(parent);
		treeViewer.setDisplayIncrementally(VIEWER_LIMIT);
		treeViewer.setContentProvider(new TestTreeContentProvider());
		treeViewer.setLabelProvider(new LabelProvider());
		treeViewer.setComparator(new TestComparator());
		return treeViewer;
	}

	@Override
	protected void setInput() {
		rootModel = createModel(DEFAULT_ELEMENTS_COUNT);
		treeViewer.setInput(rootModel);
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run(TreeViewerWithLimitTest.class);
	}

	private static class TestTreeContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof ArrayList<?>) {
				return ((ArrayList<?>) inputElement).toArray();
			}
			return ((DataModel) inputElement).children.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (!(parentElement instanceof DataModel)) {
				return null;
			}
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			if (!(element instanceof DataModel)) {
				return null;
			}
			return ((DataModel) element).parent;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof ArrayList<?>) {
				return !((ArrayList<?>) element).isEmpty();
			}
			DataModel myModel = (DataModel) element;
			return myModel.children.size() > 0;
		}
	}

	class TestTreeViewer extends TreeViewer {
		public TestTreeViewer(Composite parent) {
			super(parent);
		}
	}

}
