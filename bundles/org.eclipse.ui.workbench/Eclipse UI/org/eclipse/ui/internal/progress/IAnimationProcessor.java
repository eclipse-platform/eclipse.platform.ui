/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

import org.eclipse.core.runtime.jobs.Job;

/**
 * The IAnimationProcessor is the class that handles the animation of the
 * animation item.
 */
interface IAnimationProcessor {

	/**
	 * Add an item to the list of the items we are updating.
	 * 
	 * @param item
	 */
	void addItem(AnimationItem item);

	/**
	 * Remove an item from the list of the items we are updating.
	 * 
	 * @param item
	 */
	void removeItem(AnimationItem item);

	/**
	 * Return whether or not the receiver has any items.
	 *
	 * @return true if there are items
	 */
	boolean hasItems();

	/**
	 * Animation has begun. Inform any listeners. This is called from the UI Thread.
	 */
	void animationStarted();

	/**
	 * Animation has finished. Inform any listeners. This is called from the UI
	 * Thread.
	 */
	void animationFinished();

	/**
	 * Get the preferred width of the types of items this processor manages.
	 *
	 * @return preferred width
	 */
	int getPreferredWidth();

	/**
	 * Return whether or not this is a job used by the processor.
	 *
	 * @param job
	 * @return true if this job is used by the processor
	 */
	boolean isProcessorJob(Job job);

}
