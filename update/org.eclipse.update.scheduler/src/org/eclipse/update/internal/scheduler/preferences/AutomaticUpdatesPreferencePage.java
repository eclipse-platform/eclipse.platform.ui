/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.scheduler.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.update.internal.scheduler.UpdateScheduler;

public class AutomaticUpdatesPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private Button enabledCheck;
	private Button onStartupRadio;
	private Button onScheduleRadio;
	private Combo dayCombo;
	private Combo hourCombo;

	public void init(IWorkbench workbench) {
	}

	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		container.setLayout(layout);

		enabledCheck = new Button(container, SWT.CHECK);
		enabledCheck.setText("Automatically &find new updates and notify me");

		createSpacer(container, 1);

		Group group = new Group(container, SWT.NONE);
		group.setText("Update &Schedule");
		layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);

		onStartupRadio = new Button(group, SWT.RADIO);
		onStartupRadio.setText(
			"Look for updates each time platform is started");
		gd = new GridData();
		gd.horizontalSpan = 3;
		onStartupRadio.setLayoutData(gd);
		onStartupRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
			}
		});

		onScheduleRadio = new Button(group, SWT.RADIO);
		onScheduleRadio.setText("Look for updates on the following schedule:");
		gd = new GridData();
		gd.horizontalSpan = 3;
		onScheduleRadio.setLayoutData(gd);
		onScheduleRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
			}
		});

		dayCombo = new Combo(group, SWT.NULL);
		dayCombo.setItems(UpdateScheduler.DAYS);
		Label label = new Label(group, SWT.NULL);
		label.setText("at");
		hourCombo = new Combo(group, SWT.NULL);
		hourCombo.setItems(UpdateScheduler.HOURS);

		initialize();

		enabledCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
			}
		});

		return container;
	}

	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}

	private void initialize() {
		Preferences pref = UpdateScheduler.getDefault().getPluginPreferences();
		enabledCheck.setSelection(pref.getBoolean(UpdateScheduler.P_ENABLED));
		setSchedule(pref.getString(UpdateScheduler.P_SCHEDULE));

		dayCombo.setText(UpdateScheduler.DAYS[getDay(pref)]);
		hourCombo.setText(UpdateScheduler.HOURS[getHour(pref)]);

		pageChanged();
	}

	private void setSchedule(String value) {
		if (value.equals(UpdateScheduler.VALUE_ON_STARTUP))
			onStartupRadio.setSelection(true);
		else
			onScheduleRadio.setSelection(true);
	}

	private void pageChanged() {
		boolean master = enabledCheck.getSelection();
		onStartupRadio.setEnabled(master);
		onScheduleRadio.setEnabled(master);
		dayCombo.setEnabled(master && onScheduleRadio.getSelection());
		hourCombo.setEnabled(master && onScheduleRadio.getSelection());
	}

	protected void performDefaults() {
		super.performDefaults();
		Preferences pref = UpdateScheduler.getDefault().getPluginPreferences();
		enabledCheck.setSelection(
			pref.getDefaultBoolean(UpdateScheduler.P_ENABLED));
	}

	/** 
	 * Method declared on IPreferencePage.
	 * Subclasses should override
	 */
	public boolean performOk() {
		Preferences pref = UpdateScheduler.getDefault().getPluginPreferences();
		pref.setValue(UpdateScheduler.P_ENABLED, enabledCheck.getSelection());
		if (onStartupRadio.getSelection())
			pref.setValue(UpdateScheduler.P_SCHEDULE, UpdateScheduler.VALUE_ON_STARTUP);
		else 
			pref.setValue(UpdateScheduler.P_SCHEDULE, UpdateScheduler.VALUE_ON_SCHEDULE);
			
		pref.setValue(UpdateScheduler.P_DAY, dayCombo.getText());
		pref.setValue(UpdateScheduler.P_HOUR, hourCombo.getText());
		
		UpdateScheduler.getDefault().savePluginPreferences();
		
		UpdateScheduler.getDefault().scheduleUpdateJob();
		return true;
	}
	
	private int getDay(Preferences pref) {
		String day = pref.getString(UpdateScheduler.P_DAY);
		for (int i=0; i<UpdateScheduler.DAYS.length; i++)
			if (UpdateScheduler.DAYS[i].equals(day))
				return i;
		return 0;
	}
	
	private int getHour(Preferences pref) {
		String hour = pref.getString(UpdateScheduler.P_HOUR);
		for (int i=0; i<UpdateScheduler.HOURS.length; i++)
			if (UpdateScheduler.HOURS[i].equals(hour))
				return i;
		return 0;
	}
}
