/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Perspective listener list.
 */
public class PerspectiveListenerList {
	private ListenerList listeners = new ListenerList();
/**
 * PerspectiveListenerList constructor comment.
 */
public PerspectiveListenerList() {
	super();
}
/**
 * Adds an IInternalPerspectiveListener to the perspective service.
 */
public void addPerspectiveListener(IInternalPerspectiveListener l) {
	listeners.add(l);
}
/**
 * Notifies the listener that a perspective has been activated.
 */
public void firePerspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IInternalPerspectiveListener l = (IInternalPerspectiveListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.perspectiveActivated(page, perspective);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePerspectiveListener(l);
			}
		});
	}
}
/**
 * Notifies the listener that a perspective has been changed.
 */
public void firePerspectiveChanged(final IWorkbenchPage page, final IPerspectiveDescriptor perspective, final String changeId) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IInternalPerspectiveListener l = (IInternalPerspectiveListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.perspectiveChanged(page, perspective, changeId);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePerspectiveListener(l);
			}
		});
	}
}
/**
 * Notifies the listener that a perspective has been closed.
 */
public void firePerspectiveClosed(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IInternalPerspectiveListener l = (IInternalPerspectiveListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.perspectiveClosed(page, perspective);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePerspectiveListener(l);
			}
		});
	}
}
/**
 * Notifies the listener that a perspective has been opened.
 */
public void firePerspectiveOpened(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IInternalPerspectiveListener l = (IInternalPerspectiveListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.perspectiveOpened(page, perspective);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePerspectiveListener(l);
			}
		});
	}
}
/**
 * Removes an IInternalPerspectiveListener from the perspective service.
 */
public void removePerspectiveListener(IInternalPerspectiveListener l) {
	listeners.remove(l);
}
}
