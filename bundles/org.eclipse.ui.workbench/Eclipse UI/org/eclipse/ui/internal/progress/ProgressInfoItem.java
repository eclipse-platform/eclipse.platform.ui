package org.eclipse.ui.internal.progress;

/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.net.URL;
import com.ibm.icu.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * ProgressInfoItem is the item used to show jobs.
 * 
 * @since 3.1
 * 
 */
class ProgressInfoItem extends Composite {

	static String STOP_IMAGE_KEY = "org.eclipse.ui.internal.progress.PROGRESS_STOP"; //$NON-NLS-1$

	static String DISABLED_STOP_IMAGE_KEY = "org.eclipse.ui.internal.progress.DISABLED_PROGRESS_STOP"; //$NON-NLS-1$

	static String CLEAR_FINISHED_JOB_KEY = "org.eclipse.ui.internal.progress.CLEAR_FINISHED_JOB"; //$NON-NLS-1$

	static String DISABLED_CLEAR_FINISHED_JOB_KEY = "org.eclipse.ui.internal.progress.DISABLED_CLEAR_FINISHED_JOB"; //$NON-NLS-1$

	static String DEFAULT_JOB_KEY = "org.eclipse.ui.internal.progress.PROGRESS_DEFAULT"; //$NON-NLS-1$

	static String DARK_COLOR_KEY = "org.eclipse.ui.internal.progress.PROGRESS_DARK_COLOR"; //$NON-NLS-1$

	JobTreeElement info;

	Label progressLabel;

	ToolBar actionBar;

	ToolItem actionButton;

	List taskEntries = new ArrayList(0);

	private ProgressBar progressBar;

	private Label jobImageLabel;

	static final int MAX_PROGRESS_HEIGHT = 12;

	static final int MIN_ICON_SIZE = 16;

	private static final String TEXT_KEY = "Text"; //$NON-NLS-1$

	private static final String ACTION_KEY = "Action";//$NON-NLS-1$

	interface IndexListener {
		/**
		 * Select the item previous to the receiver.
		 */
		public void selectPrevious();

		/**
		 * Select the next previous to the receiver.
		 */
		public void selectNext();

		/**
		 * Select the receiver.
		 */
		public void select();
	}

	IndexListener indexListener;

	private int currentIndex;

	private boolean selected;

	private MouseAdapter mouseListener;

	private boolean isShowing = true;

	static {
		JFaceResources
				.getImageRegistry()
				.put(
						STOP_IMAGE_KEY,
						WorkbenchImages
								.getWorkbenchImageDescriptor("elcl16/progress_stop.gif"));//$NON-NLS-1$

		JFaceResources
				.getImageRegistry()
				.put(
						DISABLED_STOP_IMAGE_KEY,
						WorkbenchImages
								.getWorkbenchImageDescriptor("dlcl16/progress_stop.gif"));//$NON-NLS-1$

		JFaceResources
				.getImageRegistry()
				.put(
						DEFAULT_JOB_KEY,
						WorkbenchImages
								.getWorkbenchImageDescriptor("progress/progress_task.gif")); //$NON-NLS-1$

		JFaceResources
				.getImageRegistry()
				.put(
						CLEAR_FINISHED_JOB_KEY,
						WorkbenchImages
								.getWorkbenchImageDescriptor("elcl16/progress_rem.gif")); //$NON-NLS-1$

		JFaceResources
				.getImageRegistry()
				.put(
						DISABLED_CLEAR_FINISHED_JOB_KEY,
						WorkbenchImages
								.getWorkbenchImageDescriptor("dlcl16/progress_rem.gif")); //$NON-NLS-1$

		// Mac has different Gamma value
		int shift = "carbon".equals(SWT.getPlatform()) ? -25 : -10;//$NON-NLS-1$ 

		Color lightColor = PlatformUI.getWorkbench().getDisplay()
				.getSystemColor(SWT.COLOR_LIST_BACKGROUND);

		// Determine a dark color by shifting the list color
		RGB darkRGB = new RGB(Math.max(0, lightColor.getRed() + shift), Math.max(0,
						lightColor.getGreen() + shift), Math.max(0, lightColor
						.getBlue()
						+ shift));
		JFaceResources.getColorRegistry().put(DARK_COLOR_KEY,
				darkRGB);
	}

	/**
	 * Create a new instance of the receiver with the specified parent, style
	 * and info object/
	 * 
	 * @param parent
	 * @param style
	 * @param progressInfo
	 */
	public ProgressInfoItem(Composite parent, int style,
			JobTreeElement progressInfo) {
		super(parent, style);
		info = progressInfo;
		createChildren();
		setData(info);
		setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
	}

	/**
	 * Create the child widgets of the receiver.
	 */
	/**
	 * 
	 */
	protected void createChildren() {

		FormLayout layout = new FormLayout();
		setLayout(layout);

		jobImageLabel = new Label(this, SWT.NONE);
		jobImageLabel.setImage(getInfoImage());
		FormData imageData = new FormData();
		imageData.top = new FormAttachment(IDialogConstants.VERTICAL_SPACING);
		imageData.left = new FormAttachment(
				IDialogConstants.HORIZONTAL_SPACING / 2);
		jobImageLabel.setLayoutData(imageData);

		progressLabel = new Label(this, SWT.NONE);
		setMainText();

		actionBar = new ToolBar(this, SWT.FLAT);
		actionBar.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW)); // set
		// cursor
		// to
		// overwrite
		// any
		// busy

		// cursor we might have
		actionButton = new ToolItem(actionBar, SWT.NONE);
		actionButton
				.setToolTipText(ProgressMessages.NewProgressView_CancelJobToolTip);
		actionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				actionButton.setEnabled(false);
				cancelOrRemove();
			}

		});
		actionBar.addListener(SWT.Traverse, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				if (indexListener == null) {
					return;
				}
				int detail = event.detail;
				if (detail == SWT.TRAVERSE_ARROW_NEXT) {
					indexListener.selectNext();
				}
				if (detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
					indexListener.selectPrevious();
				}

			}
		});
		updateToolBarValues();

		FormData progressData = new FormData();
		progressData.top = new FormAttachment(IDialogConstants.VERTICAL_SPACING);
		progressData.left = new FormAttachment(jobImageLabel,
				IDialogConstants.HORIZONTAL_SPACING / 2);
		progressData.right = new FormAttachment(actionBar,
				IDialogConstants.HORIZONTAL_SPACING);
		progressLabel.setLayoutData(progressData);

		mouseListener = new MouseAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent e) {
				if (indexListener != null) {
					indexListener.select();
				}
			}
		};
		addMouseListener(mouseListener);
		jobImageLabel.addMouseListener(mouseListener);
		progressLabel.addMouseListener(mouseListener);

		setLayoutsForNoProgress();

		refresh();
	}

	/**
	 * Set the main text of the receiver. Truncate to fit the available space.
	 */
	private void setMainText() {
		progressLabel.setText(Dialog.shortenText(getMainTitle(),progressLabel));
	}

	/**
	 * Set the layout of the widgets for the no progress case.
	 * 
	 */
	private void setLayoutsForNoProgress() {

		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(progressLabel, 0, SWT.TOP);
		buttonData.right = new FormAttachment(100,
				IDialogConstants.HORIZONTAL_SPACING * -1);

		actionBar.setLayoutData(buttonData);
		if (taskEntries.size() > 0) {
			FormData linkData = new FormData();
			linkData.top = new FormAttachment(progressLabel,
					IDialogConstants.VERTICAL_SPACING);
			linkData.left = new FormAttachment(progressLabel, 0, SWT.LEFT);
			linkData.right = new FormAttachment(actionBar, 0, SWT.LEFT);
			((Link) taskEntries.get(0)).setLayoutData(linkData);

		}
	}

	/**
	 * Cancel or remove the reciever.
	 * 
	 */
	protected void cancelOrRemove() {

		if (FinishedJobs.getInstance().isKept(info) && isCompleted()) {
			FinishedJobs.getInstance().remove(info);
		} else {
			info.cancel();
		}

	}

	/**
	 * Get the image for the info.
	 * 
	 * @return Image
	 */
	private Image getInfoImage() {

		if (!info.isJobInfo()) {
			return JFaceResources.getImage(DEFAULT_JOB_KEY);
		}

		JobInfo jobInfo = (JobInfo) info;

		ImageDescriptor descriptor = null;
		Object property = jobInfo.getJob().getProperty(
				IProgressConstants.ICON_PROPERTY);

		if (property instanceof ImageDescriptor) {
			descriptor = (ImageDescriptor) property;
		} else if (property instanceof URL) {
			descriptor = ImageDescriptor.createFromURL((URL) property);
		}

		Image image = null;
		if (descriptor == null) {
			image = ProgressManager.getInstance().getIconFor(jobInfo.getJob());
		} else {
			image = JFaceResources.getResources().createImageWithDefault(
					descriptor);
		}

		if (image == null)
			image = jobInfo.getDisplayImage();

		return image;
	}

	/**
	 * Get the main title for the receiver.
	 * 
	 * @return String
	 */
	private String getMainTitle() {
		if (info.isJobInfo()) {
			return getJobNameAndStatus((JobInfo) info);
		}
		if (info.hasChildren()) {
			return ((GroupInfo) info).getTaskName();
		}
		return info.getDisplayString();

	}

	/**
	 * Get the name and status for a jobInfo
	 * 
	 * @return String
	 */
	protected String getJobNameAndStatus(JobInfo jobInfo) {

		Job job = jobInfo.getJob();

		String name = job.getName();

		if (job.isSystem()) {
			name = NLS.bind(ProgressMessages.JobInfo_System, name);
		}

		if (jobInfo.isCanceled()) {
			if (job.getState() == Job.RUNNING)
				return NLS
						.bind(ProgressMessages.JobInfo_Cancel_Requested, name);
			return NLS.bind(ProgressMessages.JobInfo_Cancelled, name);
		}

		if (jobInfo.isBlocked()) {
			IStatus blockedStatus = jobInfo.getBlockedStatus();
			return NLS.bind(ProgressMessages.JobInfo_Blocked, name,
					blockedStatus.getMessage());
		}

		switch (job.getState()) {
		case Job.RUNNING:
			return name;
		case Job.SLEEPING: {
			return NLS.bind(ProgressMessages.JobInfo_Sleeping, name);

		}
		case Job.NONE: // Only happens for kept jobs
			return getJobInfoFinishedString(job, true);
		default:
			return NLS.bind(ProgressMessages.JobInfo_Waiting, name);
		}
	}

	/**
	 * Return the finished String for a job.
	 * 
	 * @param job
	 *            the completed Job
	 * @param withTime
	 * @return String
	 */
	String getJobInfoFinishedString(Job job, boolean withTime) {
		String time = null;
		if (withTime) {
			time = getTimeString();
		}
		if (time != null) {
			return NLS.bind(ProgressMessages.JobInfo_FinishedAt, job.getName(),
					time);
		}
		return NLS.bind(ProgressMessages.JobInfo_Finished, job.getName());
	}

	/**
	 * Get the time string the finished job
	 * 
	 * @return String or <code>null</code> if this is not one of the finished
	 *         jobs.
	 */
	private String getTimeString() {
		Date date = FinishedJobs.getInstance().getFinishDate(info);
		if (date != null) {
			return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
		}
		return null;
	}

	/**
	 * Refresh the contents of the receiver.
	 * 
	 */
	void refresh() {

		// Don't refresh if not visible
		if (isDisposed() || !isShowing)
			return;

		jobImageLabel.setImage(getInfoImage());
		int percentDone = getPercentDone();

		JobInfo[] infos = getJobInfos();
		if (isRunning()) {
			if (progressBar == null) {
				if (percentDone == IProgressMonitor.UNKNOWN) {
					// Only do it if there is an indeterminate task
					// There may be no task so we don't want to create it
					// until we know for sure
					for (int i = 0; i < infos.length; i++) {
						if (infos[i].hasTaskInfo()
								&& infos[i].getTaskInfo().totalWork == IProgressMonitor.UNKNOWN) {
							createProgressBar(SWT.INDETERMINATE);
							break;
						}
					}
				} else {
					createProgressBar(SWT.NONE);
					progressBar.setMinimum(0);
					progressBar.setMaximum(100);
				}
			}

			// Protect against bad counters
			if (percentDone >= 0 && percentDone <= 100
					&& percentDone != progressBar.getSelection()) {
				progressBar.setSelection(percentDone);
			}
		}

		else if (isCompleted()) {

			if (progressBar != null) {
				progressBar.dispose();
				progressBar = null;
			}
			setLayoutsForNoProgress();

		}

		for (int i = 0; i < infos.length; i++) {
			JobInfo jobInfo = infos[i];
			if (jobInfo.hasTaskInfo()) {

				String taskString = jobInfo.getTaskInfo().getTaskName();
				String subTaskString = null;
				Object[] jobChildren = jobInfo.getChildren();
				if (jobChildren.length > 0) {
					subTaskString = ((JobTreeElement) jobChildren[0])
							.getDisplayString();
				}

				if (subTaskString != null) {
					if (taskString == null || taskString.length() == 0) {
						taskString = subTaskString;
					} else {
						taskString = NLS.bind(
								ProgressMessages.JobInfo_DoneNoProgressMessage,
								taskString, subTaskString);
					}
				}
				if (taskString != null) {
					setLinkText(infos[i].getJob(), taskString, i);
				}
			} else {// Check for the finished job state
				Job job = jobInfo.getJob();
				IStatus result = job.getResult();

				if (result == null || result.getMessage().length() == 0
						&& !info.isJobInfo()) {
					setLinkText(job, getJobNameAndStatus(jobInfo), i);
				} else {
					setLinkText(job, result.getMessage(), i);

				}

			}
			setColor(currentIndex);
		}

		// Remove completed tasks
		if (infos.length < taskEntries.size()) {
			for (int i = infos.length; i < taskEntries.size(); i++) {
				((Link) taskEntries.get(i)).dispose();

			}
			if (infos.length > 1)
				taskEntries = taskEntries.subList(0, infos.length - 1);
			else
				taskEntries.clear();
		}

		updateToolBarValues();
		setMainText();
	}

	/**
	 * Return whether or not the receiver is a completed job.
	 * 
	 * @return boolean <code>true</code> if the state is Job#NONE.
	 */
	private boolean isCompleted() {

		JobInfo[] infos = getJobInfos();
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].getJob().getState() != Job.NONE) {
				return false;
			}
		}
		// Only completed if there are any jobs
		return infos.length > 0;
	}

	/**
	 * Return the job infos in the receiver.
	 * 
	 * @return JobInfo[]
	 */
	private JobInfo[] getJobInfos() {
		if (info.isJobInfo()) {
			return new JobInfo[] { (JobInfo) info };
		}
		Object[] children = info.getChildren();
		JobInfo[] infos = new JobInfo[children.length];
		System.arraycopy(children, 0, infos, 0, children.length);
		return infos;
	}

	/**
	 * Return whether or not the receiver is being displayed as running.
	 * 
	 * @return boolean
	 */
	private boolean isRunning() {

		JobInfo[] infos = getJobInfos();
		for (int i = 0; i < infos.length; i++) {
			int state = infos[i].getJob().getState();
			if (state == Job.WAITING || state == Job.RUNNING)
				return true;
		}
		return false;
	}

	/**
	 * Get the current percent done.
	 * 
	 * @return int
	 */
	private int getPercentDone() {
		if (info.isJobInfo()) {
			return ((JobInfo) info).getPercentDone();
		}

		if (info.hasChildren()) {
			Object[] roots = ((GroupInfo) info).getChildren();
			if (roots.length == 1 && roots[0] instanceof JobTreeElement) {
				TaskInfo ti = ((JobInfo) roots[0]).getTaskInfo();
				if (ti != null) {
					return ti.getPercentDone();
				}
			}
			return ((GroupInfo) info).getPercentDone();
		}
		return 0;
	}

	/**
	 * Set the images in the toolbar based on whether the receiver is finished
	 * or not. Also update tooltips if required.
	 * 
	 */
	private void updateToolBarValues() {
		if (isCompleted()) {
			actionButton.setImage(JFaceResources
					.getImage(CLEAR_FINISHED_JOB_KEY));
			actionButton.setDisabledImage(JFaceResources
					.getImage(DISABLED_CLEAR_FINISHED_JOB_KEY));
			actionButton
					.setToolTipText(ProgressMessages.NewProgressView_ClearJobToolTip);
		} else {
			actionButton.setImage(JFaceResources.getImage(STOP_IMAGE_KEY));
			actionButton.setDisabledImage(JFaceResources
					.getImage(DISABLED_STOP_IMAGE_KEY));

		}
		JobInfo[] infos = getJobInfos();

		for (int i = 0; i < infos.length; i++) {
			// Only disable if there is an unresponsive operation
			if (infos[i].isCanceled() && !isCompleted()) {
				actionButton.setEnabled(false);
				return;
			}
		}
		actionButton.setEnabled(true);
	}

	/**
	 * Create the progress bar and apply any style bits from style.
	 * 
	 * @param style
	 */
	void createProgressBar(int style) {

		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(progressLabel, 0);
		buttonData.right = new FormAttachment(100,
				IDialogConstants.HORIZONTAL_SPACING * -1);

		actionBar.setLayoutData(buttonData);

		progressBar = new ProgressBar(this, SWT.HORIZONTAL | style);
		FormData barData = new FormData();
		barData.top = new FormAttachment(actionBar,
				IDialogConstants.VERTICAL_SPACING, SWT.TOP);
		barData.left = new FormAttachment(progressLabel, 0, SWT.LEFT);
		barData.right = new FormAttachment(actionBar,
				IDialogConstants.HORIZONTAL_SPACING * -1);
		barData.height = MAX_PROGRESS_HEIGHT;
		barData.width = 0;// default is too large
		progressBar.setLayoutData(barData);

		if (taskEntries.size() > 0) {
			// Reattach the link label if there is one
			FormData linkData = new FormData();
			linkData.top = new FormAttachment(progressBar,
					IDialogConstants.VERTICAL_SPACING);
			linkData.left = new FormAttachment(
					IDialogConstants.HORIZONTAL_SPACING);
			linkData.right = new FormAttachment(progressBar, 0, SWT.RIGHT);
			//Give an initial value so as to constrain the link shortening
			linkData.width = IDialogConstants.INDENT; 

			((Link) taskEntries.get(0)).setLayoutData(linkData);
		}
	}

	/**
	 * Set the text of the link to the taskString.
	 * 
	 * @param taskString
	 */
	void setLinkText(Job linkJob, String taskString, int index) {

		Link link;
		if (index >= taskEntries.size()) {// Is it new?
			link = new Link(this, SWT.NONE);

			FormData linkData = new FormData();
			if (index == 0 || taskEntries.size() == 0) {
				Control top = progressBar;
				if (top == null) {
					top = progressLabel;
				}
				linkData.top = new FormAttachment(top,
						IDialogConstants.VERTICAL_SPACING);
				linkData.left = new FormAttachment(top, 0, SWT.LEFT);
				linkData.right = new FormAttachment(progressBar, 0, SWT.RIGHT);
				//Give an initial value so as to constrain the link shortening
				linkData.width = IDialogConstants.INDENT; 
			} else {
				Link previous = (Link) taskEntries.get(index - 1);
				linkData.top = new FormAttachment(previous,
						IDialogConstants.VERTICAL_SPACING);
				linkData.left = new FormAttachment(previous, 0, SWT.LEFT);
				linkData.right = new FormAttachment(previous, 0, SWT.RIGHT);
				//Give an initial value so as to constrain the link shortening
				linkData.width = IDialogConstants.INDENT; 
			}

			link.setLayoutData(linkData);

			final Link finalLink = link;

			link.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {

					IAction action = (IAction) finalLink.getData(ACTION_KEY);
					action.run();

					updateAction(action,finalLink);

					Object text = finalLink.getData(TEXT_KEY);
					if (text == null)
						return;

					// Refresh the text as enablement might have changed
					updateText((String) text, finalLink);
				}
			});

			link.addListener(SWT.Resize, new Listener() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
				 */
				public void handleEvent(Event event) {

					Object text = finalLink.getData(TEXT_KEY);
					if (text == null)
						return;

					updateText((String) text, finalLink);

				}
			});
			taskEntries.add(link);
		} else {
			link = (Link) taskEntries.get(index);
		}

		link.setToolTipText(taskString);
		link.setData(TEXT_KEY, taskString);

		// check for action property
		Object property = linkJob
				.getProperty(IProgressConstants.ACTION_PROPERTY);
		updateAction(property, link);

		updateText(taskString, link);

	}

	/**
	 * Update the action key if action is enabled or remove it if not
	 * 
	 * @param action
	 *            {@link Object} or <code>null</code>
	 * @param link
	 */
	private void updateAction(Object action, Link link) {

		if (action != null && action instanceof IAction
				&& ((IAction) action).isEnabled())
			link.setData(ACTION_KEY, action);
		else
			link.setData(ACTION_KEY, null);

	}

	/**
	 * Update the text in the link
	 * 
	 * @param taskString
	 * @param link
	 */
	private void updateText(String taskString, Link link) {
		taskString = Dialog.shortenText(taskString, link);

		// Put in a hyperlink if there is an action
		link.setText(link.getData(ACTION_KEY) == null ? taskString : NLS.bind(
				"<a>{0}</a>", taskString));//$NON-NLS-1$
	}

	/**
	 * Set the color base on the index
	 * 
	 * @param i
	 */
	public void setColor(int i) {
		currentIndex = i;

		if (selected) {
			setAllBackgrounds(getDisplay().getSystemColor(
					SWT.COLOR_LIST_SELECTION));
			setAllForegrounds(getDisplay().getSystemColor(
					SWT.COLOR_LIST_SELECTION_TEXT));
			return;
		}

		if (i % 2 == 0) {
			setAllBackgrounds(JFaceResources.getColorRegistry().get(
					DARK_COLOR_KEY));
		} else {
			setAllBackgrounds(getDisplay().getSystemColor(
					SWT.COLOR_LIST_BACKGROUND));
		}
		setAllForegrounds(getDisplay()
				.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
	}

	/**
	 * Set the foreground of all widgets to the supplied color.
	 * 
	 * @param color
	 */
	private void setAllForegrounds(Color color) {
		setForeground(color);
		progressLabel.setForeground(color);

		Iterator taskEntryIterator = taskEntries.iterator();
		while (taskEntryIterator.hasNext()) {
			((Link) taskEntryIterator.next()).setForeground(color);
		}

	}

	/**
	 * Set the background of all widgets to the supplied color.
	 * 
	 * @param color
	 */
	private void setAllBackgrounds(Color color) {
		setBackground(color);
		progressLabel.setBackground(color);
		actionBar.setBackground(color);
		jobImageLabel.setBackground(color);

		Iterator taskEntryIterator = taskEntries.iterator();
		while (taskEntryIterator.hasNext()) {
			((Link) taskEntryIterator.next()).setBackground(color);
		}

	}

	/**
	 * Set the focus to the button.
	 * 
	 */
	void setButtonFocus() {
		actionBar.setFocus();
	}

	/**
	 * Set the selection colors.
	 * 
	 * @param select
	 *            boolean that indicates whether or not to show selection.
	 */
	void selectWidgets(boolean select) {
		if (select) {
			setButtonFocus();
		}
		selected = select;
		setColor(currentIndex);
	}

	/**
	 * Set the listener for index changes.
	 * 
	 * @param indexListener
	 */
	void setIndexListener(IndexListener indexListener) {
		this.indexListener = indexListener;
	}

	/**
	 * Return whether or not the receiver is selected.
	 * 
	 * @return boolean
	 */
	boolean isSelected() {
		return selected;
	}

	/**
	 * Set whether or not the receiver is being displayed based on the top and
	 * bottom of the currently visible area.
	 * 
	 * @param top
	 * @param bottom
	 */
	void setDisplayed(int top, int bottom) {
		int itemTop = getLocation().y;
		int itemBottom = itemTop + getBounds().height;
		setDisplayed(itemTop <= bottom && itemBottom > top);

	}

	/**
	 * Set whether or not the receiver is being displayed
	 * 
	 * @param displayed
	 */
	private void setDisplayed(boolean displayed) {
		// See if this element has been turned off
		boolean refresh = !isShowing && displayed;
		isShowing = displayed;
		if (refresh)
			refresh();
	}
}
