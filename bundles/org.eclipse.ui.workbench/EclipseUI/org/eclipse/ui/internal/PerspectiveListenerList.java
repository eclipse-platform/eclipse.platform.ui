/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.SafeRunner;
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
	 * Calls a perspective listener with associated performance event
	 * instrumentation
	 */
	private void fireEvent(SafeRunnable runnable, IPerspectiveListener listener, IPerspectiveDescriptor perspective,
			String description) {
		String label = null;// for debugging
		if (UIStats.isDebugging(UIStats.NOTIFY_PERSPECTIVE_LISTENERS)) {
			label = description + perspective.getId();
			UIStats.start(UIStats.NOTIFY_PERSPECTIVE_LISTENERS, label);
		}
		SafeRunner.run(runnable);
		if (UIStats.isDebugging(UIStats.NOTIFY_PERSPECTIVE_LISTENERS)) {
			UIStats.end(UIStats.NOTIFY_PERSPECTIVE_LISTENERS, listener, label);
		}
	}

	/**
	 * Notifies the listener that a perspective has been activated.
	 */
	public void firePerspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
		for (Object listener : getListeners()) {
			final IPerspectiveListener perspectiveListener = (IPerspectiveListener) listener;
			fireEvent(new SafeRunnable() {
				@Override
				public void run() {
					perspectiveListener.perspectiveActivated(page, perspective);
				}
			}, perspectiveListener, perspective, "activated::"); //$NON-NLS-1$
		}
	}

	/**
	 * Notifies the listener that a perspective has been deactivated.
	 *
	 * @since 3.2
	 */
	public void firePerspectivePreDeactivate(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
		for (Object listener : getListeners()) {
			if (listener instanceof IPerspectiveListener4) {
				final IPerspectiveListener4 perspectiveListener = (IPerspectiveListener4) listener;
				fireEvent(new SafeRunnable() {
					@Override
					public void run() {
						perspectiveListener.perspectivePreDeactivate(page, perspective);
					}
				}, perspectiveListener, perspective, "pre-deactivate::"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Notifies the listener that a perspective has been deactivated.
	 *
	 * @since 3.1
	 */
	public void firePerspectiveDeactivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
		for (Object listener : getListeners()) {
			if (listener instanceof IPerspectiveListener3) {
				final IPerspectiveListener3 perspectiveListener = (IPerspectiveListener3) listener;
				fireEvent(new SafeRunnable() {
					@Override
					public void run() {
						perspectiveListener.perspectiveDeactivated(page, perspective);
					}
				}, perspectiveListener, perspective, "deactivated::"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Notifies the listener that a perspective has been changed.
	 */
	public void firePerspectiveChanged(final IWorkbenchPage page, final IPerspectiveDescriptor perspective,
			final String changeId) {
		for (Object listener : getListeners()) {
			final IPerspectiveListener perspectiveListener = (IPerspectiveListener) listener;
			fireEvent(new SafeRunnable() {
				@Override
				public void run() {
					perspectiveListener.perspectiveChanged(page, perspective, changeId);
				}
			}, perspectiveListener, perspective, "changed::"); //$NON-NLS-1$
		}
	}

	/**
	 * Notifies the listener that a part has been affected in the given perspective.
	 *
	 * @since 3.0
	 */
	public void firePerspectiveChanged(final IWorkbenchPage page, final IPerspectiveDescriptor perspective,
			final IWorkbenchPartReference partRef, final String changeId) {
		for (Object listener : getListeners()) {
			if (listener instanceof IPerspectiveListener2) {
				final IPerspectiveListener2 perspectiveListener = (IPerspectiveListener2) listener;
				fireEvent(new SafeRunnable() {
					@Override
					public void run() {
						perspectiveListener.perspectiveChanged(page, perspective, partRef, changeId);
					}
				}, perspectiveListener, perspective, "changed::"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Notifies the listener that a perspective has been closed.
	 *
	 * @since 3.1
	 */
	public void firePerspectiveClosed(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
		for (Object listener : getListeners()) {
			if (listener instanceof IPerspectiveListener3) {
				final IPerspectiveListener3 perspectiveListener = (IPerspectiveListener3) listener;
				fireEvent(new SafeRunnable() {
					@Override
					public void run() {
						perspectiveListener.perspectiveClosed(page, perspective);
					}
				}, perspectiveListener, perspective, "closed::"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Notifies the listener that a perspective has been opened.
	 *
	 * @since 3.1
	 */
	public void firePerspectiveOpened(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
		for (Object listener : getListeners()) {
			if (listener instanceof IPerspectiveListener3) {
				final IPerspectiveListener3 perspectiveListener = (IPerspectiveListener3) listener;
				fireEvent(new SafeRunnable() {
					@Override
					public void run() {
						perspectiveListener.perspectiveOpened(page, perspective);
					}
				}, perspectiveListener, perspective, "opened::"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Notifies the listener that a perspective has been deactivated.
	 *
	 * @since 3.1
	 */
	public void firePerspectiveSavedAs(final IWorkbenchPage page, final IPerspectiveDescriptor oldPerspective,
			final IPerspectiveDescriptor newPerspective) {
		for (Object listener : getListeners()) {
			if (listener instanceof IPerspectiveListener3) {
				final IPerspectiveListener3 perspectiveListener = (IPerspectiveListener3) listener;
				fireEvent(new SafeRunnable() {
					@Override
					public void run() {
						perspectiveListener.perspectiveSavedAs(page, oldPerspective, newPerspective);
					}
				}, perspectiveListener, newPerspective, "saveAs::"); //$NON-NLS-1$
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
