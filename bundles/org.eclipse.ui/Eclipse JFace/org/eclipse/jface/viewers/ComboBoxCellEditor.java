package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.text.MessageFormat;

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
	super(parent);
	Assert.isNotNull(items);
	this.items = items;
	selection = 0;
	populateComboBoxItems();
}
/* (non-Javadoc)
 * Method declared on CellEditor.
 */
protected Control createControl(Composite parent) {
	
	comboBox = new CCombo(parent, SWT.NONE);
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
			boolean newValidState = isCorrect(newValue);
			if (newValidState) {
				doSetValue(newValue);
			} else {
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
 * minimum width of the cell to 30 pixels to make sure the
 * arrow button is visible even when the list contains long
 * strings.
 */
public LayoutData getLayoutData() {
	LayoutData layoutData = super.getLayoutData();
	layoutData.minimumWidth = Math.max(30, layoutData.minimumWidth);
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
 * Add the items to the combo box.
 */
private void populateComboBoxItems() {
	if (comboBox != null && items != null) {
		for (int i = 0; i < items.length; i++)
			comboBox.add(items[i], i);

		setValueValid(true);
	}
}
}
