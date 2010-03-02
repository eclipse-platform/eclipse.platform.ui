/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.jobs.Job;

/**
 * The IAnimationProcessor is the class that handles the animation of
 * the animation item.
 */
interface IAnimationProcessor {

    /**
     * Add an item to the list of the items we are updating.
     * @param item
     */
    void addItem(AnimationItem item);

    /**
     * Remove an item from the list of the items we are updating.
     * @param item
     */
    void removeItem(AnimationItem item);

    /**
     * Return whether or not the receiver has any items.
     * @return
     */
    boolean hasItems();

    /**
     * Animation has begun. Inform any listeners. This is called
     * from the UI Thread.
     */
    void animationStarted();

    /**
     * Animation has finished. Inform any listeners. This is called
     * from the UI Thread.
     */
    void animationFinished();

    /**
     * Get the preferred width of the types of items this 
     * processor manages.
     * @return
     */
    int getPreferredWidth();

    /**
     * Return whether or not this is a job used by the processor.
     * @param job
     * @return
     */
    boolean isProcessorJob(Job job);

}
