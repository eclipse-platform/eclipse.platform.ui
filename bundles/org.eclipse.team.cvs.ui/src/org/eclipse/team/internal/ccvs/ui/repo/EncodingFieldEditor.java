/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;

/**
 * A field editor that allows the user to choose an encoding.
 */
public class EncodingFieldEditor extends FieldEditor {
	private Composite container;
	private Button defaultEncodingButton;
	private String defaultEnc;
	private Button otherEncodingButton;
	private Combo encodingCombo;
	private boolean isValid = true;
	private String oldSelectedEncoding;
	
	/**
	 * Create an encoding filed editor.
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 */
	public EncodingFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {
		((GridData)getContainer().getLayoutData()).horizontalSpan = numColumns;
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
			String value = getPreferenceStore().getString(getPreferenceName());
			if (value.equals(defaultEnc)) {
				doLoadDefault();
			} else {
				encodingCombo.setText(value);
				oldSelectedEncoding = value;
				updateEncodingState(false);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		updateEncodingState(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {
		String encoding = getSelectedEncoding();
		if (encoding.equals(defaultEnc)) {
			getPreferenceStore().setToDefault(getPreferenceName());
		} else {
			getPreferenceStore().setValue(getPreferenceName(), encoding);
		}
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
		defaultEncodingButton.setText(Policy.bind("WorkbenchPreference.defaultEncoding", defaultEnc)); //$NON-NLS-1$
	}

	private Composite getContainer() {
		return container;
	}
	
	private Group createEncodingGroup(Composite parent, int numColumns) {
		
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = numColumns;
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(getLabelText());
		group.setFont(font);
		
		SelectionAdapter buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEncodingState(defaultEncodingButton.getSelection());
				updateValidState();
			}
		};
		
		if (defaultEnc == null) {
			defaultEnc = WorkbenchEncoding.getWorkbenchDefaultEncoding();
		}
		defaultEncodingButton = new Button(group, SWT.RADIO);
		updateDefaultEncoding();
		data = new GridData();
		data.horizontalSpan = 2;
		defaultEncodingButton.setLayoutData(data);
		defaultEncodingButton.addSelectionListener(buttonListener);
		defaultEncodingButton.setFont(font);
		
		otherEncodingButton = new Button(group, SWT.RADIO);
		otherEncodingButton.setText(Policy.bind("WorkbenchPreference.otherEncoding")); //$NON-NLS-1$
		otherEncodingButton.addSelectionListener(buttonListener);
		otherEncodingButton.setFont(font);
		
		encodingCombo = new Combo(group, SWT.NONE);
		encodingCombo.setFont(font);
		data = new GridData();
		data.widthHint = convertWidthInCharsToPixels(encodingCombo, 15);
		encodingCombo.setLayoutData(data);
		encodingCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateValidState();
			}
		});
		encodingCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateValidState();
			}
		});

		java.util.List encodings = IDEEncoding.getIDEEncodings();
		
		if (!encodings.contains(defaultEnc)) {
			encodings.add(defaultEnc);
		}

		String enc = IDEEncoding.getResourceEncoding();
		boolean isDefault = enc == null || enc.length() == 0;

		Collections.sort(encodings);
		for (int i = 0; i < encodings.size(); ++i) {
			encodingCombo.add((String) encodings.get(i));
		}

		encodingCombo.setText(isDefault ? defaultEnc : enc);
		
		updateEncodingState(isDefault);
		return group;
	}
	
	private int convertWidthInCharsToPixels(Control control, int chars) {
		GC gc= new GC(control);
		gc.setFont(control.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		int result = Dialog.convertWidthInCharsToPixels(fontMetrics, chars);
		gc.dispose();
		return result;
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
				showErrorMessage(Policy.bind("WorkbenchPreference.unsupportedEncoding")); //$NON-NLS-1$
			}
			fireStateChanged(IS_VALID, !isValid, isValid);
			
			String newValue = getSelectedEncoding();
			if (isValid && !newValue.equals(oldSelectedEncoding)) {
				fireValueChanged(VALUE, oldSelectedEncoding, newValue);
				oldSelectedEncoding = newValue;
			}
		}
	}
	
	private String getSelectedEncoding() {
		if (defaultEncodingButton.getSelection()) {
			return defaultEnc;
		}
		else {
			return encodingCombo.getText();
		}
	}

	private boolean isEncodingValid() {
		return defaultEncodingButton.getSelection() ||
		isValidEncoding(encodingCombo.getText());
	}
	
	private boolean isValidEncoding(String enc) {
		try {
			new String(new byte[0], enc);
			return true;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}
}
