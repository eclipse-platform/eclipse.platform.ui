/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dan Rubel (dan_rubel@instantiations.com) - accessor to get context menu ids
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.internal.progress.WorkbenchSiteProgressService;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * <code>PartSite</code> is the general implementation for an
 * <code>IWorkbenchPartSite</code>.  A site maintains the context for a part,
 * including the part, its pane, active contributions, selection provider, etc.
 * Together, these components make up the complete behavior for a
 * part as if it was implemented by one person.  
 *
 * The <code>PartSite</code> lifecycle is as follows ..
 *
 * <ol>
 * <li>a site is constructed </li>
 * <li>a part is constructed and stored in the part </li>
 * <li>the site calls part.init() </li>
 * <li>a pane is constructed and stored in the site </li>
 * <li>the action bars for a part are constructed and stored in the site </li>
 * <li>the pane is added to a presentation </li>
 * <li>the SWT widgets for the pane and part are created </li>
 * <li>the site is activated, causing the actions to become visible </li>
 * </ol>
 */
public class PartSite implements IWorkbenchPartSite {

    private IWorkbenchPartReference partReference;

    private IWorkbenchPart part;

    private IWorkbenchPage page;

    private PartPane pane;

    private IConfigurationElement configElement;

    private String extensionID;

    private String pluginID;

    private String extensionName;

    private ISelectionProvider selectionProvider;

    private SubActionBars actionBars;

    private KeyBindingService keyBindingService;

    private ArrayList menuExtenders;

    private WorkbenchSiteProgressService progressService;

    /**
     * EditorContainer constructor comment.
     */
    public PartSite(IWorkbenchPartReference ref, IWorkbenchPart part,
            IWorkbenchPage page) {
        this.partReference = ref;
        this.part = part;
        this.page = page;
        extensionID = "org.eclipse.ui.UnknownID"; //$NON-NLS-1$
        extensionName = "Unknown Name"; //$NON-NLS-1$
    }

    /**
     * Dispose the contributions.
     */
    public void dispose() {
        if (menuExtenders != null) {
            for (int i = 0; i < menuExtenders.size(); i++) {
                ((PopupMenuExtender) menuExtenders.get(i)).dispose();
            }
            menuExtenders = null;
        }

        if (keyBindingService != null)
            keyBindingService.dispose();

        if (progressService != null)
            progressService.dispose();

    }

    /**
     * Returns the action bars for the part.
     * If this part is a view then it has exclusive use of the action bars.
     * If this part is an editor then the action bars are shared among this editor and other editors of
     * the same type.
     */
    public IActionBars getActionBars() {
        return actionBars;
    }

    /**
     * Returns the configuration element for a part.
     */
    public IConfigurationElement getConfigurationElement() {
        return configElement;
    }

    /**
     * Returns the part registry extension ID.
     *
     * @return the registry extension ID
     */
    public String getId() {
        return extensionID;
    }

    /**
     * Returns the page containing this workbench site's part.
     *
     * @return the page containing this part
     */
    public IWorkbenchPage getPage() {
        return page;
    }

    /**
     * Gets the part pane.
     */
    public PartPane getPane() {
        return pane;
    }

    /**
     * Returns the part.
     */
    public IWorkbenchPart getPart() {
        return part;
    }

    /**
     * Returns the part reference.
     */
    public IWorkbenchPartReference getPartReference() {
        return partReference;
    }

    /**
     * Returns the part registry plugin ID.  It cannot be <code>null</code>.
     *
     * @return the registry plugin ID
     */
    public String getPluginId() {
        return pluginID;
    }

    /**
     * Returns the registered name for this part.
     */
    public String getRegisteredName() {
        return extensionName;
    }

    /**
     * Returns the selection provider for a part.
     */
    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    /**
     * Returns the shell containing this part.
     *
     * @return the shell containing this part
     */
    public Shell getShell() {
        return page.getWorkbenchWindow().getShell();
    }

    /**
     * Returns the workbench window containing this part.
     *
     * @return the workbench window containing this part
     */
    public IWorkbenchWindow getWorkbenchWindow() {
        return page.getWorkbenchWindow();
    }

    /**
     * Register a popup menu for extension.
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

    /**
     * Register a popup menu with the default id for extension.
     */
    public void registerContextMenu(MenuManager menuMgr,
            ISelectionProvider selProvider) {
        registerContextMenu(getId(), menuMgr, selProvider);
    }

    // getContextMenuIds() added by Dan Rubel (dan_rubel@instantiations.com)
    /**
     * Get the registered popup menu identifiers
     */
    public String[] getContextMenuIds() {
        if (menuExtenders == null)
            return new String[0];
        String[] menuIds = new String[menuExtenders.size()];
        int index = 0;
        for (Iterator iter = menuExtenders.iterator(); iter.hasNext();) {
            final PopupMenuExtender extender = (PopupMenuExtender) iter.next();
            final Set extenderMenuIds = extender.getMenuIds();
            final Iterator menuIdItr = extenderMenuIds.iterator();
            while (menuIdItr.hasNext()) {
                menuIds[index++] = (String) menuIdItr.next();
            }
        }
        return menuIds;
    }

    /**
     * Sets the action bars for the part.
     */
    public void setActionBars(SubActionBars bars) {
        actionBars = bars;
    }

    /**
     * Sets the configuration element for a part.
     */
    public void setConfigurationElement(IConfigurationElement configElement) {
        // Save for external use.
        this.configElement = configElement;

        // Get extension ID.
        extensionID = configElement.getAttribute("id"); //$NON-NLS-1$

        // Get plugin ID.
        pluginID = configElement.getDeclaringExtension().getNamespace();

        // Get extension name.
        String name = configElement.getAttribute("name"); //$NON-NLS-1$
        if (name != null)
            extensionName = name;
    }

    /**
     * Sets the part registry extension ID.
     * 
     * @param id the registry extension ID
     */
    protected void setId(String id) {
        extensionID = id;
    }

    /**
     * Sets the part pane.
     */
    public void setPane(PartPane pane) {
        this.pane = pane;
    }

    /**
     * Sets the part.
     */
    public void setPart(IWorkbenchPart newPart) {
        part = newPart;
    }

    /**
     * Sets the registered name for this part.
     * 
     * @param name the registered name
     */
    protected void setRegisteredName(String name) {
        extensionName = name;
    }

    /**
     * Set the selection provider for a part.
     */
    public void setSelectionProvider(ISelectionProvider provider) {
        selectionProvider = provider;
    }

    /**
     * @see IWorkbenchPartSite#getKeyBindingService()
     */
    public IKeyBindingService getKeyBindingService() {
        if (keyBindingService == null) {
            keyBindingService = new KeyBindingService(this);

            // TODO why is this here? and it should be using HandlerSubmissions directly..			
            if (this instanceof EditorSite) {
                EditorActionBuilder.ExternalContributor contributor = (EditorActionBuilder.ExternalContributor) ((EditorSite) this)
                        .getExtensionActionBarContributor();

                if (contributor != null) {
                    ActionDescriptor[] actionDescriptors = contributor
                            .getExtendedActions();

                    if (actionDescriptors != null) {
                        for (int i = 0; i < actionDescriptors.length; i++) {
                            ActionDescriptor actionDescriptor = actionDescriptors[i];

                            if (actionDescriptor != null) {
                                IAction action = actionDescriptors[i]
                                        .getAction();

                                if (action != null
                                        && action.getActionDefinitionId() != null)
                                    keyBindingService.registerAction(action);
                            }
                        }
                    }
                }
            }
        }

        return keyBindingService;
    }

    protected String getInitialScopeId() {
        return null;
    }

    /**
     * Get an adapter for this type.
     * @param adapter
     * @return
     */
    public Object getAdapter(Class adapter) {
        if (IWorkbenchSiteProgressService.class.equals(adapter))
            return getSiteProgressService();
        else
            return null;
    }

    /**
     * Get a progress service for the receiver.
     * @return WorkbenchSiteProgressService
     */
    private WorkbenchSiteProgressService getSiteProgressService() {
        if (progressService == null)
            progressService = new WorkbenchSiteProgressService(this);
        return progressService;
    }
}