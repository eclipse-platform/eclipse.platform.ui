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
package org.eclipse.ui.internal.ide.dialogs;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.internal.dialogs.EditorsPreferencePage;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Extends the Editors preference page with IDE-specific settings.
 * 
 * Note: want IDE settings to appear in main Editors preference page (via
 * subclassing), however the superclass, EditorsPreferencePage, is internal
 */
public class IDEEditorsPreferencePage extends EditorsPreferencePage {

	// State for encoding group
	private String defaultEnc;

	private Button defaultEncodingButton;

	private Button otherEncodingButton;

	private Combo encodingCombo;

	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent);

		createEditorHistoryGroup(composite);

		createSpace(composite);
		createShowMultipleEditorTabsPref(composite);
		createCloseEditorsOnExitPref(composite);
		createEditorReuseGroup(composite);

		createSpace(composite);
		createEncodingGroup(composite);

		updateValidState();

		// @issue need IDE-level help for this page
		//		WorkbenchHelp.setHelp(parent,
		// IHelpContextIds.WORKBENCH_EDITOR_PREFERENCE_PAGE);

		return composite;
	}

	private void createEncodingGroup(Composite parent) {

		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(IDEWorkbenchMessages
				.getString("WorkbenchPreference.encoding")); //$NON-NLS-1$
		group.setFont(font);

		SelectionAdapter buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEncodingState(defaultEncodingButton.getSelection());
				updateValidState();
			}
		};

		defaultEncodingButton = new Button(group, SWT.RADIO);
		defaultEnc = System.getProperty("file.encoding", "UTF-8"); //$NON-NLS-1$  //$NON-NLS-2$
		defaultEncodingButton
				.setText(IDEWorkbenchMessages
						.format(
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
		data.widthHint = convertWidthInCharsToPixels(15);
		encodingCombo.setFont(font);
		encodingCombo.setLayoutData(data);
		encodingCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateValidState();
			}
		});

		ArrayList encodings = new ArrayList();
		int n = 0;
		try {
			n = Integer.parseInt(IDEWorkbenchMessages
					.getString("WorkbenchPreference.numDefaultEncodings")); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			// Ignore;
		}
		for (int i = 0; i < n; ++i) {
			String enc = IDEWorkbenchMessages.getString(
					"WorkbenchPreference.defaultEncoding" + (i + 1), null); //$NON-NLS-1$
			if (enc != null) {
				encodings.add(enc);
			}
		}

		if (!encodings.contains(defaultEnc)) {
			encodings.add(defaultEnc);
		}

		String enc = ResourcesPlugin.getPlugin().getPluginPreferences()
				.getString(ResourcesPlugin.PREF_ENCODING);
		boolean isDefault = enc == null || enc.length() == 0;

		if (!isDefault && !encodings.contains(enc)) {
			encodings.add(enc);
		}
		Collections.sort(encodings);
		for (int i = 0; i < encodings.size(); ++i) {
			encodingCombo.add((String) encodings.get(i));
		}

		encodingCombo.setText(isDefault ? defaultEnc : enc);

		updateEncodingState(isDefault);
	}

	protected void updateValidState() {
		super.updateValidState();
		if (!isValid()) {
			return;
		}
		if (!isEncodingValid()) {
			setErrorMessage(IDEWorkbenchMessages
					.getString("WorkbenchPreference.unsupportedEncoding")); //$NON-NLS-1$
			setValid(false);
		}
	}

	private boolean isEncodingValid() {
		return defaultEncodingButton.getSelection()
				|| isValidEncoding(encodingCombo.getText());
	}

	private boolean isValidEncoding(String enc) {
		try {
			new String(new byte[0], enc);
			return true;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}

	private void updateEncodingState(boolean useDefault) {
		defaultEncodingButton.setSelection(useDefault);
		otherEncodingButton.setSelection(!useDefault);
		encodingCombo.setEnabled(!useDefault);
		updateValidState();
	}

	/**
	 * The default button has been pressed.
	 */
	protected void performDefaults() {
		updateEncodingState(true);
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.dialogs.FileEditorsPreferencePage#performOk()
	 */
	public boolean performOk() {

		if (hasNewEncoding()) {
			// set the workspace text file encoding
			String enc = defaultEncodingButton.getSelection() ? null
					: encodingCombo.getText();
			try {
				ResourcesPlugin.getWorkspace().getRoot().setDefaultCharset(enc,
						null);
			} catch (CoreException exception) {
				ErrorDialog.openError(getShell(), IDEWorkbenchMessages
						.getString("IDEEditorsPreferencePageEncodingError"), //$NON-NLS-1$
						exception.getMessage(), exception.getStatus());
				return false;
			}
		}
		return super.performOk();
	}

	/**
	 * Return whether or not the encoding setting changed/
	 * @return
	 */
	private boolean hasNewEncoding() {
		
		String enc = ResourcesPlugin.getPlugin().getPluginPreferences()
		.getString(ResourcesPlugin.PREF_ENCODING);
		
		
		if(defaultEncodingButton.getSelection()){
			//Changed if default is selected and there is no setting
			return enc != null && enc.length() > 0;
		}
		return !(enc.equals(encodingCombo.getText()));
	}
}