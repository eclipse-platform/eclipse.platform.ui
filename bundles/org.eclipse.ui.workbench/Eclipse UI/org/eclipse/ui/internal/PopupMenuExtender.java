/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dan Rubel (dan_rubel@instantiations.com) - accessor to get menu id
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This class extends a single popup menu
 */
public class PopupMenuExtender implements IMenuListener {

    //for dynamic UI
    public static final class PopupMenuExtenderManager {

        private Set extenders = new HashSet();

        public void clearCaches() {
            for (Iterator i = extenders.iterator(); i.hasNext();) {
                PopupMenuExtender extender = (PopupMenuExtender) i.next();
                extender.readStaticActions();
            }
        }

        private void addExtender(PopupMenuExtender extender) {
            extenders.add(extender);
        }

        private void removeExtender(PopupMenuExtender extender) {
            extenders.remove(extender);
        }
    }

    private static final PopupMenuExtenderManager manager = new PopupMenuExtenderManager();

    public static final PopupMenuExtenderManager getManager() {
        return manager;
    }

    private final Set menuIds = new HashSet();

    private final MenuManager menu;

    private SubMenuManager menuWrapper;

    private final ISelectionProvider selProvider;

    private final IWorkbenchPart part;

    private ViewerActionBuilder staticActionBuilder;

    /**
     * Construct a new menu extender.
     */
    public PopupMenuExtender(String id, MenuManager menu,
            ISelectionProvider prov, IWorkbenchPart part) {
        super();
        this.menu = menu;
        this.selProvider = prov;
        this.part = part;
        menuIds.add(id);
        menu.addMenuListener(this);
        if (!menu.getRemoveAllWhenShown()) {
            menuWrapper = new SubMenuManager(menu);
            menuWrapper.setVisible(true);
        }
        getManager().addExtender(this);
        readStaticActions();
    }

    // getMenuId() added by Dan Rubel (dan_rubel@instantiations.com)
    /**
     * Return the menu identifiers for this extender.
     * 
     * @return The set of all identifiers that represent this extender.
     */
    public Set getMenuIds() {
        return menuIds;
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
        menuIds.add(menuId);
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
                // TODO Auto-generated method stub

            }

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
             */
            public ISelection getSelection() {
                if (part instanceof IEditorPart) {
                    IEditorPart editorPart = (IEditorPart) part;
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
                // TODO Auto-generated method stub

            }

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
             */
            public void setSelection(ISelection selection) {
                // TODO Auto-generated method stub

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
        testForAdditions();
        if (menuWrapper != null) {
            mgr = menuWrapper;
            menuWrapper.removeAll();
        }
        addEditorActions(mgr);
        addObjectActions(mgr);
        addStaticActions(mgr);
    }

    /**
     * Read static items for the context menu.
     */
    private void readStaticActions() {
        // If no menu id provided, then there is no contributions
        // to add. Fix for bug #33140.
        if (menuIds.isEmpty()) {
            return;
        }

        final Iterator menuIdItr = menuIds.iterator();
        while (menuIdItr.hasNext()) {
            final String menuId = (String) menuIdItr.next();
            if ((menuId == null) || (menuId.length() < 1)) { // Bug 33140
                continue;
            }

            staticActionBuilder = new ViewerActionBuilder();
            if (!staticActionBuilder.readViewerContributions(menuId,
                    selProvider, part)) {
                staticActionBuilder = null;
            }

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
                    .log("Context menu missing standard group 'org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS'. (menu ids = " //$NON-NLS-1$
                            + menuIds.toString() + ")  part id = " //$NON-NLS-1$
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
        getManager().removeExtender(this);
        menu.removeMenuListener(this);
    }
}