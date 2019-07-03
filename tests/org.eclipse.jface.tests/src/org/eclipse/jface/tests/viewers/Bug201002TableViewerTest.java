/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
 *     Lucas Bullen (Red Hat Inc.) - Bug 497767
 ******************************************************************************/

package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertNotEquals;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

/**
 * @since 3.3
 *
 */
public class Bug201002TableViewerTest extends ViewerTestCase {

	/**
	 * @param name
	 */
	public Bug201002TableViewerTest(String name) {
		super(name);
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		final TableViewer tableViewer = new TableViewer(parent, SWT.FULL_SELECTION);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setCellEditors(new CellEditor[] { new TextCellEditor(
				tableViewer.getTable()) });
		tableViewer.setColumnProperties(new String[] { "0" });
		tableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(Object element, String property) {
				return true;
			}

			@Override
			public Object getValue(Object element, String property) {
				return "";
			}

			@Override
			public void modify(Object element, String property, Object value) {
			}

		});

		new TableColumn(tableViewer.getTable(), SWT.NONE).setWidth(200);

		return tableViewer;
	}

	@Override
	protected void setUpModel() {
		// don't do anything here - we are not using the normal fModel and
		// fRootElement
	}

	@Override
	protected void setInput() {
		String[] ar = new String[100];
		for( int i = 0; i < ar.length; i++ ) {
			ar[i] = i + "";
		}
		getTableViewer().setInput(ar);
	}

	private TableViewer getTableViewer() {
		return (TableViewer) fViewer;
	}

	public void testBug201002() {
		getTableViewer().getTable().setTopIndex(0);
		waitForTopIndexUpdate(true);
		getTableViewer().editElement(getTableViewer().getElementAt(90), 0);
		waitForTopIndexUpdate(false);
		int topIndex = getTableViewer().getTable().getTopIndex();
		assertNotEquals("TableViewer top index shouldn't be 0", 0, topIndex);
	}

	private void waitForTopIndexUpdate(boolean isTopZero) {
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				while (getTableViewer().getTable().getDisplay().readAndDispatch()) {
				}
				return isTopZero == (getTableViewer().getTable().getTopIndex() == 0);
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000);
	}
}
