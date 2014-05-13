/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Dinko Ivanov - bug 164365
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Editor Activation on DoubleClick instead of single.
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet021CellEditorsOnDoubleClick {
	private class MyCellModifier implements ICellModifier {

		private TableViewer viewer;

		private boolean enabled;

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}


		public void setViewer(TableViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public boolean canModify(Object element, String property) {
			return enabled && ((MyModel) element).counter % 2 == 0;
		}

		@Override
		public Object getValue(Object element, String property) {
			return ((MyModel) element).counter + "";
		}

		@Override
		public void modify(Object element, String property, Object value) {
			TableItem item = (TableItem) element;
			((MyModel) item.getData()).counter = Integer.parseInt(value
					.toString());
			viewer.update(item.getData(), null);
		}
	}

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

	public Snippet021CellEditorsOnDoubleClick(Shell shell) {
		final Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		final MyCellModifier modifier = new MyCellModifier();

		table.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				modifier.setEnabled(false);
			}

		});

		final TableViewer v = new TableViewer(table);

		table.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				modifier.setEnabled(true);
				TableItem[] selection = table.getSelection();

				if (selection.length != 1) {
					return;
				}

				TableItem item = table.getSelection()[0];

				for (int i = 0; i < table.getColumnCount(); i++) {
					if (item.getBounds(i).contains(event.x, event.y)) {
						v.editElement(item.getData(), i);
						modifier.setEnabled(false);
						break;
					}
				}
			}

		});

		modifier.setViewer(v);

		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setWidth(200);

		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance());
		v.setCellModifier(modifier);
		v.setColumnProperties(new String[] { "column1" });
		v.setCellEditors(new CellEditor[] { new TextCellEditor(v.getTable()) });

		v.setInput(createModel());
		v.getTable().setLinesVisible(true);
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<MyModel>();

		for (int i = 0; i < 10; i++) {
			elements.add(new MyModel(i));
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
		new Snippet021CellEditorsOnDoubleClick(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
