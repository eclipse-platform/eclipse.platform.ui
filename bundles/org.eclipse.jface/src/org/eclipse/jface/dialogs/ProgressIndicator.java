package org.eclipse.jface.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.AnimatedProgress;
import org.eclipse.swt.custom.StackLayout;

/**
 * A control for showing progress feedback for a long running operation.
 * In contrast to the native SWT ProgressBar control
 * this control supports a so called "ANIMATED" mode
 * where we don't have to know the total amount of work in advance
 * and no <code>worked</code> method needs to be called.
 */
public class ProgressIndicator extends Composite {

	private boolean animated = true;

	private StackLayout layout;

	// In TICKS mode we can use the native progress bar.
	private ProgressBar nativeProgressBar;
	private AnimatedProgress animatedProgressBar;

	private double totalWork;
	private double sumWorked;

	private static final int PROGRESS_MAX = 1000; // value to use for max in progress bar
	/**
	 * Create a ProgressIndicator as a child under the given parent.
	 * @param parent The widgets parent
	 */
	public ProgressIndicator(Composite parent) {
		super(parent, SWT.NULL);

		nativeProgressBar = new ProgressBar(this, SWT.HORIZONTAL);
		animatedProgressBar = new AnimatedProgress(this, SWT.HORIZONTAL | SWT.BORDER);
		layout = new StackLayout();
		setLayout(layout);
	}
	/**
	 * Initialize the progress bar to be animated.
	 */
	public void beginAnimatedTask() {
		done();
		animatedProgressBar.start();
		layout.topControl = animatedProgressBar;
		layout();
		animated = true;
	}
	/**
	 * Initialize the progress bar.
	 * @param max The maximum value.
	 */
	public void beginTask(int totalWork) {
		done();

		this.totalWork = totalWork;
		this.sumWorked = 0;
		
		nativeProgressBar.setMinimum(0);
		nativeProgressBar.setMaximum(PROGRESS_MAX);
		nativeProgressBar.setSelection(0);
		layout.topControl = nativeProgressBar;
		layout();
		animated = false;
	}
	/**
	 * Progress is done.
	 */
	public void done() {
		if (animated) {
			animatedProgressBar.clear();
		} else {
			nativeProgressBar.setMinimum(0);
			nativeProgressBar.setMaximum(0);
			nativeProgressBar.setSelection(0);
		}

		layout.topControl = null;
		layout();
		
	}
	/**
	 * Moves the progress indicator to the end.
	 */
	public void sendRemainingWork() {
		worked(totalWork - sumWorked);
	}
	/**
	 * Moves the progress indicator by the given amount of work units 
	 */
	public void worked(double work) {
		if (work == 0 || animated) return;

		sumWorked += work;
		if (sumWorked > totalWork)
			sumWorked = totalWork;
		if (sumWorked < 0)
			sumWorked = 0;
		int value = (int) (sumWorked / totalWork * PROGRESS_MAX);
		if (nativeProgressBar.getSelection() < value)
			nativeProgressBar.setSelection(value);
	}
}
