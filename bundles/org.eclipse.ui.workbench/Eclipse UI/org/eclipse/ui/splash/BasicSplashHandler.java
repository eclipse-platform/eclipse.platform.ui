/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 430848
 *******************************************************************************/
package org.eclipse.ui.splash;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;

/**
 * Basic splash implementation that provides an absolute positioned progress bar
 * and message string that is hooked up to a progress monitor.
 *
 * @since 3.3
 */
public abstract class BasicSplashHandler extends AbstractSplashHandler {

	private static final String SPLASH_PROGRESS_PART_ID = "org-eclipse-ui-splash-progressPart"; //$NON-NLS-1$
	private static final String SPLASH_PROGRESS_INDICATOR_ID = "org-eclipse-ui-splash-progressIndicator"; //$NON-NLS-1$
	private static final String SPLASH_PROGRESS_TEXT_ID = "org-eclipse-ui-splash-progressText"; //$NON-NLS-1$

	/**
	 * Hacks the progress monitor to have absolute positioning for its controls.
	 * In addition, all methods that access the controls will be wrapped in an
	 * asynchExec().
	 */
	class AbsolutePositionProgressMonitorPart extends ProgressMonitorPart {
		public AbsolutePositionProgressMonitorPart(Composite parent) {
			super(parent, null);
			setLayout(null);
			setCSSData();
		}

		private void setCSSData() {
			this.setData(CSSSWTConstants.CSS_ID_KEY, SPLASH_PROGRESS_PART_ID);
			fProgressIndicator.setData(CSSSWTConstants.CSS_ID_KEY, SPLASH_PROGRESS_INDICATOR_ID);
			fLabel.setData(CSSSWTConstants.CSS_ID_KEY, SPLASH_PROGRESS_TEXT_ID);
		}

		public ProgressIndicator getProgressIndicator() {
			return fProgressIndicator;
		}

		public Label getProgressText() {
			return fLabel;
		}

		@Override
		public void beginTask(final String name, final int totalWork) {

			updateUI(new Runnable() {

				@Override
				public void run() {
					if (isDisposed())
						return;
					AbsolutePositionProgressMonitorPart.super.beginTask(name,
							totalWork);
				}
			});

		}

		@Override
		public void done() {

			updateUI(new Runnable() {

				@Override
				public void run() {
					if (isDisposed())
						return;
					AbsolutePositionProgressMonitorPart.super.done();
				}
			});

		}

		@Override
		public void internalWorked(final double work) {

			updateUI(new Runnable() {

				@Override
				public void run() {
					if (isDisposed())
						return;
					AbsolutePositionProgressMonitorPart.super
							.internalWorked(work);
				}
			});

		}

		@Override
		public void setFont(final Font font) {

			updateUI(new Runnable() {

				@Override
				public void run() {
					if (isDisposed())
						return;
					AbsolutePositionProgressMonitorPart.super.setFont(font);
				}
			});

		}

		@Override
		protected void updateLabel() {

			updateUI(new Runnable() {

				@Override
				public void run() {
					if (isDisposed())
						return;
					AbsolutePositionProgressMonitorPart.super.updateLabel();
				}
			});

		}
	}

	private Color foreground = null;
	private AbsolutePositionProgressMonitorPart monitor;
	private Rectangle messageRect;
	private Rectangle progressRect;

	@Override
	public IProgressMonitor getBundleProgressMonitor() {
		if (monitor == null) {
			Composite parent = new Composite(getSplash(), Window.getDefaultOrientation());
			Point size = getSplash().getSize();
			parent.setBounds(new Rectangle(0,0,size.x,size.y));
			monitor = new AbsolutePositionProgressMonitorPart(parent);
			monitor.setSize(size);
			if (progressRect != null)
				monitor.getProgressIndicator().setBounds(progressRect);
			else
				monitor.getProgressIndicator().setVisible(false);

			if (messageRect != null)
				monitor.getProgressText().setBounds(messageRect);
			else
				monitor.getProgressText().setVisible(false);

			if (foreground != null)
				monitor.getProgressText().setForeground(foreground);
			monitor.setBackgroundMode(SWT.INHERIT_FORCE);
			monitor.setBackgroundImage(getSplash().getShell()
					.getBackgroundImage());
		}
		return monitor;
	}

	@Override
	public void dispose() {
		if (foreground != null)
			foreground.dispose();
		super.dispose();
	}

	/**
	 * Set the foreground text color. This method has no effect after
	 * {@link #getBundleProgressMonitor()} has been invoked.
	 *
	 * @param foregroundRGB
	 *            the color
	 */
	protected void setForeground(RGB foregroundRGB) {
		if (monitor != null)
			return;
		if (this.foreground != null)
			this.foreground.dispose();
		this.foreground = new Color(getSplash().getShell().getDisplay(),
				foregroundRGB);
	}

	/**
	 * Get the foreground text color. This color should not be disposed by
	 * callers.
	 *
	 * @return the foreground color
	 */
	protected Color getForeground() {
		return foreground;
	}

	/**
	 * Set the location of the message text in the splash. This method has no
	 * effect after {@link #getBundleProgressMonitor()} has been invoked.
	 *
	 * @param messageRect
	 *            the location of the message text
	 */
	protected void setMessageRect(Rectangle messageRect) {
		this.messageRect = messageRect;
	}

	/**
	 * Set the location of the progress bar in the splash. This method has no
	 * effect after {@link #getBundleProgressMonitor()} has been invoked.
	 *
	 * @param progressRect
	 *            the location of the progress bar
	 */
	protected void setProgressRect(Rectangle progressRect) {
		this.progressRect = progressRect;
	}

	/**
	 * Get the composite on which any supplemental controls should be drawn.
	 * This will not have a layout set and clients are responsible for setting
	 * the location of child controls manually.
	 *
	 * <p>
	 * This method must be called in the
	 * {@link #init(org.eclipse.swt.widgets.Shell)} method of a subclasses to
	 * ensure proper creation of controls
	 * </p>
	 *
	 * <p>
	 * Please note that the default implementation of this method assumes that
	 * the {@link IProgressMonitor} returned from
	 * {@link #getBundleProgressMonitor()} can be safely casted to a
	 * {@link Composite}. If this is not the case this method must be
	 * reimplemented to reflect the new progress controls.
	 * </p>
	 *
	 * @see #init(org.eclipse.swt.widgets.Shell)
	 * @return the composite
	 */
	protected Composite getContent() {
		return (Composite) getBundleProgressMonitor();
	}

	/**
	 * Perform some update on the splash. If called from a non-UI thread it will
	 * be wrapped by a runnable that may be run before the workbench has been
	 * fully realized.
	 *
	 * @param r
	 *            the update runnable
	 * @throws Throwable
	 */
	private void updateUI(final Runnable r) {
		Shell splashShell = getSplash();
		if (splashShell == null || splashShell.isDisposed())
			return;

		Display display = splashShell.getDisplay();

		if (Thread.currentThread() == display.getThread())
			r.run(); // run immediatley if we're on the UI thread
		else {
			// wrapper with a StartupRunnable to ensure that it will run before
			// the UI is fully initialized
			StartupRunnable startupRunnable = new StartupRunnable() {

				@Override
				public void runWithException() throws Throwable {
					r.run();
				}
			};
			display.asyncExec(startupRunnable);
		}
	}
}
