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
 * The abstract base class for all auto-refresh providers.  This class provides 
 * the infrastructure for defining an auto-refresh provider and fulfills the 
 * contract specified by the <code>org.eclipse.core.resources.refreshProviders</code> 
 * standard extension point.  
 * <p>
 * All auto-refresh providers must subclass this class. A 
 * <code>RefreshProvider</code> is responsible for creating 
 * <code>IRefreshMonitor</code> objects.  The provider must decide if 
 * it is capable of monitoring the file, or folder and subtree under the path that is given.
 * 
 * @since 3.0
 */
public abstract class RefreshProvider {
	/**
	 * Returns an <code>IRefreshMonitor</code> that will monitor a resource. If
	 * the resource is an <code>IContainer</code> the monitor will also
	 * monitor the subtree under the container. Returns <code>null</code> if
	 * this provider cannot create a monitor for the given resource.  The
	 * provider may return the same monitor instance that has been provided for
	 * other resources.
	 * <p>
	 * The monitor should send results and failures to the provided refresh
	 * result.
	 * 
	 * @param resource the resource to monitor
	 * @param result the result callback for notifying of failure or of resources that need
	 * refreshing
	 * @return a monitor on the resource, or <code>null</code>
	 * if the resource cannot be monitored
	 */
	public abstract IRefreshMonitor installMonitor(IResource resource, IRefreshResult result);
}
