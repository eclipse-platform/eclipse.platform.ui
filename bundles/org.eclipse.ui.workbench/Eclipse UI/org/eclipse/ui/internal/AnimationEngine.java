/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * This job creates an Animation Engine that uses an Animation Feedback to render
 * the animation. To begin the animation, instantiate this
 * object then call schedule().
 * @since 3.3
 *
 */
public class AnimationEngine extends Job {
	public static final int TICK_TIMER = 1;
	public static final int unlimitedDuration = -1;
	private boolean enableAnimations;
	private Display display;
	private AnimationFeedbackBase feedbackRenderer;
	private long startTime;
	private long curTime;
	private long prevTime;
	private int timingStyle = TICK_TIMER;
	private long frameCount;
	private int duration;
	private long stepCount;
	public static final int FRAME_COUNT = 2;
	private boolean stopAnimating = false;
	private long sleepAmount;

	public AnimationEngine(AnimationFeedbackBase animationFeedback,
			int durationIn) {
		this(animationFeedback, durationIn, 0);
	}

	/**
	 * Creates an Animation that will run for the given number of milliseconds.
	 * 
	 * @param animationFeedback provides renderStep(), initialize() and jobInit() methods
	 * @param durationIn number of milliseconds over which the animation will run
	 * @param sleepAmountIn number of milliseconds to slow/delay the animation
	 */
	public AnimationEngine(AnimationFeedbackBase animationFeedback,
			int durationIn, long sleepAmountIn) {
		super(WorkbenchMessages.RectangleAnimation_Animating_Rectangle);
		sleepAmount = sleepAmountIn;
		feedbackRenderer = animationFeedback;
		duration = durationIn;
		
		// if animations aren't on this is a NO-OP
		IPreferenceStore preferenceStore = PrefUtil.getAPIPreferenceStore();
		enableAnimations = preferenceStore
				.getBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS);
		if (!enableAnimations) {
			return;
		}

		stopAnimating = false;

		// Capture parameters
		display = feedbackRenderer.getAnimationShell().getDisplay();
		// Don't show the job in monitors
		setSystem(true);

		// Set it up
		feedbackRenderer.initialize(this);

		// Set the animation's initial state
		stepCount = 0;
		curTime = startTime = System.currentTimeMillis();

	}

	/**
	 * @return The current renderer
	 */
	public AnimationFeedbackBase getFeedback() {
		return feedbackRenderer;
	}
	
	private Runnable animationStep = new Runnable() {

		public void run() {
			// Capture time
			prevTime = curTime;
			curTime = System.currentTimeMillis();

			if (isUpdateStep()) {
				updateDisplay();
				frameCount++;
			}
			stepCount++;
		}

	};

	protected void updateDisplay() {
		feedbackRenderer.renderStep(this);
	}

	protected boolean isUpdateStep() {
		if (duration == unlimitedDuration) {
			return true;
		}
		
		switch (timingStyle) {
			case TICK_TIMER:
				return prevTime != curTime;
				//for testing purposes
			case FRAME_COUNT:
				return true;
		}

		return false;
	}

	private boolean done() {
		return amount() >= 1.0;
	}

	public double amount() {
		if (duration == unlimitedDuration) {
			return 0;
		}
		double amount = 0.0;
		switch (timingStyle) {
			case TICK_TIMER:
				amount = (double) (curTime - startTime) / (double) duration;
				break;
	
			// For testing purposes
			case FRAME_COUNT:
				amount = (double) frameCount / (double) duration;
		}

		if (amount > 1.0)
			amount = 1.0;

		return amount;
	}

	protected IStatus run(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

		// We use preferece value to indicate that the animation should be skipped on this platform.
		if (!enableAnimations) {
			return Status.OK_STATUS;
		}

		// We're starting, initialize
		display.syncExec(new Runnable() {
			public void run() {
				// 'jobInit' returns 'false' if it doesn't want to run...
				stopAnimating = !feedbackRenderer.jobInit(AnimationEngine.this);
			}
		});

		// Only start the animation timer -after- we've initialized
		curTime = startTime = System.currentTimeMillis();

		while (!done() && !stopAnimating) {
			display.syncExec(animationStep);
			// Don't pin the CPU
			try {
				Thread.sleep(sleepAmount);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}

		// We're done, clean up
		display.syncExec(new Runnable() {
			public void run() {
				feedbackRenderer.dispose();
			}
		});

		return Status.OK_STATUS;
	}

	public void cancelAnimation() {
		stopAnimating = true;
	}

	public long getFrameCount() {
		return frameCount;
	}
}
