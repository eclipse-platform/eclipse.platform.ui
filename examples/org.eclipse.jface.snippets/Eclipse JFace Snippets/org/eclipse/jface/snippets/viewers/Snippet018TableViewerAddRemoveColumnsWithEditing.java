/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 448143
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Example of adding and removing columns in conjunction with JFace-Viewers
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * @since 3.2
 */
public class Snippet018TableViewerAddRemoveColumnsWithEditing {

	private static final String EMAIL = "email";
	private static final String GIVENNAME = "givenname";
	private static final String SURNAME = "surname";

	public class Person {
		public String givenname;

		public String surname;

		public String email;

		public Person(String givenname, String surname, String email) {
			this.givenname = givenname;
			this.surname = surname;
			this.email = email;
		}

		public String getValue(String name) {
			Field field;
			try {
				field = getClass().getDeclaredField(name);
				return (String) field.get(this);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return "ERROR";
		}

		public void setValue(String name, String value) {
			Field field;
			try {
				field = getClass().getDeclaredField(name);
				field.set(this, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public class MyLabelProvider extends ColumnLabelProvider
			 {
		private String propertyName;

		public MyLabelProvider(TableViewer viewer, String propertyName) {
			this.propertyName = propertyName;
		}

		@Override
		public String getText(Object element) {
			return ((Person) element).getValue(propertyName);
		}
	}

	private class MyEditingSupport extends EditingSupport{

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
			return ((Person) element).getValue(property);
		}

		@Override
		protected void setValue(Object element, Object value) {
			((Person) element).setValue(property,
					value.toString());
			getViewer().update(element, null);
		}

	}

	private int activeColumn = -1;
	private TableViewerColumn emailViewerColumn;

	public Snippet018TableViewerAddRemoveColumnsWithEditing(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.setContentProvider(ArrayContentProvider.getInstance());

		TableColumn column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Givenname");
		TableViewerColumn viewerColumn = new TableViewerColumn(v, column);
		viewerColumn.setLabelProvider(new MyLabelProvider(v, GIVENNAME));
		viewerColumn.setEditingSupport(new MyEditingSupport(v, GIVENNAME));

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Surname");
		TableViewerColumn viewerColumn2 = new TableViewerColumn(v, column);
		viewerColumn2.setLabelProvider(new MyLabelProvider(v, SURNAME));
		viewerColumn2.setEditingSupport(new MyEditingSupport(v, SURNAME));

		v.setInput(createModel());
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);

		addMenu(v);
		triggerColumnSelectedColumn(v);
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

		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (v.getTable().getColumnCount() == 2) {
					manager.add(insertEmailBefore);
					manager.add(insertEmailAfter);
				} else {
					manager.add(removeEmail);
				}
			}

		});

		v.getControl().setMenu(mgr.createContextMenu(v.getControl()));
	}

	private void removeEmailColumn(TableViewer v) {
		if (emailViewerColumn != null && !emailViewerColumn.getColumn().isDisposed()) {
			emailViewerColumn.getColumn().dispose();
			emailViewerColumn = null;
			v.refresh();
		}
	}

	private void addEmailColumn(TableViewer v, int columnIndex) {

		// 1. Add new column
		emailViewerColumn = new TableViewerColumn(v, SWT.NONE, columnIndex);
		emailViewerColumn.getColumn().setText("E-Mail");
		emailViewerColumn.setLabelProvider(new MyLabelProvider(v, EMAIL));
		emailViewerColumn.setEditingSupport(new MyEditingSupport(v, EMAIL));

		// 2. Update the viewer
		v.refresh();

		// 3. Make the colum visible
		emailViewerColumn.getColumn().setWidth(300);
	}

	private List<Person> createModel() {
		return Arrays.asList(new Person("Tom", "Schindl",
				"tom.schindl@bestsolution.at"), new Person("Boris", "Bokowski",
				"boris_bokowski@ca.ibm.com"), new Person("Tod", "Creasey",
				"tod_creasey@ca.ibm.com"), new Person("Jeanderson", "Candido",
				"jeandersonbc@gmail.com"), new Person("Lars", "Vogel",
						"lars.vogel@vogella.com"), new Person("Hendrik", "Still", "hendrik.still@vogella.com"),
				new Person("Simon", "Scholz", "simon.scholz@vogella.com"));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet018TableViewerAddRemoveColumnsWithEditing(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
