package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugEvent;import org.eclipse.jface.viewers.*;import org.eclipse.swt.widgets.Control;

public abstract class BasicContentProvider implements IStructuredContentProvider {

	protected StructuredViewer fViewer;
	protected boolean fDisposed= false;

	/**
	 * @see IContentProvider#dispose
	 */
	public void dispose() {
		fDisposed= true;
	}
	
	/**
	 * Returns whether this content provider has already
	 * been disposed.
	 */
	protected boolean isDisposed() {
		return fDisposed;
	}
	
	/**
	 * @see IContentProvider#inputChanged
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fViewer= (StructuredViewer) viewer;
	}

	/**
	 * @see Display.asyncExec(Runnable)
	 */
	protected void asyncExec(Runnable r) {
		if (fViewer != null) {
			Control ctrl= fViewer.getControl();
			if (ctrl != null && !ctrl.isDisposed()) {
				ctrl.getDisplay().asyncExec(r);
			}
		}
	}
	
	/**
	 * Refreshes the viewer - must be called in UI thread.
	 */
	protected void refresh() {
		if (fViewer != null) {
			fViewer.refresh();
		}
	}
			
	/**
	 * Refresh the given element in the viewer - must be called in UI thread.
	 */
	protected void refresh(Object element) {
		if (fViewer != null) {
			 fViewer.refresh(element);
		}
	}
	
	/**
	 * @see IDebugEventListener
	 */
	public void handleDebugEvent(final DebugEvent event) {
		if (fViewer == null) {
			return;
		}
		Object element= event.getSource();
		if (element == null) {
			return;
		}
		Runnable r= new Runnable() {
			public void run() {
				if (!isDisposed()) {
					doHandleDebugEvent(event);
				}
			}
		};
		
		asyncExec(r);
	}
	
	/**
	 * @see ITreeContentProvider
	 */
	public Object[] getChildren(final Object parent) {
		return doGetChildren(parent);
	}
	
	/**
	 * Performs an update based on the event
	 */
	protected abstract void doHandleDebugEvent(DebugEvent event);
	
	protected abstract Object[] doGetChildren(Object parent);
}

