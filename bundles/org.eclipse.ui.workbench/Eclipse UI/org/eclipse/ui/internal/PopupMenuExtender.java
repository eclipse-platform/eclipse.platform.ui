/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dan Rubel (dan_rubel@instantiations.com) - accessor to get menu id
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * This class extends a single popup menu
 */
public class PopupMenuExtender implements IMenuListener, IRegistryChangeListener {

    private final String menuID;

    private final MenuManager menu;

    private SubMenuManager menuWrapper;

    private final ISelectionProvider selProvider;

    private final IWorkbenchPart part;

    private ViewerActionBuilder staticActionBuilder;

	private boolean staticActionsRead;

    /**
     * Construct a new menu extender.
     * 
     * @param id the menu id
     * @param menu the menu to extend
     * @param prov the selection provider
     * @param part the part to extend
     */
    public PopupMenuExtender(String id, MenuManager menu,
            ISelectionProvider prov, IWorkbenchPart part) {
        super();
        this.menuID = id;
        this.menu = menu;
        this.selProvider = prov;
        this.part = part;
        menu.addMenuListener(this);
        if (!menu.getRemoveAllWhenShown()) {
            menuWrapper = new SubMenuManager(menu);
            menuWrapper.setVisible(true);
        }
        readStaticActions();
        Platform.getExtensionRegistry().addRegistryChangeListener(this);
    }

    // getMenuId() added by Dan Rubel (dan_rubel@instantiations.com)
    /**
     * Return the menu identifier
     */
    public String getMenuId() {
        return menuID;
    }

    /**
     * Contributes items registered for the object type(s) in
     * the current selection.
     */
    private void addObjectActions(IMenuManager mgr) {
        if (selProvider != null) {
            if (ObjectActionContributorManager.getManager()
                    .contributeObjectActions(part, mgr, selProvider)) {
                mgr.add(new Separator());
            }
        }
    }

    /**
     * Adds static items to the context menu.
     */
    private void addStaticActions(IMenuManager mgr) {
        if (staticActionBuilder != null)
            staticActionBuilder.contribute(mgr, null, true);
    }

    /**
     * Notifies the listener that the menu is about to be shown.
     */
    public void menuAboutToShow(IMenuManager mgr) {
    	readStaticActions();
        testForAdditions();
        if (menuWrapper != null) {
            mgr = menuWrapper;
            menuWrapper.removeAll();
        }
        addObjectActions(mgr);
        addStaticActions(mgr);
    }

    /**
     * Read static items for the context menu.
     */
    private void readStaticActions() {
        // If no menu id provided, then there is no contributions
        // to add. Fix for bug #33140.
        if (menuID != null && menuID.length() > 0) {
            staticActionBuilder = new ViewerActionBuilder();
            if (!staticActionBuilder.readViewerContributions(menuID,
                    selProvider, part))
                staticActionBuilder = null;
        }
    }

    /**
     * Checks for the existance of an MB_ADDITIONS group.
     */
    private void testForAdditions() {
        IContributionItem item = menu
                .find(IWorkbenchActionConstants.MB_ADDITIONS);
        if (item == null) {
            WorkbenchPlugin
                    .log("Context menu missing standard group 'org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS'. (menu id = " //$NON-NLS-1$
                            + (menuID == null ? "???" : menuID) + ")  part id = " //$NON-NLS-1$ //$NON-NLS-2$
                            + (part == null ? "???" : part.getSite().getId()) //$NON-NLS-1$
                            + ")"); //$NON-NLS-1$
        }
    }

    /**
     * Dispose of the menu extender. Should only be called when the part
     * is disposed.
     */
    public void dispose() {
        if (staticActionBuilder != null) {
            staticActionBuilder.dispose();
        }
        Platform.getExtensionRegistry().removeRegistryChangeListener(this);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	public void registryChanged(final IRegistryChangeEvent event) {
		Display display = Display.getDefault();
		if (part != null) {
			display = part.getSite().getPage().getWorkbenchWindow().getWorkbench().getDisplay();
		}
		//check the delta to see if there are any viewer contribution changes.  if so, null our builder to cause reparsing on the next menu show
		IExtensionDelta [] deltas = event.getExtensionDeltas();
		for (int i = 0; i < deltas.length; i++) {
			IExtensionDelta delta = deltas[i];
			IExtensionPoint extensionPoint = delta.getExtensionPoint();
			if (extensionPoint.getNamespace().equals(
					WorkbenchPlugin.PI_WORKBENCH)
					&& extensionPoint.getSimpleIdentifier().equals(
							IWorkbenchConstants.PL_POPUP_MENU)) {

				boolean clearPopups = false;
				IConfigurationElement [] elements = delta.getExtension().getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					IConfigurationElement element = elements[j];
					if (element.getName().equals(IWorkbenchRegistryConstants.TAG_CONTRIBUTION_TYPE)) {
						clearPopups = true;
						break;
					}					
				}
										
				if (clearPopups) {
					display.syncExec(new Runnable() {
						public void run() {
							staticActionsRead = false;
							if (staticActionBuilder != null) {
								staticActionBuilder.dispose();
								staticActionBuilder = null;
							}
						}
					});
				}
			}
		}
	}
}
