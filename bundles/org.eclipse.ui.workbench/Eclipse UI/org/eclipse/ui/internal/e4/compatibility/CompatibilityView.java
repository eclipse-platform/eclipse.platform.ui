/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import javax.inject.Inject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.views.IViewDescriptor;

public class CompatibilityView {

	private IViewPart wrapped;

	@Inject
	private MPart part;

	@Inject
	private Composite composite;

	@Inject
	private IWorkbench workbench;

	public void create() {
		IViewDescriptor descriptor = workbench.getViewRegistry().find(part.getId());
		try {
			wrapped = descriptor.createView();
			wrapped.init(createSite(), null);
			wrapped.createPartControl(composite);
			delegateSetFocus();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void delegateSetFocus() {
		wrapped.setFocus();
	}

	private IActionBars actionBars() {
		return new IActionBars() {

			public void updateActionBars() {
				// TODO Auto-generated method stub

			}

			public void setGlobalActionHandler(String actionId, IAction handler) {
				// TODO Auto-generated method stub

			}

			public IToolBarManager getToolBarManager() {
				return new ToolBarManager();
			}

			public IStatusLineManager getStatusLineManager() {
				return new StatusLineManager();
			}

			public IServiceLocator getServiceLocator() {
				// TODO Auto-generated method stub
				return null;
			}

			public IMenuManager getMenuManager() {
				return new MenuManager();
			}

			public IAction getGlobalActionHandler(String actionId) {
				// TODO Auto-generated method stub
				return null;
			}

			public void clearGlobalActionHandlers() {
				// TODO Auto-generated method stub

			}
		};
	}

	private IViewSite createSite() {
		return new IViewSite() {

			public IActionBars getActionBars() {
				return actionBars();
			}

			public String getSecondaryId() {
				return null;
			}

			public String getId() {
				return null;
			}

			public String getPluginId() {
				return null;
			}

			public String getRegisteredName() {
				return null;
			}

			public void registerContextMenu(String menuId, MenuManager menuManager,
					ISelectionProvider selectionProvider) {

			}

			public void registerContextMenu(MenuManager menuManager,
					ISelectionProvider selectionProvider) {

			}

			public IKeyBindingService getKeyBindingService() {
				return null;
			}

			public IWorkbenchPart getPart() {
				return CompatibilityView.this.wrapped;
			}

			public IWorkbenchPage getPage() {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			}

			public ISelectionProvider getSelectionProvider() {
				return null;
			}

			public Shell getShell() {
				return null;
			}

			public IWorkbenchWindow getWorkbenchWindow() {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			}

			public void setSelectionProvider(ISelectionProvider provider) {
			}

			public Object getService(Class api) {
				return null;
			}

			public boolean hasService(Class api) {
				return false;
			}

			public Object getAdapter(Class adapter) {
				return null;
			}
		};
	}

	public IViewPart getView() {
		return wrapped;
	}

}
