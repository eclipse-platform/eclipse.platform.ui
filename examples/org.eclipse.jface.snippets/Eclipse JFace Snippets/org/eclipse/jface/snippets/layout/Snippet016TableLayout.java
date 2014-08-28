/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442343
 *******************************************************************************/

package org.eclipse.jface.snippets.layout;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A simple TableViewer to demonstrate usage of the {@link TableColumnLayout}.
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 */
public class Snippet016TableLayout {

	private class MyLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return columnIndex + " - " + element;
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

	public Snippet016TableLayout(Composite comp) {
		final TableViewer v = new TableViewer(new Table(comp, SWT.BORDER));
		v.setLabelProvider(new MyLabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance());
		v.getTable().setHeaderVisible(true);

		TableColumnLayout ad = new TableColumnLayout();
		comp.setLayout(ad);

		TableColumn column1 = createTableColumn(v.getTable(), "Column 1");
		TableColumn column2 = createTableColumn(v.getTable(), "Column 2");

		ad.setColumnData(column1, new ColumnWeightData(90, 290));
		ad.setColumnData(column2, new ColumnWeightData(10, 200));

		MyModel[] model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
	}

	private TableColumn createTableColumn(Table table, String textColumn) {
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(textColumn);
		column.setMoveable(true);
		return column;
	}

	private MyModel[] createModel() {
		MyModel[] elements = new MyModel[10];

		for (int i = 0; i < 10; i++) {
			elements[i] = new MyModel(i);
		}

		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		// shell.setSize(400, 150);
		shell.setLayout(new FillLayout());

		new Snippet016TableLayout(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
