/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
            firePartHidden(ref);
        }

        public void partVisible(IWorkbenchPartReference ref) {
            firePartVisible(ref);
        }

        public void partInputChanged(IWorkbenchPartReference ref) {
            UIListenerLogging.logPartListener2Event(selectionService.getWindow(), ref, UIListenerLogging.PE2_PART_INPUT_CHANGED);
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

        // Fire events in the following order:
        // 1. Deactivate old active part
        // 2. For each open part in the old page, make it invisible then close it
        // 3. For each open part in the new page, open it and then (if applicable) make it visible
        // 4. Activate the new active part
        
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
                
                IWorkbenchPart part = reference.getPart(false);
                if (part != null && activePage.isPartVisible(part)) {
                    firePartVisible(reference);
                }
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
        IWorkbenchPage tempPage = activePage;
        activePage = null;
        if (tempPage != null) {
            IWorkbenchPartReference activePartReference = tempPage.getActivePartReference();
            
            if (activePartReference != null) {
                firePartDeactivated(activePartReference);
            }
            
            WorkbenchPage page = (WorkbenchPage)tempPage;
            
    		IWorkbenchPartReference[] refs = page.getOpenParts(); 
    		
    		for (int i = 0; i < refs.length; i++) {
    			IWorkbenchPartReference reference = refs[i];
                
                if (page.isPartVisible(reference)) {
                    firePartHidden(reference);
                }
                
    			firePartClosed(reference);
    		}
            		
            tempPage.removePartListener(partListner);
        }
        selectionService.reset();
    }
    
    /**
     * @param ref
     */
    private void firePartActivated(IWorkbenchPartReference ref) {
    	IWorkbenchPart part = ref.getPart(false);
    	if(part != null) {
            UIListenerLogging.logPartListenerEvent(selectionService.getWindow(), part, UIListenerLogging.PE_ACTIVATED);
    		listeners.firePartActivated(part);
    		selectionService.partActivated(part);
    	}
        
        UIListenerLogging.logPartListener2Event(selectionService.getWindow(), ref, UIListenerLogging.PE2_ACTIVATED);
    	listeners2.firePartActivated(ref);
    }
    
    /**
     * @param ref
     */
    private void firePartBroughtToTop(IWorkbenchPartReference ref) {
    	IWorkbenchPart part = ref.getPart(false);
    	if(part != null) {
            UIListenerLogging.logPartListenerEvent(selectionService.getWindow(), part, UIListenerLogging.PE_PART_BROUGHT_TO_TOP);
    		listeners.firePartBroughtToTop(part);
    		selectionService.partBroughtToTop(part);
    	}
        UIListenerLogging.logPartListener2Event(selectionService.getWindow(), ref, UIListenerLogging.PE2_PART_BROUGHT_TO_TOP);
    	listeners2.firePartBroughtToTop(ref);
    }
    
    /**
     * @param ref
     */
    private void firePartClosed(IWorkbenchPartReference ref) {
    	IWorkbenchPart part = ref.getPart(false);
    	if(part != null) {
            UIListenerLogging.logPartListenerEvent(selectionService.getWindow(), part, UIListenerLogging.PE_PART_CLOSED);
    		listeners.firePartClosed(part);
    		selectionService.partClosed(part);
    	}
        UIListenerLogging.logPartListener2Event(selectionService.getWindow(), ref, UIListenerLogging.PE2_PART_CLOSED);
    	listeners2.firePartClosed(ref);
    }
    
    /**
     * @param ref
     */
    private void firePartDeactivated(IWorkbenchPartReference ref) {
    	IWorkbenchPart part = ref.getPart(false);
    	if(part != null) {
            UIListenerLogging.logPartListenerEvent(selectionService.getWindow(), part, UIListenerLogging.PE_PART_DEACTIVATED);
    		listeners.firePartDeactivated(part);
    		selectionService.partDeactivated(part);
    	}
        UIListenerLogging.logPartListener2Event(selectionService.getWindow(), ref, UIListenerLogging.PE2_PART_DEACTIVATED);
    	listeners2.firePartDeactivated(ref);
    }
    
    private void firePartVisible(IWorkbenchPartReference ref) {
        UIListenerLogging.logPartListener2Event(selectionService.getWindow(), ref, UIListenerLogging.PE2_PART_VISIBLE);
        listeners2.firePartVisible(ref);
    }

    private void firePartHidden(IWorkbenchPartReference ref) {
        UIListenerLogging.logPartListener2Event(selectionService.getWindow(), ref, UIListenerLogging.PE2_PART_HIDDEN);
        listeners2.firePartHidden(ref);
    }
    
    /**
     * @param ref
     */
    private void firePartOpened(IWorkbenchPartReference ref) {	
    	IWorkbenchPart part = ref.getPart(false);
    	if(part != null) {
            UIListenerLogging.logPartListenerEvent(selectionService.getWindow(), part, UIListenerLogging.PE_PART_OPENED);
    		listeners.firePartOpened(part);
    		selectionService.partOpened(part);
    	}
        UIListenerLogging.logPartListener2Event(selectionService.getWindow(), ref, UIListenerLogging.PE2_PART_OPENED);
    	listeners2.firePartOpened(ref);
     }
}
