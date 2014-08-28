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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442343
 *******************************************************************************/
package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Example of showing how easy cell-navigation with hidden cells is in 3.4
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 */
public class Snippet057TableViewerSkipHiddenCells {

	public class Person {
		public String givenname;
		public String surname;
		public String email;

		public Person(String givenname, String surname, String email) {
			this.givenname = givenname;
			this.surname = surname;
			this.email = email;
		}

	}

	protected abstract class AbstractEditingSupport extends EditingSupport {

		private TextCellEditor editor;

		public AbstractEditingSupport(TableViewer viewer) {
			super(viewer);
			editor = new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected void setValue(Object element, Object value) {
			this.doSetValue(element, value);
			this.getViewer().update(element, null);
		}

		protected abstract void doSetValue(Object element, Object value);

	}

	public Snippet057TableViewerSkipHiddenCells(Shell shell) {

		final TableViewer tableviewer = new TableViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		tableviewer.setContentProvider(ArrayContentProvider.getInstance());
		MenuManager mgr = new MenuManager();
		mgr.add(new Action("toggle surname visibility") {

			@Override
			public void run() {
				if( tableviewer.getTable().getColumn(1).getWidth() == 0) {
					tableviewer.getTable().getColumn(1).setWidth(200);
				} else {
					tableviewer.getTable().getColumn(1).setWidth(0);
				}

			}

		});
		tableviewer.getControl().setMenu(mgr.createContextMenu(tableviewer.getControl()));

		// Column 1
		TableViewerColumn column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Givenname");
		column.getColumn().setMoveable(false);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((Person) element).givenname;
			}

		});

		column.setEditingSupport(new AbstractEditingSupport(tableviewer) {

			@Override
			protected Object getValue(Object element) {
				return ((Person) element).givenname;
			}

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).givenname = value.toString();
			}

		});

		// Column 2 is zero-width hidden
		column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Surname");
		column.getColumn().setMoveable(false);
		column.getColumn().setResizable(false);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((Person) element).surname;
			}

		});

		column.setEditingSupport(new AbstractEditingSupport(tableviewer) {

			@Override
			protected Object getValue(Object element) {
				return ((Person) element).surname;
			}

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).surname = value.toString();
			}

		});

		// column 3
		column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("E-Mail");
		column.getColumn().setMoveable(false);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((Person) element).email;
			}

		});

		column.setEditingSupport(new AbstractEditingSupport(tableviewer) {

			@Override
			protected Object getValue(Object element) {
				return ((Person) element).email;
			}

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).email = value.toString();
			}

		});

		Person[] model = this.createModel();
		tableviewer.setInput(model);
		tableviewer.getTable().setLinesVisible(true);
		tableviewer.getTable().setHeaderVisible(true);

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(
				tableviewer, new FocusCellOwnerDrawHighlighter(tableviewer));

		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
				tableviewer) {

			@Override
			protected boolean isEditorActivationEvent(

			ColumnViewerEditorActivationEvent event) {

				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
						&& event.keyCode == SWT.CR
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;

			}

		};

		TableViewerEditor.create(tableviewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);

	}

	private Person[] createModel() {
		Person[] elements = new Person[4];
		elements[0] = new Person("Tom", "Schindl",
				"tom.schindl@bestsolution.at");
		elements[1] = new Person("Boris", "Bokowski",
				"Boris_Bokowski@ca.ibm.com");
		elements[2] = new Person("Tod", "Creasey", "Tod_Creasey@ca.ibm.com");
		elements[3] = new Person("Wayne", "Beaton", "wayne@eclipse.org");

		return elements;

	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet057TableViewerSkipHiddenCells(shell);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();

	}
}
