package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Map;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
 
/**
 * Provides debug view selection management/notification for
 * a kind of debug view in all pages of a specific workbench
 * window. This selection provider sheilds clients from a debug
 * view openning and closing, and still provides selection
 * notification/information even when the debug view is not the
 * active part.
 */ 
public class DebugWindowSelectionProvider extends AbstractDebugSelectionProvider implements IPageListener{
	
	/**
	 * The window this selection provider is working for
	 */
	private IWorkbenchWindow fWindow;
			
	/**
	 * Constructs a new selection provider for the given window
	 * and view kind.
	 * 
	 * @param window workbench window
	 * @param viewId view identifier
	 */
	public DebugWindowSelectionProvider(IWorkbenchWindow window, String viewId) {
		super(viewId);
		setWindow(window);
		window.addPageListener(this);
		IWorkbenchPage[] pages = window.getPages();
		for (int i = 0; i < pages.length; i++) {
			pageOpened(pages[i]);
		}
	}

	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
	}

	/**
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
	}

	/**
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
		DebugSelectionManager.getDefault().addSelectionChangedListener(this, page, getViewId());
	}
	
	/**
	 * Sets the window this selection provider is working in
	 * 
	 * @param window workbench window
	 */
	private void setWindow(IWorkbenchWindow window) {
		fWindow = window;
	}
	
	/**
	 * Returns the window this selection provider is working in
	 * 
	 * @return workbench window
	 */
	protected IWorkbenchWindow getWindow() {
		return fWindow;
	}	
	
	/**
	 * @see AbstractDebugSelectionProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		fWindow = null;
	}
	
	/**
	 * @see AbstractDebugSelectionProvider#getSelectionProvider()
	 */
	protected ISelectionProvider getSelectionProvider() {
		Map map = DebugSelectionManager.getDefault().getSelectionProviders(getWindow().getActivePage());
		if (map != null) {
			return (ISelectionProvider)map.get(getViewId());
		}
		return null;
	}	
}
