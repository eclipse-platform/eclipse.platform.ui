package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

/**
 * Perspective selection notifier.
 */
public class SelectionService implements ISelectionService, IPartListener {
	private ListenerList listeners = new ListenerList();
	private IWorkbenchPart activePart;
	private ISelectionProvider activeProvider;
	
	/**
	 * The JFace selection listener to hook on the active part's selection provider.
	 */
	private ISelectionChangedListener
		selListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fireSelection(activePart, event.getSelection());
			}			
		};

/**
 * Creates a new SelectionService.
 */
public SelectionService() {
}

/* (non-Javadoc)
 * Method declared on ISelectionService.
 */
public void addSelectionListener(ISelectionListener l) {
	listeners.add(l);
}

/* (non-Javadoc)
 * Method declared on ISelectionService.
 */
public void removeSelectionListener(ISelectionListener l) {
	listeners.remove(l);
}

/**
 * Fires a selection event to all listeners.
 * 
 * @param sel the selection or <code>null</code> if no active selection
 */
protected void fireSelection(final IWorkbenchPart part, final ISelection sel) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final ISelectionListener l = (ISelectionListener)array[nX];
		if ((part != null && sel != null) || l instanceof INullSelectionListener) {
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
	reset();

	// Update active part.
	activePart = newPart;

	// Hook selection on the new part.
	if (activePart != null) {
		activeProvider = activePart.getSite().getSelectionProvider();
		if (activeProvider != null) {
			// Fire an event if there's an active provider
			activeProvider.addSelectionChangedListener(selListener);
			fireSelection(newPart, activeProvider.getSelection());
		}
	}
	// No need to fire an event if no active provider, since this was done in reset()
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
		reset();
	}
}
/**
 * Notifies the listener that a part has been deactivated.
 */
public void partDeactivated(IWorkbenchPart part) {
	// Unhook selection from the part.
	if (part == activePart) {
		reset();
	}
}
/**
 * Notifies the listener that a part has been opened.
 */
public void partOpened(IWorkbenchPart part) {
	// Wait for activation.
}
/**
 * Resets the service.  The active part and selection provider are
 * dereferenced.
 */
public void reset() {
	if (activePart != null) {
		fireSelection(null, null);
		if (activeProvider != null) {
			activeProvider.removeSelectionChangedListener(selListener);
			activeProvider = null;
		}
		activePart = null;
	}
}
}
