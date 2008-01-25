/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.preferences.ViewSettingsDialog;
import org.eclipse.ui.views.markers.MarkerSupportConstants;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * MarkerPreferencesDialog is the dialog for showing marker preferences.
 * 
 * @since 3.4
 * 
 */
public class MarkerPreferencesDialog extends ViewSettingsDialog {

	private IntegerFieldEditor limitEditor;

	private Button enablementButton;

	private Composite editArea;

	private Label messageLabel;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parentShell
	 */
	public MarkerPreferencesDialog(Shell parentShell) {
		super(parentShell);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(MarkerMessages.MarkerPreferences_DialogTitle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		boolean checked = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IDEInternalPreferences.USE_MARKER_LIMITS);
		enablementButton = new Button(dialogArea, SWT.CHECK);
		enablementButton.setText(MarkerMessages.MarkerPreferences_MarkerLimits);
		enablementButton.setSelection(checked);

		editArea = new Composite(dialogArea, SWT.NONE);
		editArea.setLayout(new GridLayout());
		GridData editData = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		editData.horizontalIndent = 10;
		editArea.setLayoutData(editData);

		limitEditor = new IntegerFieldEditor(
				"limit", MarkerMessages.MarkerPreferences_VisibleItems, editArea) { //$NON-NLS-1$
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.preference.IntegerFieldEditor#checkState()
			 */
			protected boolean checkState() {
				boolean state = super.checkState();
				setValid(state, getErrorMessage());
				return state;
			}
		};
		limitEditor.setPreferenceStore(IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore());
		limitEditor
				.setPreferenceName(IDEInternalPreferences.MARKER_LIMITS_VALUE);
		limitEditor.load();

		GridData checkedData = new GridData(SWT.FILL, SWT.NONE, true, false);
		checkedData.horizontalSpan = limitEditor.getNumberOfControls();
		enablementButton.setLayoutData(checkedData);

		enablementButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setLimitEditorEnablement(editArea, enablementButton
						.getSelection());
			}
		});

		setLimitEditorEnablement(editArea, checked);
		
		
		messageLabel = new Label(dialogArea, SWT.NONE);

		messageLabel.setBackground(JFaceColors.getErrorBackground(dialogArea.getDisplay()));
		messageLabel.setForeground(JFaceColors.getErrorText(dialogArea.getDisplay()));
		messageLabel.setLayoutData(new GridData(SWT.FILL,SWT.NONE,true,false));
		return dialogArea;
	}

	/**
	 * Set the enabled state of the OK button by state.
	 * 
	 * @param state
	 */
	protected void setValid(boolean state, String errorMessage) {
		Button okButton = getButton(IDialogConstants.OK_ID);

		if (okButton == null)
			return;
		
		if(state)
			messageLabel.setText(MarkerSupportConstants.EMPTY_STRING);
		else
			messageLabel.setText(errorMessage);

		okButton.setEnabled(state);

	}

	/**
	 * Enable the limitEditor based on checked.
	 * 
	 * @param control
	 *            The parent of the editor
	 * @param checked
	 */
	private void setLimitEditorEnablement(Composite control, boolean checked) {
		limitEditor.setEnabled(checked, control);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		limitEditor.store();
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(
				IDEInternalPreferences.USE_MARKER_LIMITS,
				enablementButton.getSelection());
		IDEWorkbenchPlugin.getDefault().savePluginPreferences();

		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.ViewSettingsDialog#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		limitEditor.loadDefault();
		boolean checked = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getDefaultBoolean(IDEInternalPreferences.USE_MARKER_LIMITS);
		enablementButton.setSelection(checked);
		setLimitEditorEnablement(editArea, checked);
	}

}
