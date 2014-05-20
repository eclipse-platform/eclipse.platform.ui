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
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Example of adding and removing columns in conjunction with JFace-Viewers
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * @since 3.2
 */
public class Snippet018TableViewerAddRemoveColumnsWithEditing {

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

	public class MyLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		private TableViewer viewer;

		public MyLabelProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return ((Person) element)
					.getValue(viewer.getColumnProperties()[columnIndex]
							.toString());
		}
	}

	public class MyCellModifier implements ICellModifier {
		private TableViewer viewer;

		public MyCellModifier(TableViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public boolean canModify(Object element, String property) {
			return true;
		}

		@Override
		public Object getValue(Object element, String property) {
			return ((Person) element).getValue(property);
		}

		@Override
		public void modify(Object element, String property, Object value) {
			((Person) ((Item) element).getData()).setValue(property,
					value.toString());
			viewer.update(((Item) element).getData(), null);
		}

	}

	private int activeColumn = -1;

	public Snippet018TableViewerAddRemoveColumnsWithEditing(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.setLabelProvider(new MyLabelProvider(v));
		v.setContentProvider(ArrayContentProvider.getInstance());
		v.setCellEditors(new CellEditor[] { new TextCellEditor(v.getTable()),
				new TextCellEditor(v.getTable()),
				new TextCellEditor(v.getTable()) });
		v.setCellModifier(new MyCellModifier(v));

		v.setColumnProperties(new String[] { "givenname", "surname" });

		TableColumn column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Givenname");

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Surname");

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
		int emailIndex = -1;
		for (int i = 0; i < v.getColumnProperties().length; i++) {
			if (v.getColumnProperties()[i].toString().equals("email")) {
				emailIndex = i;
				break;
			}
		}

		List<?> list = new ArrayList<CellEditor>(Arrays.asList(v
				.getCellEditors()));
		list.remove(emailIndex);
		CellEditor[] editors = new CellEditor[list.size()];
		list.toArray(editors);
		v.setCellEditors(editors);

		list = new ArrayList<Object>(Arrays.asList(v.getColumnProperties()));
		list.remove(emailIndex);
		String[] columnProperties = new String[list.size()];
		list.toArray(columnProperties);
		v.setColumnProperties(columnProperties);

		v.getTable().getColumn(emailIndex).dispose();

		v.refresh();
	}

	private void addEmailColumn(TableViewer v, int columnIndex) {
		List<CellEditor> list = new ArrayList<CellEditor>(Arrays.asList(v
				.getCellEditors()));
		list.add(columnIndex, new TextCellEditor(v.getTable()));

		CellEditor[] editors = new CellEditor[list.size()];
		list.toArray(editors);
		v.setCellEditors(editors);

		List<Object> list2 = new ArrayList<Object>(Arrays.asList(v
				.getColumnProperties()));
		list2.add(columnIndex, "email");

		String[] columnProperties = new String[list2.size()];
		list2.toArray(columnProperties);
		v.setColumnProperties(columnProperties);

		// 1. Add new column
		TableColumn column = new TableColumn(v.getTable(), SWT.NONE,
				columnIndex);
		column.setText("E-Mail");

		// 2. Update the viewer
		v.refresh();

		// 3. Make the colum visible
		column.setWidth(200);
	}

	private List<Person> createModel() {
		return Arrays.asList(new Person("Tom", "Schindl",
				"tom.schindl@bestsolution.at"), new Person("Boris", "Bokowski",
				"boris_bokowski@ca.ibm.com"), new Person("Tod", "Creasey",
				"tod_creasey@ca.ibm.com"), new Person("Jeanderson", "Candido",
				"jeandersonbc@gmail.com"), new Person("Lars", "Vogel",
				"Lars.Vogel@gmail.com"), new Person("Hendrik", "Still",
				"hendrik.still@vogella.com"));
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
