package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Hashtable;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.misc.Assert;

/**
 * Abstract selection service.
 */
public abstract class AbstractSelectionService implements ISelectionService, IPartListener {
	
	/** 
	 * The list of selection listeners (not per-part).
	 */
	private ListenerList listeners = new ListenerList();
	
	/**
	 * The currently active part.
	 */
	private IWorkbenchPart activePart;
	
	/**
	 * The active part's selection provider, remembered in case the part 
	 * replaces its selection provider after we hooked a listener.
	 */
	private ISelectionProvider activeProvider;
	
	/**
	 * Map from part id (String) to per-part tracker (AbstractPartSelectionTracker).
	 */
	private Hashtable perPartTrackers;
	
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
protected AbstractSelectionService() {
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
public void addSelectionListener(String partId, ISelectionListener listener) {
	getPerPartTracker(partId).addSelectionListener(listener);
}

/* (non-Javadoc)
 * Method declared on ISelectionService.
 */
public void removeSelectionListener(ISelectionListener l) {
	listeners.remove(l);
}

/*
 * (non-Javadoc)
 * Method declared on ISelectionListener.
 */
public void removeSelectionListener(String partId, ISelectionListener listener) {
	getPerPartTracker(partId).removeSelectionListener(listener);
}

/**
 * Fires a selection event to the given listeners.
 * 
 * @param part the part or <code>null</code> if no active part
 * @param sel the selection or <code>null</code> if no active selection
 */
protected void fireSelection(final IWorkbenchPart part, final ISelection sel) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final ISelectionListener l = (ISelectionListener)array[i];
		if ((part != null && sel != null) || l instanceof INullSelectionListener) {
			Platform.run(new SafeRunnableAdapter() {
				public void run() {
					l.selectionChanged(part, sel);
				}
				public void handleException(Throwable e) {
					super.handleException(e);
					// If an unexpected exception happens, remove the listener
					// to make sure the workbench keeps running.
					removeSelectionListener(l);
				}
			});
		}
	}
}

/**
 * Returns the per-part selection tracker for the given part id.
 * 
 * @param partId part identifier
 * @return per-part selection tracker
 */
protected AbstractPartSelectionTracker getPerPartTracker(String partId) {
	if (perPartTrackers == null) {
		perPartTrackers = new Hashtable(4);
	}
	AbstractPartSelectionTracker tracker = (AbstractPartSelectionTracker) perPartTrackers.get(partId);
	if (tracker == null) {
		tracker = createPartTracker(partId);
		perPartTrackers.put(partId, tracker);
	}
	return tracker;
}

/**
 * Creates a new per-part selection tracker for the given part id.
 * 
 * @param partId part identifier
 * @return per-part selection tracker
 */
protected abstract AbstractPartSelectionTracker createPartTracker(String partId);

/**
 * Returns the selection.
 */
public ISelection getSelection() {
	if (activeProvider != null)
		return activeProvider.getSelection();
	else
		return null;
}

/*
 * @see ISelectionService#getSelection(String)
 */
public ISelection getSelection(String partId) {
	return getPerPartTracker(partId).getSelection();
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
		} else {
			//Reset active part. activeProvider may not be null next time this method is called.
			activePart = null;
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
