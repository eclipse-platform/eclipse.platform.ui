/*******************************************************************************
 * Copyright (c) 2009, 2017 CAS Software AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Roeck, CAS Software AG - initial API and implementation (bug 256889)
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.junit.Before;
import org.junit.Test;

public class Bug256889TableViewerTest extends ViewerTestCase {

	private static final int ADD_ENTRIES = 100;
	private static final int PREFETCH_TRESHOLD = 50;
	private static final int MAX_ENTRIES = 205;

	private int rowcounter = 0;

	private final List<String> model = new ArrayList<>();
	private Table table;
	private TableViewer tableViewer;

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.VIRTUAL | SWT.BORDER
				| SWT.MULTI);
		tableViewer.setContentProvider(new ILazyContentProvider() {

			@Override
			public void updateElement(int index) {
				if (index >= 0 && index < tableViewer.getTable().getItemCount()) {
					if (index > getModel().size() - PREFETCH_TRESHOLD
							&& (getModel().size() < MAX_ENTRIES)) {
						// simulate loading the next page of data from db
						int approxRecordCount = addElementsToModel();

						System.out.println("approx. record count: "
								+ approxRecordCount);
						tableViewer.setItemCount(approxRecordCount);
					}
					if (index < getModel().size()) {
						tableViewer.replace(getModel().get(index), index);
					} else {
						System.out.println("invalid index " + index
								+ " model count " + getModel().size());
					}
				} else {
					System.out.println("invalid index " + index
							+ " tableItemCount "
							+ tableViewer.getTable().getItemCount());
				}
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			}
		});

		String[] columnProperties = new String[] { "Spalte 1",
				"Virtual Tables rock" };
		tableViewer.setColumnProperties(columnProperties);

		table = tableViewer.getTable();

		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(columnProperties[0]);
		col.setWidth(200);

		col = new TableColumn(table, SWT.NONE);
		col.setText(columnProperties[1]);
		col.setWidth(400);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer.setItemCount(getModel().size());

		return tableViewer;
	}

	@Override
	protected void setInput() {
		tableViewer.setInput(getModel());
		tableViewer.setItemCount(getModel().size());

		// The heigt is important, otherwise lazy fetching doesn't lead to the
		// bug
		fShell.setSize(300, 1000);
	}

	@Before
	public void initModel() {
		this.rowcounter = 0;
		getModel().clear();
		addElementsToModel();
	}

	// Methods returns an approximate record count which is always
	// one page size larger than the actually fetched records, until
	// all records have been fetched and the end of list has been reached.
	protected int addElementsToModel() {
		int approxRecordCount = 0;
		int itemsToAdd;

		if (getModel().size() + ADD_ENTRIES < MAX_ENTRIES) {
			itemsToAdd = ADD_ENTRIES;
		} else {
			itemsToAdd = MAX_ENTRIES - getModel().size();
		}

		for (int i = 0; i < itemsToAdd; i++) {
			getModel().add("Item " + this.rowcounter++);
		}

		if (getModel().size() == MAX_ENTRIES) {
			approxRecordCount = MAX_ENTRIES;
		} else {
			approxRecordCount = getModel().size() + ADD_ENTRIES;
		}

		return approxRecordCount;
	}

	private List<String> getModel() {
		return this.model;
	}

	@Test
	public void testBug256889() {
		table.selectAll();
		tableViewer.getStructuredSelection();

	}

}