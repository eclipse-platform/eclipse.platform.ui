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
package org.eclipse.ui.part;

import java.util.ArrayList;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.internal.commands.SlaveCommandService;
import org.eclipse.ui.internal.contexts.NestableContextService;
import org.eclipse.ui.internal.expressions.ActivePartExpression;
import org.eclipse.ui.internal.handlers.NestableHandlerService;
import org.eclipse.ui.internal.keys.SlaveBindingService;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.services.INestable;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.keys.IBindingService;

/**
 * This implementation of <code>IPageSite</code> provides a site for a page
 * within a <code>PageBookView</code>. Most methods are forwarded to the
 * view's site.
 */
public class PageSite implements IPageSite, INestable {

	/**
	 * The list of menu extender for each registered menu.
	 */
	private ArrayList menuExtenders;

	/**
	 * The "parent" view site
	 */
	private IViewSite parentSite;

	/**
	 * A selection provider set by the page. Value is <code>null</code> until
	 * set.
	 */
	private ISelectionProvider selectionProvider;

	/**
	 * The localized service locator for this page site. This locator is never
	 * <code>null</code>.
	 */
	private final ServiceLocator serviceLocator;

	/**
	 * The action bars for this site
	 */
	private SubActionBars subActionBars;

	/**
	 * Creates a new sub view site of the given parent view site.
	 * 
	 * @param parentViewSite
	 *            the parent view site
	 */
	public PageSite(IViewSite parentViewSite) {
		Assert.isNotNull(parentViewSite);
		parentSite = parentViewSite;
		subActionBars = new SubActionBars(parentViewSite.getActionBars(), this);

		// Initialize the service locator.
		this.serviceLocator = new ServiceLocator(parentSite);
		initializeDefaultServices();
	}

	/**
	 * Initialize the slave services for this site.
	 */
	private void initializeDefaultServices() {
		final IWorkbenchPart parentPart = parentSite.getPart();
		final Expression defaultExpression = new ActivePartExpression(
				parentPart);

		final IHandlerService parentService = (IHandlerService) parentSite
				.getService(IHandlerService.class);
		final IHandlerService slave = new NestableHandlerService(parentService,
				defaultExpression);
		serviceLocator.registerService(IHandlerService.class, slave);

		final IContextService contextParent = (IContextService) serviceLocator
				.getService(IContextService.class);
		final IContextService contextSlave = new NestableContextService(
				contextParent, defaultExpression);
		serviceLocator.registerService(IContextService.class, contextSlave);
		
		final ICommandService parentCommandService = (ICommandService) serviceLocator
				.getService(ICommandService.class);
		final ICommandService commandService = new SlaveCommandService(
				parentCommandService);
		serviceLocator.registerService(ICommandService.class, commandService);

		final IBindingService parentBindingService = (IBindingService) serviceLocator
				.getService(IBindingService.class);
		final IBindingService bindingService = new SlaveBindingService(
				parentBindingService);
		serviceLocator.registerService(IBindingService.class, bindingService);
	}

	/**
	 * Disposes of the menu extender contributions.
	 */
	protected void dispose() {
		if (menuExtenders != null) {
			for (int i = 0; i < menuExtenders.size(); i++) {
				((PopupMenuExtender) menuExtenders.get(i)).dispose();
			}
			menuExtenders = null;
		}
		subActionBars.dispose();
		serviceLocator.dispose();
	}

	/**
	 * The PageSite implementation of this <code>IPageSite</code> method
	 * returns the <code>SubActionBars</code> for this site.
	 * 
	 * @return the subactionbars for this site
	 */
	public IActionBars getActionBars() {
		return subActionBars;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/*
	 * (non-Javadoc) Method declared on IPageSite.
	 */
	public IWorkbenchPage getPage() {
		return parentSite.getPage();
	}

	/*
	 * (non-Javadoc) Method declared on IPageSite.
	 */
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	public final Object getService(final Object key) {
		return serviceLocator.getService(key);
	}

	/*
	 * (non-Javadoc) Method declared on IPageSite.
	 */
	public Shell getShell() {
		return parentSite.getShell();
	}

	/*
	 * (non-Javadoc) Method declared on IPageSite.
	 */
	public IWorkbenchWindow getWorkbenchWindow() {
		return parentSite.getWorkbenchWindow();
	}

	public final boolean hasService(final Object key) {
		return serviceLocator.hasService(key);
	}

	/*
	 * (non-Javadoc) Method declared on IPageSite.
	 */
	public void registerContextMenu(String menuID, MenuManager menuMgr,
			ISelectionProvider selProvider) {
		if (menuExtenders == null) {
			menuExtenders = new ArrayList(1);
		}
		PartSite.registerContextMenu(menuID, menuMgr, selProvider, false,
				parentSite.getPart(), menuExtenders);
	}

	/*
	 * (non-Javadoc) Method declared on IPageSite.
	 */
	public void setSelectionProvider(ISelectionProvider provider) {
		selectionProvider = provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.INestable#activate()
	 */
	public void activate() {
		serviceLocator.activate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.INestable#deactivate()
	 */
	public void deactivate() {
		serviceLocator.deactivate();
	}
}
