/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 214392 missing implementation of ComboFieldEditor.setEnabled
 *     Pierre-Yves B. <pyvesdev@gmail.com> - Bug 497619 - ComboFieldEditor doesnt fire PropertyChangeEvent for doLoadDefault and doLoad
 *******************************************************************************/
package org.eclipse.jface.preference;


import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A field editor for a combo box that allows the drop-down selection of one of
 * a list of items.
 *
 * @since 3.3
 */
public class ComboFieldEditor extends FieldEditor {

	/**
	 * The <code>Combo</code> widget.
	 */
	private Combo fCombo;

	/**
	 * The value (not the name) of the currently selected item in the Combo widget.
	 */
	private String fValue;

	/**
	 * The names (labels) and underlying values to populate the combo widget.  These should be
	 * arranged as: { {name1, value1}, {name2, value2}, ...}
	 */
	private final String[][] fEntryNamesAndValues;

	/**
	 * Create the combo box field editor.
	 *
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param entryNamesAndValues the names (labels) and underlying values to populate the combo widget.  These should be
	 * arranged as: { {name1, value1}, {name2, value2}, ...}
	 * @param parent the parent composite
	 */
	public ComboFieldEditor(String name, String labelText, String[][] entryNamesAndValues, Composite parent) {
		init(name, labelText);
		Assert.isTrue(checkArray(entryNamesAndValues));
		fEntryNamesAndValues = entryNamesAndValues;
		createControl(parent);
	}

	/**
	 * Checks whether given <code>String[][]</code> contains sub arrays with minimum size 2
	 *
	 * @return <code>true</code> if it is ok, and <code>false</code> otherwise
	 */
	private static boolean checkArray(String[][] table) {
		if (table == null) {
			return false;
		}
		for (String[] array : table) {
			if (array == null || array.length < 2) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		if (numColumns > 1) {
			Control control = getLabelControl();
			int left = numColumns;
			if (control != null) {
				((GridData)control.getLayoutData()).horizontalSpan = 1;
				left = left - 1;
			}
			((GridData)fCombo.getLayoutData()).horizontalSpan = left;
		} else {
			Control control = getLabelControl();
			if (control != null) {
				((GridData)control.getLayoutData()).horizontalSpan = 1;
			}
			((GridData)fCombo.getLayoutData()).horizontalSpan = 1;
		}
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		int comboC = 1;
		if (numColumns > 1) {
			comboC = numColumns - 1;
		}
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = 1;
		control.setLayoutData(gd);
		control = getComboBoxControl(parent);
		gd = new GridData();
		gd.horizontalSpan = comboC;
		gd.horizontalAlignment = GridData.FILL;
		control.setLayoutData(gd);
		control.setFont(parent.getFont());
	}

	@Override
	protected void doLoad() {
		updateComboForValue(getPreferenceStore().getString(getPreferenceName()));
	}

	@Override
	protected void doLoadDefault() {
		String oldValue = fValue;
		updateComboForValue(getPreferenceStore().getDefaultString(getPreferenceName()));
		valueChanged(oldValue, fValue);
	}

	@Override
	protected void doStore() {
		if (fValue == null) {
			getPreferenceStore().setToDefault(getPreferenceName());
			return;
		}
		getPreferenceStore().setValue(getPreferenceName(), fValue);
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/*
	 * Lazily create and return the Combo control.
	 */
	private Combo getComboBoxControl(Composite parent) {
		if (fCombo == null) {
			fCombo = new Combo(parent, SWT.READ_ONLY);
			fCombo.setFont(parent.getFont());
			for (int i = 0; i < fEntryNamesAndValues.length; i++) {
				fCombo.add(fEntryNamesAndValues[i][0], i);
			}

			fCombo.addSelectionListener(widgetSelectedAdapter(evt -> {
				String oldValue = fValue;
				String name = fCombo.getText();
				fValue = getValueForName(name);
				setPresentsDefaultValue(false);
				valueChanged(oldValue, fValue);
			}));
		}
		return fCombo;
	}

	/*
	 * Given the name (label) of an entry, return the corresponding value.
	 */
	private String getValueForName(String name) {
		for (String[] entry : fEntryNamesAndValues) {
			if (name.equals(entry[0])) {
				return entry[1];
			}
		}
		return fEntryNamesAndValues[0][0];
	}

	/*
	 * Set the name in the combo widget to match the specified value.
	 */
	private void updateComboForValue(String value) {
		fValue = value;
		for (String[] fEntryNamesAndValue : fEntryNamesAndValues) {
			if (value.equals(fEntryNamesAndValue[1])) {
				fCombo.setText(fEntryNamesAndValue[0]);
				return;
			}
		}
		if (fEntryNamesAndValues.length > 0) {
			fValue = fEntryNamesAndValues[0][1];
			fCombo.setText(fEntryNamesAndValues[0][0]);
		}
	}

	/**
	 * Informs this field editor's listener, if it has one, about a change to the
	 * value (<code>VALUE</code> property) provided that the old and new values are
	 * different.
	 *
	 * @param oldValue the old value
	 * @param newValue the new value
	 * @since 3.18
	 */
	protected void valueChanged(String oldValue, String newValue) {
		// Only fire event if old and new values are different.
		if (oldValue != null && !oldValue.equals(newValue) || newValue != null) {
			fireValueChanged(VALUE, oldValue, newValue);
		}
	}

	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getComboBoxControl(parent).setEnabled(enabled);
	}
}
