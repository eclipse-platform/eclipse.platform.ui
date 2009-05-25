/*******************************************************************************
 * Copyright (c) 2006, 2008 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Niels Lippke - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellNavigationStrategy;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

/**
 * Example for full feature cell navigation until bug 230955 is fixed
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>, Niels Lippke <niels.lippke@airpas.com>
 * 
 */
public class Snippet058CellNavigationIn34 {

	private class MyContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return (Person[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	public class Person {
		public String givenname;
		public String surname;
		public String email;
		public String gender;

		public Person(String givenname, String surname, String email, String gender) {
			this.givenname = givenname;
			this.surname = surname;
			this.email = email;
			this.gender = gender;
		}

	}

	protected abstract class AbstractEditingSupport extends EditingSupport {
		private CellEditor editor;

		public AbstractEditingSupport(TableViewer viewer) {
			super(viewer);
			this.editor = new TextCellEditor(viewer.getTable());
		}
		
		public AbstractEditingSupport(TableViewer viewer, CellEditor editor) {
			super(viewer);
			this.editor = editor;
		}

		protected boolean canEdit(Object element) {
			return true;
		}

		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		protected void setValue(Object element, Object value) {
			doSetValue(element, value);
			getViewer().update(element, null);
		}

		protected abstract void doSetValue(Object element, Object value);
	}

	public Snippet058CellNavigationIn34(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.BORDER | SWT.FULL_SELECTION);
		v.setContentProvider(new MyContentProvider());

		TableViewerColumn column = new TableViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Givenname");
		column.getColumn().setMoveable(true);
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return ((Person) element).givenname;
			}
		});

		column.setEditingSupport(new AbstractEditingSupport(v) {

			protected Object getValue(Object element) {
				return ((Person) element).givenname;
			}

			protected void doSetValue(Object element, Object value) {
				((Person) element).givenname = value.toString();
			}

		});

		column = new TableViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Surname");
		column.getColumn().setMoveable(true);
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return ((Person) element).surname;
			}

		});

		column.setEditingSupport(new AbstractEditingSupport(v) {

			protected Object getValue(Object element) {
				return ((Person) element).surname;
			}

			protected void doSetValue(Object element, Object value) {
				((Person) element).surname = value.toString();
			}

		});

		column = new TableViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("E-Mail");
		column.getColumn().setMoveable(true);
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return ((Person) element).email;
			}

		});

		
		column.setEditingSupport(new AbstractEditingSupport(v) {

			protected Object getValue(Object element) {				
				return ((Person) element).email;	
			}

			protected void doSetValue(Object element, Object value) {
				((Person) element).email = value.toString();
			}

		});


		column = new TableViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Gender");
		column.getColumn().setMoveable(true);
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return ((Person) element).gender;
			}

		});
		
		ComboBoxCellEditor editor = new ComboBoxCellEditor(((TableViewer) v).getTable(), new String[] {"M","F"});
		editor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION | 
				ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION |
				ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION |
				ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION);
		
		column.setEditingSupport(new AbstractEditingSupport(v, editor) {

			protected Object getValue(Object element) {
				if (((Person) element).gender.equals("M"))
					return new Integer(0);
				return new Integer(1);
			}

			protected void doSetValue(Object element, Object value) {
				if (((Integer) value).intValue() == 0) {
					((Person) element).gender = "M";
				} else {
					((Person) element).gender = "F";
				}
			}

		});
		
		CellNavigationStrategy naviStrat = new CellNavigationStrategy() {

			public ViewerCell findSelectedCell(ColumnViewer viewer,
					ViewerCell currentSelectedCell, Event event) {
				ViewerCell cell = super.findSelectedCell(viewer, currentSelectedCell, event);
				
				if( cell != null ) {
					v.getTable().showColumn(v.getTable().getColumn(cell.getColumnIndex()));
				}
				
				return cell;
			}
			
		};
		
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(v,new FocusCellOwnerDrawHighlighter(v),naviStrat);
		
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(v) {
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};
		
		TableViewerEditor.create(v, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
		
		v.getColumnViewerEditor().addEditorActivationListener(new ColumnViewerEditorActivationListener() {

			public void afterEditorActivated(
					ColumnViewerEditorActivationEvent event) {
				
			}

			public void afterEditorDeactivated(
					ColumnViewerEditorDeactivationEvent event) {
				
			}

			public void beforeEditorActivated(
					ColumnViewerEditorActivationEvent event) {
				ViewerCell cell = (ViewerCell) event.getSource();
				v.getTable().showColumn(v.getTable().getColumn(cell.getColumnIndex()));
			}

			public void beforeEditorDeactivated(
					ColumnViewerEditorDeactivationEvent event) {
				
			}
			
		});
		
		Person[] model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
	}

	private Person[] createModel() {
		Person[] elements = new Person[4];
		elements[0] = new Person("Tom", "Schindl",
				"tom.schindl@bestsolution.at", "M");
		elements[1] = new Person("Boris", "Bokowski",
				"Boris_Bokowski@ca.ibm.com","M");
		elements[2] = new Person("Tod", "Creasey", "Tod_Creasey@ca.ibm.com","M");
		elements[3] = new Person("Wayne", "Beaton", "wayne@eclipse.org","M");

		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet058CellNavigationIn34(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
