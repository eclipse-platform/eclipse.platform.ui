/*******************************************************************************
 * Copyright (c) 2006, 2010 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple TreeViewer to demonstrate usage
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet056BooleanCellEditor {
	public Snippet056BooleanCellEditor(final Shell shell) {
		final TreeViewer v = new TreeViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.getTree().setLinesVisible(true);
		v.getTree().setHeaderVisible(true);

		FocusCellOwnerDrawHighlighter h = new FocusCellOwnerDrawHighlighter(v) {

			protected Color getSelectedCellBackgroundColorNoFocus(
					ViewerCell cell) {
				return shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			}

			protected Color getSelectedCellForegroundColorNoFocus(
					ViewerCell cell) {
				return shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
			}
		};

		TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(v,h);
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(v);

		TreeViewerEditor.create(v, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

		final TextCellEditor textCellEditor = new TextCellEditor(v.getTree());
		final BooleanCellEditor booleanCellEditor = new BooleanCellEditor(v.getTree());

		TreeViewerColumn column = new TreeViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Column 1");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return "Column 1 => " + element.toString();
			}

		});
		column.setEditingSupport(new EditingSupport(v) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return textCellEditor;
			}

			protected Object getValue(Object element) {
				return ((MyModel) element).counter + "";
			}

			protected void setValue(Object element, Object value) {
				((MyModel) element).counter = Integer
						.parseInt(value.toString());
				v.update(element, null);
			}
		});

		column = new TreeViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Column 2");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return ((MyModel) element).flag+"";
			}

		});
		column.setEditingSupport(new EditingSupport(v) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return booleanCellEditor;
			}

			protected Object getValue(Object element) {
				return new Boolean(((MyModel) element).flag);
			}

			protected void setValue(Object element, Object value) {
				((MyModel) element).flag = ((Boolean)value).booleanValue();
				v.update(element, null);
			}
		});

		column = new TreeViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Column 3");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return "Column 3 => " + element.toString();
			}

		});
		column.setEditingSupport(new EditingSupport(v) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return textCellEditor;
			}

			protected Object getValue(Object element) {
				return ((MyModel) element).counter + "";
			}

			protected void setValue(Object element, Object value) {
				((MyModel) element).counter = Integer
						.parseInt(value.toString());
				v.update(element, null);
			}
		});

		v.setContentProvider(new MyContentProvider());

		v.setInput(createModel());
	}

	private MyModel createModel() {

		MyModel root = new MyModel(0, null);
		root.counter = 0;

		MyModel tmp;
		MyModel subItem;
		for (int i = 1; i < 10; i++) {
			tmp = new MyModel(i, root);
			root.child.add(tmp);
			for (int j = 1; j < i; j++) {
				subItem = new MyModel(j, tmp);
				subItem.child.add(new MyModel(j * 100, subItem));
				tmp.child.add(subItem);
			}
		}

		return root;
	}

	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet056BooleanCellEditor(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

	private class MyContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((MyModel) inputElement).child.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}
			return ((MyModel) element).parent;
		}

		public boolean hasChildren(Object element) {
			return ((MyModel) element).child.size() > 0;
		}

	}

	public class MyModel {
		public MyModel parent;

		public ArrayList child = new ArrayList();

		public int counter;

		public boolean flag;

		public MyModel(int counter, MyModel parent) {
			this.parent = parent;
			this.counter = counter;
			this.flag = counter % 2 == 0;
		}

		public String toString() {
			String rv = "Item ";
			if (parent != null) {
				rv = parent.toString() + ".";
			}

			rv += counter;

			return rv;
		}
	}

}
