/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 54630
 *     Trevor S. Kaufman <endante@gmail.com> - bug 156152
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.util.Date;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;

import com.ibm.icu.util.Calendar;

/**
 * A composite that allows editing a subscriber refresh schedule. A validator can be used to allow
 * containers to show page completion.
 * 
 * @since 3.0
 */
public class ConfigureSynchronizeScheduleComposite extends Composite {

	private SubscriberRefreshSchedule schedule;
	private Button userRefreshOnly;
	private Button enableBackgroundRefresh;
	private Text timeInterval;
	private Combo hoursOrMinutes;
	private IPageValidator validator;
	private DateTime startTime;
	private Button immediately;
	private Button runOnce;
	
	public ConfigureSynchronizeScheduleComposite(Composite parent, SubscriberRefreshSchedule schedule, IPageValidator validator) {
		super(parent, SWT.NONE);
		this.schedule = schedule;
		this.validator = validator;
		createMainDialogArea(parent);
	}

	private void initializeValues() {
		boolean enableBackground = schedule.isEnabled();
		boolean hours = false;
		
		userRefreshOnly.setSelection(! enableBackground);
		enableBackgroundRefresh.setSelection(enableBackground);
		
		long seconds = schedule.getRefreshInterval();
		if(seconds <= 60) {
			seconds = 60;
		}

		long minutes = seconds / 60;
		
		if(minutes >= 60) {
			minutes = minutes / 60;
			hours = true;
		}		
		hoursOrMinutes.select(hours ? 0 : 1);
		timeInterval.setText(Long.toString(minutes));
		runOnce.setSelection(schedule.getRunOnce());
		
		Date start = schedule.getRefreshStartTime();
		Calendar cal = Calendar.getInstance();
		if (start != null) {
			cal.setTime(start);
		} else {
			// ran immediately, no startTime available
			cal.clear();
		}
		startTime.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createMainDialogArea(Composite parent) {
		GC gc = new GC(parent);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics	fontMetrics = gc.getFontMetrics();
		gc.dispose();
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.HORIZONTAL_SPACING);
		gridLayout.verticalSpacing = Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_SPACING);
		setLayout(gridLayout);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite area = this;

		createWrappingLabel(area, NLS.bind(TeamUIMessages.ConfigureRefreshScheduleDialog_1, new String[] { Utils.shortenText(SynchronizeView.MAX_NAME_LENGTH, schedule.getParticipant().getName()) }), 0, 2); 
		{
			final Label label = new Label(area, SWT.WRAP);
			final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;
			label.setLayoutData(gridData);
			label.setText(NLS.bind(TeamUIMessages.ConfigureRefreshScheduleDialog_1a, new String[] { SubscriberRefreshSchedule.refreshEventAsString(schedule.getLastRefreshEvent()) }));			 
		}
		{
			userRefreshOnly = new Button(area, SWT.RADIO);
			final GridData gridData = new GridData();
			gridData.horizontalSpan = 2;
			userRefreshOnly.setLayoutData(gridData);
			userRefreshOnly.setText(TeamUIMessages.ConfigureRefreshScheduleDialog_2); 
			userRefreshOnly.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					updateEnablements();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		{
			enableBackgroundRefresh = new Button(area, SWT.RADIO);
			final GridData gridData = new GridData();
			gridData.horizontalSpan = 2;
			enableBackgroundRefresh.setLayoutData(gridData);
			enableBackgroundRefresh.setText(TeamUIMessages.ConfigureRefreshScheduleDialog_3); 
			enableBackgroundRefresh.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					updateEnablements();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		{
			final Composite composite = new Composite(area, SWT.NONE);
			final GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_BEGINNING);
			gridData.horizontalSpan = 2;
			composite.setLayoutData(gridData);
			final GridLayout gridLayout_1 = new GridLayout();
			gridLayout_1.numColumns = 4;
			gridLayout_1.marginWidth = 0;
			gridLayout_1.marginHeight = 0;
			gridLayout_1.horizontalSpacing = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.HORIZONTAL_SPACING);
			gridLayout_1.verticalSpacing = Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_SPACING);
			composite.setLayout(gridLayout_1);
			{
				final Label label = new Label(composite, SWT.NONE);
				label.setText(TeamUIMessages.ConfigureRefreshScheduleDialog_3a);
			}
			{
				startTime = new DateTime(composite, SWT.TIME | SWT.BORDER);
				final GridData gridData_1 = new GridData();
				gridData_1.horizontalSpan = 2;
				startTime.setLayoutData(gridData_1);
				
			}
			{
				immediately = new Button(composite, SWT.CHECK);
				immediately.setText(TeamUIMessages.ConfigureRefreshScheduleDialog_3b);
				immediately.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						updateEnablements();
					}
				});
			}
			{
				final Label label = new Label(composite, SWT.NONE);
				label.setText(TeamUIMessages.ConfigureRefreshScheduleDialog_4); 
			}
			{
				timeInterval = new Text(composite, SWT.BORDER | SWT.RIGHT);
				final GridData gridData_1 = new GridData();
				gridData_1.widthHint = 35;
				timeInterval.setLayoutData(gridData_1);
				timeInterval.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						updateEnablements();
					}
				});
				timeInterval.addVerifyListener(new VerifyListener() {
					public void verifyText(VerifyEvent e) {
						String string = e.text;
						char [] chars = new char [string.length ()];
						string.getChars (0, chars.length, chars, 0);
						for (int i=0; i<chars.length; i++) {
							if (!('0' <= chars [i] && chars [i] <= '9')) {
								e.doit = false;
								return;
							}
						}
					}
				});
			}
			{
				hoursOrMinutes = new Combo(composite, SWT.READ_ONLY);
				hoursOrMinutes.setItems(new String[] { TeamUIMessages.ConfigureRefreshScheduleDialog_5, TeamUIMessages.ConfigureRefreshScheduleDialog_6 }); // 
				hoursOrMinutes.setLayoutData(new GridData());
			}
			{
				runOnce = new Button(composite, SWT.CHECK);
				runOnce.setText(TeamUIMessages.ConfigureRefreshScheduleDialog_8);
				runOnce.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						updateEnablements();
					}
				});				
			}
		}
		initializeValues();
		updateEnablements();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	public void saveValues() {
		if (!runOnce.getSelection()) {		
			int hours = hoursOrMinutes.getSelectionIndex();
			try {
				long seconds = Long.parseLong(timeInterval.getText());
				if(hours == 0) {
					seconds = seconds * 3600;
				} else {
					seconds = seconds * 60;
				}
				schedule.setRefreshInterval(seconds);
			} catch (NumberFormatException e) {
				// keep old value
			}
		} else {
			schedule.setRunOnce(runOnce.getSelection() /* true */);
		}
		
		if (!immediately.getSelection()) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, startTime.getHours());
			cal.set(Calendar.MINUTE, startTime.getMinutes());
			cal.set(Calendar.SECOND, startTime.getSeconds());
			schedule.setRefreshStartTime(cal.getTime());
		} else {
			schedule.setRefreshStartTime(null);
		}
		
		if(schedule.isEnabled() != enableBackgroundRefresh.getSelection()) {
			schedule.setEnabled(enableBackgroundRefresh.getSelection(), true /* allow to start */);
		}
		
		// update schedule
		ISynchronizeParticipant participant = schedule.getParticipant();
		if (!participant.isPinned() && schedule.isEnabled()) {
			participant.setPinned(MessageDialog.openQuestion(getShell(), 
					NLS.bind(TeamUIMessages.ConfigureSynchronizeScheduleComposite_0, new String[] { Utils.getTypeName(participant) }), 
					NLS.bind(TeamUIMessages.ConfigureSynchronizeScheduleComposite_1, new String[] { Utils.getTypeName(participant) }))); 
		}
		schedule.getRefreshable().setRefreshSchedule(schedule);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#updateEnablements()
	 */
	public void updateEnablements() {
		if (userRefreshOnly.getSelection()) {
			validator.setComplete(null);
		} else {
			try {
				long number = Long.parseLong(timeInterval.getText());
				if(number <= 0) {
					validator.setComplete(TeamUIMessages.ConfigureRefreshScheduleDialog_7); 
				} else {
					validator.setComplete(null);
				}
			} catch (NumberFormatException e) {
				validator.setComplete(TeamUIMessages.ConfigureRefreshScheduleDialog_7); 
			}	
		}
		timeInterval.setEnabled(enableBackgroundRefresh.getSelection());
		hoursOrMinutes.setEnabled(enableBackgroundRefresh.getSelection());
		runOnce.setEnabled(enableBackgroundRefresh.getSelection());
		if (runOnce.isEnabled()) {
			timeInterval.setEnabled(!runOnce.getSelection());
			hoursOrMinutes.setEnabled(!runOnce.getSelection());
		}
		
		startTime.setEnabled(enableBackgroundRefresh.getSelection());
		immediately.setEnabled(enableBackgroundRefresh.getSelection());
		if (immediately.isEnabled()) {
			startTime.setEnabled(!immediately.getSelection());
		}
	}
	
	private Label createWrappingLabel(Composite parent, String text, int indent, int horizontalSpan) {
		Label label = new Label(parent, SWT.LEFT | SWT.WRAP);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = horizontalSpan;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalIndent = indent;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = 400;
		label.setLayoutData(data);
		return label;
	}
}
