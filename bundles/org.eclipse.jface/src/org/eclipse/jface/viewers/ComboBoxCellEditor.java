package org.eclipse.jface.viewers;

/**********************************************************************
Copyright (c) 2000, 2002 International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/

import java.text.MessageFormat;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor that presents a list of items in a combo box.
 * The cell editor's value is the zero-based index of the selected
 * item.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class ComboBoxCellEditor extends CellEditor {

	/**
	 * The list of items to present in the combo box.
	 */
	private String[] items;

	/**
	 * The zero-based index of the selected item.
	 */
	private int selection;

	/**
	 * The custom combo box control.
	 */
	private CCombo comboBox;

	/**
	 * Default ComboBoxCellEditor style
	 */
	private static final int defaultStyle = SWT.NONE;

/**
 * Creates a new cell editor with no control and no  st of choices. Initially,
 * the cell editor has no cell validator.
 * 
 * @since 2.1
 * @see #setStyle
 * @see #create
 * @see #setItems
 * @see #dispose
 */
public ComboBoxCellEditor() {
	setStyle(defaultStyle);
}

/**
 * Creates a new cell editor with a combo containing the given 
 * list of choices and parented under the given control. The cell
 * editor value is the zero-based index of the selected item.
 * Initially, the cell editor has no cell validator and
 * the first item in the list is selected. 
 *
 * @param parent the parent control
 * @param items the list of strings for the combo box
 */
public ComboBoxCellEditor(Composite parent, String[] items) {
	this(parent, items, defaultStyle);
}

/**
 * Creates a new cell editor with a combo containing the given 
 * list of choices and parented under the given control. The cell
 * editor value is the zero-based index of the selected item.
 * Initially, the cell editor has no cell validator and
 * the first item in the list is selected. 
 *
 * @param parent the parent control
 * @param items the list of strings for the combo box
 * @param style the style bits
 * @since 2.1
 */
public ComboBoxCellEditor(Composite parent, String[] items, int style) {
	super(parent, style);
	setItems(items);
}

/**
 * Returns the list of choices for the combo box
 *
 * @return the list of choices for the combo box
 */
public String[] getItems() {
	return this.items;
}

/**
 * Sets the list of choices for the combo box
 *
 * @param items the list of choices for the combo box
 */
public void setItems(String[] items) {
	Assert.isNotNull(items);
	this.items = items;
	populateComboBoxItems();
}

/* (non-Javadoc)
 * Method declared on CellEditor.
 */
protected Control createControl(Composite parent) {
	
	comboBox = new CCombo(parent, getStyle());
	comboBox.setFont(parent.getFont());

	comboBox.addKeyListener(new KeyAdapter() {
		// hook key pressed - see PR 14201  
		public void keyPressed(KeyEvent e) {
			keyReleaseOccured(e);
		}
	});

	comboBox.addSelectionListener(new SelectionAdapter() {
		public void widgetDefaultSelected(SelectionEvent event) {
			// must set the selection before getting value
			selection = comboBox.getSelectionIndex();
			Object newValue = doGetValue();
			markDirty();
			boolean isValid = isCorrect(newValue);
			setValueValid(isValid);
			if (!isValid) {
				// try to insert the current value into the error message.
				setErrorMessage(
					MessageFormat.format(getErrorMessage(), new Object[] {items[selection]})); 
			}
			fireApplyEditorValue();
		}
	});

	comboBox.addTraverseListener(new TraverseListener() {
		public void keyTraversed(TraverseEvent e) {
			if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
			}
		}
	});

	return comboBox;
}

/**
 * The <code>ComboBoxCellEditor</code> implementation of
 * this <code>CellEditor</code> framework method returns
 * the zero-based index of the current selection.
 *
 * @return the zero-based index of the current selection wrapped
 *  as an <code>Integer</code>
 */
protected Object doGetValue() {
	return new Integer(selection);
}

/* (non-Javadoc)
 * Method declared on CellEditor.
 */
protected void doSetFocus() {
	comboBox.setFocus();
}

/**
 * The <code>ComboBoxCellEditor</code> implementation of
 * this <code>CellEditor</code> framework method sets the 
 * minimum width of the cell to 50 pixels to make sure the
 * arrow button and some text is visible. The list of CCombo
 * will be wide enough to show its longest item.
 */
public LayoutData getLayoutData() {
	LayoutData layoutData = super.getLayoutData();
	layoutData.minimumWidth = 50;
	return layoutData;
}

/**
 * The <code>ComboBoxCellEditor</code> implementation of
 * this <code>CellEditor</code> framework method
 * accepts a zero-based index of a selection.
 *
 * @param value the zero-based index of the selection wrapped
 *   as an <code>Integer</code>
 */
protected void doSetValue(Object value) {
	Assert.isTrue(comboBox != null && (value instanceof Integer));
	selection = ((Integer) value).intValue();
	comboBox.select(selection);
}

/**
 * Updates the list of choices for the combo box for the current control.
 */
private void populateComboBoxItems() {
	if (comboBox != null && items != null) {
		comboBox.removeAll();
		for (int i = 0; i < items.length; i++)
			comboBox.add(items[i], i);

		setValueValid(true);
		selection = 0;
	}
}
}
