/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.misc.UIStats;

/**
 * Part listener list.
 */
/*
 * This class should be deleted when IPartListener and IPartListener2 
 * renamed to IPartListener.
 */
public class PartListenerList extends EventManager {

    /**
     * PartNotifier constructor comment.
     */
    public PartListenerList() {
        super();
    }

    /**
     * Adds an IPartListener to the part service.
     */
    public void addPartListener(IPartListener l) {
        addListenerObject(l);
    }

    /**
     * Calls a part listener with associated performance event instrumentation
     * 
     * @param runnable
     * @param listener
     * @param part
     * @param description
     */
    private void fireEvent(SafeRunnable runnable, IPartListener listener, IWorkbenchPart part, String description) {
    	String label = null;//for debugging
    	if (UIStats.isDebugging(UIStats.NOTIFY_PART_LISTENERS)) {
    		label = description + part.getTitle();
    		UIStats.start(UIStats.NOTIFY_PART_LISTENERS, label);
    	}
    	Platform.run(runnable);
    	if (UIStats.isDebugging(UIStats.NOTIFY_PART_LISTENERS)) {
			UIStats.end(UIStats.NOTIFY_PART_LISTENERS, listener, label);
		}
	}

    /**
     * Notifies the listener that a part has been activated.
     */
    public void firePartActivated(final IWorkbenchPart part) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener l = (IPartListener) array[i];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partActivated(part);
                }
            }, l, part, "activated::"); //$NON-NLS-1$
        }
    }

	/**
     * Notifies the listener that a part has been brought to top.
     */
    public void firePartBroughtToTop(final IWorkbenchPart part) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener l = (IPartListener) array[i];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partBroughtToTop(part);
                }
            }, l, part, "broughtToTop::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been closed
     */
    public void firePartClosed(final IWorkbenchPart part) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener l = (IPartListener) array[i];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partClosed(part);
                }
            }, l, part, "closed::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been deactivated.
     */
    public void firePartDeactivated(final IWorkbenchPart part) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener l = (IPartListener) array[i];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partDeactivated(part);
                }
            }, l, part, "deactivated::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been opened.
     */
    public void firePartOpened(final IWorkbenchPart part) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener l = (IPartListener) array[i];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partOpened(part);
                }
            }, l, part, "opened::"); //$NON-NLS-1$
        }
    }

    /**
     * Removes an IPartListener from the part service.
     */
    public void removePartListener(IPartListener l) {
        removeListenerObject(l);
    }
}
