package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;

/**
 * A part service for the workbench window.
 */
public class WWinPartService implements IPartService, IPageListener, IPartListener
{
	private PartListenerList listeners = new PartListenerList();
	private SelectionService selectionService = new SelectionService();
	private IWorkbenchPage activePage;
/**
 * Constructor comment.
 */
public WWinPartService() {
	super();
}
/*
 * Adds an IPartListener to the part service.
 */
public void addPartListener(IPartListener l) {
	listeners.addPartListener(l);
}
/*
 * Returns the active part.
 */
public IWorkbenchPart getActivePart() {
	if (activePage != null)
		return activePage.getActivePart();
	else
		return null;
}
/*
 * Returns the selection service.
 */
public ISelectionService getSelectionService() {
	return selectionService;
}
/**
 * Notifies that a page has been activated.
 */
public void pageActivated(IWorkbenchPage newPage) {
	// Optimize.
	if (newPage == activePage)
		return;
		
	// Unhook selection from the old page.
	if (activePage != null) {
		activePage.removePartListener(this);
	}

	// Update active page.
	activePage = newPage;

	// Hook selection on the new page.
	if (activePage != null) {
		activePage.addPartListener(this);
		if (getActivePart() != null)
			partActivated(getActivePart());
	}
}
/**
 * Notifies that a page has been closed
 */
public void pageClosed(IWorkbenchPage page) {
	// Unhook selection from the old page.
	if (page == activePage) {
		page.removePartListener(this);
		activePage = null;
	}
}
/**
 * Notifies that a page has been opened.
 */
public void pageOpened(IWorkbenchPage page) {
	// Ignored.  Wait for activation.
}
/**
 * Notifes that a part has been activated.
 */
public void partActivated(IWorkbenchPart part) {
	listeners.firePartActivated(part);
	selectionService.partActivated(part);
}
/**
 * Notifes that a part has been brought to top.
 */
public void partBroughtToTop(IWorkbenchPart part) {
	listeners.firePartBroughtToTop(part);
	selectionService.partBroughtToTop(part);
}
/**
 * Notifes that a part has been closed.
 */
public void partClosed(IWorkbenchPart part) {
	listeners.firePartClosed(part);
	selectionService.partClosed(part);
}
/**
 * Notifes that a part has been deactivated.
 */
public void partDeactivated(IWorkbenchPart part) {
	listeners.firePartDeactivated(part);
	selectionService.partDeactivated(part);
}
/**
 * Notifes that a part has been opened.
 */
public void partOpened(IWorkbenchPart part) {
	listeners.firePartOpened(part);
	selectionService.partOpened(part);
}
/*
 * Removes an IPartListener from the part service.
 */
public void removePartListener(IPartListener l) {
	listeners.removePartListener(l);
}
}
