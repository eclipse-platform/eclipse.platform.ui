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
import org.eclipse.ui.*;

/**
 * Perspective listener list.
 */
public class PerspectiveListenerListOld {
	private ListenerList listeners = new ListenerList();
/**
 * PerspectiveListenerList constructor comment.
 */
public PerspectiveListenerListOld() {
	super();
}
/**
 * Adds an IPerspectiveListener to the perspective service.
 */
public void addPerspectiveListener(IPerspectiveListener l) {
	listeners.add(l);
}
/**
 * Notifies the listener that a perspective has been activated.
 */
public void firePerspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPerspectiveListener l = (IPerspectiveListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.perspectiveActivated(page, perspective);
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
		final IPerspectiveListener l = (IPerspectiveListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.perspectiveChanged(page, perspective, changeId);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been affected
 * in the given perspective.
 */
public void firePerspectiveChanged(final IWorkbenchPage page, final IPerspectiveDescriptor perspective, final IWorkbenchPartReference partRef, final String changeId) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		if (array[nX] instanceof IPerspectiveListener2) {
			final IPerspectiveListener2 l2 = (IPerspectiveListener2) array[nX];
			Platform.run(new SafeRunnable() {
				public void run() {
					l2.perspectiveChanged(page, perspective, partRef, changeId);
				}
			});
		}
	}
}
/**
 * Removes an IPerspectiveListener from the perspective service.
 */
public void removePerspectiveListener(IPerspectiveListener l) {
	listeners.remove(l);
}
}
