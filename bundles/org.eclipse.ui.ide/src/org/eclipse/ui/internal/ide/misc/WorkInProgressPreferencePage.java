/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;

import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.ide.WorkbenchActionBuilder;

/**
 * Temporary "Work in Progress" PreferencePage for Job control
 * 
 * @since 3.0
 */
public class WorkInProgressPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private static final int SLOW = Platform.MIN_PERFORMANCE;
	private static final int FAST = Platform.MAX_PERFORMANCE;

	private Slider slider;
	private Label displayLabel;

	private Button autoRefreshButton;
	private Text pollingRefreshText;
	
	Button buildPreference;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		createBuildControls(parent);
		createMachineSpeedControls(parent);
		createRefreshControls(parent);
		return parent;
	}

	private void createBuildControls(Composite parent) {
		buildPreference = new Button(parent, SWT.CHECK);
		buildPreference.setText("Temporarily restore 2.1 rebuild actions"); //$NON-NLS-1$
		buildPreference.setSelection(WorkbenchActionBuilder.INCLUDE_REBUILD_ACTIONS);
		buildPreference.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				WorkbenchActionBuilder.setIncludeRebuildActions(buildPreference.getSelection());
			}
		});
	}

	/**
	 * Create the controls for the machine speed preferences.
	 * @param parent
	 * @return
	 */
	private Composite createMachineSpeedControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.NONE);
		label.setText(WorkInProgressMessages.getString("WorkInProgressPreferencePage.0_label")); //$NON-NLS-1$
		GridData data = new GridData();
		label.setLayoutData(data);
		slider = new Slider(composite, SWT.HORIZONTAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		slider.setLayoutData(data);

		displayLabel = new Label(composite, SWT.NONE);
		data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		data.widthHint = convertWidthInCharsToPixels(10);
		displayLabel.setLayoutData(data);

		Label description = new Label(composite, SWT.WRAP);
		description.setText(WorkInProgressMessages.getString("WorkInProgressPreferencePage.SpeedExplanation")); //$NON-NLS-1$

		GridData descriptionData = new GridData();
		descriptionData.horizontalSpan = 3;
		description.setLayoutData(descriptionData);

		slider.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				setLabelText(((Slider) e.widget).getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		slider.setValues(getValue(), SLOW, FAST + 1, 1, 1, 1);
		int value = getValue();
		slider.setSelection(value);
		setLabelText(value);
		return composite;
	}

	/**
	 * Sets the value of the label control.
	 * 
	 * @param value
	 *            the integer value to set the label to.
	 */
	protected void setLabelText(int value) {
		String string;
		switch (value) {
			case SLOW :
				string = WorkInProgressMessages.getString("WorkInProgressPreferencePage.SlowTitle"); //$NON-NLS-1$
				break;
			case FAST :
				string = WorkInProgressMessages.getString("WorkInProgressPreferencePage.FastTitle"); //$NON-NLS-1$
				break;
			default :
				string = WorkInProgressMessages.getString("WorkInProgressPreferencePage.MediumTitle"); //$NON-NLS-1$
		}
		displayLabel.setText(string);
		displayLabel.redraw();
	}

	/**
	 * Gets the stored value.
	 * 
	 * @return the value for the slider, as pulled from preferences.
	 */
	private int getValue() {
		int value =
			Platform.getPlugin(Platform.PI_RUNTIME).getPluginPreferences().getInt(
				Platform.PREF_PLATFORM_PERFORMANCE);
		if (value < SLOW)
			return FAST;
		else
			return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		//Nothing to do here
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {

		Platform.getPlugin(Platform.PI_RUNTIME).getPluginPreferences().setValue(
			Platform.PREF_PLATFORM_PERFORMANCE,
			slider.getSelection());
		Platform.getPlugin(Platform.PI_RUNTIME).savePluginPreferences();

		Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
		boolean autoRefresh = autoRefreshButton.getSelection();
		preferences.setValue(ResourcesPlugin.PREF_AUTO_REFRESH, autoRefresh);

		if (autoRefresh) {
			preferences.setValue(
				ResourcesPlugin.PREF_REFRESH_POLLING_DELAY,
				pollingRefreshText.getText());
		}

		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		slider.setSelection(FAST);
		setLabelText(FAST);

		boolean autoRefresh =
			ResourcesPlugin.getPlugin().getPluginPreferences().getDefaultBoolean(
				ResourcesPlugin.PREF_AUTO_REFRESH);
		autoRefreshButton.setSelection(autoRefresh);
		pollingRefreshText.setEnabled(autoRefresh);
		pollingRefreshText.setText(
			ResourcesPlugin.getPlugin().getPluginPreferences().getDefaultString(
				ResourcesPlugin.PREF_REFRESH_POLLING_DELAY));
	}

	/**
	 * Create the Refresh controls
	 * @param parent
	 */
	private void createRefreshControls(Composite parent) {
		Group refreshParent = new Group(parent, SWT.NONE);
		refreshParent.setText(WorkInProgressMessages.getString("WorkInProgressPreferencePage.AutoRefreshGroup")); //$NON-NLS-1$

		GridData groupData = new GridData(GridData.FILL_BOTH);
		groupData.grabExcessHorizontalSpace = true;
		refreshParent.setLayoutData(groupData);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		refreshParent.setLayout(layout);

		this.autoRefreshButton = new Button(refreshParent, SWT.CHECK);
		this.autoRefreshButton.setText(WorkInProgressMessages.getString("WorkInProgressPreferencePage.AutoRefreshButton")); //$NON-NLS-1$
		this.autoRefreshButton.setToolTipText(WorkInProgressMessages.getString("WorkInProgressPreferencePage.AutoRefreshToolTip")); //$NON-NLS-1$

		boolean autoRefresh =
			ResourcesPlugin.getPlugin().getPluginPreferences().getBoolean(
				ResourcesPlugin.PREF_AUTO_REFRESH);
		this.autoRefreshButton.setSelection(autoRefresh);

		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 2;
		this.autoRefreshButton.setLayoutData(buttonData);

		Label pollingDelayLabel = new Label(refreshParent, SWT.NONE);
		pollingDelayLabel.setText(WorkInProgressMessages.getString("WorkInProgressPreferencePage.PollingDelay")); //$NON-NLS-1$

		this.pollingRefreshText = new Text(refreshParent, SWT.BORDER);
		this.pollingRefreshText.setEnabled(autoRefresh);
		this.pollingRefreshText.setText(
			ResourcesPlugin.getPlugin().getPluginPreferences().getString(
				ResourcesPlugin.PREF_REFRESH_POLLING_DELAY));

		GridData textData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		pollingRefreshText.setLayoutData(textData);

		this.autoRefreshButton.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				boolean selection = autoRefreshButton.getSelection();
				pollingRefreshText.setEnabled(selection);

			}
		});

		this.pollingRefreshText.addKeyListener(new KeyAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyReleased(KeyEvent e) {
				setValid(checkState(pollingRefreshText));
			}
		});
	}

	/**
	 * Check the state of the text
	 */
	protected boolean checkState(Text text) {

		if (text == null)
			return false;
		try {
			Integer.valueOf(text.getText()).intValue();
			setErrorMessage(null);
			return true;
		} catch (NumberFormatException e1) {
			setErrorMessage(WorkInProgressMessages.getString("WorkInProgressPreferencePage.InvalidMessage")); //$NON-NLS-1$
		}

		return false;
	}
}
