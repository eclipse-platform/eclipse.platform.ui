/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.FastViewBar;
import org.eclipse.ui.internal.FastViewManager;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.ViewPane;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuFastView extends Action implements ISelfUpdatingAction {

    private PresentablePart viewPane;

    private IStackPresentationSite site;

    private boolean realFV = true;
    
    public SystemMenuFastView(IStackPresentationSite site) {
        this.site = site;
        setText(WorkbenchMessages.ViewPane_fastView);
        update();
    }

    public void setPane(PresentablePart newPane) {
        viewPane = newPane;
        update();
    }
    
    public void update() {
        IViewReference viewRef = getReference();
        if (viewRef == null) {
        	setEnabled(false);
        	return;
        }
        
        // Are we showing a 'real' fast view or a minimized view ?
        Perspective persp = viewPane.getPane().getPage().getActivePerspective();
        FastViewManager fvm = persp.getFastViewManager();
        
        String trimId = null;
        if (fvm != null)
        	trimId = fvm.getIdForRef(viewRef);
        realFV = trimId == null || FastViewBar.FASTVIEWBAR_ID.equals(trimId);

        // it's 'restore' if we're not using a real fast view
        if (realFV) {
        	setText(WorkbenchMessages.ViewPane_fastView);
        }
        else {
        	setText(WorkbenchMessages.StandardSystemToolbar_Restore);
        	setChecked(false);
        }
        
        if (!site.isPartMoveable(viewPane)) {
            setEnabled(false);
        } else {
            setEnabled(true);
            
            if (realFV)
            	setChecked(persp.isFastView(viewRef));
        }
    }

    private IViewReference getReference() {
        IViewReference viewRef = null;
        
        if (viewPane != null) {
            IWorkbenchPartReference ref = viewPane.getPane().getPartReference();
            
            if (ref instanceof IViewReference) {
                viewRef = (IViewReference) ref;
            }
        }
        return viewRef;
    }

    public boolean shouldBeVisible() {
        if (viewPane == null || viewPane.getPane().getPage() == null) {
            return false;
        }

        WorkbenchWindow workbenchWindow = (WorkbenchWindow) viewPane.getPane().getPage()
                .getWorkbenchWindow();

        return workbenchWindow.getShowFastViewBars() && viewPane != null
                && site.isPartMoveable(viewPane);
    }

    public void dispose() {
        viewPane = null;
    }

    public void run() {
    	if (realFV) {
	        if (viewPane.getPane() instanceof ViewPane) {
	            ViewPane pane = (ViewPane) viewPane.getPane();
	            
	            if (!isChecked()) {
	                pane.doMakeFast();
	            } else {
	                pane.doRemoveFast();
	            }   
	        }
    	}
    	else {
    		// We're a minimized stack...restore it
            IViewReference viewRef = getReference();
            
            Perspective persp = viewPane.getPane().getPage().getActivePerspective();
            FastViewManager fvm = persp.getFastViewManager();
            String trimId = fvm.getIdForRef(viewRef);
            fvm.restoreToPresentation(trimId);
    	}
    }
}
