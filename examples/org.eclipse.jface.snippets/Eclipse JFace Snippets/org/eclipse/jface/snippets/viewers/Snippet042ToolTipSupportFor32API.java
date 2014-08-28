/*******************************************************************************
 * Copyright (c) 2007, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel (lars.vogel@gmail.com) - Bug 413427
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442343
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Example usage of ToolTips with the OLD viewer API but similar to
 * {@link ColumnViewerToolTipSupport}
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet042ToolTipSupportFor32API {
	private static Image[] images;

	private static class Cell {
		private Item item;
		private int index;

		public Cell(Item item, int index) {
			this.item = item;
			this.index = index;
		}

		public Object getData() {
			return item.getData();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
			result = prime * result + ((item == null) ? 0 : item.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Cell other = (Cell) obj;
			if (index != other.index)
				return false;
			if (item == null) {
				if (other.item != null)
					return false;
			} else if (!item.equals(other.item))
				return false;
			return true;
		}

	}

	private static class ToolTipSupport extends DefaultToolTip {
		private Cell cell;
		private ColumnViewer viewer;

		protected ToolTipSupport(ColumnViewer viewer, int style,
				boolean manualActivation) {
			super(viewer.getControl(), style, manualActivation);
			this.viewer = viewer;
		}

		@Override
		protected Object getToolTipArea(Event event) {
			Table table = (Table) event.widget;
			int columns = table.getColumnCount();
			Point point = new Point(event.x, event.y);
			TableItem item = table.getItem(point);

			if (item != null) {
				for (int i = 0; i < columns; i++) {
					if (item.getBounds(i).contains(point)) {
						this.cell = new Cell(item, i);
						return cell;
					}
				}
			}

			return null;
		}

		@Override
		protected Composite createToolTipContentArea(Event event,
				Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			comp.setLayout(new FillLayout());
			Button b = new Button(comp, SWT.PUSH);
			b.setText(((ITableLabelProvider) viewer.getLabelProvider())
					.getColumnText(cell.getData(), cell.index));
			b.setImage(((ITableLabelProvider) viewer.getLabelProvider())
					.getColumnImage(cell.getData(), cell.index));

			return comp;
		}

		public static void enableFor(ColumnViewer viewer) {
			new ToolTipSupport(viewer, ToolTip.NO_RECREATE, false);
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

	public class MyLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 1) {
				return images[((MyModel) element).counter % 4];
			}

			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return "Column " + columnIndex + " => " + element.toString();
		}

	}

	private static Image createImage(Display display, int red, int green,
			int blue) {
		Color color = new Color(display, red, green, blue);
		Image image = new Image(display, 10, 10);
		GC gc = new GC(image);
		gc.setBackground(color);
		gc.fillRectangle(0, 0, 10, 10);
		gc.dispose();

		return image;
	}

	public Snippet042ToolTipSupportFor32API(Shell shell) {
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

		MyModel[] model = createModel();
		v.setInput(model);
		ToolTipSupport.enableFor(v);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
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

		images = new Image[4];
		images[0] = createImage(display, 0, 0, 255);
		images[1] = createImage(display, 0, 255, 255);
		images[2] = createImage(display, 0, 255, 0);
		images[3] = createImage(display, 255, 0, 255);

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet042ToolTipSupportFor32API(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		for (int i = 0; i < images.length; i++) {
			images[i].dispose();
		}

		display.dispose();

	}

}
