/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Show a View.
 */
public class ShowViewAction extends Action implements IPluginContribution {
    private IWorkbenchWindow window;

    private IViewDescriptor desc;
    private boolean makeFast = false;
    
    
    /**
     * ShowViewAction constructor comment.
     */
    protected ShowViewAction(IWorkbenchWindow window, IViewDescriptor desc, boolean makeFast) {
        super(""); //$NON-NLS-1$
        
        // TODO: is this wart still needed? 
        String accel = desc instanceof ViewDescriptor ? ((ViewDescriptor) desc)
				.getAccelerator() : null;
        String label = desc.getLabel();
        setText(accel == null ? label : label + "@" + accel); //$NON-NLS-1$
        setImageDescriptor(desc.getImageDescriptor());
        setToolTipText(label);
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.SHOW_VIEW_ACTION);
        this.window = window;
        this.desc = desc;
        this.makeFast = makeFast;
    }

    /**
     * Implementation of method defined on <code>IAction</code>.
     */
    public void run() {
        IWorkbenchPage page = window.getActivePage();
        if (page != null) {
            try {
                if (makeFast) {
                    WorkbenchPage wp = (WorkbenchPage) page;
                	Perspective persp = wp.getActivePerspective();

                    // If we're making a fast view then use the new mechanism directly
                    boolean useNewMinMax = Perspective.useNewMinMax(persp);
                    if (useNewMinMax) {
                    	IViewReference ref = persp.getViewReference(desc.getId(), null);
                    	if (ref == null)
                    		return;

                    	persp.getFastViewManager().addViewReference(FastViewBar.FASTVIEWBAR_ID, -1, ref, true);
                		wp.activate(ref.getPart(true));
                		
                		return;
                    }
                    
                    IViewReference ref = wp.findViewReference(desc.getId());
                    
                    if (ref == null) {
                        IViewPart part = page.showView(desc.getId(), null, IWorkbenchPage.VIEW_CREATE);
                        ref = (IViewReference)wp.getReference(part); 
                    }
                    
                    if (!wp.isFastView(ref) && persp != null) {
                   		persp.getFastViewManager().addViewReference(FastViewBar.FASTVIEWBAR_ID, -1, ref, true);
                    }
                    wp.activate(ref.getPart(true));
                } else {
                    page.showView(desc.getId());
                }
            } catch (PartInitException e) {
                ErrorDialog.openError(window.getShell(), WorkbenchMessages.ShowView_errorTitle,
                        e.getMessage(), e.getStatus());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return desc.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.support.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return desc instanceof IPluginContribution ? ((IPluginContribution) desc)
                .getPluginId()
                : null;
    }
}
