/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide.dialogs;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;

import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * The abstract superclass of editors used to set an 
 * enconding. Any user entered
 * encodings will be added to the list of encodings in the 
 * IDEEncoding.
 * @since 3.1
 * @see org.eclipse.ui.ide.IDEEncoding
 */
public abstract class AbstractEncodingFieldEditor extends FieldEditor {
	private Composite container;

	private Button defaultEncodingButton;

	private String defaultEnc;

	private Button otherEncodingButton;

	private Combo encodingCombo;

	private boolean isValid = true;

	private String oldSelectedEncoding;

	/**
	 * Create an instance of the receiver with no parameters
	 * set.
	 */
	public AbstractEncodingFieldEditor() {
		super();
	}

	/**
	 * Create a new instance of the receiver on the preference called name
	 * with a label of labelText.
	 * @param name
	 * @param labelText
	 * @param parent
	 */
	public AbstractEncodingFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {
		((GridData) getContainer().getLayoutData()).horizontalSpan = numColumns;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		container = createEncodingGroup(parent, numColumns);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	protected void doLoad() {
		if (encodingCombo != null) {
			List encodings = IDEEncoding.getIDEEncodings();
			String resourcePreference = getStoredValue();
			populateEncodingsCombo(encodings, resourcePreference);
			updateEncodingState(resourcePreference == null || resourcePreference.equals(defaultEnc));
		}
	}

	/**
	 * Get the value that is current stored for the encoding.
	 * @return String
	 */
	protected abstract String getStoredValue();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		updateEncodingState(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#isValid()
	 */
	public boolean isValid() {
		return isValid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
	 */
	protected void refreshValidState() {
		updateValidState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#setPreferenceStore(org.eclipse.jface.preference.IPreferenceStore)
	 */
	public void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		defaultEnc = store.getDefaultString(getPreferenceName());
		updateDefaultEncoding();
	}

	private void updateDefaultEncoding() {
		defaultEncodingButton.setText(IDEWorkbenchMessages.format(
				"WorkbenchPreference.defaultEncoding", new String[] { defaultEnc })); //$NON-NLS-1$
	}

	private Composite getContainer() {
		return container;
	}

	private Group createEncodingGroup(Composite parent, int numColumns) {

		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.encoding")); //$NON-NLS-1$
		group.setFont(font);

		SelectionAdapter buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEncodingState(defaultEncodingButton.getSelection());
				updateValidState();
			}
		};

		defaultEncodingButton = new Button(group, SWT.RADIO);
		defaultEnc = WorkbenchEncoding.getWorkbenchDefaultEncoding();
		defaultEncodingButton.setText(IDEWorkbenchMessages.format(
				"WorkbenchPreference.defaultEncoding", new String[] { defaultEnc })); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		defaultEncodingButton.setLayoutData(data);
		defaultEncodingButton.addSelectionListener(buttonListener);
		defaultEncodingButton.setFont(font);

		otherEncodingButton = new Button(group, SWT.RADIO);
		otherEncodingButton.setText(IDEWorkbenchMessages
				.getString("WorkbenchPreference.otherEncoding")); //$NON-NLS-1$
		otherEncodingButton.addSelectionListener(buttonListener);
		otherEncodingButton.setFont(font);

		encodingCombo = new Combo(group, SWT.NONE);
		data = new GridData();
		encodingCombo.setFont(font);
		encodingCombo.setLayoutData(data);
		encodingCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateValidState();
			}
		});

		return group;
	}

	/**
	 * Populate the encodings combo. Set the text based on the
	 * selectedEncoding. If selectedEncoding is null set it to the
	 * default.
	 * @param encodings
	 * @param selectedEncoding
	 */
	private void populateEncodingsCombo(List encodings, String selectedEncoding) {
		String[] encodingStrings = new String[encodings.size()];
		encodings.toArray(encodingStrings);
		encodingCombo.setItems(encodingStrings);

		if (selectedEncoding == null)
			encodingCombo.setText(WorkbenchEncoding.getWorkbenchDefaultEncoding());
		else
			encodingCombo.setText(selectedEncoding);
	}

	private void updateEncodingState(boolean useDefault) {
		defaultEncodingButton.setSelection(useDefault);
		otherEncodingButton.setSelection(!useDefault);
		encodingCombo.setEnabled(!useDefault);
		updateValidState();
	}

	private void updateValidState() {
		boolean isValidNow = isEncodingValid();
		if (isValidNow != isValid) {
			isValid = isValidNow;
			if (isValid) {
				clearErrorMessage();
			} else {
				showErrorMessage(IDEWorkbenchMessages
						.getString("WorkbenchPreference.unsupportedEncoding")); //$NON-NLS-1$
			}
			fireStateChanged(IS_VALID, !isValid, isValid);

			String newValue = getSelectedEncoding();
			if (isValid && !newValue.equals(oldSelectedEncoding)) {
				fireValueChanged(VALUE, oldSelectedEncoding, newValue);
				oldSelectedEncoding = newValue;
			}
		}
	}

	/**
	 * Get the encoding current selected.
	 * @return String
	 */
	protected String getSelectedEncoding() {
		if (defaultEncodingButton.getSelection()) {
			return defaultEnc;
		}
		return encodingCombo.getText();
	}

	private boolean isEncodingValid() {
		return defaultEncodingButton.getSelection() || isValidEncoding(encodingCombo.getText());
	}

	private boolean isValidEncoding(String enc) {
		try {
			new String(new byte[0], enc);
			return true;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}

	/**
	 * Return the default encoding.
	 * @return String
	 */
	protected String getDefaultEnc() {
		return defaultEnc;
	}

	/**
	 * Return whether or not the encoding setting changed.
	 * @param encodingSetting the setting from the page.
	 * @return boolean <code>true</code> if the resource encoding
	 * is the same as before.
	 */
	protected boolean hasSameEncoding(String encodingSetting) {

		String current = getStoredValue();

		if (encodingSetting == null) {
			//Changed if default is selected and there is no setting
			return current == null || current.length() == 0;
		}
		return encodingSetting.equals(current);
	}

}
