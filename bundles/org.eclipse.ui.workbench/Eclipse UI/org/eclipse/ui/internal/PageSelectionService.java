package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */

import org.eclipse.ui.IWorkbenchPage;
 
/**
 * The selection service for a page.
 */
/* package */
class PageSelectionService extends AbstractSelectionService {

	private IWorkbenchPage page;

	/**
	 * Creates a new selection service for a specific workbench page.
	 */
	public PageSelectionService(IWorkbenchPage page) {
		setPage(page);
	}

	/**
	 * Sets the page.
	 */
	private void setPage(IWorkbenchPage page) {
		this.page = page;
	}
	
	/**
	 * Returns the page.
	 */
	protected IWorkbenchPage getPage() {
		return page;
	}
	
	/*
	 * @see AbstractSelectionService#createPartTracker(String)
	 */
	protected AbstractPartSelectionTracker createPartTracker(String partId) {
		return new PagePartSelectionTracker(getPage(), partId);
	}

}
