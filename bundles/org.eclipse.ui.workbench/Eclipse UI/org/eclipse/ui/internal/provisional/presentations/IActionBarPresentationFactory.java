/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.presentations;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.internal.provisional.action.ICoolBarManager2;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.internal.provisional.action.IToolBarManager2;

/**
 * The intention of this class is to allow for replacing the implementation of
 * the cool bar and tool bars in the workbench.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public interface IActionBarPresentationFactory {
	
	/**
	 * Creates the cool bar manager for the window's tool bar area.
	 * 
	 * @return the cool bar manager
	 */
	public ICoolBarManager2 createCoolBarManager();

	/**
	 * Creates a tool bar manager for window's tool bar area.
	 * 
	 * @return the tool bar manager
	 */
	public IToolBarManager2 createToolBarManager();

	/**
	 * Creates a tool bar manager for a view's tool bar.
	 * 
	 * @return the tool bar manager
	 */
	public IToolBarManager2 createViewToolBarManager();

	/**
	 * Creates a toolbar contribution item for a window toolbar manager to be
	 * added to the window's cool bar.
	 * 
	 * @param toolBarManager
	 *            the tool bar manager
	 * @param id
	 *            the id of the contribution
	 * @return the toolbar contribution item
	 */
	public IToolBarContributionItem createToolBarContributionItem(
			IToolBarManager toolBarManager, String id);

}
