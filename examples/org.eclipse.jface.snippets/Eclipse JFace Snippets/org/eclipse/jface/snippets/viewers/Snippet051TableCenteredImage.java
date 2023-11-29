/*******************************************************************************
 * Copyright (c) 2006, 2016 Tom Schindl and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442343
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 510301
 *     Wim Jongman <wim.jongman@remainsoftware.com> - Added a cool barcode
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.Random;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

/**
 * Example showing how to centre an image and use graphics using an owner draw
 * label provider
 */
public class Snippet051TableCenteredImage {
	private static Image[] images;

	private abstract static class CenterImageLabelProvider extends OwnerDrawLabelProvider {

		@Override
		protected void measure(Event event, Object element) {
		}

		@Override
		protected void paint(Event event, Object element) {

			Image img = getImage(element);

			if (img != null) {
				Rectangle bounds = ((TableItem) event.item).getBounds(event.index);
				Rectangle imgBounds = img.getBounds();
				bounds.width /= 2;
				bounds.width -= imgBounds.width / 2;
				bounds.height /= 2;
				bounds.height -= imgBounds.height / 2;

				int x = bounds.width > 0 ? bounds.x + bounds.width : bounds.x;
				int y = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;

				event.gc.drawImage(img, x, y);
			} else {
				Rectangle bounds = ((TableItem) event.item).getBounds(event.index);
				for (int i = 0; i < bounds.width; i += 4) {
					int width = new Random().nextInt(4) + 1;
					event.gc.setForeground(event.display.getSystemColor(SWT.COLOR_BLACK));
					event.gc.setLineWidth(width);
					event.gc.drawLine(bounds.x + i, bounds.y, bounds.x + i, bounds.y + bounds.height);
				}
			}
		}

		protected abstract Image getImage(Object element);
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

	private static Image createImage(Display display, int red, int green, int blue) {
		Color color = new Color(display, red, green, blue);
		Image image = new Image(display, 10, 10);
		GC gc = new GC(image);
		gc.setBackground(color);
		gc.fillRectangle(0, 0, 10, 10);
		gc.dispose();

		return image;
	}

	public Snippet051TableCenteredImage(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.BORDER | SWT.FULL_SELECTION);
		v.setContentProvider(ArrayContentProvider.getInstance());

		TableViewerColumn column = new TableViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Column 1");
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return "Column 1 => " + element + ((((MyModel) element).counter % 2 != 0) ? " barcode" : "");
			}
		});

		column = new TableViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Column 2");
		column.setLabelProvider(new CenterImageLabelProvider() {

			@Override
			protected Image getImage(Object element) {
				if (((MyModel) element).counter % 2 == 0) {
					return images[((MyModel) element).counter / 2 % 4];
				}
				return null;
			}

		});

		// OwnerDrawLabelProvider.setUpOwnerDraw(v);

		MyModel[] model = createModel();
		v.setInput(model);
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

	public static void main(String[] args) {
		Display display = new Display();

		images = new Image[4];
		images[0] = createImage(display, 0, 0, 255);
		images[1] = createImage(display, 0, 255, 255);
		images[2] = createImage(display, 0, 255, 0);
		images[3] = createImage(display, 255, 0, 255);

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet051TableCenteredImage(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		for (Image image : images) {
			image.dispose();
		}

		display.dispose();

	}

}