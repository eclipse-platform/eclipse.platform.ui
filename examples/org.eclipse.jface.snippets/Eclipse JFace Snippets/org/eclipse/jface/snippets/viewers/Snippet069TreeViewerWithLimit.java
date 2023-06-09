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
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * A simple TreeViewer to demonstrate the usage of limits
 */

public class Snippet069TreeViewerWithLimit {

	final TreeViewer viewer;
	MyModel root;
	private Object curSel;

	private SelectionListener listener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			String data = (String) e.widget.getData();
			switch (data) {
			case "refresh":
				if (curSel instanceof MyModel current) {
					viewer.refresh(current, false);
				} else {
					viewer.refresh();
				}
				break;
			case "add":
				if (curSel instanceof MyModel) {
					MyModel current = (MyModel) curSel;
					MyModel eleToAdd = new MyModel(current, 100);
					current.addChild(eleToAdd);
					viewer.add(curSel, eleToAdd);
				}
				break;
			case "remove":
				if (curSel instanceof MyModel current) {
					current.getParent().getChildren().remove(current);
					viewer.remove(current);
				}
				break;
			case "setSelection":
				IStructuredSelection sel = new StructuredSelection(
						root.getChildren().get(2).getChildren().get(7).getChildren().get(7));
				viewer.setSelection(sel, true);
				break;
			case "expandAll":
				viewer.expandAll();
				break;
			case "expandToLevel":
				if (curSel instanceof MyModel) {
					viewer.expandToLevel(curSel, 2, false);
				}
				break;
			case "collapseAll":
				if (curSel instanceof MyModel) {
					viewer.collapseAll();
				}
				break;
			case "collapseToLevel":
				if (curSel instanceof MyModel) {
					viewer.collapseToLevel(curSel, 3);
				}
				break;
			default:
				break;
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {

		}
	};

	private static class MyContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return ((MyModel) inputElement).children.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (!(parentElement instanceof MyModel)) {
				return null;
			}
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			if (!(element instanceof MyModel)) {
				return null;
			}

			return ((MyModel) element).parent;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (!(element instanceof MyModel)) {
				return false;
			}
			MyModel myModel = (MyModel) element;
			return myModel.children.size() > 0;
		}

	}

	public static class MyModel {
		private MyModel parent;

		public MyModel getParent() {
			return parent;
		}

		private List<MyModel> children = new ArrayList<>();

		public List<MyModel> getChildren() {
			return children;
		}

		static int counter;
		int id;

		public MyModel(MyModel parent, int id) {
			this.parent = parent;
			this.id = id;
		}

		@Override
		public String toString() {
			String rv = "Item ";
			if (parent != null) {
				rv = parent + ".";
			}

			rv += id;

			return rv;
		}

		public boolean removeChild(MyModel child) {
			return children.remove(child);
		}

		public boolean addChild(MyModel child) {
			return children.add(child);
		}
	}

	public Snippet069TreeViewerWithLimit(Shell shell) {
		Composite tableComp = new Composite(shell, SWT.BORDER);
		tableComp.setLayout(new FillLayout(SWT.VERTICAL));
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer = new TreeViewer(tableComp, SWT.MULTI);
		viewer.setLabelProvider(new LabelProvider());
		viewer.setDisplayIncrementally(3);
		viewer.setContentProvider(new MyContentProvider());
		createColumn(viewer.getTree(), "Column1");
		createColumn(viewer.getTree(), "Column2");
		root = createModel();
		viewer.setInput(root);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof TreeSelection) {
					curSel = ((TreeSelection) event.getSelection()).getFirstElement();
				}
			}
		});

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

		Button expAll = new Button(buttons, SWT.PUSH);
		expAll.setText("expandAll");
		expAll.setData("expandAll");
		expAll.addSelectionListener(listener);

		Button expandToLevel = new Button(buttons, SWT.PUSH);
		expandToLevel.setText("expandToLevel");
		expandToLevel.setData("expandToLevel");
		expandToLevel.addSelectionListener(listener);

		Button collapseAll = new Button(buttons, SWT.PUSH);
		collapseAll.setText("collapseAll");
		collapseAll.setData("collapseAll");
		collapseAll.addSelectionListener(listener);

		Button collapseToLevel = new Button(buttons, SWT.PUSH);
		collapseToLevel.setText("collapseToLevel");
		collapseToLevel.setData("collapseToLevel");
		collapseToLevel.addSelectionListener(listener);

	}

	public void createColumn(Tree tr, String text) {
		TreeColumn column = new TreeColumn(tr, SWT.NONE);
		column.setWidth(200);
		column.setText(text);
		tr.setHeaderVisible(true);
	}

	private MyModel createModel() {

		MyModel root = new MyModel(null, 0);

		for (int i = 0; i < 15; i++) {
			MyModel l1 = new MyModel(root, i);
			root.addChild(l1);
			for (int j = 0; j < 10; j++) {
				MyModel l2 = new MyModel(l1, j);
				l1.addChild(l2);
				for (int j2 = 0; j2 < 10; j2++) {
					MyModel l3 = new MyModel(l2, j2);
					l2.addChild(l3);

				}
			}
		}

		return root;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2, false));
		new Snippet069TreeViewerWithLimit(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
