/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * A part service for a workbench window.
 */
public class WWinPartService implements IPartService {
    private PartListenerList listeners = new PartListenerList();

    private PartListenerList2 listeners2 = new PartListenerList2();

    private WindowSelectionService selectionService;

    private IWorkbenchPage activePage;

    private IPartListener2 partListner = new IPartListener2() {
        public void partActivated(IWorkbenchPartReference ref) {
            firePartActivated(ref);
        }

        public void partBroughtToTop(IWorkbenchPartReference ref) {
            firePartBroughtToTop(ref);
        }

        public void partClosed(IWorkbenchPartReference ref) {
            firePartClosed(ref);
        }

        public void partDeactivated(IWorkbenchPartReference ref) {
            firePartDeactivated(ref);
        }

        public void partOpened(IWorkbenchPartReference ref) {
            firePartOpened(ref);
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
        	IWorkbenchPartReference[] refs = ((WorkbenchPage)activePage).getOpenParts(); 
        	
        	for (int i = 0; i < refs.length; i++) {
        		IWorkbenchPartReference reference = refs[i];
        		
        		firePartOpened(reference);
        	}
                	
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
    		IWorkbenchPartReference[] refs = ((WorkbenchPage)activePage).getOpenParts(); 
    		
    		for (int i = 0; i < refs.length; i++) {
    			IWorkbenchPartReference reference = refs[i];
    			
    			firePartClosed(reference);
    		}
            		
            activePage.removePartListener(partListner);
            activePage = null;
        }
        selectionService.reset();
    }
    
    /**
     * @param ref
     */
    private void firePartActivated(IWorkbenchPartReference ref) {
    	IWorkbenchPart part = ref.getPart(false);
    	if(part != null) {
    		listeners.firePartActivated(part);
    		selectionService.partActivated(part);
    	}
    	listeners2.firePartActivated(ref);
    }
    
    /**
     * @param ref
     */
    private void firePartBroughtToTop(IWorkbenchPartReference ref) {
    	IWorkbenchPart part = ref.getPart(false);
    	if(part != null) {
    		listeners.firePartBroughtToTop(part);
    		selectionService.partBroughtToTop(part);
    	}
    	listeners2.firePartBroughtToTop(ref);
    }
    
    /**
     * @param ref
     */
    private void firePartClosed(IWorkbenchPartReference ref) {
    	IWorkbenchPart part = ref.getPart(false);
    	if(part != null) {
    		listeners.firePartClosed(part);
    		selectionService.partClosed(part);
    	}
    	listeners2.firePartClosed(ref);
    }
    
    /**
     * @param ref
     */
    private void firePartDeactivated(IWorkbenchPartReference ref) {
    	IWorkbenchPart part = ref.getPart(false);
    	if(part != null) {
    		listeners.firePartDeactivated(part);
    		selectionService.partDeactivated(part);
    	}
    	listeners2.firePartDeactivated(ref);
    }
    
    /**
     * @param ref
     */
    private void firePartOpened(IWorkbenchPartReference ref) {	
    	IWorkbenchPart part = ref.getPart(false);
    	if(part != null) {		
    		listeners.firePartOpened(part);
    		selectionService.partOpened(part);
    	}
    	listeners2.firePartOpened(ref);
     }
}