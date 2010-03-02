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

package org.eclipse.ui.internal.provisional.application;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * Extends <code>IActionBarConfigurer</code> with API to allow the advisor to
 * be decoupled from the implementation types for tool bar managers and tool bar
 * contribution items.
 * 
 * @since 3.2
 */
public interface IActionBarConfigurer2 extends IActionBarConfigurer {
	
	/**
	 * Creates a tool bar manager for the workbench window's tool bar. The
	 * action bar advisor should use this factory method rather than creating a
	 * <code>ToolBarManager</code> directly.
	 * 
	 * @return the tool bar manager
	 */
	public IToolBarManager createToolBarManager();

	/**
	 * Creates a toolbar contribution item for the window's tool bar. The action
	 * bar advisor should use this factory method rather than creating a
	 * <code>ToolBarContributionItem</code> directly.
	 * 
	 * @param toolBarManager
	 *            a tool bar manager for the workbench window's tool bar
	 * @param id
	 *            the id of the contribution
	 * @return the tool bar contribution item
	 */
	public IToolBarContributionItem createToolBarContributionItem(
			IToolBarManager toolBarManager, String id);

}
