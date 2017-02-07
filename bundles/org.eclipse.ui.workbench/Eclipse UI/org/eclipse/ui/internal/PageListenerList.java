/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.misc.UIStats;

/**
 * Part listener list.
 */
public class PageListenerList extends EventManager {

    /**
     * PartNotifier constructor comment.
     */
    public PageListenerList() {
        super();
    }

    /**
     * Adds an IPartListener to the part service.
     */
    public void addPageListener(IPageListener l) {
    	addListenerObject(l);
    }

    /**
     * Calls a page listener with associated performance event instrumentation
     *
     * @param runnable
     * @param listener
     * @param page
     * @param description
     */
    private void fireEvent(SafeRunnable runnable, IPageListener listener, IWorkbenchPage page, String description) {
    	String label = null;//for debugging
    	if (UIStats.isDebugging(UIStats.NOTIFY_PAGE_LISTENERS)) {
    		label = description + page.getLabel();
    		UIStats.start(UIStats.NOTIFY_PAGE_LISTENERS, label);
    	}
    	SafeRunner.run(runnable);
    	if (UIStats.isDebugging(UIStats.NOTIFY_PAGE_LISTENERS)) {
			UIStats.end(UIStats.NOTIFY_PAGE_LISTENERS, listener, label);
		}
	}

    /**
     * Notifies the listener that a part has been activated.
     */
    public void firePageActivated(final IWorkbenchPage page) {
		for (Object listener : getListeners()) {
			final IPageListener pageListener = (IPageListener) listener;
            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					pageListener.pageActivated(page);
                }
			}, pageListener, page, "activated::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been closed
     */
    public void firePageClosed(final IWorkbenchPage page) {
		for (Object listener : getListeners()) {
			final IPageListener pageListener = (IPageListener) listener;
            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					pageListener.pageClosed(page);
                }
			}, pageListener, page, "closed::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been opened.
     */
    public void firePageOpened(final IWorkbenchPage page) {
		for (Object listener : getListeners()) {
			final IPageListener pageListener = (IPageListener) listener;
            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					pageListener.pageOpened(page);
                }
			}, pageListener, page, "opened::"); //$NON-NLS-1$
        }
    }

    /**
     * Removes an IPartListener from the part service.
     */
    public void removePageListener(IPageListener l) {
        removeListenerObject(l);
    }
}
