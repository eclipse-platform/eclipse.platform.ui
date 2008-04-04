/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * The primary interface between a workbench part and the workbench.
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchPartSite extends IWorkbenchSite {

    /**
     * Returns the part registry extension id for this workbench site's part.
     * <p>
     * The name comes from the <code>id</code> attribute in the configuration
     * element.
     * </p>
     *
     * @return the registry extension id
     */
    public String getId();

    /**
     * Returns the unique identifier of the plug-in that defines this workbench
     * site's part.
     *
     * @return the unique identifier of the declaring plug-in
     */
    public String getPluginId();

    /**
     * Returns the registered name for this workbench site's part.
     * <p>
     * The name comes from the <code>name</code> attribute in the configuration
     * element.
     * </p>
     *
     * @return the part name
     */
    public String getRegisteredName();

    /**
     * Registers a pop-up menu with a particular id for extension.
     * This method should only be called if the target part has more
     * than one context menu to register.
     * <p>
     * For a detailed description of context menu registration see 
     * <code>registerContextMenu(MenuManager, ISelectionProvider);
     * </p>
     *
     * @param menuId the menu id
     * @param menuManager the menu manager
     * @param selectionProvider the selection provider
     */
    public void registerContextMenu(String menuId, MenuManager menuManager,
            ISelectionProvider selectionProvider);

    /**
     * Registers a pop-up menu with the default id for extension.  
     * The default id is defined as the part id.
     * <p>
     * Within the workbench one plug-in may extend the pop-up menus for a view
     * or editor within another plug-in.  In order to be eligible for extension,
     * the target part must publish each menu by calling <code>registerContextMenu</code>.
     * Once this has been done the workbench will automatically insert any action 
     * extensions which exist.
     * </p>
     * <p>
     * A menu id must be provided for each registered menu.  For consistency across
     * parts the following strategy should be adopted by all part implementors.
     * </p>
     * <ol>
     *		<li>If the target part has only one context menu it should be registered
     *			with <code>id == part id</code>.  This can be done easily by calling
     *			<code>registerContextMenu(MenuManager, ISelectionProvider).  
     *		<li>If the target part has more than one context menu a unique id should be
     *			defined for each.  Prefix each menu id with the part id and publish these
     *			ids within the javadoc for the target part.  Register each menu at 
     *			runtime by calling <code>registerContextMenu(String, MenuManager, 
     *			ISelectionProvider)</code>.  </li>
     * </ol>
     * <p>
     * Any pop-up menu which is registered with the workbench should also define a  
     * <code>GroupMarker</code> in the registered menu with id 
     * <code>IWorkbenchActionConstants.MB_ADDITIONS</code>.  Other plug-ins will use this 
     * group as a reference point for insertion.  The marker should be defined at an 
     * appropriate location within the menu for insertion.  
     * </p>
     *
     * @param menuManager the menu manager
     * @param selectionProvider the selection provider
     */
    public void registerContextMenu(MenuManager menuManager,
            ISelectionProvider selectionProvider);

    /**
	 * Returns the key binding service in use.
	 * <p>
	 * The part will access this service to register all of its actions, to set
	 * the active scope.
	 * </p>
	 * 
	 * @return the key binding service in use
	 * @since 2.1
	 * @deprecated Use {@link IServiceLocator#getService(Class)} instead.
	 * @see IContextService
	 * @see IHandlerService
	 */
    public IKeyBindingService getKeyBindingService();
    
    /**
     * Returns the part associated with this site 
     * 
     * @since 3.1 
     *
     * @return the part associated with this site
     */
    public IWorkbenchPart getPart(); 
}
