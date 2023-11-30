/*******************************************************************************
 * Copyright (c) 2006, 2018 Tom Schindl and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565, 489234
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442343
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Example usage of none mandatory interfaces of ITableFontProvider and
 * ITableColorProvider
 *
 * @since 3.2
 */
public class Snippet017TableViewerHideShowColumns {
	private static class ShrinkThread extends Thread {
		private int width = 0;
		private final TableColumn column;

		public ShrinkThread(int width, TableColumn column) {
			super();
			this.width = width;
			this.column = column;
		}

		@Override
		public void run() {
			column.getDisplay().syncExec(() -> column.setData("restoredWidth", Integer.valueOf(width)));

			for( int i = width; i >= 0; i-- ) {
				final int index = i;
				column.getDisplay().syncExec(() -> column.setWidth(index));
			}
		}
	}

	private static class ExpandThread extends Thread {
		private int width = 0;
		private final TableColumn column;

		public ExpandThread(int width, TableColumn column) {
			super();
			this.width = width;
			this.column = column;
		}

		@Override
		public void run() {
			for( int i = 0; i <= width; i++ ) {
				final int index = i;
				column.getDisplay().syncExec(() -> column.setWidth(index));
			}
		}
	}

	public static class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	public static class MyLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return "Column " + columnIndex + " => " + element;
		}
	}

	public Snippet017TableViewerHideShowColumns(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.setLabelProvider(new MyLabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance());

		TableColumn column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 1");

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 2");

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 3");

		MyModel[] model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
		addMenu(v);
	}

	private void addMenu(TableViewer v) {
		final MenuManager mgr = new MenuManager();
		Action action;

		for( int i = 0; i < v.getTable().getColumnCount(); i++ ) {
			final TableColumn column = v.getTable().getColumn(i);

			action = new Action(v.getTable().getColumn(i).getText(),SWT.CHECK) {
				@Override
				public void runWithEvent(Event event) {
					if( ! isChecked() ) {
						ShrinkThread t = new ShrinkThread(column.getWidth(),column);
						t.run();
					} else {
						ExpandThread t = new ExpandThread(((Integer)column.getData("restoredWidth")).intValue(),column);
						t.run();
					}
				}

			};
			action.setChecked(true);
			mgr.add(action);
		}

		v.getControl().setMenu(mgr.createContextMenu(v.getControl()));
	}



	private MyModel[] createModel() {
		MyModel[] elements = new MyModel[10];

		for (int i = 0; i < 10; i++) {
			elements[i] = new MyModel(i);
		}

		return elements;
	}

	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet017TableViewerHideShowColumns(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
