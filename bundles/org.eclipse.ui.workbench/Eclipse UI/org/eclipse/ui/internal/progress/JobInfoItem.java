package org.eclipse.ui.internal.progress;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * JobInfoItem is a special composite used to display 
 * infos.
 * @since 3.1
 *
 */
public class JobInfoItem extends Composite {

	private static String STOP_IMAGE_KEY = "org.eclipse.ui.internal.progress.PROGRESS_STOP_IMAGE_KEY"; //$NON-NLS-1$

	private Label progressLabel;

	private Button stopButton;

	private ProgressBar progressBar;

	private Link subTaskLabel;

	private JobInfo info;

	static {
		JFaceResources
				.getImageRegistry()
				.put(
						STOP_IMAGE_KEY,
						WorkbenchImages
								.getWorkbenchImageDescriptor("elcl16/progress_stop.gif"));//$NON-NLS-1$

	}

	/**
	 * Create a new instance of the receiver for JobInfo.
	 * 
	 * @param parent
	 * @param style
	 * @param jobInfo
	 */
	public JobInfoItem(Composite parent, int style, JobInfo jobInfo) {
		super(parent, style);
		info = jobInfo;
		createChildren();
		setData(info);
		setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

	}

	/**
	 * Create the child widgets of the receiver.
	 */
	private void createChildren() {
		FormLayout layout = new FormLayout();
		setLayout(layout);
	
		progressLabel = new Label(this, SWT.NONE);
		progressLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		progressLabel.setText(getJobNameAndStatus(false, false));
		FormData progressData = new FormData();
		progressData.top = new FormAttachment(0);
		progressData.left = new FormAttachment(
				IDialogConstants.HORIZONTAL_SPACING);
		progressData.right = new FormAttachment(100);
		progressLabel.setLayoutData(progressData);

		stopButton = new Button(this, SWT.FLAT);

		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(progressLabel,
				IDialogConstants.VERTICAL_SPACING);
		buttonData.right = new FormAttachment(100,IDialogConstants.HORIZONTAL_SPACING * -1);

		setButtonImage(buttonData);
		
		stopButton.setLayoutData(buttonData);
		stopButton.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				info.cancel();
				layout(true);
			}
		});

		subTaskLabel = new Link(this, SWT.NONE);

		FormData linkData = new FormData();
		linkData.top = new FormAttachment(progressLabel,
				IDialogConstants.VERTICAL_SPACING);
		linkData.left = new FormAttachment(IDialogConstants.HORIZONTAL_SPACING);
		linkData.right = new FormAttachment(100);
		subTaskLabel.setLayoutData(linkData);
		
		refresh();
	}

	/**
	 * Set the button image and update the button data to
	 * reflect it.
	 * @param buttonData
	 */
	private void setButtonImage(FormData buttonData) {
		Image stopImage = JFaceResources.getImageRegistry().get(
				STOP_IMAGE_KEY);
		Rectangle imageBounds = stopImage.getBounds();
		stopButton.setImage(stopImage);
		
		buttonData.height = imageBounds.height;
		buttonData.width = imageBounds.width;
	}

	/**
	 * Create the progress bar and apply any style bits from style.
	 * 
	 * @param style
	 */
	private void createProgressBar(int style) {
		progressBar = new ProgressBar(this, SWT.HORIZONTAL | style);
		FormData barData = new FormData();
		barData.top = new FormAttachment(progressLabel,
				IDialogConstants.VERTICAL_SPACING);
		barData.left = new FormAttachment(IDialogConstants.HORIZONTAL_SPACING);
		barData.right = new FormAttachment(stopButton,IDialogConstants.HORIZONTAL_SPACING * -1);
		progressBar.setLayoutData(barData);

		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(progressLabel,
				IDialogConstants.VERTICAL_SPACING);
		buttonData.right = new FormAttachment(100,IDialogConstants.HORIZONTAL_SPACING * -1);
		setButtonImage(buttonData);
	
		stopButton.setLayoutData(buttonData);

		// Reattach the link label
		FormData linkData = new FormData();
		linkData.top = new FormAttachment(progressBar,
				IDialogConstants.VERTICAL_SPACING);
		linkData.left = new FormAttachment(IDialogConstants.HORIZONTAL_SPACING);
		linkData.right = new FormAttachment(100);
		subTaskLabel.setLayoutData(linkData);
	}

	/**
	 * Refresh the receiver.
	 */
	void refresh() {
		
		if(isDisposed())
			return;
		
		progressLabel.setText(getJobNameAndStatus(false, false));
		int percentDone = info.getPercentDone();

		if (info.getJob().getState() == Job.RUNNING) {
			if (progressBar == null)
				createProgressBar(percentDone == IProgressMonitor.UNKNOWN ? SWT.INDETERMINATE
						: SWT.NONE);

			// Protect against bad counters
			if (percentDone >= 0 && percentDone <= 100)
				progressBar.setSelection(percentDone);
		}

		if (info.hasTaskInfo()) {
			String taskString = info.getTaskInfo().getTaskName();
			String subTaskString = null;
			Object[] jobChildren = info.getChildren();
			if (jobChildren.length > 0)
				subTaskString = ((JobTreeElement) jobChildren[0])
						.getDisplayString();

			if (subTaskString != null) {
				if (taskString == null)
					taskString = subTaskString;
				else
					taskString = NLS.bind(
							ProgressMessages.JobInfo_DoneNoProgressMessage,
							taskString, subTaskString);
			}
			if (taskString != null)
				subTaskLabel.setText(taskString);
		}
	}

	/**
	 * Remap the receiver to show element.
	 * 
	 * @param element
	 */
	void remap(JobInfo element) {
		info = element;
		refresh();
	}

	JobInfo getInfo() {
		return info;
	}

	/**
	 * Set the color based on the index.
	 * 
	 * @param i
	 */
	public void setColor(int i) {
		if (i % 2 == 0)
			setAllBackgrounds(getDisplay().getSystemColor(
					SWT.COLOR_LIST_BACKGROUND));
		else
			setAllBackgrounds(getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_LIGHT_SHADOW));

	}

	/**
	 * Set the background of all widgets to the supplied systemCOlour
	 * 
	 * @param systemColor
	 */
	private void setAllBackgrounds(Color systemColor) {
		setBackground(systemColor);
		progressLabel.setBackground(systemColor);
		stopButton.setBackground(systemColor);

		subTaskLabel.setBackground(systemColor);
		if (progressBar != null)
			progressBar.setBackground(systemColor);
	}

	/**
	 * Return the finished String for the receiver.
	 * 
	 * @param withTime
	 * @return String
	 */
	private String getFinishedString(boolean withTime) {
		String time = null;
		if (withTime)
			time = getTimeString(info);
		if (time != null)
			return NLS.bind(ProgressMessages.JobInfo_FinishedAt, info.getJob()
					.getName(), time);
		return NLS.bind(ProgressMessages.JobInfo_Finished, info.getJob()
				.getName());
	}

	/**
	 * Get the name and status for the main label.
	 * 
	 * @param terminated
	 * @param withTime
	 * @return String
	 */
	private String getJobNameAndStatus(boolean terminated, boolean withTime) {

		Job job = info.getJob();

		String name = job.getName();

		if (job.isSystem())
			name = NLS.bind(ProgressMessages.JobInfo_System, name);

		if (info.isCanceled())
			return NLS.bind(ProgressMessages.JobInfo_Cancelled, name);

		if (terminated)
			return getFinishedString(withTime);

		if (info.isBlocked()) {
			IStatus blockedStatus = info.getBlockedStatus();
			return NLS.bind(ProgressMessages.JobInfo_Blocked, name,
					blockedStatus.getMessage());
		}

		switch (job.getState()) {
		case Job.RUNNING:
			return name;
		case Job.SLEEPING:
			return NLS.bind(ProgressMessages.JobInfo_Sleeping, name);
		default:
			return NLS.bind(ProgressMessages.JobInfo_Waiting, name);
		}
	}

	/**
	 * Get the time string for a finished job
	 * 
	 * @param jte
	 * @return String or <code>null</code> if this is not one of the finished
	 *         jobs.
	 */
	private String getTimeString(JobTreeElement jte) {
		Date date = ProgressManager.getInstance().finishedJobs
				.getFinishDate(jte);
		if (date != null)
			return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
		return null;
	}

}
