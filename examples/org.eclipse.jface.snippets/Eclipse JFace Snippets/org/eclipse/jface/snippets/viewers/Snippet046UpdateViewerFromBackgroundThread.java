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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442343
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 510301
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.Random;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Example how to update a viewer from a long running task
 *
 * @author Tom Schindl &lt;tom.schindl@bestsolution.at&gt;
 */
public class Snippet046UpdateViewerFromBackgroundThread {
	private static Image[] images;

	public static class MyModel {
		public int counter;

		public boolean finished;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	public static class MyLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return images[((MyModel) element).finished ? 0 : 1];
			}

			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return "Column " + columnIndex + " => " + element;
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

	public Snippet046UpdateViewerFromBackgroundThread(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.BORDER | SWT.FULL_SELECTION);
		v.setLabelProvider(new MyLabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance());

		TableColumn column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 1");

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 2");

		final MyModel[] model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);

		Button b = new Button(shell, SWT.PUSH);
		b.setText("Start Long Task");
		b.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				b.setText("Update running..");
				b.setEnabled(false);
				final Thread t = new Thread() {

					@Override
					public void run() {
						for (int i = 0; i < model.length; i++) {
							if (v.getTable().isDisposed()) {
								return;
							}
							final int j = i;
							v.getTable().getDisplay().asyncExec(() -> {
								if (model[j].finished) {
									model[j].finished = false;
								} else {
									model[j].finished = true;
								}
								v.update(model[j], null);
							});
							try {
								Thread.sleep(new Random().nextInt(1000) + 100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						v.getTable().getDisplay().asyncExec(() -> {
							b.setText("Start Long Task");
							b.setEnabled(true);
						});
					}
				};
				t.start();
			}
		});
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

		images = new Image[2];
		images[0] = createImage(display, 0, 255, 0);
		images[1] = createImage(display, 255, 0, 0);

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet046UpdateViewerFromBackgroundThread(shell);
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