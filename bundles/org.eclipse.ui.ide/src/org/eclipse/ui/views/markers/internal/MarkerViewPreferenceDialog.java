/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.preferences.ViewSettingsDialog;

/**
 * The MarkerViewPreferenceDialog is the dialog that is used for preference
 * settings in a markers view.
 * 
 * @since 3.1
 * 
 */
public class MarkerViewPreferenceDialog extends ViewSettingsDialog {

	String enablementKey;

	String limitKey;

	String dialogTitle;

	private IntegerFieldEditor limitEditor;

	private Button enablementButton;

	private Composite editArea;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parentShell
	 * @param enablementPreference
	 *            The key for the enablement preference.
	 * @param limitPreference
	 *            The key for the limit preference.
	 * @param title
	 *            The title for the dialog.
	 */
	public MarkerViewPreferenceDialog(Shell parentShell,
			String enablementPreference, String limitPreference, String title) {
		super(parentShell);
		enablementKey = enablementPreference;
		limitKey = limitPreference;
		dialogTitle = title;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(dialogTitle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite topComposite = (Composite) super.createDialogArea(parent);

		boolean checked = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(enablementKey);
		enablementButton = new Button(topComposite, SWT.CHECK);
		enablementButton.setText(MarkerMessages.MarkerPreferences_MarkerLimits);
		enablementButton.setSelection(checked);

		editArea = new Composite(topComposite, SWT.NONE);
		editArea.setLayout(new GridLayout());
		GridData editData = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		editData.horizontalIndent = 10;
		editArea.setLayoutData(editData);

		limitEditor = new IntegerFieldEditor(
				"limit", MarkerMessages.MarkerPreferences_VisibleItems, editArea){ //$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.jface.preference.IntegerFieldEditor#checkState()
			 */
			protected boolean checkState() {
				boolean state = super.checkState();
				Button okButton = getButton(IDialogConstants.OK_ID);
				if (okButton != null){
					okButton.setEnabled(state);
				}
				return state;
			}
		};
		limitEditor.setPreferenceStore(IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore());
		limitEditor.setPreferenceName(limitKey);
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

		return topComposite;
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
				enablementKey, enablementButton.getSelection());
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
				.getDefaultBoolean(enablementKey);
		enablementButton.setSelection(checked);
		setLimitEditorEnablement(editArea, checked);
	}

}
