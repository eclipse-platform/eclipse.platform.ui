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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.Components;
import org.eclipse.ui.internal.components.framework.FactoryMap;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.part.components.services.IPartActionBars;
import org.eclipse.ui.internal.part.components.services.ISecondaryId;
import org.eclipse.ui.internal.part.components.services.ISelectionHandler;
import org.eclipse.ui.internal.part.components.services.IStatusHandler;
import org.eclipse.ui.internal.part.components.services.IWorkbenchPartFactory;

/**
 * Wraps a new-style Part in an IWorkbenchPart. This object will create and manage
 * the lifecycle of the part. Subclasses will support wrapping a Part in an
 * IViewPart and IEditorPart respectively. If you are interested in adapting
 * an existing Part rather than wrapping a new one, use <code>NewPartToOldAdapter</code>
 * instead.
 * 
 * @since 3.1
 */
abstract class NewPartToOldWrapper extends NewPartToWorkbenchPartAdapter {

    private Part part = null;
    private SelectionProviderAdapter selectionProvider;

    private PartServices services;
    private IWorkbenchPartSite partSite;
    private IPartActionBars partActionBars = null;
    private ResourceManager rm;
    private ImageDescriptor currentStatusImageDescriptor = null;
    
    private final class PartServices implements ISecondaryId, IAdaptable, ISelectionHandler, IStatusHandler {
       		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.workbench.services.ISecondaryId#getSecondaryId()
		 */
		public String getSecondaryId() {
			return NewPartToOldWrapper.this.getSecondaryId();
		}
        
        public void set(IStatus message, ImageDescriptor image) {
            setStatus(message, image);
        }
        
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
            
            if (adapter == IPartActionBars.class) {
                return getPartActionBars();
            }
            
//		    return getViewSite().getAdapter(adapter);
//			if (adapter == IActionBars.class) {
//				return getViewSite().getActionBars();
//			} else 
            if (adapter == IKeyBindingService.class) {
				return getSite().getKeyBindingService();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.part.components.interfaces.ISelectable#setSelection(org.eclipse.jface.viewers.ISelection)
		 */
		public void setSelection(ISelection newSelection) {
            ISelectionProvider newSelectionProvider = null;
            
            if (newSelection != null) {
                //getSelectionProvider().setSelection(newSelection);
                newSelectionProvider = getSelectionProvider();
                newSelectionProvider.setSelection(newSelection);
            }
            
            if (getSite().getSelectionProvider() != newSelectionProvider) {
                getSite().setSelectionProvider(newSelectionProvider); 
            }
		}

    }

    public NewPartToOldWrapper(IPartPropertyProvider provider) {
        super(provider);
        
        this.services = new PartServices();
    }
    
    public void setStatus(IStatus message, ImageDescriptor image) {
        IStatusLineManager mgr = getStatusLineManager();
        
        Image newImage = null;
        
        if (image != null) {
            newImage = getResources().createImageWithDefault(image);
        }
        
        if (message == null) {
            mgr.setErrorMessage(null);
            mgr.setMessage(null);
        } else if (message.getSeverity() == IStatus.ERROR){
            mgr.setMessage(null);
            
            if (newImage == null) {
                mgr.setErrorMessage(message.getMessage());
            } else {
                mgr.setErrorMessage(newImage, message.getMessage());
            }
        } else {
            mgr.setErrorMessage(null);
            
            if (newImage == null) {
                mgr.setMessage(message.getMessage());
            } else {
                mgr.setMessage(newImage, message.getMessage());
            }
        }
        
        disposeStatusImage();
        currentStatusImageDescriptor = image;
        
    }
    
    private void disposeStatusImage() {
        if (currentStatusImageDescriptor != null) {
            getResources().destroy(currentStatusImageDescriptor);
            currentStatusImageDescriptor = null;
        }
    }

    protected abstract IStatusLineManager getStatusLineManager();

    public IPartActionBars getPartActionBars() {
    	if (partActionBars == null) {
    		partActionBars = (IPartActionBars) getSite().getAdapter(IPartActionBars.class);
    		if(partActionBars==null) {
    			partActionBars = createPartActionBars();
    		}
        }
        return partActionBars ;
    }

    protected abstract IPartActionBars createPartActionBars();

    protected abstract IMemento getMemento();

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        try {
            
            
        	FactoryMap args = new FactoryMap();
            args.addInstance(services);
            args.addInstance(getPropertyProvider());
            addServices(args);
            args.mapInstance(Composite.class, parent);
            parent.setLayout(new FillLayout());
        	
            part = createPart(parent, args); 
            //parent.layout(true);
        } catch (ComponentException e) {
            WorkbenchPlugin.getDefault().getLog().log(e.getStatus());
            new StatusPart(parent, e.getStatus());
        }
    }

    private ResourceManager getResources() {
        if (rm == null) {
            rm = (ResourceManager)getSite().getAdapter(ResourceManager.class);
        }
        return rm;
    }
    
    protected void addServices(FactoryMap context) {
        context.mapInstance(IPartPropertyProvider.class, getPropertyProvider());
    }
  
    /**
     * @since 3.1 
     *
     * @param args
     * @return
     */
    protected abstract Part createPart(Composite parent, ServiceFactory args) throws ComponentException;

    /**
     * @since 3.1 
     *
     * @return
     */
    protected abstract String getSecondaryId();

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
    	if (part != null) {
    		part.getControl().setFocus();
    	}
    }
    
    private SelectionProviderAdapter getSelectionProvider() {
    	if (selectionProvider == null) {
    		selectionProvider = new SelectionProviderAdapter();
            getSite().setSelectionProvider(selectionProvider);
    	}
    	return selectionProvider;
    }
        
    protected Part getPart() {
        return part;
    }
    
    protected IWorkbenchPartFactory getFactory() {
        // Try to be well-behaved and get the factory from our site
        IWorkbenchPartFactory siteFactory = (IWorkbenchPartFactory) getSite().getAdapter(IWorkbenchPartFactory.class);
        
        // If the site doesn't want to play nicely, reach to the workbench page
        if (siteFactory == null) {
            return ((WorkbenchPage)getSite().getPage()).getPartFactory();
        }
        
        return siteFactory;
    }

    /**
     * Sets the part site.
     * <p>
     * Subclasses must invoke this method from <code>IEditorPart.init</code>
     * and <code>IViewPart.init</code>.
     *
     * @param site the workbench part site
     */
    protected void setSite(IWorkbenchPartSite site) {
        this.partSite = site;
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getSite()
     */
    public IWorkbenchPartSite getSite() {
        return partSite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter.isInstance(this)) {
            return this;
        }
        
        if (part != null) {
            return Components.getAdapter(part, adapter);
        }
        
        return null;
    }
    
    public void dispose() {
        disposeStatusImage();
        
        super.dispose();
    }
    
}


