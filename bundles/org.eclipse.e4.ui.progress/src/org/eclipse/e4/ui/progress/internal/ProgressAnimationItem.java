/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Philipp Bumann <bumannp@gmail.com> - Bug 477602
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.progress.IProgressConstants;
import org.eclipse.e4.ui.progress.internal.legacy.PlatformUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * The ProgressAnimationItem is the animation items that uses the progress bar.
 */
public class ProgressAnimationItem extends AnimationItem implements
		FinishedJobs.KeptJobsListener {

	ProgressBar bar;

	MouseListener mouseListener;

	Composite top;

	ToolBar toolbar;

	ToolItem toolButton;

	ProgressRegion progressRegion;

	Image noneImage, okImage, errorImage;

	boolean animationRunning;

	// ProgressBar flags
	private int flags;

	private FinishedJobs finishedJobs;

	/**
	 * Create an instance of the receiver in the supplied region.
	 *
	 * @param region
	 *            The ProgressRegion that contains the receiver.
	 * @param flags
	 *            flags to use for creation of the progress bar
	 */
	ProgressAnimationItem(ProgressRegion region, int flags,
	        AnimationManager animationManager, FinishedJobs finishedJobs) {
		super(animationManager);
		this.flags = flags;
		this.finishedJobs = finishedJobs;
		finishedJobs.addListener(this);

		progressRegion = region;
		mouseListener = MouseListener.mouseDoubleClickAdapter(e -> doAction());
	}

	void doAction() {

		JobTreeElement[] jobTreeElements = finishedJobs.getKeptElements();
		// search from end (youngest)
		for (int i = jobTreeElements.length - 1; i >= 0; i--) {
			if (jobTreeElements[i] instanceof JobInfo) {
				JobInfo ji = (JobInfo) jobTreeElements[i];
				Job job = ji.getJob();
				if (job != null) {

					IStatus status = job.getResult();
					if (status != null && status.getSeverity() == IStatus.ERROR) {
						//TODO E4
//						StatusAdapter statusAdapter = StatusAdapterHelper
//								.getInstance().getStatusAdapter(ji);

//						if (statusAdapter == null)
//							statusAdapter = new StatusAdapter(status);
						getStatusReporter().report(status,
						        StatusReporter.SHOW, new Object[0]);
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
	 * @param ji
	 * @param job
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

		prop = job.getProperty(IProgressConstants.COMMAND_PROPERTY);
		if (prop instanceof ParameterizedCommand) {
			ParameterizedCommand command = (ParameterizedCommand) prop;
			getEHandlerService().executeHandler(command);
			removeTopElement(ji);
			return true;
		}
		return false;
	}

	/**
	 * @param ji
	 */
	private void removeTopElement(JobInfo ji) {
		JobTreeElement topElement = ji.getParent();
		if (topElement == null) {
			topElement = ji;
		}
		finishedJobs.remove(topElement);
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

		JobTreeElement[] jobTreeElements = finishedJobs.getKeptElements();
		// search from end (youngest)
		for (int i = jobTreeElements.length - 1; i >= 0; i--) {
			if (jobTreeElements[i] instanceof JobInfo) {
				JobInfo ji = (JobInfo) jobTreeElements[i];
				Job job = ji.getJob();
				if (job != null) {
					IStatus status = job.getResult();
					if (status != null && status.getSeverity() == IStatus.ERROR) {
						// green arrow with error overlay
						initButton(errorImage, NLS.bind(
								ProgressMessages.ProgressAnimationItem_error,
								job.getName()));
						return;
					}
					IAction action = getAction(job);
					if (action != null && action.isEnabled()) {
						// green arrow with exclamation mark
						String tt = action.getToolTipText();
						if (tt == null || tt.trim().length() == 0) {
							tt = NLS.bind(
									ProgressMessages.ProgressAnimationItem_ok,
									job.getName());
						}
						initButton(okImage, tt);
						return;
					}
					// just the green arrow
					initButton(noneImage,
							ProgressMessages.ProgressAnimationItem_tasks);
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

    	toolbar.getAccessible().addAccessibleListener(new AccessibleAdapter() {
        	@Override
			public void getName(AccessibleEvent e) {
        		e.result = tt;
        	}
        });
	}

	@Override
	protected Control createAnimationItem(Composite parent) {

		if (okImage == null) {
			Display display = parent.getDisplay();
			ImageTools imageTools = ImageTools.getInstance();

			noneImage = imageTools.getImage("progress/progress_none.png", display); //$NON-NLS-1$
			okImage = imageTools.getImage("progress/progress_ok.png", display); //$NON-NLS-1$
			errorImage = imageTools.getImage("progress/progress_error.png", display); //$NON-NLS-1$
		}

		top = new Composite(parent, SWT.NULL);
		top.addDisposeListener(e -> {
			finishedJobs.removeListener(ProgressAnimationItem.this);
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

		bar = new ProgressBar(top, flags | SWT.INDETERMINATE);
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
		toolButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doAction();
			}
		});

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
		final Display display = Display.getDefault();
		display.asyncExec(() -> refresh());
	}

	@Override
	public void finished(final JobTreeElement jte) {
		final Display display = Display.getDefault();
		display.asyncExec(() -> refresh());
	}

	protected StatusReporter getStatusReporter() {
	    return Services.getInstance().getStatusReporter();
    }

	protected EHandlerService getEHandlerService() {
		return Services.getInstance().getEHandlerService();
	}

}
