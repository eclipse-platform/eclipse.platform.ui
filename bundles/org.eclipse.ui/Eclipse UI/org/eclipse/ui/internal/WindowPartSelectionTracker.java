/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
 
/**
 * Provides part selection tracking for a part with a specific id in all pages of a specific workbench
 * window. This tracker shields clients from a part opening and closing, and still provides selection
 * notification/information even when the part is not active..
 */ 
public class WindowPartSelectionTracker extends AbstractPartSelectionTracker implements IPageListener, ISelectionListener {
	
	/**
	 * The window this selection tracker is working in
	 */
	private IWorkbenchWindow fWindow;
			
	/**
	 * Constructs a new selection tracker for the given window and part id.
	 * 
	 * @param window workbench window
	 * @param partId part identifier
	 */
	public WindowPartSelectionTracker(IWorkbenchWindow window, String partId) {
		super(partId);
		setWindow(window);
		window.addPageListener(this);
		IWorkbenchPage[] pages = window.getPages();
		for (int i = 0; i < pages.length; i++) {
			pageOpened(pages[i]);
		}
	}

	/*
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
	}

	/*
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
	}
	
	/*
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
		page.addSelectionListener(getPartId(), new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				fireSelection(part, selection);
			}
		});
		page.addPostSelectionListener(getPartId(), new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				firePostSelection(part, selection);
			}
		});				
	}
	
	/**
	 * Sets the window this tracker is working in.
	 * 
	 * @param window workbench window
	 */
	private void setWindow(IWorkbenchWindow window) {
		fWindow = window;
	}
	
	/**
	 * Returns the window this tracker is working in.
	 * 
	 * @return workbench window
	 */
	protected IWorkbenchWindow getWindow() {
		return fWindow;
	}	
	
	/**
	 * @see AbstractPartSelectionTracker#dispose()
	 */
	public void dispose() {
		super.dispose();
		fWindow = null;
	}
	
	/*
	 * @see AbstractPartSelectionTracker#getSelection()
	 */
	public ISelection getSelection() {
		IWorkbenchPage page = getWindow().getActivePage();
		if (page != null) {
			return page.getSelection(getPartId());
		}
		return null;
	}

	/*
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		fireSelection(part, selection);
	}

}
