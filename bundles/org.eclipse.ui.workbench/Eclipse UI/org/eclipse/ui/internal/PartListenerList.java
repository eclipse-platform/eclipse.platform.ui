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
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Part listener list.
 */
/*
 * This class should be deleted when IPartListener and IPartListener2 
 * renamed to IPartListener.
 */
public class PartListenerList {
	private ListenerList listeners = new ListenerList();
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
	listeners.add(l);
}
/**
 * Notifies the listener that a part has been activated.
 */
public void firePartActivated(final IWorkbenchPart part) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener l = (IPartListener)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partActivated(part);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been brought to top.
 */
public void firePartBroughtToTop(final IWorkbenchPart part) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener l = (IPartListener)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partBroughtToTop(part);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been closed
 */
public void firePartClosed(final IWorkbenchPart part) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener l = (IPartListener)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partClosed(part);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been deactivated.
 */
public void firePartDeactivated(final IWorkbenchPart part) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener l = (IPartListener)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partDeactivated(part);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been opened.
 */
public void firePartOpened(final IWorkbenchPart part) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener l = (IPartListener)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partOpened(part);
			}
		});
	}
}
/**
 * Removes an IPartListener from the part service.
 */
public void removePartListener(IPartListener l) {
	listeners.remove(l);
}
}
