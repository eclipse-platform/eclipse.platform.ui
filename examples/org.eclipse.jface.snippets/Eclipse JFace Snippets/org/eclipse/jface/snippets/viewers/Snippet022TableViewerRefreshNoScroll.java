/*******************************************************************************
 * Copyright (c) 2006, 2015 Tom Schindl and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 442278, 475361
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A simple TableViewer to demonstrating how viewers could be refresh and
 * scrolling avoided in 3.2. In 3.3 implementors should consider using the
 * {@link StructuredViewer#refresh(Object, boolean)} instead.
 */
public class Snippet022TableViewerRefreshNoScroll {

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

	public Snippet022TableViewerRefreshNoScroll(Shell shell) {
		shell.setLayout(new GridLayout(2, false));
		final TableViewer v = new TableViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);

		TableColumn column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 0");

		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance());
		v.setInput(createModel(100));
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
		v.getTable().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		Button b = new Button(shell, SWT.PUSH);
		b.setText("Refresh with Scrolling");
		b.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				v.refresh();
			}

		});

		b = new Button(shell, SWT.PUSH);
		b.setText("Refresh with NO-Scrolling");
		b.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				v.getTable().setTopIndex(0);
				IStructuredSelection selection = v.getStructuredSelection();
				v.getTable().deselectAll();
				v.refresh();
				if (!selection.isEmpty()) {
					int[] indices = new int[selection.size()];

					TableItem[] items = v.getTable().getItems();

					int counter = 0;
					for (Object modelElement : selection) {
						for (int i = 0; i < items.length; i++) {
							if (items[i].getData() == modelElement) {
								indices[counter++] = i;
							}
						}
					}

					if (counter < indices.length) {
						System.arraycopy(items, 0, indices = new int[counter],
								0, counter);
					}

					v.getTable().select(indices);
				}
			}

		});
	}

	private List<MyModel> createModel(int size) {
		List<MyModel> elements = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			elements.add(new MyModel(i));
		}
		return elements;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		new Snippet022TableViewerRefreshNoScroll(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
