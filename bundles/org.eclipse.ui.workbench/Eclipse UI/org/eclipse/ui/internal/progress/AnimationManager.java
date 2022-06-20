/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The AnimationManager is the class that keeps track of the animation items to
 * update.
 */
public class AnimationManager {
	private static AnimationManager singleton;

	boolean animated = false;

	private IJobProgressManagerListener listener;

	IAnimationProcessor animationProcessor;

	WorkbenchJob animationUpdateJob;

	/**
	 * Returns the singleton {@link AnimationManager} instance
	 *
	 * @return the singleton {@link AnimationManager} instance
	 */
	public static AnimationManager getInstance() {
		if (singleton == null) {
			singleton = new AnimationManager();
		}
		return singleton;
	}

	/**
	 * Get the background color to be used.
	 *
	 * @param control The source of the display.
	 * @return Color
	 */
	static Color getItemBackgroundColor(Control control) {
		return control.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
	}

	AnimationManager() {

		animationProcessor = new ProgressAnimationProcessor(this);

	// = new WorkbenchJob(ProgressMessages.AnimationManager_AnimationStart) {
//
//			@Override
//			public IStatus runInUIThread(IProgressMonitor monitor) {
//
//				if (animated) {
//					animationProcessor.animationStarted();
//				} else {
//					animationProcessor.animationFinished();
//				}
//				return Status.OK_STATUS;
//			}
//		};
//		animationUpdateJob.setSystem(true);
//
//		listener = getProgressListener();
//		ProgressManager.getInstance().addListener(listener);

	}

	/**
	 * Add an item to the list
	 *
	 * @param item animation item to add
	 */
	void addItem(final AnimationItem item) {
//		animationProcessor.addItem(item);
	}

	/**
	 * Remove an item from the list
	 *
	 * @param item animation item to remove
	 */
	void removeItem(final AnimationItem item) {
		animationProcessor.removeItem(item);
	}

	/**
	 * Return whether or not the current state is animated.
	 *
	 * @return whether or not the current state is animated
	 */
	boolean isAnimated() {
		return false;
	}

	/**
	 * Set whether or not the receiver is animated.
	 *
	 * @param bool receivers new animated state
	 */
	void setAnimated(final boolean bool) {
	}

	/**
	 * Dispose the images in the receiver.
	 */
	void dispose() {
		setAnimated(false);
		ProgressManager.getInstance().removeListener(listener);
	}

	/**
	 * Get the preferred width for widgets displaying the animation.
	 *
	 * @return int. Return 0 if there is no image data.
	 */
	int getPreferredWidth() {
		return animationProcessor.getPreferredWidth();
	}

}
