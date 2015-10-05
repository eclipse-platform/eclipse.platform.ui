/*******************************************************************************
 *  Copyright (c) 2004, 2014 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.refresh;

import org.eclipse.core.internal.refresh.InternalRefreshProvider;
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
 * it is capable of monitoring the file, or folder and subtree under the path that is provided.
 *
 * @since 3.0
 */
public abstract class RefreshProvider extends InternalRefreshProvider {
	/**
	 * Creates a new refresh monitor that performs naive polling of the resource
	 * in the file system to detect changes. The returned monitor will immediately begin
	 * monitoring the specified resource root and report changes back to the workspace.
	 * <p>
	 * This default monitor can be returned by subclasses  when
	 * <code>installMonitor</code> is called.
	 * <p>
	 * If the returned monitor is not immediately returned from the <code>installMonitor</code>
	 * method, then clients are responsible for telling the returned monitor to
	 * stop polling when it is no longer needed. The returned monitor can be told to
	 * stop working by invoking <code>IRefreshMonitor.unmonitor(IResource)</code>.
	 *
	 * @param resource The resource to begin monitoring
	 * @return A refresh monitor instance
	 * @see #installMonitor(IResource, IRefreshResult)
	 */
	@Override
	protected IRefreshMonitor createPollingMonitor(IResource resource) {
		return super.createPollingMonitor(resource);
	}

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
	 * @see #createPollingMonitor(IResource)
	 */
	public abstract IRefreshMonitor installMonitor(IResource resource, IRefreshResult result);

	/**
	 * Resets the installed monitors for the given resource.  This will remove all
	 * existing monitors that are installed on the resource, and then ask all
	 * refresh providers to begin monitoring the resource again.
	 * <p>
	 * This method is intended to be used by refresh providers that need to change
	 * the refresh monitor that they previously used to monitor a resource.
	 *
	 * @param resource The resource to reset the monitors for
	 */
	@Override
	public void resetMonitors(IResource resource) {
		super.resetMonitors(resource);
	}
}
