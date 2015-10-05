/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources.refresh.win32;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.refresh.*;

/**
 * The <code>Win32RefreshProvider</code> creates monitors that
 * can monitor drives on Win32 platforms.
 *
 * @see org.eclipse.core.resources.refresh.RefreshProvider
 */
public class Win32RefreshProvider extends RefreshProvider {
	private Win32Monitor monitor;

	/**
	 * Creates a standard Win32 monitor if the given resource is local.
	 *
	 * @see org.eclipse.core.resources.refresh.RefreshProvider#installMonitor(IResource,IRefreshResult)
	 */
	@Override
	public IRefreshMonitor installMonitor(IResource resource, IRefreshResult result) {
		if (resource.getLocation() == null || !resource.exists() || resource.getType() == IResource.FILE)
			return null;
		if (monitor == null)
			monitor = new Win32Monitor(result);
		if (monitor.monitor(resource))
			return monitor;
		return null;
	}
}
