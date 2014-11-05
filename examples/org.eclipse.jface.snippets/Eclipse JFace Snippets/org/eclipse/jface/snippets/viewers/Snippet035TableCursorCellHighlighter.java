/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel (lars.vogel@gmail.com) - Bug 413427
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442747
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Example usage of none mandatory interfaces of ITableFontProvider and
 * ITableColorProvider
 *
 */
public class Snippet035TableCursorCellHighlighter {

	public static boolean flag = true;

	private class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	private class MyEditingSupport extends EditingSupport {

		private String property;

		public MyEditingSupport(ColumnViewer viewer, String property) {
			super(viewer);
			this.property = property;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return "Column " + property + " => " + element.toString();
		}

		@Override
		protected void setValue(Object element, Object value) {

		}

	}

	private class MyColumnLabelProvider extends ColumnLabelProvider {
		FontRegistry registry = JFaceResources.getFontRegistry();
		private String columnIndex;

		public MyColumnLabelProvider(String columnIndex) {
			this.columnIndex = columnIndex;
		}

		@Override
		public Font getFont(Object element) {
			if (((MyModel) element).counter % 2 == 0) {
				return registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());
			}
			return null;
		}

		@Override
		public Color getBackground(Object element) {
			if (((MyModel) element).counter % 2 == 0) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			}
			return null;
		}

		@Override
		public Color getForeground(Object element) {
			if (((MyModel) element).counter % 2 == 1) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			return "Column " + columnIndex + " => " + element.toString();
		}
	}

	public Snippet035TableCursorCellHighlighter(Shell shell) {
		int style = SWT.BORDER | SWT.HIDE_SELECTION | SWT.FULL_SELECTION;
		final TableViewer v = new TableViewer(shell, style);
		v.setContentProvider(ArrayContentProvider.getInstance());

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(
				v, new CursorCellHighlighter(v, new TableCursor(v)));
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

		int features = ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION;

		TableViewerEditor.create(v, focusCellManager, actSupport, features);

		TableViewerColumn viewerColumn = new TableViewerColumn(v, SWT.NONE);
		viewerColumn.getColumn().setWidth(200);
		viewerColumn.getColumn().setText("Column 1");
		viewerColumn.setEditingSupport(new MyEditingSupport(v, "1"));
		viewerColumn.setLabelProvider(new MyColumnLabelProvider("1"));

		viewerColumn = new TableViewerColumn(v, SWT.NONE);
		viewerColumn.getColumn().setWidth(200);
		viewerColumn.getColumn().setText("Column 2");
		viewerColumn.setEditingSupport(new MyEditingSupport(v, "2"));
		viewerColumn.setLabelProvider(new MyColumnLabelProvider("2"));

		MyModel[] model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
	}

	private MyModel[] createModel() {
		MyModel[] elements = new MyModel[10];

		for (int i = 0; i < elements.length; i++) {
			elements[i] = new MyModel(i);
		}
		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet035TableCursorCellHighlighter(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

	}

}
