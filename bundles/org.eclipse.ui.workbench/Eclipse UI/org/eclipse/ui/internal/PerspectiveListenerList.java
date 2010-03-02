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
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IPerspectiveListener3;
import org.eclipse.ui.IPerspectiveListener4;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.misc.UIStats;

/**
 * Perspective listener list.
 */
public class PerspectiveListenerList extends EventManager {

    /**
     * PerspectiveListenerList constructor comment.
     */
    public PerspectiveListenerList() {
        super();
    }

    /**
     * Adds an IPerspectiveListener to the perspective service.
     */
    public void addPerspectiveListener(IPerspectiveListener l) {
        addListenerObject(l);
    }

    /**
     * Calls a perspective listener with associated performance event instrumentation
     * 
     * @param runnable
     * @param listener
     * @param perspective
     * @param description
     */
    private void fireEvent(SafeRunnable runnable, IPerspectiveListener listener, IPerspectiveDescriptor perspective, String description) {
    	String label = null;//for debugging
    	if (UIStats.isDebugging(UIStats.NOTIFY_PERSPECTIVE_LISTENERS)) {
    		label = description + perspective.getId();
    		UIStats.start(UIStats.NOTIFY_PERSPECTIVE_LISTENERS, label);
    	}
    	Platform.run(runnable);
    	if (UIStats.isDebugging(UIStats.NOTIFY_PERSPECTIVE_LISTENERS)) {
			UIStats.end(UIStats.NOTIFY_PERSPECTIVE_LISTENERS, listener, label);
		}
	}

    /**
     * Notifies the listener that a perspective has been activated.
     */
    public void firePerspectiveActivated(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective) {
        Object[] array = getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            final IPerspectiveListener l = (IPerspectiveListener) array[nX];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.perspectiveActivated(page, perspective);
                }
            }, l, perspective, "activated::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a perspective has been deactivated.
     * 
     * @since 3.2
     */
    public void firePerspectivePreDeactivate(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective) {
        Object[] array = getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener4) {
                final IPerspectiveListener4 l4 = (IPerspectiveListener4) array[nX];
                fireEvent(new SafeRunnable() {
                    public void run() {
                        l4.perspectivePreDeactivate(page, perspective);
                    }
                }, l4, perspective, "pre-deactivate::"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Notifies the listener that a perspective has been deactivated.
     * 
     * @since 3.1
     */
    public void firePerspectiveDeactivated(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective) {
        Object[] array = getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener3) {
                final IPerspectiveListener3 l3 = (IPerspectiveListener3) array[nX];
                fireEvent(new SafeRunnable() {
                    public void run() {
                        l3.perspectiveDeactivated(page, perspective);
                    }
                }, l3, perspective, "deactivated::"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Notifies the listener that a perspective has been changed.
     */
    public void firePerspectiveChanged(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective, final String changeId) {
        Object[] array = getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            final IPerspectiveListener l = (IPerspectiveListener) array[nX];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.perspectiveChanged(page, perspective, changeId);
                }
            }, l, perspective, "changed::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been affected
     * in the given perspective.
     * 
     * @since 3.0
     */
    public void firePerspectiveChanged(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective,
            final IWorkbenchPartReference partRef, final String changeId) {
        Object[] array = getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener2) {
                final IPerspectiveListener2 l2 = (IPerspectiveListener2) array[nX];
                fireEvent(new SafeRunnable() {
                    public void run() {
                        l2.perspectiveChanged(page, perspective, partRef,
                                changeId);
                    }
                }, l2, perspective, "changed::"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Notifies the listener that a perspective has been closed.
     * 
     * @since 3.1
     */
    public void firePerspectiveClosed(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective) {
        Object[] array = getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener3) {
                final IPerspectiveListener3 l3 = (IPerspectiveListener3) array[nX];
                fireEvent(new SafeRunnable() {
                    public void run() {
                        l3.perspectiveClosed(page, perspective);
                    }
                }, l3, perspective, "closed::"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Notifies the listener that a perspective has been opened.
     * 
     * @since 3.1
     */
    public void firePerspectiveOpened(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective) {
        Object[] array = getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener3) {
                final IPerspectiveListener3 l3 = (IPerspectiveListener3) array[nX];
                fireEvent(new SafeRunnable() {
                    public void run() {
                        l3.perspectiveOpened(page, perspective);
                    }
                }, l3, perspective, "opened::"); //$NON-NLS-1$
            }
        }
    }
    
    /**
     * Notifies the listener that a perspective has been deactivated.
     * 
     * @since 3.1
     */
    public void firePerspectiveSavedAs(final IWorkbenchPage page,
            final IPerspectiveDescriptor oldPerspective,
            final IPerspectiveDescriptor newPerspective) {
        Object[] array = getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener3) {
                final IPerspectiveListener3 l3 = (IPerspectiveListener3) array[nX];
                fireEvent(new SafeRunnable() {
                    public void run() {
                        l3.perspectiveSavedAs(page, oldPerspective, newPerspective);
                    }
                }, l3, newPerspective, "saveAs::"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Removes an IPerspectiveListener from the perspective service.
     */
    public void removePerspectiveListener(IPerspectiveListener l) {
        removeListenerObject(l);
    }
}
