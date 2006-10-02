/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.spreadsheet;

import java.text.NumberFormat;

import org.eclipse.jface.databinding.observable.value.ComputedValue;
import org.eclipse.jface.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.observable.value.WritableValue;
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
public class Spreadsheet {

	/**
	 * 
	 */
	private static final int NUM_COLUMNS = 26;

	/**
	 * 
	 */
	private static final int NUM_ROWS = 200;

	private static boolean DEBUG = false;

	static WritableValue[][] cellFormulas = new WritableValue[NUM_ROWS][NUM_COLUMNS];

	static ComputedValue[][] cellValues = new ComputedValue[NUM_ROWS][NUM_COLUMNS];

	static class ComputedCellValue extends ComputedValue {
		private final IObservableValue cellFormula;

		private boolean calculating;

		ComputedCellValue(IObservableValue cellFormula) {
			this.cellFormula = cellFormula;
		}

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
			if (DEBUG) {
				System.out.println("evaluating...");
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
		 */
		private double eval(String s) {
			if (s.length() == 0) {
				return 0;
			}
			char character = s.charAt(0);
			if (Character.isLetter(character)) {
				character = Character.toLowerCase(character);
				// reference to other cell
				int columnIndex = character - 'a';
				int rowIndex = Integer
						.parseInt(Character.toString(s.charAt(1))) - 1;
				String value = (String) cellValues[rowIndex][columnIndex]
						.getValue();
				return value.length() == 0 ? 0 : Double.parseDouble(value);
			}
			return Double.parseDouble(s);
		}
	}

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);

		final Table table = new Table(shell, SWT.BORDER | SWT.MULTI
				| SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		for (int i = 0; i < NUM_COLUMNS; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setText(Character.toString((char) ('A' + i)));
			tableColumn.setWidth(40);
		}
		for (int i = 0; i < NUM_ROWS; i++) {
			new TableItem(table, SWT.NONE);
			for (int j = 0; j < NUM_COLUMNS; j++) {
				cellFormulas[i][j] = new WritableValue(null);
				cellValues[i][j] = new ComputedCellValue(cellFormulas[i][j]);
				cellFormulas[i][j].setValue("");
			}
		}

		new TableUpdater(table) {
			protected void updateItem(TableItem item) {
				int rowIndex = item.getParent().indexOf(item);
				if (DEBUG) {
					System.out.println("updating row " + rowIndex);
				}
				for (int j = 0; j < NUM_COLUMNS; j++) {
					item
							.setText(j, (String) cellValues[rowIndex][j]
									.getValue());
				}
			}
		};

		// create a TableCursor to navigate around the table
		final TableCursor cursor = new TableCursor(table, SWT.NONE);
		// create an editor to edit the cell when the user hits "ENTER"
		// while over a cell in the table
		final ControlEditor editor = new ControlEditor(cursor);
		editor.grabHorizontal = true;
		editor.grabVertical = true;

		cursor.addSelectionListener(new SelectionAdapter() {
			// when the TableEditor is over a cell, select the corresponding row
			// in
			// the table
			public void widgetSelected(SelectionEvent e) {
				table.setSelection(new TableItem[] { cursor.getRow() });
			}

			// when the user hits "ENTER" in the TableCursor, pop up a text
			// editor so that
			// they can change the text of the cell
			public void widgetDefaultSelected(SelectionEvent e) {
				final Text text = new Text(cursor, SWT.NONE);
				TableItem row = cursor.getRow();
				int rowIndex = table.indexOf(row);
				int columnIndex = cursor.getColumn();
				text.setText((String) cellFormulas[rowIndex][columnIndex]
						.getValue());
				text.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						// close the text editor and copy the data over
						// when the user hits "ENTER"
						if (e.character == SWT.CR) {
							TableItem row = cursor.getRow();
							int rowIndex = table.indexOf(row);
							int columnIndex = cursor.getColumn();
							cellFormulas[rowIndex][columnIndex].setValue(text
									.getText());
							text.dispose();
						}
						// close the text editor when the user hits "ESC"
						if (e.character == SWT.ESC) {
							text.dispose();
						}
					}
				});
				editor.setEditor(text);
				text.setFocus();
			}
		});
		// Hide the TableCursor when the user hits the "MOD1" or "MOD2" key.
		// This alows the user to select multiple items in the table.
		cursor.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.MOD1 || e.keyCode == SWT.MOD2
						|| (e.stateMask & SWT.MOD1) != 0
						|| (e.stateMask & SWT.MOD2) != 0) {
					cursor.setVisible(false);
				}
			}
		});
		// Show the TableCursor when the user releases the "MOD2" or "MOD1" key.
		// This signals the end of the multiple selection task.
		table.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.MOD1 && (e.stateMask & SWT.MOD2) != 0)
					return;
				if (e.keyCode == SWT.MOD2 && (e.stateMask & SWT.MOD1) != 0)
					return;
				if (e.keyCode != SWT.MOD1 && (e.stateMask & SWT.MOD1) != 0)
					return;
				if (e.keyCode != SWT.MOD2 && (e.stateMask & SWT.MOD2) != 0)
					return;

				TableItem[] selection = table.getSelection();
				TableItem row = (selection.length == 0) ? table.getItem(table
						.getTopIndex()) : selection[0];
				table.showItem(row);
				cursor.setSelection(row, 0);
				cursor.setVisible(true);
				cursor.setFocus();
			}
		});

		GridLayoutFactory.fillDefaults().generateLayout(shell);
		shell.open();

		// The SWT event loop
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

}
