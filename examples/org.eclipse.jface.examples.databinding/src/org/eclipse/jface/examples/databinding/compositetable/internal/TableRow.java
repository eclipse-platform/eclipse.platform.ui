/*
 * Copyright (C) 2005 David Orme <djo@coconut-palm-software.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme     - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.compositetable.internal;

import org.eclipse.jface.examples.databinding.compositetable.InternalCompositeTable;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Class TableRow.  Encapsulates operations on a SWT row control.  Discovers the 
 * SWT controls inside the row control representing columns and exposes those for
 * operations by the CompositeTable.  Listens to SWT events on the column controls
 * and forwards them back to the table control for processing.
 * 
 * @author djo
 */
public class TableRow {
	private Control row;
	private Control[] columns;
	protected InternalCompositeTable parent;
	
	/**
	 * Constructor TableRow.  Construct a TableRow object.
	 * 
	 * @param parent The table containing this row.
	 * @param row The SWT control implementing this row.
	 */
	public TableRow(InternalCompositeTable parent, Control row) {
		this.parent = parent;
		this.row = row;
		if (row instanceof Composite) {
			Composite rowComposite = (Composite) row;
			columns = rowComposite.getTabList();
		} else {
			columns = new Control[] {row};
		}
		
		for (int i = 0; i < columns.length; i++) {
			addListeners(columns[i]);
		}
	}
	
	/**
	 * Remove all listeners from each control.
	 */
	public void dispose() {
		for (int i = 0; i < columns.length; i++) {
			removeListeners(columns[i]);
		}
	}
	
	/**
	 * Add listeners to each control.
	 * 
	 * @param control The control to listen to.
	 */
	private void addListeners(Control control) {
		control.addKeyListener(keyListener);
		control.addFocusListener(focusListener);
		control.addTraverseListener(traverseListener);
	}
	
	/**
	 * Remove listeners from each control.
	 * 
	 * @param control The control to no longer listen to.
	 */
	private void removeListeners(Control control) {
		control.removeKeyListener(keyListener);
		control.removeFocusListener(focusListener);
		control.removeTraverseListener(traverseListener);
	}
	
	/**
	 * Forward key presses to the parent control
	 */
	private KeyListener keyListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			parent.keyPressed(TableRow.this, e);
		}
	};
	
	/**
	 * Forward focuse events to the parent control
	 */
	private FocusListener focusListener = new FocusAdapter() {
		public void focusLost(FocusEvent e) {
			parent.focusLost(TableRow.this, e);
		}
		public void focusGained(FocusEvent e) {
			parent.focusGained(TableRow.this, e);
		}
	};
	
	/**
	 * Forward traverse events to the parent control
	 */
	private TraverseListener traverseListener = new TraverseListener() {
		public void keyTraversed(TraverseEvent e) {
			parent.keyTraversed(TableRow.this, e);
		}
	};

	/**
	 * Return the SWT control implementing the row's GUI.
	 * 
	 * @return The row's SWT control
	 */
	public Control getRowControl() {
		return row;
	}
	
	/**
	 * Return the SWT control corresponding to a particular column within
	 * this row.
	 * 
	 * @param i the 0-based offset of the column to return.
	 * @return The corresponding control
	 */
	public Control getColumnControl(int i) {
		return columns[i];
	}
	
	/**
	 * Return the column number of a specified SWT control or -1 if not found.
	 * 
	 * @param control The control to find.
	 * @return control's column number or -1 if that column control is not in this row.
	 */
	public int getColumnNumber(Control control) {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == control) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Return the number of columns in this row.
	 * 
	 * @return The number of columns in this row.
	 */
	public int getNumColumns() {
		return columns.length;
	}
	
	/**
	 * Sets the visibility of this row.
	 * 
	 * @param visible true if the row should be visible; false otherwise.
	 */
	public void setVisible(boolean visible) {
		row.setVisible(visible);
	}
	
	/**
	 * Returns if this row is visible.
	 * 
	 * @return true if the row is visible; false otherwise.
	 */
	public boolean getVisible() {
		return row.getVisible();
	}
}