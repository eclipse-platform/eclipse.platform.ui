/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.services.IServiceLocator;

public class ActionBars extends SubActionBars {

	private ToolBarManager toolbarManager;

	private IMenuManager menuManager;

	private MPart part;

	public ActionBars(final IActionBars parent, final IServiceLocator serviceLocator, MPart part) {
		super(parent, serviceLocator);
		this.part = part;
	}

	@Override
	public IMenuManager getMenuManager() {
		if (menuManager == null) {
			menuManager = new MenuManager();
		}
		return menuManager;
	}

	@Override
	public IToolBarManager getToolBarManager() {
		if (toolbarManager == null) {
			toolbarManager = new ToolBarManager(SWT.FLAT | SWT.RIGHT | SWT.WRAP);
		}
		return toolbarManager;
	}

	@Override
	public void updateActionBars() {
		// FIXME compat: updateActionBars : should do something useful
		getStatusLineManager().update(false);
		if (menuManager != null) {
			menuManager.update(false);

			// Changes in the menuManager are not propagated to the E4 model, forcing UI
			// update to properly show the view menu, see Bug 566375
			forceUpdateTopRight();
		}

		if (toolbarManager != null) {
			toolbarManager.update(true);

			MToolBar toolbar = part.getToolbar();
			if (toolbar != null) {
				Object renderer = toolbar.getRenderer();
				if (renderer instanceof ToolBarManagerRenderer) {
					// update the mapping of opaque items
					((ToolBarManagerRenderer) renderer).reconcileManagerToModel(toolbarManager, toolbar);
				}
			}
		}

		super.updateActionBars();
	}

	private void forceUpdateTopRight() {
		// Get the partstack and the element in the partstack
		MStackElement element = part;
		if (element.getCurSharedRef() != null) {
			element = element.getCurSharedRef();
		}
		MUIElement parentElement = element.getParent();

		if (!(parentElement instanceof MPartStack)) {
			return;
		}

		Object widget = parentElement.getWidget();
		if (widget instanceof CTabFolder) {
			((StackRenderer) parentElement.getRenderer()).adjustTopRight((CTabFolder) widget);
		}
	}

	@Override
	public void dispose() {
		menuManager.dispose();
		if (toolbarManager != null) {
			toolbarManager.dispose();
			toolbarManager.removeAll();
		}
		super.dispose();
	}

}
