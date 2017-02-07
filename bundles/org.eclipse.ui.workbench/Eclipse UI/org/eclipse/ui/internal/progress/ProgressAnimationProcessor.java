/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

/**
 * The ProgressAnimationProcessor is the processor for the animation using the
 * system progress.
 */
class ProgressAnimationProcessor implements IAnimationProcessor {

    AnimationManager manager;

    /**
     * Create a new instance of the receiver and listen to the animation
     * manager.
     *
     * @param animationManager
     */
    ProgressAnimationProcessor(AnimationManager animationManager) {
        manager = animationManager;
    }

	List items = new ArrayList();

    public void startAnimationLoop(IProgressMonitor monitor) {

        // Create an off-screen image to draw on, and a GC to draw with.
        // Both are disposed after the animation.
        if (items.size() == 0) {
			return;
		}
        if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}

        while (manager.isAnimated() && !monitor.isCanceled()) {
            //Do nothing while animation is happening
        }

		for (ProgressAnimationItem animationItem : getAnimationItems()) {
            animationItem.animationDone();
        }

    }

    @Override
	public void addItem(AnimationItem item) {
        Assert.isTrue(item instanceof ProgressAnimationItem);
        items.add(item);
    }

    @Override
	public void removeItem(AnimationItem item) {
        Assert.isTrue(item instanceof ProgressAnimationItem);
        items.remove(item);
    }

    @Override
	public boolean hasItems() {
        return items.size() > 0;
    }

    public void itemsInactiveRedraw() {
        //Nothing to do here as SWT handles redraw

    }

    @Override
	public void animationStarted() {
		for (AnimationItem animationItem : getAnimationItems()) {
            animationItem.animationStart();
        }

    }

    @Override
	public int getPreferredWidth() {
        return 30;
    }

    /**
     * Get the animation items currently registered for the receiver.
     *
     * @return ProgressAnimationItem[]
     */
    private ProgressAnimationItem[] getAnimationItems() {
		ProgressAnimationItem[] animationItems = new ProgressAnimationItem[items.size()];
        items.toArray(animationItems);
        return animationItems;
    }

    @Override
	public void animationFinished() {
		for (AnimationItem animationItem : getAnimationItems()) {
            animationItem.animationDone();
        }

    }

    @Override
	public boolean isProcessorJob(Job job) {
        // We have no jobs
        return false;
    }

}
