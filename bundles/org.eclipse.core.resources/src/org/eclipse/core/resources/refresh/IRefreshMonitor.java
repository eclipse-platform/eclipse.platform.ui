/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources.refresh;

import org.eclipse.core.resources.IResource;

/**
 * An <code>IRefreshMonitor</code> monitors <code>IResources</code>.
 * <p>
 * When an <code>IRefreshMonitor</code> notices changes, it should report them 
 * to the <code>IRefreshResult</code> provided at the time of the monitor's
 * creation.
 * <p>
 * 
 * @since 3.0
 */
public interface IRefreshMonitor {
	/**
	 * Informs the monitor that it should stop monitoring the given resource.
	 * 
	 * @param resource the resource that should no longer be monitored, or
	 * <code>null</code> if this monitor should stop monitoring all resources
	 * it is currently monitoring.
	 */
	void unmonitor(IResource resource);
}