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
package org.eclipse.update.internal.scheduler.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.update.internal.scheduler.*;
import org.eclipse.update.internal.scheduler.UpdateScheduler;

public class AutomaticUpdatesPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private Button enabledCheck;
	private Button onStartupRadio;
	private Button onScheduleRadio;
	private Combo dayCombo;
	private Combo hourCombo;
	private Button searchOnlyRadio;
	private Button searchAndDownloadRadio;
	private Group updateScheduleGroup;
	private Group downloadGroup;

	public void init(IWorkbench workbench) {
	}

	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		container.setLayout(layout);

		enabledCheck = new Button(container, SWT.CHECK);
		enabledCheck.setText(UpdateScheduler.getString("AutomaticUpdatesPreferencePage.findUpdates")); //$NON-NLS-1$

		createSpacer(container, 1);

		updateScheduleGroup = new Group(container, SWT.NONE);
		updateScheduleGroup.setText(UpdateScheduler.getString("AutomaticUpdatesPreferencePage.UpdateSchedule")); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 3;
		updateScheduleGroup.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		updateScheduleGroup.setLayoutData(gd);

		onStartupRadio = new Button(updateScheduleGroup, SWT.RADIO);
		onStartupRadio.setText(
			UpdateScheduler.getString("AutomaticUpdatesPreferencePage.findOnStart")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		onStartupRadio.setLayoutData(gd);
		onStartupRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
			}
		});

		onScheduleRadio = new Button(updateScheduleGroup, SWT.RADIO);
		onScheduleRadio.setText(UpdateScheduler.getString("AutomaticUpdatesPreferencePage.findOnSchedule")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		onScheduleRadio.setLayoutData(gd);
		onScheduleRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
			}
		});

		dayCombo = new Combo(updateScheduleGroup, SWT.READ_ONLY);
		dayCombo.setItems(SchedulerStartup.DAYS);
		gd = new GridData();
		gd.widthHint = 200;
		gd.horizontalIndent = 30;
		dayCombo.setLayoutData(gd);
		
		Label label = new Label(updateScheduleGroup, SWT.NULL);
		label.setText(UpdateScheduler.getString("AutomaticUpdatesPreferencePage.at")); //$NON-NLS-1$
		
		hourCombo = new Combo(updateScheduleGroup, SWT.READ_ONLY);
		hourCombo.setItems(SchedulerStartup.HOURS);
		gd = new GridData();
		gd.widthHint = 100;
		hourCombo.setLayoutData(gd);
	
		createSpacer(container, 1);
		
		downloadGroup = new Group(container, SWT.NONE);
		downloadGroup.setText(UpdateScheduler.getString("AutomaticUpdatesPreferencePage.downloadOptions")); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 3;
		downloadGroup.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		downloadGroup.setLayoutData(gd);

		searchOnlyRadio = new Button(downloadGroup, SWT.RADIO);
		searchOnlyRadio.setText(
			UpdateScheduler.getString("AutomaticUpdatesPreferencePage.searchAndNotify")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		searchOnlyRadio.setLayoutData(gd);
		searchOnlyRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
			}
		});

		searchAndDownloadRadio = new Button(downloadGroup, SWT.RADIO);
		searchAndDownloadRadio.setText(UpdateScheduler.getString("AutomaticUpdatesPreferencePage.downloadAndNotify")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		searchAndDownloadRadio.setLayoutData(gd);
		searchAndDownloadRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
			}
		});
			
		initialize();

		enabledCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
			}
		});

		Dialog.applyDialogFont(container);
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

		dayCombo.setText(SchedulerStartup.DAYS[getDay(pref)]);
		hourCombo.setText(SchedulerStartup.HOURS[getHour(pref)]);
		
		searchOnlyRadio.setSelection(!pref.getBoolean(UpdateScheduler.P_DOWNLOAD));
		searchAndDownloadRadio.setSelection(pref.getBoolean(UpdateScheduler.P_DOWNLOAD));

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
		updateScheduleGroup.setEnabled(master);
		onStartupRadio.setEnabled(master);
		onScheduleRadio.setEnabled(master);
		dayCombo.setEnabled(master && onScheduleRadio.getSelection());
		hourCombo.setEnabled(master && onScheduleRadio.getSelection());
		downloadGroup.setEnabled(master);
		searchOnlyRadio.setEnabled(master);
		searchAndDownloadRadio.setEnabled(master);
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
			
		pref.setValue(SchedulerStartup.P_DAY, dayCombo.getText());
		pref.setValue(SchedulerStartup.P_HOUR, hourCombo.getText());
		
		pref.setValue(UpdateScheduler.P_DOWNLOAD, searchAndDownloadRadio.getSelection());
		
		UpdateScheduler.getDefault().savePluginPreferences();
		
		UpdateScheduler.getScheduler().scheduleUpdateJob();
		return true;
	}
	
	private int getDay(Preferences pref) {
		String day = pref.getString(SchedulerStartup.P_DAY);
		for (int i=0; i<SchedulerStartup.DAYS.length; i++)
			if (SchedulerStartup.DAYS[i].equals(day))
				return i;
		return 0;
	}
	
	private int getHour(Preferences pref) {
		String hour = pref.getString(SchedulerStartup.P_HOUR);
		for (int i=0; i<SchedulerStartup.HOURS.length; i++)
			if (SchedulerStartup.HOURS[i].equals(hour))
				return i;
		return 0;
	}
}
