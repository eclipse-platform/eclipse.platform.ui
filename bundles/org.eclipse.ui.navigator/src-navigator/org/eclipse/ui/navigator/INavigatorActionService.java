/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;

public interface INavigatorActionService {

	public abstract void refresh();

	public abstract void fillContextMenu(IMenuManager aMenu,
			IStructuredSelection aStructuredSelection);

	public abstract void fillActionBars(IActionBars theActionBars,
			IStructuredSelection aStructuredSelection);

	public abstract void dispose();

	/**
	 * @param aMemento
	 */
	public abstract void restoreState(IMemento aMemento);

	/**
	 * @param memento2
	 */
	public abstract void saveState(IMemento aMemento);

	public abstract void initialize(String anExtensionId,
			ICommonActionProvider anActionProvider);

	public abstract void setUpdateMenu(MenuManager menuMgr);

}