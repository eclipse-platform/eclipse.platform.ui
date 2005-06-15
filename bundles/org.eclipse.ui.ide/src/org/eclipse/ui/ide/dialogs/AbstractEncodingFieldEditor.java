/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide.dialogs;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * The abstract superclass of field editors used to set an encoding. 
 * Any user entered encodings will be added to the list of encodings available via {@link org.eclipse.ui.ide.IDEEncoding}.
 * <p>
 * Subclasses may extend, but must call <code>createEncodingGroup</code> during <code>doFillIntoGrid</code>.
 * </p>
 * 
 * @see org.eclipse.ui.ide.IDEEncoding
 * @since 3.1
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
	 * Creates a new encoding field editor with no settings set.
	 */
	protected AbstractEncodingFieldEditor() {
		super();
	}

	/**
	 * Creates a new encoding field editor with the given preference name, label and parent.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
	 */
	protected AbstractEncodingFieldEditor(String name, String labelText, Composite parent) {
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
			updateEncodingState(resourcePreference == null);
		}
	}

	/**
	 * Returns the value that is currently stored for the encoding.
     * 
	 * @return the currently stored encoding
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
		defaultEncodingButton.setText(defaultButtonText()); //$NON-NLS-1$
	}

	private Composite getContainer() {
		return container;
	}

	/**
	 * Creates a composite with all the encoding controls.
     * <p>
     * Subclasses may extend.
     * </p>
     * 
	 * @param parent the parent widget
	 * @param numColumns the number of columns in the parent
	 * @return the group control
	 */
	protected Composite createEncodingGroup(Composite parent, int numColumns) {

		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(IDEWorkbenchMessages.WorkbenchPreference_encoding);
		group.setFont(font);

		SelectionAdapter buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEncodingState(defaultEncodingButton.getSelection());
				updateValidState();
			}
		};

		defaultEncodingButton = new Button(group, SWT.RADIO);
		defaultEnc = findDefaultEncoding();
		defaultEncodingButton.setText(defaultButtonText()); 
		data = new GridData();
		data.horizontalSpan = 2;
		defaultEncodingButton.setLayoutData(data);
		defaultEncodingButton.addSelectionListener(buttonListener);
		defaultEncodingButton.setFont(font);

		otherEncodingButton = new Button(group, SWT.RADIO);
		otherEncodingButton.setText(IDEWorkbenchMessages.WorkbenchPreference_otherEncoding);
		otherEncodingButton.addSelectionListener(buttonListener);
		otherEncodingButton.setFont(font);

		encodingCombo = new Combo(group, SWT.NONE);
		data = new GridData();
		encodingCombo.setFont(font);
		encodingCombo.setLayoutData(data);
		encodingCombo.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updateValidState();
			}
		});
		encodingCombo.addKeyListener(new KeyAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyReleased(KeyEvent e) {
				updateValidState();
			}
		});


		return group;
	}

	/**
	 * Returns the default encoding for the object being shown.
     * 
	 * @return the default encoding for the object being shown
	 */
	protected String findDefaultEncoding() {
		return WorkbenchEncoding.getWorkbenchDefaultEncoding();
	}

	/**
	 * Returns the text for the default encoding button.
     * 
	 * @return the text for the default encoding button 
	 */
	protected String defaultButtonText() {
		return NLS.bind(IDEWorkbenchMessages.WorkbenchPreference_defaultEncoding, defaultEnc);
	}

	/**
	 * Populates the encodings combo. Sets the text based on the
	 * selected encoding. If there is no selected encoding, the text is set to the default encoding.
     * 
	 * @param encodings the list of encodings (list of String)
	 * @param selectedEncoding the selected encoding, or <code>null</code>
	 */
	private void populateEncodingsCombo(List encodings, String selectedEncoding) {
		String[] encodingStrings = new String[encodings.size()];
		encodings.toArray(encodingStrings);
		encodingCombo.setItems(encodingStrings);

		if (selectedEncoding == null)
			encodingCombo.setText(getDefaultEnc());
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
				showErrorMessage(IDEWorkbenchMessages.WorkbenchPreference_unsupportedEncoding);
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
	 * Returns the currently selected encoding.
     * 
	 * @return the currently selected encoding
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

	/**
	 * Returns whether or not the given encoding is valid.
     * 
	 * @param enc the encoding to validate
	 * @return <code>true</code> if the encoding is valid, <code>false</code> otherwise
	 */
	private boolean isValidEncoding(String enc) {
		try {
			return Charset.isSupported(enc);
		} catch (IllegalCharsetNameException e) {
			//This is a valid exception
			return false;
		}
		
	}

	/**
	 * Returns the default encoding.
     * 
	 * @return the default encoding
	 */
	protected String getDefaultEnc() {
		return defaultEnc;
	}

	/**
	 * Returns whether or not the encoding setting changed.
     * 
	 * @param encodingSetting the setting from the page.
	 * @return boolean <code>true</code> if the resource encoding
	 *   is the same as before.
	 */
	protected boolean hasSameEncoding(String encodingSetting) {

		String current = getStoredValue();

		if (encodingSetting == null) {
			//Changed if default is selected and there is no setting
			return current == null || current.length() == 0;
		}
		return encodingSetting.equals(current);
	}

	/**
	 * Return whether or not the default has been selected.
	 * @return <code>true</code> if the default button has been
	 * selected.
	 */
	boolean isDefaultSelected() {
		return defaultEncodingButton.getSelection();
	}

}
