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
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
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
    	SafeRunner.run(runnable);
    	if (UIStats.isDebugging(UIStats.NOTIFY_PART_LISTENERS)) {
			UIStats.end(UIStats.NOTIFY_PART_LISTENERS, listener, label);
		}
	}

    /**
     * Notifies the listener that a part has been activated.
     */
    public void firePartActivated(final IWorkbenchPartReference ref) {
		for (Object listener : getListeners()) {
			final IPartListener2 partListener = (IPartListener2) listener;
            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					partListener.partActivated(ref);
                }
			}, partListener, ref, "activated::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been brought to top.
     */
    public void firePartBroughtToTop(final IWorkbenchPartReference ref) {
		for (Object listener : getListeners()) {
			final IPartListener2 partListener = (IPartListener2) listener;
            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					partListener.partBroughtToTop(ref);
                }
			}, partListener, ref, "broughtToTop::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been closed
     */
    public void firePartClosed(final IWorkbenchPartReference ref) {
		for (Object listener : getListeners()) {
			final IPartListener2 partListener = (IPartListener2) listener;
            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					partListener.partClosed(ref);
                }
			}, partListener, ref, "closed::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been deactivated.
     */
    public void firePartDeactivated(final IWorkbenchPartReference ref) {
		for (Object listener : getListeners()) {
			final IPartListener2 partListener = (IPartListener2) listener;
            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					partListener.partDeactivated(ref);
                }
			}, partListener, ref, "deactivated::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been opened.
     */
    public void firePartOpened(final IWorkbenchPartReference ref) {
		for (Object listener : getListeners()) {
			final IPartListener2 partListener = (IPartListener2) listener;
            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					partListener.partOpened(ref);
                }
			}, partListener, ref, "opened::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been opened.
     */
    public void firePartHidden(final IWorkbenchPartReference ref) {
		for (Object element : getListeners()) {
			final IPartListener2 partListener;
            if (element instanceof IPartListener2) {
				partListener = (IPartListener2) element;
			} else {
				continue;
			}

            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					partListener.partHidden(ref);
                }
			}, partListener, ref, "hidden::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been opened.
     */
    public void firePartVisible(final IWorkbenchPartReference ref) {
		for (Object listener : getListeners()) {
			final IPartListener2 partListener;
			if (listener instanceof IPartListener2) {
				partListener = (IPartListener2) listener;
			} else {
				continue;
			}

            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					partListener.partVisible(ref);
                }
			}, partListener, ref, "visible::"); //$NON-NLS-1$
        }
    }

    /**
     * Notifies the listener that a part has been opened.
     */
    public void firePartInputChanged(final IWorkbenchPartReference ref) {
		for (Object listener : getListeners()) {
			final IPartListener2 partListener;
			if (listener instanceof IPartListener2) {
				partListener = (IPartListener2) listener;
			} else {
				continue;
			}

            fireEvent(new SafeRunnable() {
                @Override
				public void run() {
					partListener.partInputChanged(ref);
                }
			}, partListener, ref, "inputChanged::"); //$NON-NLS-1$
        }
    }

    /**
     * Removes an IPartListener from the part service.
     */
    public void removePartListener(IPartListener2 l) {
        removeListenerObject(l);
    }

	public void firePageChanged(final PageChangedEvent event) {
		for (Object listener : getListeners()) {
			final IPageChangedListener partListener;
			if (listener instanceof IPageChangedListener) {
				partListener = (IPageChangedListener) listener;
			} else {
				continue;
			}

            SafeRunnable.run(new SafeRunnable() {
                @Override
				public void run() {
					partListener.pageChanged(event);
                }
            });
        }
	}
}
