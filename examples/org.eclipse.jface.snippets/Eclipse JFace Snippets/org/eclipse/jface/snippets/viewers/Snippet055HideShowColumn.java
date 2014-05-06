/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Henrik Still <hendrik.still@gammas.de> - Bug 415875
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple TreeViewer to demonstrate usage
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet055HideShowColumn {

	private final class MyEditingSupport extends EditingSupport {
		private final TreeViewer v;
		private final TextCellEditor textCellEditor;

		private MyEditingSupport(ColumnViewer viewer, TreeViewer v,
				TextCellEditor textCellEditor) {
			super(viewer);
			this.v = v;
			this.textCellEditor = textCellEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return textCellEditor;
		}

		@Override
		protected Object getValue(Object element) {
			return ((MyModel) element).counter + "";
		}

		@Override
		protected void setValue(Object element, Object value) {
			((MyModel) element).counter = Integer.parseInt(value.toString());
			v.update(element, null);
		}
	}

	class MyContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return ((MyModel) inputElement).child.toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}
			return ((MyModel) element).parent;
		}

		@Override
		public boolean hasChildren(Object element) {
			return ((MyModel) element).child.size() > 0;
		}

	}

	class MyColumnLabelProvider extends ColumnLabelProvider {

		private String prefix;

		public MyColumnLabelProvider(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public String getText(Object element) {
			return prefix + " => " + element.toString();
		}

	}

	public Snippet055HideShowColumn(final Shell shell) {
		final TreeViewer v = new TreeViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.getTree().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		v.getTree().setLinesVisible(true);
		v.getTree().setHeaderVisible(true);

		TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(
				v, new FocusCellOwnerDrawHighlighter(v));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
				v) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TreeViewerEditor.create(v, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		final TextCellEditor textCellEditor = new TextCellEditor(v.getTree());

		createColumnFor(v, "Column 1", textCellEditor);
		final TreeViewerColumn column_2 = createColumnFor(v, "Column 2",
				textCellEditor);
		createColumnFor(v, "Column 3", textCellEditor);

		v.setContentProvider(new MyContentProvider());
		v.setInput(createModel());

		Button b = createButtonFor(shell, "Edit-Element");
		b.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				MyModel root = (MyModel) v.getInput();
				TreePath path = new TreePath(new Object[] { root,
						root.child.get(1), root.child.get(1).child.get(0) });
				v.editElement(path, 0);
			}

		});
		b = createButtonFor(shell, "Hide/Show 2nd Column");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int width = column_2.getColumn().getWidth();
				column_2.getColumn().setWidth(width == 0 ? 200 : 0);
			}
		});
	}

	private TreeViewerColumn createColumnFor(final TreeViewer v, String label,
			TextCellEditor textCellEditor) {
		TreeViewerColumn column = new TreeViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText(label);
		column.setLabelProvider(new MyColumnLabelProvider(label));
		column.setEditingSupport(new MyEditingSupport(v, v, textCellEditor));
		return column;
	}

	private Button createButtonFor(final Shell shell, String label) {
		Button b = new Button(shell, SWT.PUSH);
		b.setText(label);
		b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return b;
	}

	public class MyModel {

		public MyModel parent;
		public List<MyModel> child = new ArrayList<MyModel>();
		public int counter;

		public MyModel(int counter, MyModel parent) {
			this.parent = parent;
			this.counter = counter;
		}

		@Override
		public String toString() {
			String rv = "Item ";
			if (parent != null) {
				rv = parent.toString() + ".";
			}
			rv += counter;

			return rv;
		}
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
		shell.setLayout(new GridLayout(2, true));
		new Snippet055HideShowColumn(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
