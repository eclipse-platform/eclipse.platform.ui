package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;

/**
 * A part service for a workbench window.
 */
public class WWinPartService implements IPartService, IPageListener, IPartListener
{
	private IWorkbenchWindow window;
	private PartListenerList listeners = new PartListenerList();
	private WindowSelectionService selectionService;
	private IWorkbenchPage activePage;
	
/**
 * Creates a new part service for a workbench window.
 */
public WWinPartService(IWorkbenchWindow window) {
	setWindow(window);
	selectionService = new WindowSelectionService(window);
}
	
/**
 * Sets the window.
 */
private void setWindow(IWorkbenchWindow window) {
	this.window = window;
}

/**
 * Returns the window.
 */
protected IWorkbenchWindow getWindow() {
	return window;
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
		
	// Unhook listener from the old page.
	reset();

	// Update active page.
	activePage = newPage;

	// Hook listener on the new page.
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
	// Unhook listener from the old page.
	if (page == activePage) {
		reset();
	}
}
/**
 * Notifies that a page has been opened.
 */
public void pageOpened(IWorkbenchPage page) {
	pageActivated(page);
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
/*
 * Resets the part service.  The active page, part and selection are
 * dereferenced.
 */
public void reset() {
	if (activePage != null) {
		activePage.removePartListener(this);
		activePage = null;
	}
	selectionService.reset();
}
}
