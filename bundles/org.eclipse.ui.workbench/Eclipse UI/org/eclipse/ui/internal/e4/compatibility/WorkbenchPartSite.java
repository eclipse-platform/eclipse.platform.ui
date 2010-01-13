/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.5
 *
 */
public class WorkbenchPartSite implements IWorkbenchPartSite {

	private MPart model;
	private IWorkbenchPart part;
	private IConfigurationElement element;

	private ISelectionProvider selectionProvider;

	WorkbenchPartSite(MPart model, IWorkbenchPart part, IConfigurationElement element) {
		this.model = model;
		this.part = part;
		this.element = element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getId()
	 */
	public String getId() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getPluginId()
	 */
	public String getPluginId() {
		return element.getNamespaceIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getRegisteredName()
	 */
	public String getRegisteredName() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(java.lang.String, org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		// FIXME compat registerContextMenu
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider) {
		// FIXME compat registerContextMenu
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
	 */
	public IKeyBindingService getKeyBindingService() {
		// FIXME compat getKeyBindingService
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getPart()
	 */
	public IWorkbenchPart getPart() {
		return part;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchSite#getPage()
	 */
	public IWorkbenchPage getPage() {
		return getWorkbenchWindow().getActivePage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchSite#getSelectionProvider()
	 */
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchSite#getShell()
	 */
	public Shell getShell() {
		Control control = (Control) model.getWidget();
		return control.getShell();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchSite#getWorkbenchWindow()
	 */
	public IWorkbenchWindow getWorkbenchWindow() {
		MElementContainer<?> parent = model.getParent();
		while (!(parent instanceof MWindow)) {
			parent = parent.getParent();
		}

		MWindow window = (MWindow) parent;
		MApplication application = (MApplication) window.getContext().get(
				MApplication.class.getName());
		Workbench workbench = (Workbench) application.getContext().get(IWorkbench.class.getName());

		return workbench.createWorkbenchWindow(window);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchSite#setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void setSelectionProvider(ISelectionProvider provider) {
		selectionProvider = provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		// FIXME compat getAdapter
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IServiceLocator#getService(java.lang.Class)
	 */
	public Object getService(Class api) {
		return model.getContext().get(api.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IServiceLocator#hasService(java.lang.Class)
	 */
	public boolean hasService(Class api) {
		return model.getContext().containsKey(api.getName());
	}

}
