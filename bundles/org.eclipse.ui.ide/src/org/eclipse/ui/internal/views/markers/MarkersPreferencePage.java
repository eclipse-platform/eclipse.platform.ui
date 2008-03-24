/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * MarkersPreferencePage is the preference page for the markers.
 * 
 * @since 3.4
 * 
 */
public class MarkersPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private IntegerFieldEditor limitEditor;

	private Button enablementButton;

	private Composite editArea;

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkersPreferencePage() {
		super();
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param title
	 */
	public MarkersPreferencePage(String title) {
		super(title);
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param title
	 * @param image
	 */
	public MarkersPreferencePage(String title, ImageDescriptor image) {
		super(title, image);

	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {

		boolean checked = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IDEInternalPreferences.USE_MARKER_LIMITS);
		enablementButton = new Button(parent, SWT.CHECK);
		enablementButton.setText(MarkerMessages.MarkerPreferences_MarkerLimits);
		enablementButton.setSelection(checked);

		editArea = new Composite(parent, SWT.NONE);
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
				setValid(state);
				return state;
			}
		};
		limitEditor.setPreferenceStore(IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore());
		limitEditor.setPreferenceName(IDEInternalPreferences.MARKER_LIMITS_VALUE);
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
		return parent;
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {

		limitEditor.store();
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(
				IDEInternalPreferences.USE_MARKER_LIMITS, enablementButton.getSelection());
		IDEWorkbenchPlugin.getDefault().savePluginPreferences();
	
		return super.performOk();
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
