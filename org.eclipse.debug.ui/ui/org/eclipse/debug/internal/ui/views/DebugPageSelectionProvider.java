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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Provides debug view selection management/notification for
 * a debug view in a specific workbench page. This selection
 * provider sheilds clients from a debug view openning and closing,
 * and still provides selection notification/information even
 * when the debug view is not the active part.
 */
public class DebugPageSelectionProvider extends AbstractDebugSelectionProvider implements IPartListener {
	
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
		
	public DebugPageSelectionProvider(IWorkbenchPage page, String id) {
		super(id);
		setPage(page);
		page.addPartListener(this);
		IViewPart part = page.findView(id);
		if (part != null) {
			partOpened(part);
		}
	}
	
	/**
	 * Disposes this selection provider - removes all listeners
	 * currently registered.
	 */
	public void dispose() {
		setDebugView(null);
		setPage(null);
		super.dispose();
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
	 * Sets the page this selection provider works for
	 * 
	 * @param page workbench page
	 */
	private void setPage(IWorkbenchPage page) {
		fPage = page;
	}
	
	/**
	 * Returns the page this selection provider works for
	 * 
	 * @return workbench page
	 */
	protected IWorkbenchPage getPage() {
		return fPage;
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
	
	/**
	 * @see AbstractDebugSelectionProvider#getSelectionProvider()
	 */
	protected ISelectionProvider getSelectionProvider() {
		IDebugViewAdapter sp = getDebugView();
		if (sp != null) {
			return sp.getViewer();
		} 
		return null;
	}	

	/**
	 * Sets the debug view for this selection provider
	 */
	protected void setDebugView(IDebugViewAdapter view) {
		if (fDebugView != null) {
			// remove myself as a listener from the existing
			// debug view
			Viewer v = fDebugView.getViewer();
			if (v != null) {
				v.removeSelectionChangedListener(this);	
			}			
		}
		fDebugView = view;
		if (view != null) {
			Viewer v = fDebugView.getViewer();
			if (v != null) {
				v.addSelectionChangedListener(this);	
			}
		}
	}
}
