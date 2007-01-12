/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.splash;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Basic splash implementation that provides an absolute positioned progress bar
 * and message string that is hooked up to a progress monitor.
 * 
 * @since 3.3
 */
public class BasicSplashHandler extends AbstractSplashHandler {

	/**
	 * Hacks the progress monitor to have absolute positioning for its controls.
	 */
	class AbsolutePositionProgressMonitorPart extends ProgressMonitorPart {
		public AbsolutePositionProgressMonitorPart(Composite parent) {
			super(parent, null);
			setLayout(null);
		}

		public ProgressIndicator getProgressIndicator() {
			return fProgressIndicator;
		}

		public Label getProgressText() {
			return fLabel;
		}
	}

	private Color foreground = null;
	private AbsolutePositionProgressMonitorPart monitor;
	private Rectangle messageRect;
	private Rectangle progressRect;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.splash.AbstractSplashHandler#getBundleProgressMonitor()
	 */
	public IProgressMonitor getBundleProgressMonitor() {
		if (monitor == null) {
			monitor = new AbsolutePositionProgressMonitorPart(getSplash());
			monitor.setSize(getSplash().getShell().getSize());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.splash.AbstractSplashHandler#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (foreground != null)
			foreground.dispose();
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
	 * @return the composite
	 */
	protected Composite getContent() {
		return (Composite) getBundleProgressMonitor();
	}
}
