package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Handles debug events, updating a view and viewer.
 */
public abstract class AbstractDebugEventHandler implements IDebugEventSetListener {
	
	/**
	 * This event handler's view
	 */
	private AbstractDebugView fView;

	/**
	 * The part listener for this view. Set to <code>null</code> when this view
	 * isn't currently listening to part changes.
	 */
	private DebugViewPartListener fPartListener= null;

	/**
	 * Part listener that disables updating when the variables view is not
	 * visible and reenables updating when the view appears.
	 */
	private class DebugViewPartListener implements IPartListener2 {
		public void partActivated(IWorkbenchPartReference ref) {
		}
		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}
		public void partOpened(IWorkbenchPartReference ref) {
		}
		/**
		 * Refresh the view when it becomes visible.
		 */
		public void partVisible(IWorkbenchPartReference ref) {
			IWorkbenchPart part= ref.getPart(false);
			// The event handler is created before the viewer is set.
			if (part != null && part == getView() && getViewer() != null) {
				refresh();
			}
		}
		public void partHidden(IWorkbenchPartReference ref) {
		}
		public void partClosed(IWorkbenchPartReference ref) {
		}
		public void partDeactivated(IWorkbenchPartReference ref) {
		}
	}
	
	/**
	 * Constructs an event handler for the given view.
	 * 
	 * @param view debug view
	 */
	public AbstractDebugEventHandler(AbstractDebugView view) {
		setView(view);
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.addDebugEventListener(this);
		// The launch listener registers the view's part listener when there are active launches
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new ILaunchesListener() {
			public void launchesRemoved(ILaunch[] launches) {
			}
			/**
			 * Start listening to part activations when a launch is added.
			 */
			public void launchesAdded(ILaunch[] launches) {
				registerPartListener();
			}
			public void launchesChanged(ILaunch[] launches) {
			}
		});
		// if there are already launches, must add a part listener
		if (activeTargetsRemain()) {
			registerPartListener();
		}
	}

	/**
	 * Creates and registers a part listener with this event handler's page,
	 * if one does not already exist.
	 */
	protected void registerPartListener() {
		if (fPartListener == null) {
			fPartListener= new DebugViewPartListener();
			getView().getSite().getPage().addPartListener(new DebugViewPartListener());
		}
	}

	/**
	 * Deregisters and disposes this event handler's part listener.
	 */
	protected void deregisterPartListener() {
		if (fPartListener != null) {
			getView().getSite().getPage().removePartListener(fPartListener);
			fPartListener = null;
		}
	}

	/**
	 * Returns the active workbench page or <code>null</code> if none.
	 */
	protected IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getActivePage();
	}
	
	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(final DebugEvent[] events) {
		if (!isAvailable()) {
			return;
		}
		Runnable r= new Runnable() {
			public void run() {
				if (isAvailable() && isViewVisible()) {
					doHandleDebugEvents(events);
				}
				updateForDebugEvents(events);
			}
		};
		getView().asyncExec(r);
	}
	
	/**
	 * Updates this view for the given debug events. Unline
	 * doHandleDebugEvents(DebugEvent[]) which is only called if the view is
	 * visible, this method is always called. This allows the view to perform
	 * updating that must always be performed, even when the view is not
	 * visible.
	 */
	protected void updateForDebugEvents(DebugEvent[] events) {
		boolean terminatingTarget= false;
		for (int i = 0; i < events.length; i++) {
			DebugEvent event= events[i];
			if (event.getKind() == DebugEvent.TERMINATE && event.getSource() instanceof IDebugTarget) {
				terminatingTarget= true;
				break;
			}
		}
		if (!terminatingTarget || fPartListener == null || activeTargetsRemain()) {
			return;
		}
		// To get here, there must be no running IDebugTargets
		IWorkbenchPage page= getActivePage();
		if (page != null) {
			page.removePartListener(fPartListener);
			fPartListener= null;
		}
	}
	
	/**
	 * Returns whether or not there are any active (running) debug targets.
	 */
	protected boolean activeTargetsRemain() {
		IDebugTarget[] targets= DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
		for (int i = 0; i < targets.length; i++) {
			IDebugTarget target = targets[i];
			if (!target.isDisconnected() && !target.isTerminated()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Implementation specific handling of debug events.
	 * Subclasses should override.
	 */
	protected abstract void doHandleDebugEvents(DebugEvent[] events);	
		
	/**
	 * Helper method for inserting the given element - must be called in UI thread
	 */
	protected void insert(Object element) {
		if (isAvailable()) {
			final Object parent= ((ITreeContentProvider)getTreeViewer().getContentProvider()).getParent(element);
			// a parent can be null for a debug target or process that has not yet been associated
			// with a launch
			if (parent != null) {
				getView().showViewer();
				getTreeViewer().add(parent, element);
			}
		}
	}

	/**
	 * Helper method to remove the given element - must be called in UI thread.
	 */
	protected void remove(Object element) {
		if (isAvailable()) {
			getView().showViewer();
			getTreeViewer().remove(element);
		}
	}

	/**
	 * Helper method to update the label of the given element - must be called in UI thread
	 */
	protected void labelChanged(Object element) {
		if (isAvailable()) {
			getView().showViewer();
			getTreeViewer().update(element, new String[] {IBasicPropertyConstants.P_TEXT});
		}
	}

	/**
	 * Refresh the given element in the viewer - must be called in UI thread.
	 */
	protected void refresh(Object element) {
		if (isAvailable()) {
			 getView().showViewer();
			 getTreeViewer().refresh(element);
		}
	}
	
	/**
	 * Refresh the viewer - must be called in UI thread.
	 */
	public void refresh() {
		if (isAvailable()) {
			 getView().showViewer();
			 getTreeViewer().refresh();
		}
	}	

	/**
	 * Helper method to select and reveal the given element - must be called in UI thread
	 */
	protected void selectAndReveal(Object element) {
		if (isAvailable()) {
			getViewer().setSelection(new StructuredSelection(element), true);
		}
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
		return getView().getViewer();
	}
	
	/**
	 * Returns this event handler's viewer as a tree
	 * viewer or <code>null</code> if none.
	 * 
	 * @return this event handler's viewer as a tree
	 * viewer or <code>null</code> if none
	 */
	protected TreeViewer getTreeViewer() {
		if (getViewer() instanceof TreeViewer) {
			return (TreeViewer)getViewer();
		} 
		return null;
	}
	
	/**
	 * Returns whether this event handler's viewer is
	 * currently available.
	 * 
	 * @return whether this event handler's viewer is
	 * currently available
	 */
	protected boolean isAvailable() {
		return getView().isAvailable();
	}
	
	/**
	 * Returns whether this event handler's view is currently visible.
	 * 
	 * @return boolean
	 */
	protected boolean isViewVisible() {
		AbstractDebugView view = getView();
		if (view != null) {
			IWorkbenchPartSite site = view.getSite();
			if (site != null) {
				IWorkbenchPage page = site.getPage();
				if (page != null) {
					return page.isPartVisible(view);
				}
			}
		}
		return false;		
	}	
}

