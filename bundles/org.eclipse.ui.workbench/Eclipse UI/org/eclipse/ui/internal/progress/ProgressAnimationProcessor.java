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

import java.util.ArrayList;
import java.util.Collections;
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

    List items = Collections.synchronizedList(new ArrayList());

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IAnimationProcessor#startAnimation(org.eclipse.core.runtime.IProgressMonitor)
     */
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

        ProgressAnimationItem[] animationItems = getAnimationItems();
        for (int i = 0; i < animationItems.length; i++) {
            animationItems[i].animationDone();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IAnimationProcessor#addItem(org.eclipse.ui.internal.progress.AnimationItem)
     */
    public void addItem(AnimationItem item) {
        Assert.isTrue(item instanceof ProgressAnimationItem);
        items.add(item);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IAnimationProcessor#removeItem(org.eclipse.ui.internal.progress.AnimationItem)
     */
    public void removeItem(AnimationItem item) {
        Assert.isTrue(item instanceof ProgressAnimationItem);
        items.remove(item);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IAnimationProcessor#hasItems()
     */
    public boolean hasItems() {
        return items.size() > 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IAnimationProcessor#itemsInactiveRedraw()
     */
    public void itemsInactiveRedraw() {
        //Nothing to do here as SWT handles redraw

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IAnimationProcessor#animationStarted(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void animationStarted() {
        AnimationItem[] animationItems = getAnimationItems();
        for (int i = 0; i < animationItems.length; i++) {
            animationItems[i].animationStart();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IAnimationProcessor#getPreferredWidth()
     */
    public int getPreferredWidth() {
        return 30;
    }

    /**
     * Get the animation items currently registered for the receiver.
     * 
     * @return ProgressAnimationItem[]
     */
    private ProgressAnimationItem[] getAnimationItems() {
        ProgressAnimationItem[] animationItems = new ProgressAnimationItem[items
                .size()];
        items.toArray(animationItems);
        return animationItems;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.IAnimationProcessor#animationFinished()
     */
    public void animationFinished() {
        AnimationItem[] animationItems = getAnimationItems();
        for (int i = 0; i < animationItems.length; i++) {
            animationItems[i].animationDone();
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.IAnimationProcessor#isProcessorJob(org.eclipse.core.runtime.jobs.Job)
     */
    public boolean isProcessorJob(Job job) {
        // We have no jobs
        return false;
    }

}
