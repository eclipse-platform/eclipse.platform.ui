/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.IToolBarManager2;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

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
public class ActionBarPresentation {

	/**
	 * Creates the cool bar manager for the window's tool bar area.
	 * 
	 * @return the cool bar manager
	 */
	public ICoolBarManager createCoolBarManager() {
		return new CoolBarManager(SWT.FLAT);
	}

	/**
	 * Creates the control for the window's cool bar. Subclasses may override.
	 * 
	 * @param coolBarManager
	 *            the window's cool bar manager
	 * @param parent
	 *            the parent composite
	 * @return the window's cool bar control
	 */
	public Control createCoolBarControl(ICoolBarManager coolBarManager,
			Composite parent) {
		return coolBarManager.createControl2(parent);
	}

	/**
	 * Creates a tool bar manager for window's tool bar area.
	 * 
	 * @return the tool bar manager
	 */
	public IToolBarManager2 createToolBarManager() {
		return new ToolBarManager(SWT.FLAT | SWT.RIGHT);
	}

	/**
	 * Creates the control for the window's tool bar. Subclasses may override.
	 * 
	 * @param toolBarManager
	 *            the window's toolbar manager
	 * @param parent
	 *            the parent composite
	 * @return the window's tool bar control
	 */
	public Control createToolBarControl(IToolBarManager2 toolBarManager,
			Composite parent) {
		return toolBarManager.createControl2(parent);
	}

	/**
	 * Creates a tool bar manager for a view's tool bar.
	 * 
	 * @return the tool bar manager
	 */
	public IToolBarManager2 createViewToolBarManager() {
		return new ToolBarManager(SWT.FLAT | SWT.RIGHT);
	}

	/**
	 * Creates the control for a view tool bar. Subclasses may override.
	 * 
	 * @param toolBarManager
	 *            the view's tool bar manager
	 * @param parent
	 *            the parent composite
	 * @return the view's tool bar control
	 */
	public Control createViewToolBarControl(IToolBarManager2 toolBarManager,
			Composite parent) {
		return toolBarManager.createControl2(parent);
	}

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
			IToolBarManager toolBarManager, String id) {
		return new ToolBarContributionItem(toolBarManager, id);
	}
}
