/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.RectangleAnimation;
import org.eclipse.ui.internal.ViewPane;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuFastView extends Action implements ISelfUpdatingAction {

    private PresentablePart viewPane;

    private IStackPresentationSite site;

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

        return getWorkbenchWindow().getShowFastViewBars() && viewPane != null
                && site.isPartMoveable(viewPane);
    }
    
    public void dispose() {
        viewPane = null;
    }

    public void run() {
        if (viewPane.getPane() instanceof ViewPane) {
            if (!isChecked()) {
            	Rectangle viewBounds = DragUtil.getDisplayBounds(viewPane.getControl());
            	Rectangle fvbBounds = DragUtil.getDisplayBounds(getWorkbenchWindow().getFastViewBar().getControl());
            	RectangleAnimation animation = new RectangleAnimation(getWorkbenchWindow().getShell(), viewBounds, fvbBounds);
            	
                getWorkbenchWindow().getFastViewBar().adoptView(getReference(), -1, true, false);
                
                animation.schedule();
            } else {
                getWorkbenchWindow().getFastViewBar().restoreView(getReference(), true);
            }
        }
    }
}
