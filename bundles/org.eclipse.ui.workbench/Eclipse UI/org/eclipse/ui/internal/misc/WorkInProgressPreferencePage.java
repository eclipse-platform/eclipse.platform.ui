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
package org.eclipse.ui.internal.misc;

import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Temporary "Work in Progress" PreferencePage for Job control
 * 
 * @since 3.0
 */
public class WorkInProgressPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final int SLOW = Platform.MIN_PERFORMANCE;
	private static final int FAST = Platform.MAX_PERFORMANCE;

	private Slider slider;
	private Label displayLabel;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
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
		description.setText(
			WorkInProgressMessages.getString("WorkInProgressPreferencePage.SpeedExplanation")); //$NON-NLS-1$

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
		int value = Platform.getPlugin(Platform.PI_RUNTIME).getPluginPreferences().getInt(Platform.PREF_PLATFORM_PERFORMANCE);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {

		Platform.getPlugin(Platform.PI_RUNTIME).getPluginPreferences().setValue(Platform.PREF_PLATFORM_PERFORMANCE, slider.getSelection());

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
	}
}
