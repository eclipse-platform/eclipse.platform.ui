package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.ui.IDebugViewAdapter;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Provides debug view selection management/notification for
 * a debug view in a specific workbench page.
 */
public class DebugSelectionProvider implements ISelectionProvider, IPartListener, ISelectionChangedListener {

	/**
	 * List of selection listeners for this selection provider
	 */
	private ListenerList fListeners = new ListenerList(2);
	
	/**
	 * The workbench page this selection provider is providing
	 * debug selection for
	 */
	private IWorkbenchPage fPage;
	
	/**
	 * The debug view in this selection provider's page,
	 * or <code>null</code> if one is not open.
	 */
	private IDebugViewAdapter fDebugView;
	
	/**
	 * The id of the view this selection provider works for
	 */
	private String fViewId;
	
	public DebugSelectionProvider(IWorkbenchPage page, String id) {
		fPage = page;
		setViewId(id);
		page.addPartListener(this);
		IViewPart part = page.findView(id);
		if (part != null) {
			partOpened(part);
		}
	}
	
	/**
	 * @see ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fListeners.add(listener);
	}

	/**
	 * @see ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		IDebugViewAdapter view = getDebugView();
		if (view != null) {
			StructuredViewer viewer = view.getViewer();
			if (viewer != null) {
				return viewer.getSelection();
			}
		}
		return new StructuredSelection();
	}

	/**
	 * @see ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * @see ISelectionProvider#setSelection(ISelection)
	 */
	public void setSelection(ISelection selection) {
		IDebugViewAdapter view = getDebugView();
		if (view != null) {
			view.getViewer().setSelection(selection);
		}
	}
	
	/**
	 * Disposes this selection provider - removes all listeners
	 * currently registered.
	 */
	public void dispose() {
		synchronized (fListeners) {
			Object[] listeners = fListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				fListeners.remove(listeners[i]);
			}
		}
		if (getDebugView() != null) {
			partClosed(getDebugView());
		}
		fPage = null;
	}

	/**
	 * Returns the debug view for this selection provider,
	 * or <code>null</code> if none is open.
	 * 
	 * @return debug view, or <code>null</code>
	 */
	protected IDebugViewAdapter getDebugView() {
		return fDebugView;
	}
	
	/*
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
	}

	/*
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part.getSite().getId().equals(getViewId())) {
			IDebugViewAdapter view = getDebugView();
			if (view != null) {
				setDebugView(null);
			}	
		}		
	}

	/*
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
		if (part.getSite().getId().equals(getViewId())) {
			setDebugView((IDebugViewAdapter)part.getAdapter(IDebugViewAdapter.class));
		}
	}

	/**
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((ISelectionChangedListener)listeners[i]).selectionChanged(event);
		}
	}
	
	/**
	 * Sets the debug view for this selection provider
	 */
	private void setDebugView(IDebugViewAdapter view) {
		fDebugView = view;
		if (view != null) {
			((AbstractDebugView)view).setDebugSelectionProvider(this);
		}
	}
	
	/**
	 * Sets the id of the view that this selection provider
	 * works on
	 * 
	 * @param id view identifier
	 */
	private void setViewId(String id) {
		fViewId = id;
	}
	
	/**
	 * Returns the id of the view that this selection provider
	 * works on
	 * 
	 * @return view identifier
	 */
	protected String getViewId() {
		return fViewId;
	}	

}
