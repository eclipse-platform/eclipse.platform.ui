/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;

/**
 * <p>
 * This service manages Common Navigator Action Providers. 
 * </p>
 * <p>
 * An Action Provider ((@link org.eclipse.ui.navigator.ICommonActionProvider}) is
 * defined through the <b>org.eclipse.ui.navigator.actionProvider</b> 
 * extension point or through the <b>actionprovider</b> attribute of 
 * the <b>org.eclipes.ui.navigator.navigatorContent</b> extension point. 
 * </p>
 * <p>
 * Action Providers have opportunities to contribute to the context menu
 * and {@link org.eclipse.ui.IActionBars} whenever the selection
 * in the viewer changes. Action Providers are selected based on the 
 * enablement expressions of the respective extension. See the schema 
 * documentation for more information on how to specify an Action Provider. 
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *
 */
public interface INavigatorActionService {

	/**
	 * Requests that the INavigatorActionService refresh
	 * the view menu and context menu based on the current 
	 * state of Navigator Content Extension activation state.
	 *
	 */
	void refresh();

	/** 
	 * Requests that the service invoke extensions to fill
	 * the given menu with Action Providers that are interested in 
	 * elements from the given selection.
	 * 
	 * <p>
	 * Object contributions (see <b>org.eclipes.ui.popupMenus</b>) 
	 * may also respected by this method if <code>toRespectObjectContributions</code>
	 * is true.
	 * </p>
	 * 
	 * @param aMenu The menu being presented to the user.
	 * @param aStructuredSelection The current selection from the viewer.
	 */
	void fillContextMenu(IMenuManager aMenu,
			IStructuredSelection aStructuredSelection);

	/**
	 * Request that the service invoke extensions to fill
	 * the given IActionBars with retargetable actions or
	 * view menu contributions from Action Providers that 
	 * are interested in the given selection. 
	 * 
	 * @param theActionBars The action bars in use by the current view site.
	 * @param aStructuredSelection The current selection from the viewer.
	 */
	void fillActionBars(IActionBars theActionBars,
			IStructuredSelection aStructuredSelection);

	/**
	 * Dispose of any state or resources held by the service. 
	 *
	 */
	void dispose();

	/**
	 * Use the given memento to restore the state of each Action Provider
	 * as it is initialized. 
	 * 
	 * @param aMemento The memento retrieved from the dialog settings
	 */
	void restoreState(IMemento aMemento);

	/**
	 * Request that Action Providers save any state that they find interesting.
	 * 
	 * @param aMemento The memento retrieved from the dialog settings
	 */
	void saveState(IMemento aMemento);

	/**
	 * Initialize the given Action Provider correctly. 
	 * 
	 * 
	 * @param anExtensionId The Id of the Action Provider or the associated Content Extension 
	 * @param anActionProvider A non-null Action Provider to be initialized.
	 */
	void initialize(String anExtensionId,
			ICommonActionProvider anActionProvider);

	/**
	 * 
	 * Set the menu that should be used whenever an opportunity 
	 * to fillContextMenu() is available.
	 * 
	 * @param aMenu The context menu of the viewer.
	 */
	
	void setUpdateMenu(IMenuManager aMenu);

}
