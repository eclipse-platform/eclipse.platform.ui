package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.dialogs.DetailsDialog;
import org.eclipse.team.ui.synchronize.subscribers.SubscriberRefreshSchedule;


public class ConfigureRefreshScheduleDialog extends DetailsDialog {

	private SubscriberRefreshSchedule schedule;
	private Button userRefreshOnly;
	private Button enableBackgroundRefresh;
	private Text time;
	private Combo hoursOrSeconds;

	public ConfigureRefreshScheduleDialog(Shell parentShell, SubscriberRefreshSchedule schedule) {
		super(parentShell, Policy.bind("ConfigureRefreshScheduleDialog.0", schedule.getParticipant().getName())); //$NON-NLS-1$
		this.schedule = schedule;
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
		Composite area = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		area.setLayout(gridLayout);
		area.setLayoutData(new GridData(GridData.FILL_BOTH));

		{
			final Label label = new Label(area, SWT.WRAP);
			final GridData gridData = new GridData(GridData.FILL_BOTH);
			gridData.horizontalSpan = 2;
			gridData.widthHint = 325;
			label.setLayoutData(gridData);
			label.setText(Policy.bind("ConfigureRefreshScheduleDialog.1", schedule.getParticipant().getName()));			 //$NON-NLS-1$
		}
		{
			final Label label = new Label(area, SWT.WRAP);
			final GridData gridData = new GridData(GridData.FILL_BOTH);
			gridData.horizontalSpan = 2;
			gridData.widthHint = 325;
			label.setLayoutData(gridData);
			label.setText(Policy.bind("ConfigureRefreshScheduleDialog.1a", SubscriberRefreshSchedule.refreshEventAsString(schedule.getLastRefreshEvent())));			 //$NON-NLS-1$
		}
		{
			userRefreshOnly = new Button(area, SWT.RADIO);
			final GridData gridData = new GridData();
			gridData.horizontalSpan = 2;
			userRefreshOnly.setLayoutData(gridData);
			userRefreshOnly.setText(Policy.bind("ConfigureRefreshScheduleDialog.2")); //$NON-NLS-1$
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
			enableBackgroundRefresh.setText(Policy.bind("ConfigureRefreshScheduleDialog.3")); //$NON-NLS-1$
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
				label.setText(Policy.bind("ConfigureRefreshScheduleDialog.4")); //$NON-NLS-1$
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
				hoursOrSeconds.setItems(new String[] { Policy.bind("ConfigureRefreshScheduleDialog.5"), Policy.bind("ConfigureRefreshScheduleDialog.6") }); //$NON-NLS-1$ //$NON-NLS-2$
				final GridData gridData_1 = new GridData();
				gridData_1.widthHint = 75;
				hoursOrSeconds.setLayoutData(gridData_1);
			}
		}
		initializeValues();
		Dialog.applyDialogFont(parent);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
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
		schedule.getParticipant().setRefreshSchedule(schedule);
		super.okPressed();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createDropDownDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Composite createDropDownDialogArea(Composite parent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#updateEnablements()
	 */
	protected void updateEnablements() {
		try {
			long number = Long.parseLong(time.getText());
			if(number <= 0) {
				setPageComplete(false);
				setErrorMessage(Policy.bind("ConfigureRefreshScheduleDialog.7")); //$NON-NLS-1$
			} else {
				setPageComplete(true);
				setErrorMessage(null);
			}
		} catch (NumberFormatException e) {
			setPageComplete(false);
			setErrorMessage(Policy.bind("ConfigureRefreshScheduleDialog.8")); //$NON-NLS-1$
		}	
		
		time.setEnabled(enableBackgroundRefresh.getSelection());
		hoursOrSeconds.setEnabled(enableBackgroundRefresh.getSelection());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeDetailsButton()
	 */
	protected boolean includeDetailsButton() {
		return false;
	}
}
