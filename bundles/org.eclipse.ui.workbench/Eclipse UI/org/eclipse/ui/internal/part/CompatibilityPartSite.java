/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import java.util.ArrayList;
import java.util.Iterator;

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
import org.eclipse.ui.internal.KeyBindingService;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.internal.part.services.NullActionBars;

/**
 * @since 3.1
 */
public class CompatibilityPartSite implements IWorkbenchPartSite, IViewSite, IEditorSite {
    private ArrayList menuExtenders;
    private IWorkbenchPart part;
    private IKeyBindingService keyBindingService;
    private ISelectionProvider selectionProvider;
    private IActionBars actionBars = new NullActionBars();
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

    /**
     * @param adapterProvider
     * @param pluginId
     * @param id
     * @param registeredName
     * @param part
     * @param page
     */
    public CompatibilityPartSite(StandardWorkbenchServices services, IWorkbenchPart part, IEditorActionBarContributor actionBarContributor) {
        super();
        this.services = services;
        this.part = part;
        this.actionBarContributor = actionBarContributor;
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
        /*
         * Check to see if the same menu manager and selection provider have
         * already been used. If they have, then we can just add another menu
         * identifier to the existing PopupMenuExtender.
         */
        final Iterator extenderItr = menuExtenders.iterator();
        boolean foundMatch = false;
        while (extenderItr.hasNext()) {
            final PopupMenuExtender existingExtender = (PopupMenuExtender) extenderItr
                    .next();
            if (existingExtender.matches(menuMgr, selProvider, part)) {
                existingExtender.addMenuId(menuID);
                foundMatch = true;
                break;
            }
        }

        if (!foundMatch) {
            menuExtenders.add(new PopupMenuExtender(menuID, menuMgr,
                    selProvider, part));
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
     */
    public IKeyBindingService getKeyBindingService() {
        if (keyBindingService == null) {
            keyBindingService = new KeyBindingService(this);
        }
        return keyBindingService;
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
}
