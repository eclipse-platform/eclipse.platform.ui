/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Label provider for the breakpoints view
 */
public class BreakpointsLabelProvider extends LabelProvider {

    private ILabelProvider fWorkbenchLabelProvider;
    private IDebugModelPresentation fPresentation;
    
    /**
     * Constructs a new label provide for the breakpoints view.
     */
    public BreakpointsLabelProvider() {
        fWorkbenchLabelProvider = new WorkbenchLabelProvider();
        fPresentation = DebugUITools.newDebugModelPresentation();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
        fWorkbenchLabelProvider.dispose();
        fPresentation.dispose();
        super.dispose();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        if (element instanceof IBreakpoint) {
            return fPresentation.getImage(element);
        }
        return fWorkbenchLabelProvider.getImage(element);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element instanceof IBreakpoint) {
            return fPresentation.getText(element);
        }
        return fWorkbenchLabelProvider.getText(element);
    }
}
