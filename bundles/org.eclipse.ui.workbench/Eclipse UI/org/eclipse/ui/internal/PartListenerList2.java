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
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.misc.UIStats;

/**
 * Part listener list.
 */
public class PartListenerList2 extends EventManager {

    /**
     * PartListenerList2 constructor comment.
     */
    public PartListenerList2() {
        super();
    }

    /**
     * Adds an PartListener to the part service.
     */
    public void addPartListener(IPartListener2 l) {
        addListenerObject(l);
    }

    /**
     * Calls a part listener with associated performance event instrumentation
     * 
     * @param runnable
     * @param listener
     * @param ref
     * @param string
     */
    private void fireEvent(SafeRunnable runnable, IPartListener2 listener, IWorkbenchPartReference ref, String string) {
    	String label = null;//for debugging
    	if (UIStats.isDebugging(UIStats.NOTIFY_PART_LISTENERS)) {
    		label = string + ref.getTitle();
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
    public void firePartActivated(final IWorkbenchPartReference ref) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener2 l = (IPartListener2) array[i];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partActivated(ref);
                }
            }, l, ref, "activated::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been brought to top.
     */
    public void firePartBroughtToTop(final IWorkbenchPartReference ref) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener2 l = (IPartListener2) array[i];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partBroughtToTop(ref);
                }
            }, l, ref, "broughtToTop::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been closed
     */
    public void firePartClosed(final IWorkbenchPartReference ref) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener2 l = (IPartListener2) array[i];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partClosed(ref);
                }
            }, l, ref, "closed::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been deactivated.
     */
    public void firePartDeactivated(final IWorkbenchPartReference ref) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener2 l = (IPartListener2) array[i];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partDeactivated(ref);
                }
            }, l, ref, "deactivated::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been opened.
     */
    public void firePartOpened(final IWorkbenchPartReference ref) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener2 l = (IPartListener2) array[i];
            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partOpened(ref);
                }
            }, l, ref, "opened::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been opened.
     */
    public void firePartHidden(final IWorkbenchPartReference ref) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener2 l;
            if (array[i] instanceof IPartListener2) {
				l = (IPartListener2) array[i];
			} else {
				continue;
			}

            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partHidden(ref);
                }
            }, l, ref, "hidden::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been opened.
     */
    public void firePartVisible(final IWorkbenchPartReference ref) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener2 l;
            if (array[i] instanceof IPartListener2) {
				l = (IPartListener2) array[i];
			} else {
				continue;
			}

            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partVisible(ref);
                }
            }, l, ref, "visible::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been opened.
     */
    public void firePartInputChanged(final IWorkbenchPartReference ref) {
        Object[] array = getListeners();
        for (int i = 0; i < array.length; i++) {
            final IPartListener2 l;
            if (array[i] instanceof IPartListener2) {
				l = (IPartListener2) array[i];
			} else {
				continue;
			}

            fireEvent(new SafeRunnable() {
                public void run() {
                    l.partInputChanged(ref);
                }
            }, l, ref, "inputChanged::"); //$NON-NLS-1$
        }
    }
    
    /**
     * Removes an IPartListener from the part service.
     */
    public void removePartListener(IPartListener2 l) {
        removeListenerObject(l);
    }
}
