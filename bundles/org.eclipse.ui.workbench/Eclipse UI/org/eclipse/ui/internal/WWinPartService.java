package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;

/**
 * A part service for a workbench window.
 */
public class WWinPartService implements IPartService {
	private IWorkbenchWindow window;
	private PartListenerList listeners = new PartListenerList();
	private PartListenerList2 listeners2 = new PartListenerList2();
	private WindowSelectionService selectionService;
	private IWorkbenchPage activePage;
	private IPartListener2 partListner = new IPartListener2() {
		public void partActivated(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			if(part != null) {
				listeners.firePartActivated(part);
				selectionService.partActivated(part);
			}
			listeners2.firePartActivated(ref);	
		}
		public void partBroughtToTop(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			if(part != null) {
				listeners.firePartBroughtToTop(part);
				selectionService.partBroughtToTop(part);
			}
			listeners2.firePartBroughtToTop(ref);	
		}
		public void partClosed(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			if(part != null) {
				listeners.firePartClosed(part);
				selectionService.partClosed(part);
			}
			listeners2.firePartClosed(ref);	
		}
		public void partDeactivated(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			if(part != null) {
				listeners.firePartDeactivated(part);
				selectionService.partDeactivated(part);
			}
			listeners2.firePartDeactivated(ref);	
		}
		public void partOpened(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			if(part != null) {
				listeners.firePartOpened(part);
				selectionService.partOpened(part);
			}
			listeners2.firePartOpened(ref);	
		}
		public void partHidden(IWorkbenchPartReference ref) {
			listeners2.firePartHidden(ref);	
		}
		public void partVisible(IWorkbenchPartReference ref) {
			listeners2.firePartVisible(ref);	
		}
		public void partInputChanged(IWorkbenchPartReference ref) {
			listeners2.firePartInputChanged(ref);	
		}
	};
	
/**
 * Creates a new part service for a workbench window.
 */
public WWinPartService(IWorkbenchWindow window) {
	this.window = window;
	selectionService = new WindowSelectionService(window);
}
/*
 * (non-Javadoc)
 * Method declared on IPartService
 */
public void addPartListener(IPartListener l) {
	listeners.addPartListener(l);
}
/*
 * (non-Javadoc)
 * Method declared on IPartService
 */
public void addPartListener(IPartListener2 l) {
	listeners2.addPartListener(l);
}
/*
 * (non-Javadoc)
 * Method declared on IPartService
 */
public void removePartListener(IPartListener l) {
	listeners.removePartListener(l);
}
/*
 * (non-Javadoc)
 * Method declared on IPartService
 */
public void removePartListener(IPartListener2 l) {
	listeners2.removePartListener(l);
}
/*
 * (non-Javadoc)
 * Method declared on IPartService
 */
public IWorkbenchPart getActivePart() {
	if (activePage != null)
		return activePage.getActivePart();
	else
		return null;
}
/*
 * (non-Javadoc)
 * Method declared on IPartService
 */
public IWorkbenchPartReference getActivePartReference() {
	if (activePage != null)
		return activePage.getActivePartReference();
	else
		return null;
}
/*
 * Returns the selection service.
 */
ISelectionService getSelectionService() {
	return selectionService;
}
/*
 * Notifies that a page has been activated.
 */
void pageActivated(IWorkbenchPage newPage) {
	// Optimize.
	if (newPage == activePage)
		return;
		
	// Unhook listener from the old page.
	reset();

	// Update active page.
	activePage = newPage;

	// Hook listener on the new page.
	if (activePage != null) {
		activePage.addPartListener(partListner);
		if (getActivePart() != null)
			partListner.partActivated(getActivePartReference());
	}
}
/*
 * Notifies that a page has been closed
 */
void pageClosed(IWorkbenchPage page) {
	// Unhook listener from the old page.
	if (page == activePage) {
		reset();
	}
}
/*
 * Notifies that a page has been opened.
 */
void pageOpened(IWorkbenchPage page) {
	pageActivated(page);
}
/*
 * Resets the part service.  The active page, part and selection are
 * dereferenced.
 */
private void reset() {
	if (activePage != null) {
		activePage.removePartListener(partListner);
		activePage = null;
	}
	selectionService.reset();
}
}
