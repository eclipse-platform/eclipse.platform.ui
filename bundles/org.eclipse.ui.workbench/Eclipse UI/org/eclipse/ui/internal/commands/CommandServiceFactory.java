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
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
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
	 * @see
	 * org.eclipse.ui.services.AbstractServiceFactory#create(java.lang.Class,
	 * org.eclipse.ui.services.IServiceLocator,
	 * org.eclipse.ui.services.IServiceLocator)
	 */
	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		if (!ICommandService.class.equals(serviceInterface)) {
			return null;
		}
		IWorkbenchLocationService wls = (IWorkbenchLocationService) locator
				.getService(IWorkbenchLocationService.class);
		final IWorkbench wb = wls.getWorkbench();
		if (wb == null) {
			return null;
		}

		Object parent = parentLocator.getService(serviceInterface);
		if (parent == null) {
			// we are registering the global services in the Workbench
			return null;
		}
		final IWorkbenchWindow window = wls.getWorkbenchWindow();
		final IWorkbenchPartSite site = wls.getPartSite();

		if (site == null) {
			return new SlaveCommandService((ICommandService) parent,
					IServiceScopes.WINDOW_SCOPE, window);
		}

		if (parent instanceof SlaveCommandService) {
			Object pageSite = wls.getPageSite();
			if (pageSite != null) {
				return new SlaveCommandService((ICommandService) parent,
						IServiceScopes.PAGESITE_SCOPE, pageSite);
			}
			Object mpepSite = wls.getMultiPageEditorSite();
			if (mpepSite != null) {
				return new SlaveCommandService((ICommandService) parent,
						IServiceScopes.MPESITE_SCOPE, mpepSite);
			}
		}

		return new SlaveCommandService((ICommandService) parent,
				IServiceScopes.PARTSITE_SCOPE, site);
	}

}
