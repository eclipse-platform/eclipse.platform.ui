/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 ******************************************************************************/

package org.eclipse.ui.internal.commands;

import org.eclipse.e4.ui.model.application.ui.MContext;
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
 */
public class CommandServiceFactory extends AbstractServiceFactory {

	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		if (!ICommandService.class.equals(serviceInterface)) {
			return null;
		}
		IWorkbenchLocationService wls = locator.getService(IWorkbenchLocationService.class);
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
			return new SlaveCommandService((ICommandService) parent, IServiceScopes.WINDOW_SCOPE, window);
		}

		if (parent instanceof SlaveCommandService) {
			IServiceLocator pageSite = wls.getPageSite();
			if (pageSite != null) {
				MContext context = pageSite.getService(MContext.class);
				if (context == null) {
					return new SlaveCommandService((ICommandService) parent, IServiceScopes.PAGESITE_SCOPE, pageSite);
				}
				return new SlaveCommandService((ICommandService) parent, IServiceScopes.PAGESITE_SCOPE, pageSite,
						context.getContext());
			}
			IServiceLocator mpepSite = wls.getMultiPageEditorSite();
			if (mpepSite != null) {
				MContext context = mpepSite.getService(MContext.class);
				if (context == null) {
					return new SlaveCommandService((ICommandService) parent, IServiceScopes.MPESITE_SCOPE, mpepSite);
				}
				return new SlaveCommandService((ICommandService) parent, IServiceScopes.MPESITE_SCOPE, mpepSite,
						context.getContext());
			}
		}

		return new SlaveCommandService((ICommandService) parent, IServiceScopes.PARTSITE_SCOPE, site);
	}

}
