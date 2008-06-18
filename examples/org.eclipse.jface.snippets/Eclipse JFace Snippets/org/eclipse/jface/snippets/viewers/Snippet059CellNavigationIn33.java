/*******************************************************************************
 * Copyright (c) 2006 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.lang.reflect.Field;

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
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Example for full feature cell navigation in 3.3. This snippet uses internal
 * API by reflection so its not guaranteed to work for ever. The problem of
 * invisible cells is fixed in 3.4. The problem with horizontal scrolling is
 * going to be fixed in 3.5.
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet059CellNavigationIn33 {

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

		public Person(String givenname, String surname, String email,
				String gender) {
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

	public Snippet059CellNavigationIn33(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
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

		final TableViewerColumn columnA = new TableViewerColumn(v, SWT.NONE);
		columnA.getColumn().setWidth(200);
		columnA.getColumn().setText("Surname");
		columnA.getColumn().setMoveable(true);
		columnA.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return ((Person) element).surname;
			}

		});

		columnA.setEditingSupport(new AbstractEditingSupport(v) {

			protected boolean canEdit(Object element) {
				return columnA.getColumn().getWidth() != 0;
			}

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

		ComboBoxCellEditor editor = new ComboBoxCellEditor(((TableViewer) v)
				.getTable(), new String[] { "M", "F" });
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
			private int getVisualIndex(ViewerRow row, int creationIndex) {
				TableItem item = (TableItem) row.getItem();
				int[] order = item.getParent().getColumnOrder();

				for (int i = 0; i < order.length; i++) {
					if (order[i] == creationIndex) {
						return i;
					}
				}
				return creationIndex;
			}

			private int getCreationIndex(ViewerRow row, int visualIndex) {
				TableItem item = (TableItem) row.getItem();
				if( item != null && ! item.isDisposed() /*&& hasColumns() && isValidOrderIndex(visualIndex)*/ ) {
					return item.getParent().getColumnOrder()[visualIndex];
				}
				return visualIndex;
			}

			private ViewerCell getCellAtVisualIndex(ViewerRow row, int visualIndex) {
				return getCell(row,getCreationIndex(row, visualIndex));
			}

			private boolean isVisible(ViewerCell cell) {
				return getWidth(cell) > 0;
			}

			private int getWidth(ViewerCell cell) {
				TableItem item = (TableItem) cell.getViewerRow().getItem();
				return item.getParent().getColumn(cell.getColumnIndex()).getWidth();
			}

			private ViewerCell getCell(ViewerRow row, int index) {
				return row.getCell(index);
			}

			private ViewerCell getNeighbor(ViewerCell currentCell, int directionMask, boolean sameLevel) {
				ViewerRow row;

				if ((directionMask & ViewerCell.ABOVE) == ViewerCell.ABOVE) {
					row = currentCell.getViewerRow().getNeighbor(ViewerRow.ABOVE, sameLevel);
				} else if ((directionMask & ViewerCell.BELOW) == ViewerCell.BELOW) {
					row = currentCell.getViewerRow().getNeighbor(ViewerRow.BELOW, sameLevel);
				} else {
					row = currentCell.getViewerRow();
				}

				if (row != null) {
					int columnIndex;
					columnIndex = getVisualIndex(row,currentCell.getColumnIndex());

					int modifier = 0;

					if ((directionMask & ViewerCell.LEFT) == ViewerCell.LEFT) {
						modifier = -1;
					} else if ((directionMask & ViewerCell.RIGHT) == ViewerCell.RIGHT) {
						modifier = 1;
					}

					columnIndex += modifier;

					if (columnIndex >= 0 && columnIndex < row.getColumnCount()) {
						ViewerCell cell = getCellAtVisualIndex(row,columnIndex);
						if( cell != null ) {
							while( cell != null && columnIndex < row.getColumnCount() - 1  && columnIndex > 0 ) {
								if( isVisible(cell) ) {
									break;
								}

								columnIndex += modifier;
								cell = getCellAtVisualIndex(row,columnIndex);
								if( cell == null ) {
									break;
								}
							}
						}

						return cell;
					}
				}
				return null;
			}

			private ViewerCell internalFindSelectedCell(ColumnViewer viewer,
					ViewerCell currentSelectedCell, Event event) {
				switch (event.keyCode) {
				case SWT.ARROW_UP:
					if (currentSelectedCell != null) {
						return getNeighbor(currentSelectedCell, ViewerCell.ABOVE, false);
					}
					break;
				case SWT.ARROW_DOWN:
					if (currentSelectedCell != null) {
						return getNeighbor(currentSelectedCell, ViewerCell.BELOW, false);
					}
					break;
				case SWT.ARROW_LEFT:
					if (currentSelectedCell != null) {
						return getNeighbor(currentSelectedCell, ViewerCell.LEFT, true);
					}
					break;
				case SWT.ARROW_RIGHT:
					if (currentSelectedCell != null) {
						return getNeighbor(currentSelectedCell, ViewerCell.RIGHT, true);
					}
					break;
				}

				return null;
			}

			public ViewerCell findSelectedCell(ColumnViewer viewer,
					ViewerCell currentSelectedCell, Event event) {
				ViewerCell cell = internalFindSelectedCell(viewer, currentSelectedCell, event);

				if (cell != null) {
					TableColumn t = v.getTable().getColumn(
							cell.getColumnIndex());
					v.getTable().showColumn(t);
				}

				return cell;
			}

		};

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(
				v, new FocusCellOwnerDrawHighlighter(v));
		try {
			Field f = focusCellManager.getClass().getSuperclass()
					.getDeclaredField("navigationStrategy");
			f.setAccessible(true);
			f.set(focusCellManager, naviStrat);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
				v) {
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TableViewerEditor.create(v, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		v.getColumnViewerEditor().addEditorActivationListener(
				new ColumnViewerEditorActivationListener() {

					public void afterEditorActivated(
							ColumnViewerEditorActivationEvent event) {

					}

					public void afterEditorDeactivated(
							ColumnViewerEditorDeactivationEvent event) {

					}

					public void beforeEditorActivated(
							ColumnViewerEditorActivationEvent event) {
						ViewerCell cell = (ViewerCell) event.getSource();
						v.getTable().showColumn(
								v.getTable().getColumn(cell.getColumnIndex()));
					}

					public void beforeEditorDeactivated(
							ColumnViewerEditorDeactivationEvent event) {

					}

				});

		v.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		final Button b = new Button(shell, SWT.PUSH);
		b.setText("Hide");
		b.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if (columnA.getColumn().getWidth() == 0) {
					b.setText("Hide");
					columnA.getColumn().setWidth(200);
				} else {
					b.setText("Show");
					columnA.getColumn().setWidth(0);
				}
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
				"Boris_Bokowski@ca.ibm.com", "M");
		elements[2] = new Person("Tod", "Creasey", "Tod_Creasey@ca.ibm.com",
				"M");
		elements[3] = new Person("Wayne", "Beaton", "wayne@eclipse.org", "M");

		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout());
		new Snippet059CellNavigationIn33(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
