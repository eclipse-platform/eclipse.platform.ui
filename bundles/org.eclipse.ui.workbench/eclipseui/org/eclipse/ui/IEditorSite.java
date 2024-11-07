/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * The primary interface between an editor part and the workbench.
 * <p>
 * The workbench exposes its implemention of editor part sites via this
 * interface, which is not intended to be implemented or extended by clients.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEditorSite extends IWorkbenchPartSite {

	/**
	 * Returns the action bar contributor for this editor.
	 * <p>
	 * An action contributor is responsable for the creation of actions. By design,
	 * this contributor is used for one or more editors of the same type. Thus, the
	 * contributor returned by this method is not owned completely by the editor -
	 * it is shared.
	 * </p>
	 *
	 * @return the editor action bar contributor, or <code>null</code> if none
	 *         exists
	 */
	IEditorActionBarContributor getActionBarContributor();

	/**
	 * Returns the action bars for this part site. Editors of the same type (and in
	 * the same window) share the same action bars. Contributions to the action bars
	 * are done by the <code>IEditorActionBarContributor</code>.
	 *
	 * @return the action bars
	 * @since 2.1
	 */
	IActionBars getActionBars();

	/**
	 * <p>
	 * Registers a pop-up menu with the default id for extension. The default id is
	 * defined as the part id.
	 * </p>
	 * <p>
	 * By default, context menus include object contributions based on the editor
	 * input for the current editor. It is possible to override this behaviour by
	 * calling this method with <code>includeEditorInput</code> as
	 * <code>false</code>. This might be desirable for editors that present a
	 * localized view of an editor input (e.g., a node in a model editor).
	 * </p>
	 * <p>
	 * For a detailed description of context menu registration see
	 * {@link IWorkbenchPartSite#registerContextMenu(MenuManager, ISelectionProvider)}
	 * </p>
	 *
	 * @param menuManager        the menu manager; must not be <code>null</code>.
	 * @param selectionProvider  the selection provider; must not be
	 *                           <code>null</code>.
	 * @param includeEditorInput Whether the editor input should be included when
	 *                           adding object contributions to this context menu.
	 * @see IWorkbenchPartSite#registerContextMenu(MenuManager, ISelectionProvider)
	 * @since 3.1
	 */
	void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider, boolean includeEditorInput);

	/**
	 * <p>
	 * Registers a pop-up menu with a particular id for extension. This method
	 * should only be called if the target part has more than one context menu to
	 * register.
	 * </p>
	 * <p>
	 * By default, context menus include object contributions based on the editor
	 * input for the current editor. It is possible to override this behaviour by
	 * calling this method with <code>includeEditorInput</code> as
	 * <code>false</code>. This might be desirable for editors that present a
	 * localized view of an editor input (e.g., a node in a model editor).
	 * </p>
	 * <p>
	 * For a detailed description of context menu registration see
	 * {@link IWorkbenchPartSite#registerContextMenu(MenuManager, ISelectionProvider)}
	 * </p>
	 *
	 * @param menuId             the menu id; must not be <code>null</code>.
	 * @param menuManager        the menu manager; must not be <code>null</code>.
	 * @param selectionProvider  the selection provider; must not be
	 *                           <code>null</code>.
	 * @param includeEditorInput Whether the editor input should be included when
	 *                           adding object contributions to this context menu.
	 * @see IWorkbenchPartSite#registerContextMenu(MenuManager, ISelectionProvider)
	 * @since 3.1
	 */
	void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider,
			boolean includeEditorInput);
}
