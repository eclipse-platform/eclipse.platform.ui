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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TableViewerWithLimitTest extends BaseLimitBasedViewerTest {

	public TableViewerWithLimitTest(String name) {
		super(name);
	}

	TestTableViewer tableViewer;

	public void testLimitedItemsCreatedWithExpansionNode() {
		Table table = tableViewer.getTable();
		TableItem[] items = table.getItems();
		assertLimitedItems(items);
	}

	public void testAddElement() {
		Table table = tableViewer.getTable();
		assertLimitedItems(table.getItems());
		DataModel data = (DataModel) table.getItems()[2].getData();
		assertEquals("wrong item is found at given location", Integer.valueOf(4), data.id);

		// this element must be visible
		DataModel newEle = new DataModel(Integer.valueOf(3));
		rootModel.add(newEle);
		tableViewer.add(newEle);
		processEvents();
		// check items and label after addition.
		assertLimitedItems(table.getItems());
		data = (DataModel) table.getItems()[2].getData();
		assertEquals("wrong item is found at given location", Integer.valueOf(3), data.id);

		// this element must not be visible only expandable node label must be updated.
		DataModel newEle1 = new DataModel(Integer.valueOf(9));
		rootModel.add(newEle1);
		tableViewer.add(newEle1);
		processEvents();
		// check items and label after addition.
		assertLimitedItems(table.getItems());
		data = (DataModel) table.getItems()[2].getData();
		assertEquals("wrong item is found at given location", Integer.valueOf(3), data.id);
	}

	public void testRemoveElement() {
		Table table = tableViewer.getTable();
		assertLimitedItems(table.getItems());
		DataModel data = (DataModel) table.getItems()[2].getData();
		assertEquals("wrong item is found at given location", Integer.valueOf(4), data.id);

		// this element must be visible
		DataModel removed = rootModel.remove(2);
		tableViewer.remove(removed);
		processEvents();
		// check items and label after addition.
		assertLimitedItems(table.getItems());
		data = (DataModel) table.getItems()[2].getData();
		assertEquals("wrong item is found at given location", Integer.valueOf(6), data.id);

		// this element must not be visible only expandable node label must be updated.
		DataModel removed1 = rootModel.remove(7);
		tableViewer.remove(removed1);
		processEvents();
		// check items and label after addition.
		assertLimitedItems(table.getItems());
		data = (DataModel) table.getItems()[2].getData();
		assertEquals("wrong item is found at given location", Integer.valueOf(6), data.id);
	}

	public void testClickExpandableNode() {
		Table table = tableViewer.getTable();
		assertLimitedItems(table.getItems());
		TableItem lastItem = table.getItems()[table.getItems().length - 1];
		clickTableItem(table, lastItem);
		processEvents();
		Item[] itemsBefore = table.getItems();
		assertEquals("There are more/less items rendered than viewer limit", VIEWER_LIMIT * 2 + 1, itemsBefore.length);
		Item item = itemsBefore[itemsBefore.length - 1];
		assertTrue("Last node must be an Expandable Node", tableViewer.isExpandableNode(item.getData()));

		String expected = JFaceResources.format("ExpandableNode.defaultLabel", Integer.valueOf(VIEWER_LIMIT * 2 + 1),
				Integer.valueOf(VIEWER_LIMIT * 3), Integer.valueOf(rootModel.size()));
		assertEquals("Expandable node has an incorrect text", expected, item.getText());

		// click until all expandable nodes are expanded.
		clickUntilAllExpandableNodes(table);

		// all the elements of the model should be visible
		Item[] itemsAfterExp = table.getItems();
		assertEquals("There are more/less items rendered than viewer limit", rootModel.size(), itemsAfterExp.length);
		assertEquals("Last node must be an DataModel after all the elements expanded",
				itemsAfterExp[itemsAfterExp.length - 1].getData().getClass(), DataModel.class);
	}

	public void testApplyFilter() {
		Table table = tableViewer.getTable();
		clickUntilAllExpandableNodes(table);

		// all the elements of the model should be visible
		assertEquals("There are more/less items rendered than viewer limit", rootModel.size(), table.getItems().length);
		DataModel data = (DataModel) table.getItems()[2].getData();
		assertEquals("wrong item is found at given location", Integer.valueOf(4), data.id);

		tableViewer.setFilters(new TestViewerFilter());
		processEvents();

		clickUntilAllExpandableNodes(table);

		// only filtered items are visible
		assertEquals("There are more/less items rendered than viewer limit", 14, table.getItems().length);
		data = (DataModel) table.getItems()[2].getData();
		assertEquals("wrong item is found at given location", Integer.valueOf(16), data.id);
	}

	private void clickUntilAllExpandableNodes(Table table) {
		TableItem lastItem = table.getItems()[table.getItems().length - 1];
		processEvents();
		while (tableViewer.isExpandableNode(lastItem.getData())) {
			clickTableItem(table, lastItem);
			processEvents();
			lastItem = table.getItems()[table.getItems().length - 1];
		}
		processEvents();
	}

	public void testResetComparator() {
		Table table = tableViewer.getTable();
		// add an element at the end of the model. comparator will add it to right
		// location
		DataModel newEle1 = new DataModel(Integer.valueOf(3));
		rootModel.add(newEle1);
		tableViewer.add(newEle1);
		processEvents();
		DataModel data = (DataModel) table.getItems()[2].getData();
		assertEquals("wrong item is found at given location", Integer.valueOf(3), data.id);
		// reset comparator
		tableViewer.setComparator(null);
		processEvents();
		data = (DataModel) table.getItems()[2].getData();
		assertEquals("wrong item is found at given location", Integer.valueOf(4), data.id);
	}

	public void testSelection() {
		// select an element which is not visible
		DataModel toSelect = rootModel.get(rootModel.size() - VIEWER_LIMIT);
		tableViewer.setSelection(new StructuredSelection(toSelect));
		processEvents();
		ISelection selection = tableViewer.getSelection();
		assertTrue("Selection must not be empty", selection instanceof IStructuredSelection);
		Object selEle = ((IStructuredSelection) selection).getFirstElement();
		assertTrue("Selection must be ExpandableNode", tableViewer.isExpandableNode(selEle));

		// select an element which is visible
		toSelect = rootModel.get(VIEWER_LIMIT / 2);
		tableViewer.setSelection(new StructuredSelection(toSelect));
		processEvents();
		selection = tableViewer.getSelection();
		assertTrue("Selection must not be empty", selection instanceof IStructuredSelection);
		selEle = ((IStructuredSelection) selection).getFirstElement();
		assertEquals("selection must be desired element which is visible", toSelect, selEle);

		// select something not present in model.
		tableViewer.setSelection(new StructuredSelection("dummy"));
		processEvents();
		selection = tableViewer.getSelection();
		assertTrue("Selection must not be empty", selection.isEmpty());
	}

	public void testRefresh() {
		Table table = tableViewer.getTable();
		assertLimitedItems(table.getItems());
		assertEquals("third element must be third element of the input", rootModel.get(2), table.getItem(2).getData());
		DataModel ele1 = new DataModel(Integer.valueOf(100));
		rootModel.add(ele1);
		tableViewer.add(ele1);
		processEvents();
		assertLimitedItems(table.getItems());
		assertEquals("third element must be third element of the input", rootModel.get(2), table.getItem(2).getData());
		DataModel newEle = new DataModel(Integer.valueOf(3));
		rootModel.add(newEle);
		tableViewer.add(newEle);
		processEvents();
		assertLimitedItems(table.getItems());
		assertEquals("third element must be newly added element", newEle, table.getItem(2).getData());
	}

	private void assertLimitedItems(TableItem[] itemsBefore) {
		assertEquals("There are more/less items rendered than viewer limit", VIEWER_LIMIT + 1, itemsBefore.length);
		TableItem tableItem = itemsBefore[itemsBefore.length - 1];
		assertTrue("Last node must be an Expandable Node", tableViewer.isExpandableNode(tableItem.getData()));

		String expectedLabel = JFaceResources.format("ExpandableNode.defaultLabel", Integer.valueOf(VIEWER_LIMIT + 1),
				Integer.valueOf(VIEWER_LIMIT + VIEWER_LIMIT), Integer.valueOf(rootModel.size()));
		assertEquals("Expandable node has an incorrect text", expectedLabel, tableItem.getText());
	}

	public void testSetInput() {
		List<DataModel> rootModel = new ArrayList<>();
		DataModel rootLevel = new DataModel(Integer.valueOf(100));
		rootModel.add(rootLevel);
		tableViewer.setInput(rootModel);
		processEvents();
		assertEquals("there must be only one item", 1, tableViewer.getTable().getItems().length);
		tableViewer.setInput(createModel());
		processEvents();
		assertLimitedItems(tableViewer.getTable().getItems());
	}

	public void testBoundaryConditions() {
		List<DataModel> dummy = new ArrayList<>();
		dummy.add(new DataModel(Integer.valueOf(100)));
		tableViewer.setInput(dummy);
		int numOfEle = 7;
		int limit = 2;
		tableViewer.setDisplayIncrementally(limit);
		processEvents();
		List<DataModel> newInput = new ArrayList<>();
		for (int i = 0; i < numOfEle; i++) {
			DataModel rootLevel = new DataModel(Integer.valueOf(i));
			newInput.add(rootLevel);
		}
		tableViewer.setInput(newInput);
		processEvents();
		assertEquals("visible items length should be " + (limit + 1), limit + 1,
				tableViewer.getTable().getItems().length);

		tableViewer.setDisplayIncrementally(limit = 4);
		tableViewer.refresh();
		processEvents();
		assertEquals("visible items length should be " + (limit + 1), limit + 1,
				tableViewer.getTable().getItems().length);

		tableViewer.setDisplayIncrementally(limit = 6);
		tableViewer.refresh();
		processEvents();
		assertEquals("visible items length should be " + (limit + 1), limit + 1,
				tableViewer.getTable().getItems().length);

		tableViewer.setDisplayIncrementally(limit = 8);
		tableViewer.refresh();
		processEvents();
		assertEquals("visible items length should be " + newInput.size(), newInput.size(),
				tableViewer.getTable().getItems().length);

	}

	private static class TestContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return ((ArrayList<?>) inputElement).toArray();
		}
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		tableViewer = new TestTableViewer(parent);
		tableViewer.setDisplayIncrementally(VIEWER_LIMIT);
		tableViewer.setLabelProvider(new LabelProvider());
		tableViewer.setContentProvider(new TestContentProvider());
		tableViewer.setComparator(new TestComparator());
		return tableViewer;
	}

	@Override
	protected void setInput() {
		rootModel = createModel();
		fViewer.setInput(rootModel);
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run(TableViewerWithLimitTest.class);
	}

	private static void clickTableItem(Control viewerControl, TableItem item) {
		Rectangle bounds = item.getBounds();
		Event event = new Event();
		event.x = bounds.x + 5;
		event.y = bounds.y + 5;
		viewerControl.notifyListeners(SWT.MouseDown, event);
	}

	class TestTableViewer extends TableViewer {
		public TestTableViewer(Composite parent) {
			super(parent);
		}

		@Override
		public boolean isExpandableNode(Object element) {
			return super.isExpandableNode(element);
		}
	}

}
