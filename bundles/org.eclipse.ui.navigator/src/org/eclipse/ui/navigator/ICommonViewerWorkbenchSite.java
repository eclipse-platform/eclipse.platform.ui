/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * 
 * Provides a page, set of action bars, menu registration callback, and active
 * window.
 * 
 * @since 3.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 */
public interface ICommonViewerWorkbenchSite extends ICommonViewerSite {

	/**
	 * Returns the page corresponding to this viewer site.
	 * 
	 * @return the page corresponding to this viewer site
	 */
	public IWorkbenchPage getPage();

	/**
	 * Registers a pop-up menu with a particular id for extension.
	 * <p>
	 * Within the workbench one plug-in may extend the pop-up menus for a view
	 * or editor within another plug-in. In order to be eligible for extension,
	 * the menu must be registered by calling <code>registerContextMenu</code>.
	 * Once this has been done the workbench will automatically insert any
	 * action extensions which exist.
	 * </p>
	 * <p>
	 * A unique menu id must be provided for each registered menu. This id
	 * should be published in the Javadoc for the page.
	 * </p>
	 * <p>
	 * Any pop-up menu which is registered with the workbench should also define
	 * a <code>GroupMarker</code> in the registered menu with id
	 * <code>IWorkbenchActionConstants.MB_ADDITIONS</code>. Other plug-ins
	 * will use this group as a reference point for insertion. The marker should
	 * be defined at an appropriate location within the menu for insertion.
	 * </p>
	 * 
	 * @param menuId
	 *            the menu id
	 * @param menuManager
	 *            the menu manager
	 * @param selectionProvider
	 *            the selection provider
	 */
	void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider);

	/**
	 * Returns the action bars for this page site. Pages have exclusive use of
	 * their site's action bars.
	 * 
	 * @return the action bars
	 */
	IActionBars getActionBars();

	/**
	 * 
	 * @return A workbench window corresponding to the container of the
	 *         {@link CommonViewer}
	 */
	IWorkbenchWindow getWorkbenchWindow();

	/**
	 * @return the IWorkbenchPart that this site is embedded within.
	 */
	IWorkbenchPart getPart();

	/**
	 * @return the IWorkbenchPartSite that this site is embedded within.
	 */
	IWorkbenchPartSite getSite();
}
