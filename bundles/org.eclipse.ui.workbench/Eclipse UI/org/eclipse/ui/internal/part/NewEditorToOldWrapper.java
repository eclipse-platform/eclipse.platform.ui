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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.Components;
import org.eclipse.ui.internal.components.framework.FactoryMap;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.part.components.services.IPartActionBars;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;
import org.eclipse.ui.internal.part.components.services.IWorkbenchPartFactory;
import org.eclipse.ui.internal.part.services.NullEditorInput;
import org.eclipse.ui.internal.part.services.NullPartActionBars;

/**
 * Wraps a new-style Part in an IEditorPart. The wrapper creates and manages 
 * the lifecycle of the Part. If you are interested in adapting an existing
 * part, use <code>NewPartToOldAdapter</code> instead.
 * 
 * @since 3.1
 */
public class NewEditorToOldWrapper extends NewPartToOldWrapper implements
        IEditorPart {

    private IAdaptable additionalServices = new IAdaptable() {
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        public Object getAdapter(Class adapter) {
            if (adapter == IEditorInput.class) {
                return getPropertyProvider().getEditorInput();
            }
            if (adapter == IActionBars.class) {
                return getEditorSite().getActionBars();
            }
            return null;
        }  
    };

    public NewEditorToOldWrapper(IPartDescriptor descriptor) {
        super(new PartPropertyProvider(null, null, null, descriptor, new NullEditorInput()));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.compatibility.NewPartToOldAdapter#getMemento()
     */
    protected IMemento getMemento() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.compatibility.NewPartToOldAdapter#createPart(org.eclipse.swt.widgets.Composite, org.eclipse.core.component.IContainerContext)
     */
    protected Part createPart(Composite parent, ServiceFactory args) throws ComponentException {
        IWorkbenchPartFactory factory = getFactory();
        return factory.createEditor(getSite().getId(), parent, getPropertyProvider().getEditorInput(), 
                getMemento(), args);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.compatibility.NewPartToOldAdapter#getSecondaryId()
     */
    protected String getSecondaryId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorPart#getEditorSite()
     */
    public IEditorSite getEditorSite() {
        return (IEditorSite)getSite();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.compatibility.NewPartToOldAdapter#addServices(org.eclipse.core.component.ContainerContext)
     */
    protected void addServices(FactoryMap context) {
        super.addServices(context);
        
        context.addInstance(additionalServices);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) {
        ((PartPropertyProvider)getPropertyProvider()).setEditorInput(input);
        setSite(site);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
    	ISaveablePart saveablePart = (ISaveablePart) Components.getAdapter(getPart(), ISaveablePart.class);
    	if(saveablePart!=null) {
    		saveablePart.doSave(monitor);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    public void doSaveAs() {
    	ISaveablePart saveablePart = (ISaveablePart) Components.getAdapter(getPart(), ISaveablePart.class);
    	if(saveablePart!=null) {
    		saveablePart.doSaveAs();
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
    	ISaveablePart saveablePart = (ISaveablePart) Components.getAdapter(getPart(), ISaveablePart.class);
    	if(saveablePart!=null) {
    		return saveablePart.isSaveAsAllowed();
    	}
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
     */
    public boolean isSaveOnCloseNeeded() {
    	ISaveablePart saveablePart = (ISaveablePart) Components.getAdapter(getPart(), ISaveablePart.class);
    	if(saveablePart!=null) {
    		return saveablePart.isSaveOnCloseNeeded();
    	}
        return false;
    }

    protected IPartActionBars createPartActionBars() {
        return new NullPartActionBars();
    }
    
    protected IStatusLineManager getStatusLineManager() {
        return getEditorSite().getActionBars().getStatusLineManager();
    }
}
