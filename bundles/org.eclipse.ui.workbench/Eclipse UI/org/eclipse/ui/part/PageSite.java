/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.e4.compatibility.WorkbenchPartSite;
import org.eclipse.ui.internal.part.IPageSiteHolder;
import org.eclipse.ui.internal.services.INestable;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.WorkbenchLocationService;
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

	private IEclipseContext lastActivePageContext;

	private IEclipseContext pageContext;

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
	public PageSite(final IViewSite parentViewSite) {
		Assert.isNotNull(parentViewSite);
		parentSite = parentViewSite;
		subActionBars = new SubActionBars(parentViewSite.getActionBars(), this);

		initializeDefaultServices();
	}

	/**
	 * Initialize the slave services for this site.
	 */
	private void initializeDefaultServices() {
		WorkbenchPartSite partSite = (WorkbenchPartSite) parentSite;
		pageContext = EclipseContextFactory.create(partSite.getModel().getContext(), null);
		pageContext.set(IWorkbenchLocationService.class.getName(), new WorkbenchLocationService(
				IServiceScopes.PAGESITE_SCOPE, getWorkbenchWindow().getWorkbench(),
				getWorkbenchWindow(), parentSite, null, this, 3));
		pageContext.set(IPageSiteHolder.class.getName(), new IPageSiteHolder() {
			public IPageSite getSite() {
				return PageSite.this;
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
				// PopupMenuExtender ext = (PopupMenuExtender)
				// menuExtenders.get(i);
				// managers.add(ext.getManager());
				// ext.dispose();
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

		if (pageContext instanceof IDisposable) {
			((IDisposable) pageContext).dispose();
		}

		WorkbenchPartSite partSite = (WorkbenchPartSite) parentSite;
		IEclipseContext context = partSite.getModel().getContext();
		context.remove(IWorkbenchLocationService.class.getName());
		context.remove(IPageSiteHolder.class.getName());
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
		return pageContext.get(key.getName());
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
		return pageContext.containsKey(key.getName());
	}

	/*
	 * (non-Javadoc) Method declared on IPageSite.
	 */
	public void registerContextMenu(String menuID, MenuManager menuMgr,
			ISelectionProvider selProvider) {
		if (menuExtenders == null) {
			menuExtenders = new ArrayList(1);
		}
		// TODO compat: registerContextMenu
		E4Util.unsupported("registerContextMenu"); //$NON-NLS-1$
		// PartSite.registerContextMenu(menuID, menuMgr, selProvider, false,
		// parentSite.getPart(), menuExtenders);
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
	 * 
	 * @since 3.2
	 */
	public void activate() {
		IEclipseContext parent = (IEclipseContext) pageContext.getLocal(IContextConstants.PARENT);
		lastActivePageContext = (IEclipseContext) parent.getLocal(IContextConstants.ACTIVE_CHILD);
		parent.set(IContextConstants.ACTIVE_CHILD, pageContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.INestable#deactivate()
	 * 
	 * @since 3.2
	 */
	public void deactivate() {
		IEclipseContext parent = (IEclipseContext) pageContext.getLocal(IContextConstants.PARENT);
		parent.set(IContextConstants.ACTIVE_CHILD, lastActivePageContext);
	}
}
