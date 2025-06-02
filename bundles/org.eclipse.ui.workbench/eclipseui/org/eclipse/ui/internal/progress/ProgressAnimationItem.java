/*******************************************************************************
 * Copyright (c) 2004, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422040, 440810
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.time.Duration;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.util.Throttler;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.accessibility.AccessibleListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.IProgressConstants2;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * The ProgressAnimationItem is the animation items that uses the progress bar.
 */
public class ProgressAnimationItem extends AnimationItem implements FinishedJobs.KeptJobsListener {

	ProgressIndicator bar;

	MouseListener mouseListener;

	Composite top;

	ToolBar toolbar;

	ToolItem toolButton;

	ProgressRegion progressRegion;

	Image noneImage, okImage, errorImage;

	boolean animationRunning;

	// ProgressBar flags
	private int flags;

	private AccessibleListener currentAccessibleListener;

	private Throttler throttledRefresh = new Throttler(PlatformUI.getWorkbench().getDisplay(), Duration.ofMillis(100),
			this::refresh);

	/**
	 * Create an instance of the receiver in the supplied region.
	 *
	 * @param region The ProgressRegion that contains the receiver.
	 * @param flags  flags to use for creation of the progress bar
	 */
	ProgressAnimationItem(ProgressRegion region, int flags) {
		super(region.workbenchWindow);
		this.flags = flags;
		FinishedJobs.getInstance().addListener(this);

		progressRegion = region;
		mouseListener = new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				doAction();
			}
		};
	}

	void doAction() {

		JobTreeElement[] jobTreeElements = FinishedJobs.getInstance().getKeptElements();
		// search from end (youngest)
		for (int i = jobTreeElements.length - 1; i >= 0; i--) {
			if (jobTreeElements[i] instanceof JobInfo) {
				JobInfo ji = (JobInfo) jobTreeElements[i];
				Job job = ji.getJob();
				if (job != null) {

					IStatus status = job.getResult();
					if (status != null && status.getSeverity() == IStatus.ERROR) {
						StatusAdapter statusAdapter = StatusAdapterHelper.getInstance().getStatusAdapter(ji);

						if (statusAdapter == null)
							statusAdapter = new StatusAdapter(status);

						StatusManager.getManager().handle(statusAdapter, StatusManager.SHOW);

						removeTopElement(ji);
					}

					// To fix a bug (335543) introduced in 3.6.1.
					// doAction() should return if progress region button was
					// selected to open a job result action or command.
					if (execute(ji, job)) {
						return;
					}
				}
			}
		}

		progressRegion.processDoubleClick();
		refresh();
	}

	/**
	 * @return <code>true</code> if Action or Command is executed
	 */
	private boolean execute(JobInfo ji, Job job) {

		Object prop = job.getProperty(IProgressConstants.ACTION_PROPERTY);
		if (prop instanceof IAction && ((IAction) prop).isEnabled()) {
			IAction action = (IAction) prop;
			action.run();
			removeTopElement(ji);
			return true;
		}

		prop = job.getProperty(IProgressConstants2.COMMAND_PROPERTY);
		if (prop instanceof ParameterizedCommand) {
			ParameterizedCommand command = (ParameterizedCommand) prop;
			IWorkbenchWindow window = getWindow();
			IHandlerService service = window.getService(IHandlerService.class);
			Exception exception = null;
			try {
				service.executeCommand(command, null);
				removeTopElement(ji);
			} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
				exception = e;
			}

			if (exception != null) {
				Status status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, exception.getMessage(), exception);
				StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
			}
			return true;
		}
		return false;
	}

	private void removeTopElement(JobInfo ji) {
		JobTreeElement topElement = ji.getParent();
		if (topElement == null) {
			topElement = ji;
		}
		FinishedJobs.getInstance().remove(topElement);
	}

	private IAction getAction(Job job) {
		Object property = job.getProperty(IProgressConstants.ACTION_PROPERTY);
		if (property instanceof IAction) {
			return (IAction) property;
		}
		return null;
	}

	private void refresh() {

		// Abort the refresh if we are in the process of shutting down
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}

		if (toolbar == null || toolbar.isDisposed()) {
			return;
		}
		if (bar != null && !bar.isDisposed()) {
			JobInfo[] jobInfos = ProgressManager.getInstance().getJobInfos(false);
			int percentSum = 0;
			int percentCount = 0;
			for (JobInfo jobInfo : jobInfos) {
				if (jobInfo != null && !(jobInfo.isBlocked() || jobInfo.getJob().getState() == Job.WAITING)) {
					int percentDone = jobInfo.getPercentDone();
					if (percentDone >= 0) {
						percentSum += percentDone;
						percentCount++;
					}
				}
			}
			if (percentCount > 0) {
				bar.setWork(100, percentSum / percentCount); // average
				AnimationManager.getInstance().setAnimated(true); // reschedule
			} else {
				if (jobInfos.length > 0) {
					bar.beginAnimatedTask();
				}
			}
		}

		JobTreeElement[] jobTreeElements = FinishedJobs.getInstance().getKeptElements();
		// search from end (youngest)
		for (int i = jobTreeElements.length - 1; i >= 0; i--) {
			if (jobTreeElements[i] instanceof JobInfo) {
				JobInfo ji = (JobInfo) jobTreeElements[i];
				Job job = ji.getJob();
				if (job != null) {
					IStatus status = job.getResult();
					if (status != null && status.getSeverity() == IStatus.ERROR) {
						// green arrow with error overlay
						initButton(errorImage, NLS.bind(ProgressMessages.ProgressAnimationItem_error, job.getName()));
						return;
					}
					IAction action = getAction(job);
					if (action != null && action.isEnabled()) {
						// green arrow with exclamation mark
						String tt = action.getToolTipText();
						if (tt == null || tt.trim().isEmpty()) {
							tt = NLS.bind(ProgressMessages.ProgressAnimationItem_ok, job.getName());
						}
						initButton(okImage, tt);
						return;
					}
					// just the green arrow
					initButton(noneImage, ProgressMessages.ProgressAnimationItem_tasks);
					return;
				}
			}
		}

		if (animationRunning) {
			initButton(noneImage, ProgressMessages.ProgressAnimationItem_tasks);
			return;
		}

		// if nothing found hide tool item
		toolbar.setVisible(false);
	}

	private void initButton(Image im, final String tt) {
		toolButton.setImage(im);
		toolButton.setToolTipText(tt);
		toolbar.setVisible(true);
		toolbar.getParent().requestLayout(); // must layout

		if (currentAccessibleListener != null)
			toolbar.getAccessible().removeAccessibleListener(currentAccessibleListener);
		currentAccessibleListener = new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = tt;
			}
		};
		toolbar.getAccessible().addAccessibleListener(currentAccessibleListener);
	}

	@Override
	protected Control createAnimationItem(Composite parent) {

		if (okImage == null) {
			Display display = parent.getDisplay();
			noneImage = WorkbenchImages.getWorkbenchImageDescriptor("progress/progress_none.svg").createImage(display); //$NON-NLS-1$
			okImage = WorkbenchImages.getWorkbenchImageDescriptor("progress/progress_ok.svg").createImage(display); //$NON-NLS-1$
			errorImage = WorkbenchImages.getWorkbenchImageDescriptor("progress/progress_error.svg") //$NON-NLS-1$
					.createImage(display);
		}

		top = new Composite(parent, SWT.NULL);
		top.addDisposeListener(e -> {
			FinishedJobs.getInstance().removeListener(ProgressAnimationItem.this);
			noneImage.dispose();
			okImage.dispose();
			errorImage.dispose();
		});

		boolean isCarbon = Util.isMac();

		GridLayout gl = new GridLayout();
		if (isHorizontal())
			gl.numColumns = isCarbon ? 3 : 2;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		if (isHorizontal()) {
			gl.horizontalSpacing = 2;
		} else {
			gl.verticalSpacing = 2;
		}
		top.setLayout(gl);

		bar = new ProgressIndicator(top, flags);
		bar.setVisible(false);
		bar.addMouseListener(mouseListener);

		GridData gd;
		int hh = 12;
		if (isHorizontal()) {
			gd = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
			gd.heightHint = hh;
		} else {
			gd = new GridData(SWT.CENTER, SWT.BEGINNING, false, true);
			gd.widthHint = hh;
		}

		bar.setLayoutData(gd);

		toolbar = new ToolBar(top, SWT.FLAT);
		toolbar.setVisible(false);

		toolButton = new ToolItem(toolbar, SWT.NONE);
		toolButton.addSelectionListener(widgetSelectedAdapter(e -> doAction()));

		if (isCarbon) {
			new Label(top, SWT.NONE).setLayoutData(new GridData(4, 4));
		}

		refresh();

		return top;
	}

	/**
	 * @return <code>true</code> if the control is horizontally oriented
	 */
	private boolean isHorizontal() {
		return (flags & SWT.HORIZONTAL) != 0;
	}

	@Override
	public Control getControl() {
		return top;
	}

	@Override
	void animationDone() {
		super.animationDone();
		animationRunning = false;
		if (bar.isDisposed()) {
			return;
		}
		bar.setVisible(false);
		refresh();
	}

	/**
	 * @return <code>true</code> when the animation is running
	 */
	public boolean animationRunning() {
		return animationRunning;
	}

	@Override
	void animationStart() {
		super.animationStart();
		animationRunning = true;
		if (bar.isDisposed()) {
			return;
		}
		bar.setVisible(true);
		refresh();
	}

	@Override
	public void removed(JobTreeElement info) {
		throttledRefresh.throttledExec();
	}

	@Override
	public void finished(final JobTreeElement jte) {
		throttledRefresh.throttledExec();
	}

}
