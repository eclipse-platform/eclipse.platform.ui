package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.core.runtime.Platform;
import java.util.*;

/**
 * Perspective selection notifier.
 */
public class SelectionService implements ISelectionService, IPartListener {
	private ListenerList listeners = new ListenerList();
	private IWorkbenchPart activePart;
	private ISelectionProvider activeProvider;
	
	private org.eclipse.jface.viewers.ISelectionChangedListener
		selListener = new org.eclipse.jface.viewers.ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fireSelection(activePart, event.getSelection());
			}			
		};
/**
 * PerspSelectionNotifier constructor comment.
 */
public SelectionService() {
	super();
}
/*
 * Adds an ISelectionListener to the service.
 */
public void addSelectionListener(ISelectionListener l) {
	listeners.add(l);
}
/**
 * Fires a selection event to all listeners.
 */
protected void fireSelection(final IWorkbenchPart part, final ISelection sel) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final ISelectionListener l = (ISelectionListener)array[nX];
		Platform.run(new SafeRunnableAdapter() {
			public void run() {
				l.selectionChanged(part, sel);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removeSelectionListener(l);
			}
		});
	}
}
/**
 * Returns the selection.
 */
public ISelection getSelection() {
	if (activeProvider != null)
		return activeProvider.getSelection();
	else
		return null;
}
/**
 * Notifies the listener that a part has been activated.
 */
public void partActivated(IWorkbenchPart newPart) {
	// Optimize.
	if (newPart == activePart)
		return;
		
	// Unhook selection from the old part.
	if (activeProvider != null) {
		activeProvider.removeSelectionChangedListener(selListener);
		activeProvider = null;
	}
	activePart = null;

	// Update active part.
	activePart = newPart;

	// Hook selection on the new part.
	if (activePart != null) {
		activeProvider = activePart.getSite().getSelectionProvider();
		if (activeProvider != null) {
			activeProvider.addSelectionChangedListener(selListener);
			fireSelection(newPart, activeProvider.getSelection());
		}
	}
}
/**
 * Notifies the listener that a part has been brought to the front.
 */
public void partBroughtToTop(IWorkbenchPart newPart) {
	// do nothing, the active part has not changed,
	// so the selection is unaffected
}
/**
 * Notifies the listener that a part has been closed
 */
public void partClosed(IWorkbenchPart part) {
	// Unhook selection from the part.
	if (part == activePart) {
		if (activeProvider != null) {
			activeProvider.removeSelectionChangedListener(selListener);
			activeProvider = null;
		}
		activePart = null;
	}
}
/**
 * Notifies the listener that a part has been deactivated.
 */
public void partDeactivated(org.eclipse.ui.IWorkbenchPart part) {}
/**
 * Notifies the listener that a part has been opened.
 */
public void partOpened(IWorkbenchPart part) {
	// Wait for activation.
}
/*
 * Removes an ISelectionListener from the service.
 */
public void removeSelectionListener(ISelectionListener l) {
	listeners.remove(l);
}
}
