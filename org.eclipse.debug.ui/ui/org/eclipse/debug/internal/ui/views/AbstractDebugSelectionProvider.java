package org.eclipse.debug.internal.ui.views;

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
 * a kind of debug view.
 */
public abstract class AbstractDebugSelectionProvider implements ISelectionProvider, ISelectionChangedListener {

	/**
	 * List of selection listeners for this selection provider
	 */
	private ListenerList fListeners = new ListenerList(2);
			
	/**
	 * The id of the view this selection provider works for
	 */
	private String fViewId;
	
	/**
	 * Constructs a debug view selection provider for the specified
	 * kind of debug view.
	 * 
	 * @param id view identifier
	 */
	public AbstractDebugSelectionProvider(String id) {
		setViewId(id);
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
		ISelectionProvider sp = getSelectionProvider();
		if (sp != null) {
			return sp.getSelection();
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
		ISelectionProvider sp = getSelectionProvider();
		if (sp != null) {
			sp.setSelection(selection);
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


	/**
	 * Returns the selection provider for this service,
	 * or <code>null</code> if none.
	 * 
	 * @return selection provider or <code>null</code>
	 */
	protected abstract ISelectionProvider getSelectionProvider();
}
