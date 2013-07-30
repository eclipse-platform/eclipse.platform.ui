/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Iterator;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.internal.contexts.NestableContextService;
import org.eclipse.ui.internal.expressions.ActivePartExpression;
import org.eclipse.ui.internal.handlers.LegacyHandlerService;
import org.eclipse.ui.internal.part.IPageSiteHolder;
import org.eclipse.ui.internal.services.INestable;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.internal.services.WorkbenchLocationService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceScopes;

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

	private IEclipseContext e4Context;

	private NestableContextService contextService;

	private boolean active = false;

	/**
	 * Creates a new sub view site of the given parent view site.
	 * 
	 * @param parentViewSite
	 *            the parent view site
	 */
	public PageSite(final IViewSite parentViewSite) {
		Assert.isNotNull(parentViewSite);
		parentSite = parentViewSite;
		subActionBars = new SubActionBars(parentViewSite.getActionBars(), this);

		// Initialize the service locator.
		IServiceLocatorCreator slc = (IServiceLocatorCreator) parentSite
				.getService(IServiceLocatorCreator.class);
		e4Context = ((PartSite) parentViewSite).getContext().createChild("PageSite"); //$NON-NLS-1$
		this.serviceLocator = (ServiceLocator) slc.createServiceLocator(parentViewSite, null,
				new IDisposable() {
					public void dispose() {
						// final Control control =
						// ((PartSite)parentViewSite).getPane().getControl();
						// if (control != null && !control.isDisposed()) {
						// ((PartSite)parentViewSite).getPane().doHide();
						// }
						// TODO compat: not tsure what this should do
					}
				}, e4Context);
		initializeDefaultServices();
	}

	/**
	 * Initialize the slave services for this site.
	 */
	private void initializeDefaultServices() {
		serviceLocator.registerService(IWorkbenchLocationService.class,
				new WorkbenchLocationService(IServiceScopes.PAGESITE_SCOPE,
						getWorkbenchWindow().getWorkbench(),
						getWorkbenchWindow(), parentSite, null, this, 3));
		serviceLocator.registerService(IPageSiteHolder.class,
				new IPageSiteHolder() {
					public IPageSite getSite() {
						return PageSite.this;
					}
				});

		// create a local handler service so that when this page
		// activates/deactivates, its handlers will also be taken into/out of
		// consideration during handler lookups
		IHandlerService handlerService = new LegacyHandlerService(e4Context);
		e4Context.set(IHandlerService.class, handlerService);

		e4Context.set(IContextService.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (contextService == null) {
					contextService = new NestableContextService(context.getParent().get(
							IContextService.class), new ActivePartExpression(parentSite.getPart()));
				}
				return contextService;
			}
		});
	}

	/**
	 * Disposes of the menu extender contributions.
	 */
	protected void dispose() {
		if (menuExtenders != null) {
			HashSet managers = new HashSet(menuExtenders.size());
			for (int i = 0; i < menuExtenders.size(); i++) {
				PopupMenuExtender ext = (PopupMenuExtender) menuExtenders.get(i);
				managers.add(ext.getManager());
				ext.dispose();
			}
			if (managers.size()>0) {
				for (Iterator iterator = managers.iterator(); iterator
						.hasNext();) {
					MenuManager mgr = (MenuManager) iterator.next();
					mgr.dispose();
				}
			}
			menuExtenders = null;
		}
		subActionBars.dispose();

		if (contextService != null) {
			contextService.dispose();
		}

		serviceLocator.dispose();
		e4Context.dispose();
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

	public final Object getService(final Class key) {
		Object service = serviceLocator.getService(key);
		if (active && service instanceof INestable) {
			((INestable) service).activate();
		}
		return service;
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

	public final boolean hasService(final Class key) {
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
		PartSite.registerContextMenu(menuID, menuMgr, selProvider, false, parentSite.getPart(),
				e4Context, menuExtenders);
	}

	/*
	 * (non-Javadoc) Method declared on IPageSite.
	 */
	public void setSelectionProvider(ISelectionProvider provider) {
		selectionProvider = provider;
	}

	/* Package */IEclipseContext getSiteContext() {
		return e4Context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.INestable#activate()
	 * 
	 * @since 3.2
	 */
	public void activate() {
		active = true;

		serviceLocator.activate();

		if (contextService != null) {
			contextService.activate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.INestable#deactivate()
	 * 
	 * @since 3.2
	 */
	public void deactivate() {
		active = false;
		if (contextService != null) {
			contextService.deactivate();
		}

		serviceLocator.deactivate();
	}
}
