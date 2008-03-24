/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.menus.InternalMenuService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;

/**
 * This class extends a single popup menu
 */
public class PopupMenuExtender implements IMenuListener2,
		IRegistryChangeListener {
    
    /**
     * The bit in <code>bitSet</code> that stores whether the static actions
     * have been read from the registry.
     */
    private static final int STATIC_ACTION_READ = 1;

    /**
     * The bit in <code>bitSet</code> that stores whether the editor input
     * should be included for the sake of object contributions.
     */
    private static final int INCLUDE_EDITOR_INPUT = 1 << 1;

    private final MenuManager menu;

    private SubMenuManager menuWrapper;

    private final ISelectionProvider selProvider;

    private final IWorkbenchPart part;

    private Map staticActionBuilders = null;

    /**
     * The boolean properties maintained by this extender. A bit set is used to
     * save memory.
     */
	private int bitSet = 0;
	
	private ArrayList contributionCache = new ArrayList();

    /**
     * Construct a new menu extender.
     * 
     * @param id
     *            the menu id
     * @param menu
     *            the menu to extend
     * @param prov
     *            the selection provider
     * @param part
     *            the part to extend
     */
    public PopupMenuExtender(String id, MenuManager menu,
            ISelectionProvider prov, IWorkbenchPart part) {
        this(id, menu, prov, part, true);
    }

    /**
     * Construct a new menu extender.
     * 
     * @param id
     *            the menu id
     * @param menu
     *            the menu to extend
     * @param prov
     *            the selection provider
     * @param part
     *            the part to extend
     * @param includeEditorInput
     *            Whether the editor input should be included when adding object
     *            contributions to this context menu.
     */
    public PopupMenuExtender(final String id, final MenuManager menu,
            final ISelectionProvider prov, final IWorkbenchPart part,
            final boolean includeEditorInput) {
		super();
		this.menu = menu;
		this.selProvider = prov;
		this.part = part;
		if (includeEditorInput) {
			bitSet |= INCLUDE_EDITOR_INPUT;
		}
		menu.addMenuListener(this);
		if (!menu.getRemoveAllWhenShown()) {
			menuWrapper = new SubMenuManager(menu);
			menuWrapper.setVisible(true);
		}
		readStaticActionsFor(id);
				
		Platform.getExtensionRegistry().addRegistryChangeListener(this);
	}

	// getMenuId() added by Dan Rubel (dan_rubel@instantiations.com)
    /**
     * Return the menu identifiers for this extender.
     * 
     * @return The set of all identifiers that represent this extender.
     */
    public Set getMenuIds() {
    	if (staticActionBuilders == null) {
    		return Collections.EMPTY_SET;
    	}
    	
        return staticActionBuilders.keySet();
    }

    /**
     * <p>
     * Adds another menu identifier to this extender. An extender can represent
     * many menu identifiers. These identifiers should represent the same menu
     * manager, selection provider and part. Duplicate identifiers are
     * automatically ignored.
     * </p>
     * <p>
     * For example, it is necessary to filter out duplicate identifiers for
     * <code>CompilationUnitEditor</code> instances, as these define both
     * <code>"#CompilationUnitEditorContext"</code> and
     * <code>"org.eclipse.jdt.ui.CompilationUnitEditor.EditorContext"</code>
     * as menu identifier for the same pop-up menu. We don't want to contribute
     * duplicate items in this case.
     * </p>
     * 
     * @param menuId
     *            The menu identifier to add to this extender; should not be
     *            <code>null</code>.
     */
    public final void addMenuId(final String menuId) {
		bitSet &= ~STATIC_ACTION_READ;
		readStaticActionsFor(menuId);
	}

    /**
     * Determines whether this extender would be the same as another extender
     * created with the given values. Two extenders are equivalent if they have
     * the same menu manager, selection provider and part (i.e., if the menu
     * they represent is about to show, they would populate it with duplicate
     * values).
     * 
     * @param menuManager
     *            The menu manager with which to compare; may be
     *            <code>null</code>.
     * @param selectionProvider
     *            The selection provider with which to compare; may be
     *            <code>null</code>.
     * @param part
     *            The part with which to compare; may be <code>null</code>.
     * @return <code>true</code> if the menu manager, selection provider and
     *         part are all the same.
     */
    public final boolean matches(final MenuManager menuManager,
            final ISelectionProvider selectionProvider,
            final IWorkbenchPart part) {
        return (this.menu == menuManager)
                && (this.selProvider == selectionProvider)
                && (this.part == part);
    }

    /**
     * Contributes items registered for the currently active editor.
     */
    private void addEditorActions(IMenuManager mgr) {
        ISelectionProvider activeEditor = new ISelectionProvider() {

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
             */
            public void addSelectionChangedListener(
                    ISelectionChangedListener listener) {
                throw new UnsupportedOperationException(
                "This ISelectionProvider is static, and cannot be modified."); //$NON-NLS-1$
            }

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
             */
            public ISelection getSelection() {
                if (part instanceof IEditorPart) {
                    final IEditorPart editorPart = (IEditorPart) part;
                    return new StructuredSelection(new Object[] { editorPart
                            .getEditorInput() });
                }

                return new StructuredSelection(new Object[0]);
            }

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
             */
            public void removeSelectionChangedListener(
                    ISelectionChangedListener listener) {
                throw new UnsupportedOperationException(
                "This ISelectionProvider is static, and cannot be modified."); //$NON-NLS-1$
            }

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
             */
            public void setSelection(ISelection selection) {
                throw new UnsupportedOperationException(
                        "This ISelectionProvider is static, and cannot be modified."); //$NON-NLS-1$
            }
        };
        
        if (ObjectActionContributorManager.getManager()
                .contributeObjectActions(part, mgr, activeEditor)) {
            mgr.add(new Separator());
        }
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
     * Disposes all of the static actions.
     */
    private final void clearStaticActions() {
		bitSet &= ~STATIC_ACTION_READ;
		if (staticActionBuilders != null) {
			final Iterator staticActionBuilderItr = staticActionBuilders
					.values().iterator();
			while (staticActionBuilderItr.hasNext()) {
				final Object staticActionBuilder = staticActionBuilderItr
						.next();
				if (staticActionBuilder instanceof ViewerActionBuilder) {
					((ViewerActionBuilder) staticActionBuilder).dispose();
				}
			}
		}
	}

    /**
     * Adds static items to the context menu.
     */
    private void addStaticActions(IMenuManager mgr) {
		if (staticActionBuilders != null) {
			final Iterator staticActionBuilderItr = staticActionBuilders
					.values().iterator();
			while (staticActionBuilderItr.hasNext()) {
				final ViewerActionBuilder staticActionBuilder = (ViewerActionBuilder) staticActionBuilderItr
						.next();
				staticActionBuilder.contribute(mgr, null, true);
			}
		}
	}

    /**
     * Notifies the listener that the menu is about to be shown.
     */
    public void menuAboutToShow(IMenuManager mgr) {
    	IMenuManager originalManager = mgr;
    	
    	// Add this menu as a visible menu.
    	final IWorkbenchPartSite site = part.getSite();
    	if (site != null) {
			final IWorkbench workbench = site.getWorkbenchWindow()
					.getWorkbench();
			if (workbench instanceof Workbench) {
				final Workbench realWorkbench = (Workbench) workbench;
				ISelection input = null;
				if ((bitSet & INCLUDE_EDITOR_INPUT) != 0) {
					if (part instanceof IEditorPart) {
						final IEditorPart editorPart = (IEditorPart) part;
						input = new StructuredSelection(
								new Object[] { editorPart.getEditorInput() });
					}
				}
				ISelection s = (selProvider == null ? null : selProvider
						.getSelection());
				realWorkbench.addShowingMenus(getMenuIds(), s, input);
			}
		}
    	
    	readStaticActions();
        // test for additions removed to comply with menu contributions
        if (menuWrapper != null) {
            mgr = menuWrapper;
            menuWrapper.removeAll();
        }
        addMenuContributions(originalManager);
        if ((bitSet & INCLUDE_EDITOR_INPUT) != 0) {
            addEditorActions(mgr);
        }
        addObjectActions(mgr);
        addStaticActions(mgr);
        cleanUpContributionCache();
    }
    
    private boolean contributionsPopulated = false;
    
    private void addMenuContributions(IMenuManager mgr) {
		final IMenuService menuService = (IMenuService) part.getSite()
				.getService(IMenuService.class);
		if (menuService == null) {
			return;
		}
		if ((mgr.getRemoveAllWhenShown() || !contributionsPopulated)
				&& mgr instanceof ContributionManager) {
			ContributionManager manager = (ContributionManager) mgr;
			contributionsPopulated = true;
			menuService
					.populateContributionManager(manager, MenuUtil.ANY_POPUP);
			Iterator i = getMenuIds().iterator();
			InternalMenuService realService = (InternalMenuService) menuService;
			while (i.hasNext()) {
				String id = "popup:" + i.next(); //$NON-NLS-1$
				realService.populateContributionManager(manager, id, false);
			}
		}
	}

    /**
	 * Notifies the listener that the menu is about to be hidden.
	 */
    public final void menuAboutToHide(final IMenuManager mgr) {
    	gatherContributions(mgr);
    	// Remove this menu as a visible menu.
    	final IWorkbenchPartSite site = part.getSite();
    	if (site != null) {
    		final IWorkbench workbench = site.getWorkbenchWindow().getWorkbench();
    		if (workbench instanceof Workbench) {
    			// try delaying this until after the selection event
    			// has been fired.
    			// This is less threatening if the popup: menu
    			// contributions aren't tied to the evaluation service
				workbench.getDisplay().asyncExec(new Runnable() {
					public void run() {
						final Workbench realWorkbench = (Workbench) workbench;
						realWorkbench.removeShowingMenus(getMenuIds(), null, null);
					}
				});
			}
    	}
    }
    

    /**
	 * @param mgr
	 */
	private void gatherContributions(final IMenuManager mgr) {
		final IContributionItem[] items = mgr.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof PluginActionContributionItem) {
				contributionCache.add(items[i]);
			} else if (items[i] instanceof IMenuManager) {
				gatherContributions(((IMenuManager)items[i]));
			}
		}
	}
	
	private void cleanUpContributionCache() {
		PluginActionContributionItem[] items = (PluginActionContributionItem[]) contributionCache
				.toArray(new PluginActionContributionItem[contributionCache.size()]);
		contributionCache.clear();
		for (int i = 0; i < items.length; i++) {
			items[i].dispose();
		}
	}

	/**
     * Read all of the static items for the content menu.
     */
    private final void readStaticActions() {
    	if (staticActionBuilders != null) {
			final Iterator menuIdItr = staticActionBuilders.keySet().iterator();
			while (menuIdItr.hasNext()) {
				final String menuId = (String) menuIdItr.next();
				readStaticActionsFor(menuId);
			}
		}
    }

    /**
	 * Read static items for a particular menu id, into the context menu.
	 */
    private void readStaticActionsFor(final String menuId) {
		if ((bitSet & STATIC_ACTION_READ) != 0) {
			return;
		}

		bitSet |= STATIC_ACTION_READ;

		// If no menu id provided, then there is no contributions
		// to add. Fix for bug #33140.
		if ((menuId == null) || (menuId.length() < 1)) {
			return;
		}

		if (staticActionBuilders == null) {
			staticActionBuilders = new HashMap();
		}

		Object object = staticActionBuilders.get(menuId);
		if (!(object instanceof ViewerActionBuilder)) {
			object = new ViewerActionBuilder();
			staticActionBuilders.put(menuId, object);
		}
		final ViewerActionBuilder staticActionBuilder = (ViewerActionBuilder) object;
		staticActionBuilder.readViewerContributions(menuId, selProvider, part);
	}

    /**
     * Dispose of the menu extender. Should only be called when the part
     * is disposed.
     */
    public void dispose() {
		clearStaticActions();
		final IMenuService menuService = (IMenuService) part.getSite()
				.getService(IMenuService.class);
		if (menuService != null) {
			menuService.releaseContributions(menu);
		}
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
		menu.removeMenuListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
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
							IWorkbenchRegistryConstants.PL_POPUP_MENU)) {

				boolean clearPopups = false;
				IConfigurationElement [] elements = delta.getExtension().getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					IConfigurationElement element = elements[j];
					if (element.getName().equals(IWorkbenchRegistryConstants.TAG_VIEWER_CONTRIBUTION)) {
						clearPopups = true;
						break;
					}					
				}
										
				if (clearPopups) {
					display.syncExec(new Runnable() {
						public void run() {
							clearStaticActions();
						}
					});
				}
			}
		}
	}
	
	public MenuManager getManager() {
		return menu;
	}
}
