/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.internal.misc.UIListenerLogging;

/**
 * A part service for a workbench window.
 */
public class WWinPartService implements IPartService {
    private PartService partService = new PartService(UIListenerLogging.WINDOW_PARTLISTENER_EVENTS, 
            UIListenerLogging.WINDOW_PARTLISTENER2_EVENTS);

    private WindowSelectionService selectionService;

    private IWorkbenchPage activePage;
    
    private IPartListener2 partListner = new IPartListener2() {
        public void partActivated(IWorkbenchPartReference ref) {
            updateActivePart();
        }

        public void partBroughtToTop(IWorkbenchPartReference ref) {
            partService.firePartBroughtToTop(ref);
        }

        public void partClosed(IWorkbenchPartReference ref) {
            partService.firePartClosed(ref);
        }

        public void partDeactivated(IWorkbenchPartReference ref) {
            updateActivePart();
        }

        public void partOpened(IWorkbenchPartReference ref) {
            partService.firePartOpened(ref);
        }

        public void partHidden(IWorkbenchPartReference ref) {
            partService.firePartHidden(ref);
        }

        public void partVisible(IWorkbenchPartReference ref) {
            partService.firePartVisible(ref);
        }

        public void partInputChanged(IWorkbenchPartReference ref) {
            partService.firePartInputChanged(ref);
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
        partService.addPartListener(l);
    }

    /*
     * (non-Javadoc)
     * Method declared on IPartService
     */
    public void addPartListener(IPartListener2 l) {
        partService.addPartListener(l);
    }

    /*
     * (non-Javadoc)
     * Method declared on IPartService
     */
    public void removePartListener(IPartListener l) {
        partService.removePartListener(l);
    }

    /*
     * (non-Javadoc)
     * Method declared on IPartService
     */
    public void removePartListener(IPartListener2 l) {
        partService.removePartListener(l);
    }

    /*
     * (non-Javadoc)
     * Method declared on IPartService
     */
    public IWorkbenchPart getActivePart() {
        return partService.getActivePart();
    }
    
    private void updateActivePart() {
        IWorkbenchPartReference activeRef = null;
        IWorkbenchPart activePart = null;
        
        if (activePage != null) {
            activePart = activePage.getActivePart();
            activeRef = activePage.getActivePartReference();
        }
        
        partService.setActivePart(activeRef);
        selectionService.setActivePart(activePart);
    }

    /*
     * (non-Javadoc)
     * Method declared on IPartService
     */
    public IWorkbenchPartReference getActivePartReference() {
        return partService.getActivePartReference();
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
        if (newPage == activePage) {
			return;
		}

        // Fire events in the following order:

        // 1. For each open part in the new page, open it and then (if applicable) make it visible
        // 2. Deactivate old active part
        // 3. Activate the new active part
        // 4. For each open part in the old page, make it invisible then close it        

        // Hook listener on the new page.
        if (newPage != null) {      
            IWorkbenchPartReference[] refs = ((WorkbenchPage)newPage).getOpenParts(); 
            
            for (int i = 0; i < refs.length; i++) {
                IWorkbenchPartReference reference = refs[i];
                
                partService.firePartOpened(reference);
                
                IWorkbenchPart part = reference.getPart(false);
                if (part != null && newPage.isPartVisible(part)) {
                    partService.firePartVisible(reference);
                }
            }            

            partService.setActivePart(newPage.getActivePartReference());
            selectionService.setActivePart(newPage.getActivePart());
        } else {
            partService.setActivePart(null);
            selectionService.setActivePart(null);
        }

        // Unhook listener from the old page.
        reset();

        // Update active page.
        activePage = newPage;

        if (newPage != null) {
            newPage.addPartListener(partListner);
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
        IWorkbenchPage tempPage = activePage;
        activePage = null;
        if (tempPage != null) {            
            WorkbenchPage page = (WorkbenchPage)tempPage;
            
    		IWorkbenchPartReference[] refs = page.getOpenParts(); 
    		
    		for (int i = 0; i < refs.length; i++) {
    			IWorkbenchPartReference reference = refs[i];
                
                if (page.isPartVisible(reference)) {
                    partService.firePartHidden(reference);
                }
                
    			partService.firePartClosed(reference);
    		}
            		
            tempPage.removePartListener(partListner);
        }
        
    }
    
}
