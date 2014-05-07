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
 *******************************************************************************/

package org.eclipse.jface.snippets.window;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Example how one can create a tooltip which is not recreated for every table
 * cell
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet031TableStaticTooltip {
	private static Image[] images;

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

	public Snippet031TableStaticTooltip(Shell shell) {
		final TableViewer viewer = new TableViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);

		viewer.setLabelProvider(new MyLabelProvider());
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		createColumnFor(viewer, "Column 1");
		createColumnFor(viewer, "Column 2");

		viewer.setInput(createModel());
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		createToolTipFor(viewer);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		images = new Image[] { createImage(display, 0, 0, 255),
				createImage(display, 0, 255, 255),
				createImage(display, 0, 255, 0),
				createImage(display, 255, 0, 255) };

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet031TableStaticTooltip(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		for (Image img : images) {
			img.dispose();
		}
		display.dispose();

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

	private void createToolTipFor(final TableViewer viewer) {
		DefaultToolTip toolTip = new DefaultToolTip(viewer.getControl(),
				ToolTip.NO_RECREATE, false);

		toolTip.setText("Hello World\nHello World");
		toolTip.setBackgroundColor(viewer.getTable().getDisplay()
				.getSystemColor(SWT.COLOR_RED));

		toolTip.setShift(new Point(10, 5));
	}

	private TableColumn createColumnFor(TableViewer viewer, String label) {
		TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText(label);
		return column;
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<MyModel>();

		for (int i = 0; i < 10; i++) {
			elements.add(new MyModel(i));
		}
		return elements;
	}

}
