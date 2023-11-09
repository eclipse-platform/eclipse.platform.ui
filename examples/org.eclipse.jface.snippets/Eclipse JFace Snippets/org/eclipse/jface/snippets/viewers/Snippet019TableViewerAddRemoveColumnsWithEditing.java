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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.ConfigureColumns;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Explore the new API added in 3.3 and see how easily you can create reusable
 * components
 *
 * @author Tom Schindl &lt;tom.schindl@bestsolution.at&gt;
 * @since 3.2
 */
public class Snippet019TableViewerAddRemoveColumnsWithEditing {

	public static class Person {
		public String givenname;
		public String surname;
		public String email;

		public Person(String givenname, String surname, String email) {
			this.givenname = givenname;
			this.surname = surname;
			this.email = email;
		}
	}

	private static class GivenNameLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			return ((Person) element).givenname;
		}
	}

	private class GivenNameEditing extends EditingSupport {
		private final TextCellEditor cellEditor;

		public GivenNameEditing(TableViewer viewer) {
			super(viewer);
			cellEditor = new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected Object getValue(Object element) {
			return ((Person) element).givenname;
		}

		@Override
		protected void setValue(Object element, Object value) {
			((Person) element).givenname = value.toString();
			getViewer().update(element, null);
		}
	}

	private static class SurNameLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			return ((Person) element).surname;
		}
	}

	private class SurNameEditing extends EditingSupport {
		private final TextCellEditor cellEditor;

		public SurNameEditing(TableViewer viewer) {
			super(viewer);
			cellEditor = new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected Object getValue(Object element) {
			return ((Person) element).surname;
		}

		@Override
		protected void setValue(Object element, Object value) {
			((Person) element).surname = value.toString();
			getViewer().update(element, null);
		}
	}

	private static class EmailLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			return ((Person) element).email;
		}
	}

	private class EmailEditing extends EditingSupport {
		private final TextCellEditor cellEditor;

		public EmailEditing(TableViewer viewer) {
			super(viewer);
			cellEditor = new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected Object getValue(Object element) {
			return ((Person) element).email;
		}

		@Override
		protected void setValue(Object element, Object value) {
			((Person) element).email = value.toString();
			getViewer().update(element, null);
		}
	}

	private int activeColumn = -1;

	private TableViewerColumn column;

	public Snippet019TableViewerAddRemoveColumnsWithEditing(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);

		TableViewerColumn column1 = createColumnFor(v, "Givenname");
		column1.setLabelProvider(new GivenNameLabelProvider());
		column1.setEditingSupport(new GivenNameEditing(v));

		TableViewerColumn column2 = createColumnFor(v, "Surname");
		column2.setLabelProvider(new SurNameLabelProvider());
		column2.setEditingSupport(new SurNameEditing(v));

		v.setContentProvider(ArrayContentProvider.getInstance());
		v.setInput(createModel());
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);

		addMenu(v);
		triggerColumnSelectedColumn(v);
	}

	private TableViewerColumn createColumnFor(TableViewer viewer, String label) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText(label);
		column.getColumn().setMoveable(true);
		return column;
	}

	private void triggerColumnSelectedColumn(final TableViewer v) {
		v.getTable().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				int x = 0;
				for (int i = 0; i < v.getTable().getColumnCount(); i++) {
					x += v.getTable().getColumn(i).getWidth();
					if (e.x <= x) {
						activeColumn = i;
						break;
					}
				}
			}

		});
	}

	private void removeEmailColumn(TableViewer v) {
		column.getColumn().dispose();
		v.refresh();
	}

	private void addEmailColumn(TableViewer v, int columnIndex) {
		column = new TableViewerColumn(v, SWT.NONE, columnIndex);
		column.setLabelProvider(new EmailLabelProvider());
		column.setEditingSupport(new EmailEditing(v));
		column.getColumn().setText("E-Mail");
		column.getColumn().setResizable(false);

		v.refresh();

		column.getColumn().setWidth(200);

	}

	private void addMenu(final TableViewer v) {
		final MenuManager mgr = new MenuManager();

		final Action insertEmailBefore = new Action("Insert E-Mail before") {
			@Override
			public void run() {
				addEmailColumn(v, activeColumn);
			}
		};

		final Action insertEmailAfter = new Action("Insert E-Mail after") {
			@Override
			public void run() {
				addEmailColumn(v, activeColumn + 1);
			}
		};

		final Action removeEmail = new Action("Remove E-Mail") {
			@Override
			public void run() {
				removeEmailColumn(v);
			}
		};

		final Action configureColumns = new Action("Configure Columns...") {
			@Override
			public void run() {
				ConfigureColumns.forTable(v.getTable(),
						new SameShellProvider(v.getControl()));
			}
		};

		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(manager -> {
			if (v.getTable().getColumnCount() == 2) {
				manager.add(insertEmailBefore);
				manager.add(insertEmailAfter);
			} else {
				manager.add(removeEmail);
			}
			manager.add(configureColumns);
		});

		v.getControl().setMenu(mgr.createContextMenu(v.getControl()));
	}

	private Person[] createModel() {
		return new Person[] {
				new Person("Tom", "Schindl", "tom.schindl@bestsolution.at"),
				new Person("Boris", "Bokowski", "boris_bokowski@ca.ibm.com"),
				new Person("Tod", "Creasey", "tod_creasey@ca.ibm.com"),
				new Person("Lars", "Vogel", "lars.vogel@gmail.com"),
				new Person("Hendrik", "Still", "hendrik.still@vogella.com"),
				new Person("Jeanderson", "Candido", "jeandersonbc@gmail.com") };
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet019TableViewerAddRemoveColumnsWithEditing(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
