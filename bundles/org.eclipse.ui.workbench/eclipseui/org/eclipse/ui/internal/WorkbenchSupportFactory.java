/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Create singleton services to make the Workbench singletons available. This is
 * a "hack" to provide access to the Workbench singletons.
 *
 * @since 3.4
 */
public class WorkbenchSupportFactory extends AbstractServiceFactory {

	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {

		IWorkbenchLocationService wls = locator.getService(IWorkbenchLocationService.class);
		final IWorkbench wb = wls.getWorkbench();
		if (wb == null) {
			return null;
		}
		final IWorkbenchWindow window = wls.getWorkbenchWindow();
		final IWorkbenchPartSite site = wls.getPartSite();
		Object parent = parentLocator.getService(serviceInterface);

		if (parent == null) {
			// return top level services
			if (IProgressService.class.equals(serviceInterface)) {
				return wb.getProgressService();
			}
			if (IWorkbenchSiteProgressService.class.equals(serviceInterface)) {
				if (site instanceof PartSite) {
					return ((PartSite) site).getSiteProgressService();
				}
			}
			if (IPartService.class.equals(serviceInterface)) {
				if (window != null) {
					return window.getPartService();
				}
			}
			if (IPageService.class.equals(serviceInterface)) {
				if (window != null) {
					return window;
				}
			}
			if (ISelectionService.class.equals(serviceInterface)) {
				if (window != null) {
					return window.getSelectionService();
				}
			}
			return null;
		}

		if (ISelectionService.class.equals(serviceInterface)) {
			return new SlaveSelectionService((ISelectionService) parent);
		}

		if (IProgressService.class.equals(serviceInterface)) {
			if (site instanceof PartSite) {
				return ((PartSite) site).getSiteProgressService();
			}
		}
		if (IPartService.class.equals(serviceInterface)) {
			return new SlavePartService((IPartService) parent);
		}
		if (IPageService.class.equals(serviceInterface)) {
			return new SlavePageService((IPageService) parent);
		}

		return null;
	}
}
