/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl - bug 151205, 170381
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 481490
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * The TableViewerTest is a test of the SWT#VIRTUAL support in TableViewers,
 */
public class VirtualTableViewerTest extends TableViewerTest {

	Set visibleItems = new HashSet();

	/**
	 * Checks if the virtual tree / table functionality can be tested in the current settings.
	 * The virtual trees and tables rely on SWT.SetData event which is only sent if OS requests
	 * information about the tree / table. If the window is not visible (obscured by another window,
	 * outside of visible area, or OS determined that it can skip drawing), then OS request won't
	 * be send, causing automated tests to fail.
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=118919 .
	 */
	protected boolean setDataCalled = false;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param name
	 */
	public VirtualTableViewerTest(String name) {
		super(name);
	}

	@Override
	public void setUp() {
		super.setUp();
		processEvents(); // run events for SetData precondition test
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.ON_TOP;
	}

	@Override
	protected TableViewer createTableViewer(Composite parent) {
		visibleItems = new HashSet();
		TableViewer viewer = new TableViewer(parent, SWT.VIRTUAL | SWT.MULTI);
		viewer.setUseHashlookup(true);
		final Table table = viewer.getTable();
		table.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setDataCalled = true;
				TableItem item = (TableItem) event.item;
				visibleItems.add(item);
			}
		});
		return viewer;
	}

	/**
	 * Checks if update occurred. Updates for virtual items will be skipped
	 * if, for instance, another window is in the foreground.
	 * @return <code>true</code> if update occurred
	 */
	protected boolean updateTable() {
		setDataCalled = false;
		((TableViewer) fViewer).getControl().update();
		if (setDataCalled) {
			return true;
		}
		System.err.println("SWT.SetData is not received. Cancelled test " + getName());
		return false;
	}

	/**
	 * Get the collection of currently visible table items.
	 *
	 * @return TableItem[]
	 */
	private TableItem[] getVisibleItems() {
		return (TableItem[]) visibleItems.toArray(new TableItem[visibleItems.size()]);
	}

	public void testElementsCreated() {

		TableItem[] items = getVisibleItems();

		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			assertTrue("Missing data in item " + String.valueOf(i) + " of " + items.length, item
					.getData() instanceof TestElement);
		}
	}

	@Override
	protected int getItemCount() {
		return getVisibleItems().length;
	}

	@Override
	public void testFilter() {
		ViewerFilter filter = new TestLabelFilter();
		visibleItems = new HashSet();
		fViewer.addFilter(filter);
		if (!updateTable()) {
			return;
		}
		assertEquals("filtered count", 5, getItemCount());

		visibleItems = new HashSet();
		fViewer.removeFilter(filter);
		if (!updateTable()) {
			return;
		}
		assertEquals("unfiltered count", 10, getItemCount());
	}

	@Override
	public void testSetFilters() {
		ViewerFilter filter = new TestLabelFilter();
		visibleItems = new HashSet();
		fViewer.setFilters(filter, new TestLabelFilter2());
		if (!updateTable()) {
			return;
		}
		assertEquals("2 filters count",1, getItemCount());

		visibleItems = new HashSet();
		fViewer.setFilters(filter);
		if (!updateTable()) {
			return;
		}
		assertEquals("1 filtered count",5, getItemCount());

		visibleItems = new HashSet();
		fViewer.setFilters();
		if (!updateTable()) {
			return;
		}
		assertEquals("unfiltered count",10, getItemCount());
	}

	@Override
	public void testInsertSibling() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}

	@Override
	public void testInsertSiblingReveal() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}

	@Override
	public void testInsertSiblings() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}

	@Override
	public void testInsertSiblingWithFilterFiltered() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}

	@Override
	public void testInsertSiblingWithFilterNotFiltered() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}

	@Override
	public void testInsertSiblingWithSorter() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}

	@Override
	public void testRenameWithFilter() {
		if (!setDataCalled) {
			System.err.println("SWT.SetData is not received. Cancelled test " + getName());
			return;
		}
		fViewer.addFilter(new TestLabelFilter());
		if (!updateTable()) {
			return;
		}
        TestElement first = fRootElement.getFirstChild();
        first.setLabel("name-1111"); // should disappear
        ((TableViewer) fViewer).getControl().update();
        assertNull("changed sibling is not visible", fViewer
                .testFindItem(first));
        first.setLabel("name-2222"); // should reappear
        fViewer.refresh();
        ((TableViewer) fViewer).getControl().update();
        assertNotNull("changed sibling is not visible", fViewer
                .testFindItem(first));
	}

	@Override
	public void testSetInput() {
		// This test us based on findItem assuming all items
		// are created so it is not valid.
	}

	@Override
	public void testRenameWithSorter() {
		// Call update to make sure the viewer is in a correct state
		// At least on MacOSX I get failures without this call
		((TableViewer) fViewer).getControl().update();
		fViewer.setSorter(new TestLabelSorter());
		TestElement first = fRootElement.getFirstChild();
		first.setLabel("name-9999");
		String newElementLabel = first.toString();
		((TableViewer) fViewer).getControl().update();
		assertEquals("sorted first", newElementLabel, getItemText(0));
	}

	@Override
	public void testSorter() {
		TestElement first = fRootElement.getFirstChild();
		TestElement last = fRootElement.getLastChild();

		String firstLabel = first.toString();
		String lastLabel = last.toString();

		((TableViewer) fViewer).getControl().update();
		assertEquals("unsorted", firstLabel, getItemText(0));
		fViewer.setSorter(new TestLabelSorter());

		((TableViewer) fViewer).getControl().update();
		assertEquals("reverse sorted", lastLabel, getItemText(0));

		fViewer.setSorter(null);
		((TableViewer) fViewer).getControl().update();
		assertEquals("unsorted", firstLabel, getItemText(0));
	}

	@Override
	public void testInsertSiblingSelectExpanded() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}

	@Override
	public void testSomeChildrenChanged() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}

	@Override
	public void testWorldChanged() {
		// This test is no use here as it is
		// based on the assumption that all items
		// are created.
	}

	@Override
	public void testDeleteSibling() {
		//Force creation of the item first
		((TableViewer) fViewer).getTable().getItem(0).getText();
		super.testDeleteSibling();
	}

	@Override
	public void testSetSelection() {
		//Force creation of the item first
		((TableViewer) fViewer).getTable().getItem(0).getText();
		super.testSetSelection();
	}

	/**
	 * Test selecting all elements.
	 */
	public void testSetAllSelection() {
		TestElement[] children = fRootElement.getChildren();
		StructuredSelection selection = new StructuredSelection(children);
		fViewer.setSelection(selection);
		IStructuredSelection result = (IStructuredSelection) fViewer
				.getSelection();
		assertTrue("Size was " + String.valueOf(result.size()) + " expected "
				+ String.valueOf(children.length),
				(result.size() == children.length));
		Set childrenSet = new HashSet(Arrays.asList(children));
		Set selectedSet = new HashSet(result.toList());
		assertTrue("Elements do not match ", childrenSet.equals(selectedSet));
	}
}
