/*******************************************************************************
 * Copyright (c) 2006, 2015 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 475361
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

/**
 * Example how to place native controls into a viewer with the new JFace-API
 * because has the potential to eat up all your handles you should think about
 * alternate approaches e.g. taking a screenshot of the control
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet054NativeControlsInViewers {

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

	public Snippet054NativeControlsInViewers(Shell shell) {
		final TableViewer viewer = new TableViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.getTable().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		TableViewerColumn column = createColumnFor(viewer, "Column 1");
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return element.toString();
			}

		});

		column = createColumnFor(viewer, "Column 2");
		column.setLabelProvider(createCellLabelProvider());

		viewer.setInput(createModel(10));
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);

		Button b = createButtonFor(shell, "Modify Input");
		b.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setInput(createModel((int) (Math.random() * 10)));
			}

		});

		b = createButtonFor(shell, "Refresh");
		b.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.refresh();
			}

		});
	}

	private CellLabelProvider createCellLabelProvider() {
		return new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				final TableItem item = (TableItem) cell.getItem();
				DisposeListener listener = new DisposeListener() {

					@Override
					public void widgetDisposed(DisposeEvent e) {
						if (item.getData("EDITOR") != null) {
							TableEditor editor = (TableEditor) item
									.getData("EDITOR");
							editor.getEditor().dispose();
							editor.dispose();
						}
					}

				};
				if (item.getData("EDITOR") != null) {
					TableEditor editor = (TableEditor) item.getData("EDITOR");
					editor.getEditor().dispose();
					editor.dispose();
				}
				if (item.getData("DISPOSELISTNER") != null) {
					item.removeDisposeListener((DisposeListener) item
							.getData("DISPOSELISTNER"));
				}
				TableEditor editor = new TableEditor(item.getParent());
				item.setData("EDITOR", editor);
				Composite comp = new Composite(item.getParent(), SWT.NONE);
				comp.setBackground(item.getParent().getBackground());
				comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
				RowLayout l = new RowLayout();
				l.marginHeight = 0;
				l.marginWidth = 0;
				l.marginTop = 0;
				l.marginBottom = 0;
				comp.setLayout(l);
				new Button(comp, SWT.RADIO);
				new Button(comp, SWT.RADIO);
				new Button(comp, SWT.RADIO);

				editor.grabHorizontal = true;
				editor.setEditor(comp, item, 1);

				item.addDisposeListener(listener);
				item.setData("DISPOSELISTNER", listener);
			}

		};
	}

	private Button createButtonFor(Shell shell, String label) {
		Button b = new Button(shell, SWT.PUSH);
		b.setText(label);
		b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return b;
	}

	private TableViewerColumn createColumnFor(final TableViewer viewer,
			String columnLabel) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText(columnLabel);
		return column;
	}

	private List<MyModel> createModel(int amount) {
		List<MyModel> elements = new ArrayList<>();

		for (int i = 0; i < amount; i++) {
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
		shell.setLayout(new GridLayout(2, true));
		new Snippet054NativeControlsInViewers(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
