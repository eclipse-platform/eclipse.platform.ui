package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Part listener list.
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
	for (int nX = 0; nX < array.length; nX ++) {
		final IPartListener l = (IPartListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partActivated(part);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePartListener(l);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been brought to top.
 */
public void firePartBroughtToTop(final IWorkbenchPart part) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPartListener l = (IPartListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partBroughtToTop(part);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePartListener(l);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been closed
 */
public void firePartClosed(final IWorkbenchPart part) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPartListener l = (IPartListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partClosed(part);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePartListener(l);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been deactivated.
 */
public void firePartDeactivated(final IWorkbenchPart part) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPartListener l = (IPartListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partDeactivated(part);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePartListener(l);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been opened.
 */
public void firePartOpened(final IWorkbenchPart part) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPartListener l = (IPartListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partOpened(part);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePartListener(l);
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
