package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
	for (int nX = 0; nX < array.length; nX ++) {
		final IPageListener l = (IPageListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.pageActivated(page);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePageListener(l);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been closed
 */
public void firePageClosed(final IWorkbenchPage page) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPageListener l = (IPageListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.pageClosed(page);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePageListener(l);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been opened.
 */
public void firePageOpened(final IWorkbenchPage page) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPageListener l = (IPageListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.pageOpened(page);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePageListener(l);
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
