package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * Part listener list.
 */
public class PartListenerList2 {
	private ListenerList listeners = new ListenerList();
/**
 * PartNotifier constructor comment.
 */
public PartListenerList2() {
	super();
}
/**
 * Adds an PartListener to the part service.
 */
public void addPartListener(IPartListener2 l) {
	listeners.add(l);
}
/**
 * Notifies the listener that a part has been activated.
 */
public void firePartActivated(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l = (IPartListener2)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partActivated(ref);
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
public void firePartBroughtToTop(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l = (IPartListener2)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partBroughtToTop(ref);
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
public void firePartClosed(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l = (IPartListener2)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partClosed(ref);
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
public void firePartDeactivated(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l = (IPartListener2)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partDeactivated(ref);
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
public void firePartOpened(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l = (IPartListener2)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partOpened(ref);
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
public void firePartHidden(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l;
		if(array[i] instanceof IPartListener2)
			l = (IPartListener2)array[i];
		else
			continue;
			
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partHidden(ref);
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
public void firePartVisible(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l;
		if(array[i] instanceof IPartListener2)
			l = (IPartListener2)array[i];
		else
			continue;
			
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partVisible(ref);
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
public void firePartInputChanged(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l;
		if(array[i] instanceof IPartListener2)
			l = (IPartListener2)array[i];
		else
			continue;
			
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partInputChanged(ref);
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
public void removePartListener(IPartListener2 l) {
	listeners.remove(l);
}
}
