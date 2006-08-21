/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.ViewStack;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * mplements the 'Move to Trim' action
 * 
 * @since 3.2
 *
 */
public class SystemMenuMoveToTrim extends Action implements ISelfUpdatingAction {

    private PresentablePart viewPane;

    private IStackPresentationSite site;

    public SystemMenuMoveToTrim(IStackPresentationSite site) {
        this.site = site;
        setText(WorkbenchMessages.ViewPane_moveToTrim);
        update();
    }

    public void setPane(PresentablePart newPane) {
        viewPane = newPane;
        update();
    }
    
    public void update() {
        IViewReference viewRef = getReference();
        
        if (viewRef == null
                || !site.isPartMoveable(viewPane)) {
            setEnabled(false);
        } else {
            setEnabled(true);
            
            setChecked(viewPane.getPane().getPage().getActivePerspective().isFastView(
                    viewRef));
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

    private WorkbenchWindow getWorkbenchWindow() {
    	return (WorkbenchWindow) viewPane.getPane().getPage().getWorkbenchWindow();
    }

    public boolean shouldBeVisible() {
        if (viewPane == null || viewPane.getPane().getPage() == null) {
            return false;
        }

        String enabled  = System.getProperty("MultiFVB"); //$NON-NLS-1$
        if (enabled == null)
        	return false;
        
        return getWorkbenchWindow().getShowFastViewBars() && viewPane != null
                && site.isPartMoveable(viewPane);
    }
    
    public void dispose() {
        viewPane = null;
    }

    public void run() {
        Perspective psp = viewPane.getPane().getPage().getActivePerspective();
        psp.moveToTrim((ViewStack) viewPane.getPane().getStack(), FastViewBar.GROUP_FVB);
    }
}
