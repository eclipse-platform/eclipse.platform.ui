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
package org.eclipse.ui.internal.part;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.part.services.NullActionBars;
import org.eclipse.ui.internal.part.services.PartToViewActionBarsAdapter;

/**
 * Can be used to convert an existing Part into an IEditorPart or IViewPart. The lifecycle
 * is managed by the original Part, so all of the lifecycle methods on this class do nothing.
 * 
 * @since 3.1
 */
public class NewPartToOldAdapter extends NewPartToWorkbenchPartAdapter implements IViewPart, IEditorPart {

    private CompatibilityPartSite site;
    
    /**
     * @param services
     * @param propertyProvider
     * @param isView determines whether we want this to behave like an IViewPart or an IEditorPart (their
     * IActionBars behave differently)
     * @throws ComponentException
     */
    public NewPartToOldAdapter(
            StandardWorkbenchServices services,
            IPartPropertyProvider propertyProvider,
            boolean isView) throws ComponentException {
        super(propertyProvider);
        
        IActionBars actionBars;
        
        if (isView) { 
            actionBars = new PartToViewActionBarsAdapter(services.getActionBars(), 
                    services.getStatusHandler(), services.getStatusFactory());
        } else {
            actionBars = new NullActionBars(); 
        }
        
        this.site = new CompatibilityPartSite(
                services, this, null, actionBars);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getSite()
     */
    public IWorkbenchPartSite getSite() {
        return site;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IViewPart.class || adapter == IEditorPart.class) {
            return this;
        }
        
        return site.getAdapter(adapter);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#getViewSite()
     */
    public IViewSite getViewSite() {
        return site;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException {
        // Lifecycle methods won't be called -- this object adapts an existing Part
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        // Lifecycle methods won't be called -- this object adapts an existing Part
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        // Lifecycle methods won't be called -- this object adapts an existing Part
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorPart#getEditorSite()
     */
    public IEditorSite getEditorSite() {
        return site;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        // Lifecycle methods won't be called -- this object adapts an existing Part
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    public void doSaveAs() {
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
     */
    public boolean isSaveOnCloseNeeded() {
        return false;
    }

}
