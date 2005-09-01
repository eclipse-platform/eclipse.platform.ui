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

import java.util.ArrayList;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.PartSite;

/**
 * @since 3.1
 */
public class CompatibilityPartSite implements IViewSite, IEditorSite {
    private ArrayList menuExtenders;
    private IWorkbenchPart part;
    private ISelectionProvider selectionProvider;
    private IActionBars actionBars;
    private IEditorActionBarContributor actionBarContributor;
    private StandardWorkbenchServices services;
    
    private ISelectionChangedListener selectionChangeListener = new ISelectionChangedListener() {
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
         */
        public void selectionChanged(SelectionChangedEvent event) {
            services.getSelectionHandler().setSelection(event.getSelection());
        }
    };
	private IWorkbenchPartSite parentSite;

    /**
     * @param adapterProvider
     * @param pluginId
     * @param id
     * @param registeredName
     * @param part
     * @param page
     */
    public CompatibilityPartSite(StandardWorkbenchServices services, IWorkbenchPart part, 
            IEditorActionBarContributor actionBarContributor, IActionBars actionBars) {
        super();
        IPartHost partHost = (IPartHost) services.getAdapter(IPartHost.class);
        if(partHost!=null) {
        	this.parentSite = partHost.getSite();
        }
        this.services = services;
        this.part = part;
        this.actionBarContributor = actionBarContributor;
        this.actionBars = actionBars;
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#getId()
     */
    public String getId() {
        return services.getDescriptor().getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#getPluginId()
     */
    public String getPluginId() {
        return services.getPluginBundle().getSymbolicName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#getRegisteredName()
     */
    public String getRegisteredName() {
        return services.getDescriptor().getLabel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
     */
    public void registerContextMenu(MenuManager menuManager,
            ISelectionProvider selProvider) {
        registerContextMenu(getId(), menuManager, selProvider);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(java.lang.String, org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
     */
    public void registerContextMenu(String menuID, MenuManager menuMgr,
            ISelectionProvider selProvider) {
        if (menuExtenders == null) {
            menuExtenders = new ArrayList(1);
        }
        
        PartSite.registerContextMenu(menuID, menuMgr, selProvider, true, part,
                menuExtenders);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
     */
    public IKeyBindingService getKeyBindingService() {
        return services.getKeyBindingService();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchSite#getPage()
     */
    public IWorkbenchPage getPage() {
        return services.getPage();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchSite#getSelectionProvider()
     */
    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchSite#getShell()
     */
    public Shell getShell() {
        return getWorkbenchWindow().getShell();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchSite#getWorkbenchWindow()
     */
    public IWorkbenchWindow getWorkbenchWindow() {
        return getPage().getWorkbenchWindow();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchSite#setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)
     */
    public void setSelectionProvider(ISelectionProvider provider) {
        if (selectionProvider != null) {
            selectionProvider.removeSelectionChangedListener(selectionChangeListener);
        }
        
        selectionProvider = provider;
        
        if (selectionProvider != null) {
            selectionProvider.addSelectionChangedListener(selectionChangeListener);
            services.getSelectionHandler().setSelection(provider.getSelection());
        } else {
            provider.setSelection(null);
        }
         
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return services.getAdapter(adapter);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#getPart()
     */
    public IWorkbenchPart getPart() {
        return part;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorSite#getActionBarContributor()
     */
    public IEditorActionBarContributor getActionBarContributor() {
        return actionBarContributor;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewSite#getActionBars()
     */
    public IActionBars getActionBars() {
        return actionBars;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewSite#getSecondaryId()
     */
    public String getSecondaryId() {
        return services.getSecondaryId().getSecondaryId();
    }
    
    public final void registerContextMenu(final String menuId,
            final MenuManager menuManager,
            final ISelectionProvider selectionProvider,
            final boolean includeEditorInput) {
        if (menuExtenders == null) {
            menuExtenders = new ArrayList(1);
        }
        
        PartSite.registerContextMenu(menuId, menuManager, selectionProvider,
                includeEditorInput, part, menuExtenders);
    }
    
    public final void registerContextMenu(final MenuManager menuManager,
            final ISelectionProvider selectionProvider,
            final boolean includeEditorInput) {
        registerContextMenu(getId(), menuManager, selectionProvider,
                includeEditorInput);
    }
	public IWorkbenchPartSite getParentSite() {
		return parentSite;
	}
	public void dispose() {
	}
	public void activateActionBars(boolean enable) {
	}
	public void deactivateActionBars(boolean enable) {
	}
}
