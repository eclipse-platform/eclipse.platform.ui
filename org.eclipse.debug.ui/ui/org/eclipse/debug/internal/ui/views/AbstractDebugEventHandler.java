package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Handles debug events, updating a view and viewer.
 */
public abstract class AbstractDebugEventHandler implements IDebugEventListener {
	
	/**
	 * This event handler's view
	 */
	private AbstractDebugView fView;
	
	/**
	 * This event handler's viewer
	 */
	private Viewer fViewer;
		
	/**
	 * Constructs an event handler for the given
	 * view and viewer.
	 * 
	 * @param view debug view
	 * @param viewer viewer
	 */
	public AbstractDebugEventHandler(AbstractDebugView view, Viewer viewer) {
		setView(view);
		setViewer(viewer);
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.addDebugEventListener(this);
	}
	
	/**
	 * @see IDebugEventListener#handleDebugEvent(DebugEvent)
	 */
	public void handleDebugEvent(final DebugEvent event) {
		if (getViewer() == null) {
			return;
		}
		Object element= event.getSource();
		if (element == null) {
			return;
		}
		if (getViewer().getControl() == null || getViewer().getControl().isDisposed()) {
			return;
		}
		Runnable r= new Runnable() {
			public void run() {
				if (getViewer().getControl() == null || getViewer().getControl().isDisposed()) {
					return;
				}
				doHandleDebugEvent(event);
			}
		};
		
		getView().asyncExec(r);
	}
	

	/**
	 * Implementation specific handling of debug events.
	 * Subclasses should override.
	 */
	protected abstract void doHandleDebugEvent(DebugEvent event);	
		
	/**
	 * Helper method for inserting the given element - must be called in UI thread
	 */
	protected void insert(Object element) {
		final Object parent= ((ITreeContentProvider)getTreeViewer().getContentProvider()).getParent(element);
		// a parent can be null for a debug target or process that has not yet been associated
		// with a launch
		if (parent != null) {
			getView().showViewer();
			getTreeViewer().add(parent, element);
		}
	}

	/**
	 * Helper method to remove the given element - must be called in UI thread.
	 */
	protected void remove(Object element) {
		 getView().showViewer();
		getTreeViewer().remove(element);
	}

	/**
	 * Helper method to update the label of the given element - must be called in UI thread
	 */
	protected void labelChanged(Object element) {
		getView().showViewer();
		getTreeViewer().update(element, new String[] {IBasicPropertyConstants.P_TEXT});
	}

	/**
	 * Refresh the given element in the viewer - must be called in UI thread.
	 */
	protected void refresh(Object element) {
		if (getTreeViewer() != null) {
			 getView().showViewer();
			 getTreeViewer().refresh(element);
		}
	}
	
	/**
	 * Refresh the viewer - must be called in UI thread.
	 */
	protected void refresh() {
		if (getTreeViewer() != null) {
			 getView().showViewer();
			 getTreeViewer().refresh();
		}
	}	

	/**
	 * Helper method to select and reveal the given element - must be called in UI thread
	 */
	protected void selectAndReveal(Object element) {
		getViewer().setSelection(new StructuredSelection(element), true);
	}
	
	/**
	 * De-registers this event handler from the debug model.
	 */
	public void dispose() {
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.removeDebugEventListener(this);
	}
	
	/**
	 * Returns the view this event handler is
	 * updating.
	 * 
	 * @return debug view
	 */
	protected AbstractDebugView getView() {
		return fView;
	}
	
	/**
	 * Sets the view this event handler is updating.
	 * 
	 * @param view debug view
	 */
	private void setView(AbstractDebugView view) {
		fView = view;
	}

	/**
	 * Returns the viewer this event handler is 
	 * updating.
	 * 
	 * @return viewer
	 */	
	protected Viewer getViewer() {
		return fViewer;
	}
	
	/**
	 * Sets the viewer this event handler is updating.
	 * 
	 * @param viewer launch viewer
	 */
	private void setViewer(Viewer viewer) {
		fViewer = viewer;
	}
	
	protected TreeViewer getTreeViewer() {
		if (getViewer() instanceof TreeViewer) {
			return (TreeViewer)getViewer();
		} 
		return null;
	}
}

