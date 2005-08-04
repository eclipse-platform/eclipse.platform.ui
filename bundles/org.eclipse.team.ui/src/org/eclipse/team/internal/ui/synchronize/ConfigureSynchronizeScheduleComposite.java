/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

/**
 * A composite that allows editing a subscriber refresh schedule. A validator can be used to allow
 * containers to show page completiong.
 * 
 * @since 3.0
 */
public class ConfigureSynchronizeScheduleComposite extends Composite {

	private SubscriberRefreshSchedule schedule;
	private Button userRefreshOnly;
	private Button enableBackgroundRefresh;
	private Text time;
	private Combo hoursOrSeconds;
	private IPageValidator validator;
	
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
		hoursOrSeconds.select(hours ? 0 : 1);
		time.setText(Long.toString(minutes));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createMainDialogArea(Composite parent) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);
		setLayoutData(new GridData());
		Composite area = this;

		createWrappingLabel(area, NLS.bind(TeamUIMessages.ConfigureRefreshScheduleDialog_1, new String[] { schedule.getParticipant().getName() }), 0, 2); 
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
			gridLayout_1.numColumns = 3;
			composite.setLayout(gridLayout_1);
			{
				final Label label = new Label(composite, SWT.NONE);
				label.setText(TeamUIMessages.ConfigureRefreshScheduleDialog_4); 
			}
			{
				time = new Text(composite, SWT.BORDER | SWT.RIGHT);
				final GridData gridData_1 = new GridData();
				gridData_1.widthHint = 35;
				time.setLayoutData(gridData_1);
				time.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						updateEnablements();
					}
				});
			}
			{
				hoursOrSeconds = new Combo(composite, SWT.READ_ONLY);
				hoursOrSeconds.setItems(new String[] { TeamUIMessages.ConfigureRefreshScheduleDialog_5, TeamUIMessages.ConfigureRefreshScheduleDialog_6 }); // 
				hoursOrSeconds.setLayoutData(new GridData());
			}
		}
		initializeValues();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	public void saveValues() {
		int hours = hoursOrSeconds.getSelectionIndex();
		long seconds = Long.parseLong(time.getText());
		if(hours == 0) {
			seconds = seconds * 3600;
		} else {
			seconds = seconds * 60;
		}
		schedule.setRefreshInterval(seconds);
		if(schedule.isEnabled() != enableBackgroundRefresh.getSelection()) {
			schedule.setEnabled(enableBackgroundRefresh.getSelection(), true /* allow to start */);
		}
		
		// update schedule
		SubscriberParticipant participant = schedule.getParticipant();
		if (!participant.isPinned() && schedule.isEnabled()) {
			participant.setPinned(MessageDialog.openQuestion(getShell(), 
					NLS.bind(TeamUIMessages.ConfigureSynchronizeScheduleComposite_0, new String[] { Utils.getTypeName(participant) }), 
					NLS.bind(TeamUIMessages.ConfigureSynchronizeScheduleComposite_1, new String[] { Utils.getTypeName(participant) }))); 
		}
		participant.setRefreshSchedule(schedule);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#updateEnablements()
	 */
	public void updateEnablements() {
		try {
			long number = Long.parseLong(time.getText());
			if(number <= 0) {
				validator.setComplete(TeamUIMessages.ConfigureRefreshScheduleDialog_7); 
			} else {
				validator.setComplete(null);
			}
		} catch (NumberFormatException e) {
			validator.setComplete(TeamUIMessages.ConfigureRefreshScheduleDialog_8); 
		}	
		time.setEnabled(enableBackgroundRefresh.getSelection());
		hoursOrSeconds.setEnabled(enableBackgroundRefresh.getSelection());
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
