/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.misc.UIListenerLogging;

public class WorkbenchPagePartList extends PartList {
    
    private PageSelectionService selectionService;
    
    private PartService partService = new PartService(UIListenerLogging.PAGE_PARTLISTENER_EVENTS, 
            UIListenerLogging.PAGE_PARTLISTENER2_EVENTS);
    
    public WorkbenchPagePartList(PageSelectionService selectionService) {
        this.selectionService = selectionService;
    }
    
    public IPartService getPartService() {
        return partService;
    }
    
    protected void firePartOpened(IWorkbenchPartReference part) {
        partService.firePartOpened(part);     
    }

    protected void firePartClosed(IWorkbenchPartReference part) {
        partService.firePartClosed(part);
    }

    protected void firePartAdded(IWorkbenchPartReference part) {
        // TODO: There is no listener for workbench page additions yet 
    }

    protected void firePartRemoved(IWorkbenchPartReference part) {
        // TODO: There is no listener for workbench page removals yet
    }

    protected void fireActiveEditorChanged(IWorkbenchPartReference ref) {
        if (ref != null) {
            firePartBroughtToTop(ref);
        }
    }

    protected void fireActivePartChanged(IWorkbenchPartReference oldRef, IWorkbenchPartReference newRef) {
        partService.setActivePart(newRef);
        
        IWorkbenchPart realPart = newRef == null? null : newRef.getPart(false);
        selectionService.setActivePart(realPart);
    }
    
    protected void firePartHidden(IWorkbenchPartReference ref) {
        partService.firePartHidden(ref);
    }

    protected void firePartVisible(IWorkbenchPartReference ref) {
        partService.firePartVisible(ref);
    }
    
    protected void firePartInputChanged(IWorkbenchPartReference ref) {
        partService.firePartInputChanged(ref);
    }

    public void firePartBroughtToTop(IWorkbenchPartReference ref) {
        partService.firePartBroughtToTop(ref);
    }
}
