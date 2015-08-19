/*******************************************************************************
 * Copyright (c) 2007, 2015 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *     Wayne Beaton - bug 185540
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 413427, 475361
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
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
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Snippet to present editor different CellEditors within one column in 3.2
 * for 3.3 and above please use the new EditingSupport class
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet034CellEditorPerRow {
	private class MyEditingSupport extends EditingSupport {
		private CellEditor textEditor;

		private CellEditor dropDownEditor;

		public MyEditingSupport(TableViewer viewer) {
			super(viewer);
			textEditor = new TextCellEditor(viewer.getTable());

			String[] elements = new String[10];

			for (int i = 0; i < 10; i++) {
				elements[i] = i+"";
			}

			dropDownEditor = new ComboBoxCellEditor(viewer.getTable(),elements);
		}

		@Override
		protected boolean canEdit(Object element) {
			return ((MyModel) element).counter % 2 == 0;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			if( element instanceof MyModel2 ) {
				return dropDownEditor;
			} else {
				return textEditor;
			}
		}

		@Override
		protected Object getValue(Object element) {
			if( element instanceof MyModel2 ) {
				return new Integer(((MyModel) element).counter);
			} else {
				return ((MyModel) element).counter + "";
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			((MyModel)element).counter = Integer.parseInt(value.toString());
			getViewer().update(element, null);
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

	public class MyModel2 extends MyModel {

		public MyModel2(int counter) {
			super(counter);
		}

		@Override
		public String toString() {
			return "Special Item " + this.counter;
		}
	}

	public Snippet034CellEditorPerRow(Shell shell) {
		final Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);

		final TableViewer v = new TableViewer(table);
		v.getTable().setLinesVisible(true);

		TableViewerColumn column = new TableViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return element.toString();
			}

		});

		column.setEditingSupport(new MyEditingSupport(v));
		v.setContentProvider(ArrayContentProvider.getInstance());
		v.setInput(createModel());
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			elements.add(new MyModel(i));
		}
		for (int i = 0; i < 10; i++) {
			elements.add(new MyModel2(i));
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
		new Snippet034CellEditorPerRow(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
