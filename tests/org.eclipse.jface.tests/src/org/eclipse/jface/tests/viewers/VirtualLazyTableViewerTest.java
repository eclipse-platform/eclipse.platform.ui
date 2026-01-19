/*******************************************************************************
 * Copyright (c) 2005, 2023 IBM Corporation and others.
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
 *     Tom Schindl <tom.schindl@bestsolution.at> - bug 170381
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * The VirtualLazyTableViewerTest is a test of table viewers with lazy
 * population.
 */
public class VirtualLazyTableViewerTest extends VirtualTableViewerTest {

	private List<Integer> updatedElements;
	// by default, no failure is triggered when updateElement is called
	int updatedElementFailureTriggerIndex = -1;

	@Override
	protected TestModelContentProvider getContentProvider() {
		return new TestLazyModelContentProvider(this);
	}

	@BeforeEach
	@Override
	public void setUp() {
		updatedElements = new ArrayList<>();
		super.setUp();
	}

	@Override
	protected void setUpModel() {
		fRootElement = TestElement.createModel(2, 100);
		fModel = fRootElement.getModel();
	}

	@AfterEach
	@Override
	public void tearDown() {
		super.tearDown();
		updatedElements = null;
	}

	// this method is called from TestLazyModelContentProvider
	public void updateElementCalled(int index) {
		updatedElements.add(Integer.valueOf(index));
		if (updatedElementFailureTriggerIndex != -1 && updatedElements.size() >= updatedElementFailureTriggerIndex) {
			fail("unexpected call to updateElement, this is the " + updatedElements.size() + "th call");
		}
	}

	/**
	 * Test selecting all elements.
	 */
	@Test
	public void testSetIndexedSelection() {
		TestElement[] children = fRootElement.getChildren();
		int selectionSize = children.length / 2;
		int[] indices = new int[selectionSize];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = i * 2;
		}

		Table table = ((TableViewer) fViewer).getTable();
		table.setSelection(indices);

		// we are virtual, so not all indices we requested to select will be selected.
		indices = table.getSelectionIndices();
		selectionSize = indices.length;
		assertTrue(selectionSize > 0, "Expected at least one selected element");

		table.showSelection();

		IStructuredSelection result = fViewer.getStructuredSelection();
		assertEquals(selectionSize, result.size());
		assertEquals(children[indices[0]], result.getFirstElement(), "First elements do not match ");
		int lastIndex = indices[indices.length - 1];
		assertEquals(children[lastIndex], result.toArray()[result.size() - 1], "Last elements do not match ");

	}

	@Test
	public void testSetInputDoesNotMaterializeEverything() {
		fViewer.setInput(null);
		updatedElements.clear();
		// Assume something is wrong if all TableItems are materialized:
		updatedElementFailureTriggerIndex = fRootElement.getChildCount();
		fViewer.setInput(fRootElement);

		int materializedSize = updatedElements.size();
		assertTrue(materializedSize < fRootElement.getChildCount(), "Expected less than " + fRootElement.getChildCount() + ", actual " + materializedSize);
		// create a new model and check if we get an equal number of calls to
		// updateElement
		setUpModel();
		updatedElements.clear();
		fViewer.setInput(fRootElement);
		assertEquals(materializedSize, updatedElements.size());
	}

	@Test
	public void testBug160153() {
		int childCount = fRootElement.getChildCount();
		TestElement lastChild = fRootElement.getChildAt(childCount - 1);
		// materialize last child
		fViewer.setSelection(new StructuredSelection(lastChild));
		processEvents();
		assertNotNull(fViewer.testFindItem(lastChild), "last Child should be in the map");
		((TableViewer) fViewer).setItemCount(childCount - 1);
		assertNull(fViewer.testFindItem(lastChild), "last Child should no longer be in the map");
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testSorter() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testRenameWithSorter() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testSetFilters() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testFilter() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testRenameWithFilter() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testContains() {
	}
}
