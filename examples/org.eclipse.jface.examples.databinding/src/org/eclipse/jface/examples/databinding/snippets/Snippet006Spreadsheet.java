/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.internal.databinding.provisional.swt.TableUpdater;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * @since 1.1
 *
 */
public class Snippet006Spreadsheet {

	private static final int COUNTER_UPDATE_DELAY = 1000;

	private static final int NUM_COLUMNS = 6;

	private static final int NUM_ROWS = 16;

	/**
	 * 0 for no output, 1 for some, 2 for more
	 */
	private static int DEBUG_LEVEL = 0;

	/**
	 * If true, there will be a automatic counter at B1.
	 */
	private static boolean FUNKY_COUNTER = false;

	/**
	 * // * If true, all formulas (except for row 1 and column A) will be the
	 * sum of the values of their left and top neighbouring cells.
	 */
	private static boolean FUNKY_FORMULAS = true;

	static WritableValue[][] cellFormulas = new WritableValue[NUM_ROWS][NUM_COLUMNS];

	static ComputedValue[][] cellValues = new ComputedValue[NUM_ROWS][NUM_COLUMNS];

	static class ComputedCellValue extends ComputedValue {
		private final IObservableValue cellFormula;

		private boolean calculating;

		ComputedCellValue(IObservableValue cellFormula) {
			this.cellFormula = cellFormula;
		}

		@Override
		protected Object calculate() {
			if (calculating) {
				return "#cycle";
			}
			try {
				calculating = true;
				return evaluate(cellFormula.getValue());
			} finally {
				calculating = false;
			}
		}

		private Object evaluate(Object value) {
			if (DEBUG_LEVEL >= 2) {
				System.out.println("evaluating " + this + " ...");
			}
			if (value == null) {
				return "";
			}
			try {
				String s = (String) value;
				if (!s.startsWith("=")) {
					return s;
				}
				String addition = s.substring(1);
				int indexOfPlus = addition.indexOf('+');
				String operand1 = addition.substring(0, indexOfPlus);
				double value1 = eval(operand1);
				String operand2 = addition.substring(indexOfPlus + 1);
				double value2 = eval(operand2);
				return NumberFormat.getNumberInstance().format(value1 + value2);
			} catch (Exception ex) {
				return ex.getMessage();
			}
		}

		/**
		 * @param s
		 * @return
		 * @throws ParseException
		 */
		private double eval(String s) throws ParseException {
			if (s.length() == 0) {
				return 0;
			}
			char character = s.charAt(0);
			if (Character.isLetter(character)) {
				character = Character.toLowerCase(character);
				// reference to other cell
				int columnIndex = character - 'a';
				int rowIndex = 0;
				rowIndex = NumberFormat.getNumberInstance().parse(
						s.substring(1)).intValue() - 1;
				String value = (String) cellValues[rowIndex][columnIndex]
						.getValue();
				return value.length() == 0 ? 0 : NumberFormat
						.getNumberInstance().parse(value).doubleValue();
			}
			return NumberFormat.getNumberInstance().parse(s).doubleValue();
		}
	}

	protected static int counter;

	public static void main(String[] args) {

		final Display display = new Display();
		Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell(display);
				shell.setText("Data Binding Snippet 006");

				final Table table = new Table(shell, SWT.BORDER | SWT.MULTI
						| SWT.FULL_SELECTION | SWT.VIRTUAL);
				table.setLinesVisible(true);
				table.setHeaderVisible(true);

				for (int i = 0; i < NUM_COLUMNS; i++) {
					TableColumn tableColumn = new TableColumn(table, SWT.NONE);
					tableColumn.setText(Character.toString((char) ('A' + i)));
					tableColumn.setWidth(60);
				}
				WritableList list = new WritableList();
				for (int i = 0; i < NUM_ROWS; i++) {
					list.add(new Object());
					for (int j = 0; j < NUM_COLUMNS; j++) {
						cellFormulas[i][j] = new WritableValue();
						cellValues[i][j] = new ComputedCellValue(
								cellFormulas[i][j]);
						if (!FUNKY_FORMULAS || i == 0 || j == 0) {
							cellFormulas[i][j].setValue("");
						} else {
							cellFormulas[i][j].setValue("="
									+ cellReference(i - 1, j) + "+"
									+ cellReference(i, j - 1));
						}
					}
				}

				new TableUpdater(table, list) {
					@Override
					protected void updateItem(int rowIndex, TableItem item, Object element) {
						if (DEBUG_LEVEL >= 1) {
							System.out.println("updating row " + rowIndex);
						}
						for (int j = 0; j < NUM_COLUMNS; j++) {
							item.setText(j, (String) cellValues[rowIndex][j]
									.getValue());
						}
					}
				};

				if (FUNKY_COUNTER) {
					// counter in A1
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							cellFormulas[0][1].setValue("" + counter++);
							display.timerExec(COUNTER_UPDATE_DELAY, this);
						}
					});
				}

				// create a TableCursor to navigate around the table
				final TableCursor cursor = new TableCursor(table, SWT.NONE);
				// create an editor to edit the cell when the user hits "ENTER"
				// while over a cell in the table
				final ControlEditor editor = new ControlEditor(cursor);
				editor.grabHorizontal = true;
				editor.grabVertical = true;

				cursor.addSelectionListener(new SelectionAdapter() {
					// when the TableEditor is over a cell, select the
					// corresponding row
					// in
					// the table
					@Override
					public void widgetSelected(SelectionEvent e) {
						table.setSelection(new TableItem[] { cursor.getRow() });
					}

					// when the user hits "ENTER" in the TableCursor, pop up a
					// text
					// editor so that
					// they can change the text of the cell
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						final Text text = new Text(cursor, SWT.NONE);
						TableItem row = cursor.getRow();
						int rowIndex = table.indexOf(row);
						int columnIndex = cursor.getColumn();
						text
								.setText((String) cellFormulas[rowIndex][columnIndex]
										.getValue());
						text.addKeyListener(new KeyAdapter() {
							@Override
							public void keyPressed(KeyEvent e) {
								// close the text editor and copy the data over
								// when the user hits "ENTER"
								if (e.character == SWT.CR) {
									TableItem row = cursor.getRow();
									int rowIndex = table.indexOf(row);
									int columnIndex = cursor.getColumn();
									cellFormulas[rowIndex][columnIndex]
											.setValue(text.getText());
									text.dispose();
								}
								// close the text editor when the user hits
								// "ESC"
								if (e.character == SWT.ESC) {
									text.dispose();
								}
							}
						});
						editor.setEditor(text);
						text.setFocus();
					}
				});
				// Hide the TableCursor when the user hits the "MOD1" or "MOD2"
				// key.
				// This alows the user to select multiple items in the table.
				cursor.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.MOD1 || e.keyCode == SWT.MOD2
								|| (e.stateMask & SWT.MOD1) != 0
								|| (e.stateMask & SWT.MOD2) != 0) {
							cursor.setVisible(false);
						}
					}
				});
				// Show the TableCursor when the user releases the "MOD2" or
				// "MOD1" key.
				// This signals the end of the multiple selection task.
				table.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						if (e.keyCode == SWT.MOD1
								&& (e.stateMask & SWT.MOD2) != 0)
							return;
						if (e.keyCode == SWT.MOD2
								&& (e.stateMask & SWT.MOD1) != 0)
							return;
						if (e.keyCode != SWT.MOD1
								&& (e.stateMask & SWT.MOD1) != 0)
							return;
						if (e.keyCode != SWT.MOD2
								&& (e.stateMask & SWT.MOD2) != 0)
							return;

						TableItem[] selection = table.getSelection();
						TableItem row = (selection.length == 0) ? table
								.getItem(table.getTopIndex()) : selection[0];
						table.showItem(row);
						cursor.setSelection(row, 0);
						cursor.setVisible(true);
						cursor.setFocus();
					}
				});

				GridLayoutFactory.fillDefaults().generateLayout(shell);
				shell.setSize(400, 300);
				shell.open();

				// The SWT event loop
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});
		display.dispose();
	}

	private static String cellReference(int rowIndex, int columnIndex) {
		String cellReference = "" + ((char) ('A' + columnIndex))
				+ (rowIndex + 1);
		return cellReference;
	}

}
