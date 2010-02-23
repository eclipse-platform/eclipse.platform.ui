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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.internal.handlers.LegacyHandlerService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.services.IServiceScopes;

/**
 * @since 3.5
 *
 */
public class WorkbenchPartSite implements IWorkbenchLocationService, IWorkbenchPartSite {

	MPart model;
	private IWorkbenchPart part;
	private IConfigurationElement element;

	private IKeyBindingService keyBindingService;
	private ISelectionProvider selectionProvider;
	private ArrayList menuExtenders;

	WorkbenchPartSite(MPart model, IWorkbenchPart part, IConfigurationElement element) {
		this.model = model;
		this.part = part;
		this.element = element;
		
		IEclipseContext e4Context = model.getContext();
		e4Context.set(IWorkbenchLocationService.class.getName(), this);
		IHandlerService handlerService = new LegacyHandlerService(e4Context);
		e4Context.set(IHandlerService.class.getName(), handlerService);
	}

	public MPart getModel() {
		return model;
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

	/**
	 * This is a helper method for the register context menu functionality. It
	 * is provided so that different implementations of the
	 * <code>IWorkbenchPartSite</code> interface don't have to worry about how
	 * context menus should work.
	 * 
	 * @param menuId
	 *            the menu id
	 * @param menuManager
	 *            the menu manager
	 * @param selectionProvider
	 *            the selection provider
	 * @param includeEditorInput
	 *            whether editor inputs should be included in the structured
	 *            selection when calculating contributions
	 * @param part
	 *            the part for this site
	 * @param menuExtenders
	 *            the collection of menu extenders for this site
	 * @see IWorkbenchPartSite#registerContextMenu(MenuManager,
	 *      ISelectionProvider)
	 */
	public static final void registerContextMenu(final String menuId,
			final MenuManager menuManager, final ISelectionProvider selectionProvider,
			final boolean includeEditorInput, final IWorkbenchPart part,
			final Collection menuExtenders) {
		/*
		 * Check to see if the same menu manager and selection provider have
		 * already been used. If they have, then we can just add another menu
		 * identifier to the existing PopupMenuExtender.
		 */
		final Iterator extenderItr = menuExtenders.iterator();
		boolean foundMatch = false;
		while (extenderItr.hasNext()) {
			final PopupMenuExtender existingExtender = (PopupMenuExtender) extenderItr.next();
			if (existingExtender.matches(menuManager, selectionProvider, part)) {
				existingExtender.addMenuId(menuId);
				foundMatch = true;
				break;
			}
		}

		if (!foundMatch) {
			menuExtenders.add(new PopupMenuExtender(menuId, menuManager, selectionProvider, part,
					includeEditorInput));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(java.lang.String, org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void registerContextMenu(String menuID, MenuManager menuMgr,
			ISelectionProvider selProvider) {
		if (menuExtenders == null) {
			menuExtenders = new ArrayList(1);
		}

		registerContextMenu(menuID, menuMgr, selProvider, true, getPart(), menuExtenders);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void registerContextMenu(MenuManager menuMgr, ISelectionProvider selProvider) {
		registerContextMenu(getId(), menuMgr, selProvider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
	 */
	public IKeyBindingService getKeyBindingService() {
		// FIXME compat getKeyBindingService
		if (keyBindingService == null) {
			keyBindingService = new KeyBindingService();
		}
		return keyBindingService;
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
		// CompilationUnitEditor's reconciled(*) method calls this from a non-UI
		// thread
		Display currentDisplay = Display.getCurrent();
		if (currentDisplay == null) {
			// FIXME: this is not the right shell if this part is detached!!!
			return getWorkbenchWindow().getShell();
		}
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
		if (IWorkbenchSiteProgressService.class == adapter) {
			return getService(adapter);
		}
		return Platform.getAdapterManager().getAdapter(this, adapter);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IWorkbenchLocationService#getServiceScope
	 * ()
	 */
	public String getServiceScope() {
		return IServiceScopes.PARTSITE_SCOPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IWorkbenchLocationService#getServiceLevel
	 * ()
	 */
	public int getServiceLevel() {
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IWorkbenchLocationService#getWorkbench()
	 */
	public IWorkbench getWorkbench() {
		return getWorkbenchWindow().getWorkbench();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IWorkbenchLocationService#getPartSite()
	 */
	public IWorkbenchPartSite getPartSite() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IWorkbenchLocationService#
	 * getMultiPageEditorSite()
	 */
	public IEditorSite getMultiPageEditorSite() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IWorkbenchLocationService#getPageSite()
	 */
	public IPageSite getPageSite() {
		return null;
	}

}
