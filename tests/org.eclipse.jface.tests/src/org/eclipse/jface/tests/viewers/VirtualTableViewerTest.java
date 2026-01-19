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
 *     Tom Schindl - bug 151205, 170381
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 481490
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * The TableViewerTest is a test of the SWT#VIRTUAL support in TableViewers,
 */
public class VirtualTableViewerTest extends TableViewerTest {

	private static final Duration TABLE_DATA_UPDATE_TIMEOUT = Duration.ofSeconds(5);

	private Set<TableItem> visibleItems = new HashSet<>();

	/**
	 * Checks if the virtual tree / table functionality can be tested in the current
	 * settings. The virtual trees and tables rely on SWT.SetData event which is
	 * only sent if OS requests information about the tree / table. If the window is
	 * not visible (obscured by another window, outside of visible area, or OS
	 * determined that it can skip drawing), then OS request won't be send, causing
	 * automated tests to fail. See
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=118919 .
	 */
	protected volatile boolean setDataCalled = false;

	@BeforeEach
	@Override
	public void setUp() {
		super.setUp();
		waitForDataToBeSet();
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.ON_TOP;
	}

	@Override
	protected TableViewer createTableViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent, SWT.VIRTUAL | SWT.MULTI);
		viewer.setUseHashlookup(true);
		final Table table = viewer.getTable();
		table.addListener(SWT.SetData, event -> {
			setDataCalled = true;
			TableItem item = (TableItem) event.item;
			visibleItems.add(item);
		});
		return viewer;
	}

	/**
	 * Updates the table, also forcing the containing shell to be active in order to
	 * process according events.
	 */
	private void updateTable() {
		setDataCalled = false;
		fViewer.refresh();
		fViewer.getControl().update();
		waitForDataToBeSet();
	}

	private void waitForDataToBeSet() {
		Duration timeoutEnd = Duration.ofMillis(System.currentTimeMillis()).plus(TABLE_DATA_UPDATE_TIMEOUT);
		while (!setDataCalled && !timeoutEnd.minusMillis(System.currentTimeMillis()).isNegative()) {
			fShell.forceActive();
			processEvents();
		}
		assertTrue(setDataCalled, "waiting for setting table data timed out");
	}

	/**
	 * Get the collection of currently visible table items.
	 *
	 * @return TableItem[]
	 */
	private TableItem[] getVisibleItems() {
		return visibleItems.toArray(new TableItem[visibleItems.size()]);
	}

	@Test
	public void testElementsCreated() {

		TableItem[] items = getVisibleItems();

		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			assertTrue(item.getData() instanceof TestElement, "Missing data in item " + i + " of " + items.length);
		}
	}

	@Override
	protected int getItemCount() {
		return getVisibleItems().length;
	}

	@Test
	@Override
	public void testFilter() {
		ViewerFilter filter = new TestLabelFilter();
		visibleItems = new HashSet<>();
		fViewer.addFilter(filter);
		updateTable();
		assertEquals(5, getItemCount(), "filtered count");

		visibleItems = new HashSet<>();
		fViewer.removeFilter(filter);
		updateTable();
		assertEquals(10, getItemCount(), "unfiltered count");
	}

	@Test
	@Override
	public void testSetFilters() {
		ViewerFilter filter = new TestLabelFilter();
		visibleItems = new HashSet<>();
		fViewer.setFilters(filter, new TestLabelFilter2());
		updateTable();
		assertEquals(1, getItemCount(), "2 filters count");

		visibleItems = new HashSet<>();
		fViewer.setFilters(filter);
		updateTable();
		assertEquals(5, getItemCount(), "1 filtered count");

		visibleItems = new HashSet<>();
		fViewer.setFilters();
		updateTable();
		assertEquals(10, getItemCount(), "unfiltered count");
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testInsertSibling() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testInsertSiblingReveal() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testInsertSiblings() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testInsertSiblingWithFilterFiltered() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testInsertSiblingWithFilterNotFiltered() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testInsertSiblingWithSorter() {
	}

	@Test
	@Override
	public void testRenameWithFilter() {
		fViewer.addFilter(new TestLabelFilter());
		TestElement first = fRootElement.getFirstChild();
		first.setLabel("name-1111"); // should disappear
		updateTable();
		assertNull(fViewer.testFindItem(first), "changed sibling is still visible");
		first.setLabel("name-2222"); // should reappear
		updateTable();
		assertNotNull(fViewer.testFindItem(first), "changed sibling is not visible");
	}

	@Disabled("This test us based on findItem assuming all items are created so it is not valid.")
	@Override
	public void testSetInput() {
	}

	@Test
	@Override
	public void testRenameWithSorter() {
		fViewer.setComparator(new TestLabelComparator());
		TestElement first = fRootElement.getFirstChild();
		first.setLabel("name-9999");
		String newElementLabel = first.toString();
		assertEquals(newElementLabel, getItemText(0), "sorted first");
	}

	@Override
	public void testSorter() {
		TestElement first = fRootElement.getFirstChild();
		TestElement last = fRootElement.getLastChild();

		String firstLabel = first.toString();
		String lastLabel = last.toString();

		assertEquals(firstLabel, getItemText(0), "unsorted");
		fViewer.setComparator(new TestLabelComparator());

		assertEquals(lastLabel, getItemText(0), "reverse sorted");

		fViewer.setComparator(null);
		assertEquals(firstLabel, getItemText(0), "unsorted");
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testInsertSiblingSelectExpanded() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testSomeChildrenChanged() {
	}

	@Disabled("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testWorldChanged() {
	}

	@Test
	@Override
	public void testDeleteSibling() {
		// Force creation of the item first
		((TableViewer) fViewer).getTable().getItem(0).getText();
		super.testDeleteSibling();
	}

	@Test
	@Override
	public void testSetSelection() {
		// Force creation of the item first
		((TableViewer) fViewer).getTable().getItem(0).getText();
		super.testSetSelection();
	}

	/**
	 * Test selecting all elements.
	 */
	@Test
	public void testSetAllSelection() {
		TestElement[] children = fRootElement.getChildren();
		StructuredSelection selection = new StructuredSelection(children);
		fViewer.setSelection(selection);
		IStructuredSelection result = fViewer.getStructuredSelection();
		assertEquals(children.length, result.size(), "Size was " + result.size() + " expected " + children.length);
		Set<TestElement> childrenSet = new HashSet<>(Arrays.asList(children));
		@SuppressWarnings("unchecked")
		Set<?> selectedSet = new HashSet<Object>(result.toList());
		assertTrue(childrenSet.equals(selectedSet), "Elements do not match ");
	}
}
