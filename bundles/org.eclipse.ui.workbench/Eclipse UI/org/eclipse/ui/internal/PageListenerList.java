/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Part listener list.
 */
public class PageListenerList {
	private ListenerList listeners = new ListenerList();
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
	listeners.add(l);
}
/**
 * Notifies the listener that a part has been activated.
 */
public void firePageActivated(final IWorkbenchPage page) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPageListener l = (IPageListener)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.pageActivated(page);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been closed
 */
public void firePageClosed(final IWorkbenchPage page) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPageListener l = (IPageListener)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.pageClosed(page);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been opened.
 */
public void firePageOpened(final IWorkbenchPage page) {
	Object [] listeners = this.listeners.getListeners();
	for (int i = 0; i < listeners.length; i ++) {
		final IPageListener l = (IPageListener)listeners[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.pageOpened(page);
			}
		});
	}
}
/**
 * Removes an IPartListener from the part service.
 */
public void removePageListener(IPageListener l) {
	listeners.remove(l);
}
}
