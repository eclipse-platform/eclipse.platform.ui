/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.commands;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.part.IMultiPageEditorSiteHolder;
import org.eclipse.ui.internal.part.IPageSiteHolder;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.IServiceScopes;

/**
 * @since 3.4
 * 
 */
public class CommandServiceFactory extends AbstractServiceFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.AbstractServiceFactory#create(java.lang.Class,
	 *      org.eclipse.ui.services.IServiceLocator,
	 *      org.eclipse.ui.services.IServiceLocator)
	 */
	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		if (!ICommandService.class.equals(serviceInterface)) {
			return null;
		}
		final IWorkbench wb = (IWorkbench) locator.getService(IWorkbench.class);
		if (wb == null) {
			return null;
		}

		Object parent = parentLocator.getService(serviceInterface);
		if (parent == null) {
			// we are registering the global services in the Workbench
			return null;
		}
		final IWorkbenchWindow window = (IWorkbenchWindow) locator
				.getService(IWorkbenchWindow.class);
		final IWorkbenchPartSite site = (IWorkbenchPartSite) locator
				.getService(IWorkbenchPartSite.class);

		if (site == null) {
			return new SlaveCommandService((ICommandService) parent,
					IServiceScopes.WINDOW_SCOPE, window);
		}

		if (parent instanceof SlaveCommandService) {
			Object pageSite = locator.getService(IPageSiteHolder.class);
			if (pageSite != null) {
				return new SlaveCommandService((ICommandService) parent,
						IServiceScopes.PAGESITE_SCOPE,
						((IPageSiteHolder) pageSite).getSite());
			}
			Object mpepSite = locator
					.getService(IMultiPageEditorSiteHolder.class);
			if (mpepSite != null) {
				return new SlaveCommandService((ICommandService) parent,
						IServiceScopes.MPESITE_SCOPE,
						((IMultiPageEditorSiteHolder) mpepSite).getSite());
			}
		}

		return new SlaveCommandService((ICommandService) parent,
				IServiceScopes.PARTSITE_SCOPE, site);
	}

}
