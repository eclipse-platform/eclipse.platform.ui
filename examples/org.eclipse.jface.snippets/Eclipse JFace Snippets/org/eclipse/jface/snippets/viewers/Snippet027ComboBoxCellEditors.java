/*******************************************************************************
 * Copyright (c) 2006, 2015 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Dinko Ivanov - bug 164365
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 448143
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 475361, 489234
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This snippet represents usage of the ComboBoxCell-Editor
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet027ComboBoxCellEditors {
	public class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	public Snippet027ComboBoxCellEditors(Shell shell) {
		final Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		final TableViewer v = new TableViewer(table);
		v.setContentProvider(ArrayContentProvider.getInstance());

		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setWidth(200);
		TableViewerColumn viewerColumn = new TableViewerColumn(v, column);
		viewerColumn.setLabelProvider(new ColumnLabelProvider());
		viewerColumn.setEditingSupport(new EditingSupport(v) {

			@Override
			protected void setValue(Object element, Object value) {
				((MyModel) element).counter = ((Integer) value).intValue() * 10;
				getViewer().update(element, null);
			}

			@Override
			protected Object getValue(Object element) {
				// We need to calculate back to the index
				return Integer.valueOf(((MyModel) element).counter / 10);
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor(
						v.getTable(), new String[] { "Zero", "Ten", "Twenty", "Thirty",
							"Fourty", "Fifty", "Sixty", "Seventy", "Eighty",
							"Ninety" });
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});

		v.setInput(createModel());
		v.getTable().setLinesVisible(true);
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			elements.add(new MyModel(i * 10));
		}
		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet027ComboBoxCellEditors(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
