/*******************************************************************************
 * Copyright (c) 2023 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Table viewer with limits
 */
public class Snippet068TableViewerWithLimit {
	List<MyModel> model;
	final TableViewer viewer;

	private SelectionListener listener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			String data = (String) e.widget.getData();
			MyModel element = null;
			switch (data) {
			case "refresh":
				viewer.refresh();
				break;
			case "add":
				element = new MyModel();
				model.add(element);
				viewer.add(element);
				break;
			case "remove":
				viewer.remove(model.remove(model.size() - 1));
				break;
			case "setSelection":
				viewer.setSelection(
						new StructuredSelection(Arrays.asList(model.get(0), "test_Data", model.get(model.size() - 2))));
			default:
				break;
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {

		}
	};

	private static class MyContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return ((ArrayList<?>) inputElement).toArray();
		}

	}

	public static class MyModel {
		static int counter;
		int id;

		public MyModel() {
			this.id = counter++;
		}

		@Override
		public String toString() {
			return "Item " + id;
		}
	}

	public Snippet068TableViewerWithLimit(Shell shell) {
		Composite tableComp = new Composite(shell, SWT.BORDER);
		tableComp.setLayout(new FillLayout(SWT.VERTICAL));
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer = new TableViewer(tableComp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setLabelProvider(new LabelProvider());
		viewer.setDisplayIncrementally(3);
		viewer.setContentProvider(new MyContentProvider());
		createColumn(viewer.getTable(), "column1");
		model = createModel();
		viewer.setInput(model);
		viewer.getTable().setLinesVisible(true);

		Composite buttons = new Composite(shell, SWT.BORDER);
		buttons.setLayout(new FillLayout(SWT.VERTICAL));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		Button refresh = new Button(buttons, SWT.PUSH);
		refresh.setData("refresh");
		refresh.setText("refresh");
		refresh.addSelectionListener(listener);

		Button add = new Button(buttons, SWT.PUSH);
		add.setText("add");
		add.setData("add");
		add.addSelectionListener(listener);

		Button remove = new Button(buttons, SWT.PUSH);
		remove.setText("remove");
		remove.setData("remove");
		remove.addSelectionListener(listener);

		Button setSel = new Button(buttons, SWT.PUSH);
		setSel.setText("setSelection");
		setSel.setData("setSelection");
		setSel.addSelectionListener(listener);
	}

	public void createColumn(Table tb, String text) {
		TableColumn column = new TableColumn(tb, SWT.NONE);
		column.setWidth(100);
		column.setText(text);
		tb.setHeaderVisible(true);
	}

	private List<MyModel> createModel() {
		List<MyModel> model = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			model.add(new MyModel());
		}
		return model;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2, false));
		new Snippet068TableViewerWithLimit(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
