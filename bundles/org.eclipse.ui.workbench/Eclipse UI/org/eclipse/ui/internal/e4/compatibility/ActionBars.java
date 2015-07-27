/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.services.IServiceLocator;

public class ActionBars extends SubActionBars {

	private IToolBarManager toolbarManager;

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
		getMenuManager().update(false);
		if (toolbarManager != null) {
			//			System.err.println("update toolbar manager for " + part.getElementId()); //$NON-NLS-1$
			if (toolbarManager instanceof ToolBarManager) {
				ToolBarManager tbm = (ToolBarManager) toolbarManager;
				Control tbCtrl = tbm.getControl();
				if (tbCtrl == null || tbCtrl.isDisposed()) {
					if (part.getContext() != null) {
						// TODO what to do
					}
				} else {
					tbm.update(true);
					if (!tbCtrl.isDisposed()) {
						Control packParent = getPackParent(tbCtrl);
						packParent.pack();

						// Specifically lay out the CTF
						if (packParent.getParent() instanceof CTabFolder)
							packParent.getParent().layout(true);
					}
				}
			} else {
				toolbarManager.update(false);
			}

			MToolBar toolbar = part.getToolbar();
			if (toolbar != null) {
				Object renderer = toolbar.getRenderer();
				if (renderer instanceof ToolBarManagerRenderer) {
					// update the mapping of opaque items
					((ToolBarManagerRenderer) renderer).reconcileManagerToModel(toolbarManager,
							toolbar);
				}
			}
		}

		MUIElement parent = getParentModel();
		if (parent != null && isOnTop()) {
			Object renderer = parent.getRenderer();
			if (renderer instanceof StackRenderer) {
				StackRenderer stackRenderer = (StackRenderer) renderer;
				CTabFolder folder = (CTabFolder) parent.getWidget();
				stackRenderer.adjustTopRight(folder);
			}
		}

		super.updateActionBars();
	}

	private MUIElement getParentModel() {
		MElementContainer<MUIElement> parent = part.getParent();
		if (parent == null) {
			MPlaceholder placeholder = part.getCurSharedRef();
			return placeholder == null ? null : placeholder.getParent();
		}
		return parent;
	}

	private boolean isOnTop() {
		MUIElement parentModel = getParentModel();
		if (parentModel.getRenderer() instanceof StackRenderer) {
			MPartStack stack = (MPartStack) parentModel;
			if (stack.getSelectedElement() == part)
				return true;
			if (stack.getSelectedElement() instanceof MPlaceholder) {
				MPlaceholder ph = (MPlaceholder) stack.getSelectedElement();
				return ph.getRef() == part;
			}
		}

		return true;
	}

	private Control getPackParent(Control control) {
		Composite parent = control.getParent();
		while (parent != null) {
			if (parent instanceof CTabFolder) {
				Control topRight = ((CTabFolder) parent).getTopRight();
				if (topRight != null) {
					return topRight;
				}
				break;
			}
			parent = parent.getParent();
		}
		return control.getParent();
	}

	boolean isSelected(MPart part) {
		MElementContainer<?> parent = part.getParent();
		if (parent == null) {
			MPlaceholder placeholder = part.getCurSharedRef();
			if (placeholder == null) {
				return false;
			}

			parent = placeholder.getParent();
			return parent instanceof MGenericStack ? parent.getSelectedElement() == placeholder
					: parent != null;
		}
		return parent instanceof MGenericStack ? parent.getSelectedElement() == part
				: parent != null;
	}

	@Override
	public void dispose() {
		menuManager.dispose();
		if (toolbarManager instanceof ToolBarManager) {
			((ToolBarManager) toolbarManager).dispose();
		}
		super.dispose();
	}

}
