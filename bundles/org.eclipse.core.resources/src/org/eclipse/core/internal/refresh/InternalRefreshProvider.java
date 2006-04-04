/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.refresh;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.refresh.IRefreshMonitor;

/**
 * Internal abstract superclass of all refresh providers.  This class must not be
 * subclassed directly by clients.  All refresh providers must subclass the public
 * API class <code>org.eclipse.core.resources.refresh.RefreshProvider</code>.
 * 
 * @since 3.0
 */
public class InternalRefreshProvider {
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.refresh.RefreshProvider#createPollingMonitor(IResource)
	 */
	protected IRefreshMonitor createPollingMonitor(IResource resource) {
		PollingMonitor monitor = ((Workspace)resource.getWorkspace()).getRefreshManager().monitors.pollMonitor;
		monitor.monitor(resource);
		return monitor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.refresh.RefreshProvider#resetMonitors(IResource)
	 */
	public void resetMonitors(IResource resource) {
		MonitorManager manager = ((Workspace)resource.getWorkspace()).getRefreshManager().monitors;
		manager.unmonitor(resource);
		manager.monitor(resource);
	}
}
