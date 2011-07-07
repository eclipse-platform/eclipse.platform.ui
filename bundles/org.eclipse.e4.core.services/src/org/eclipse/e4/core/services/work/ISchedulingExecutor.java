/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.work;

import org.eclipse.equinox.concurrent.future.*;
import org.osgi.framework.BundleContext;

/**
 * A service that allows clients to schedule tasks to be executed asynchronously.
 * TODO Consider adding to org.eclipse.equinox.concurrent
 */
public interface ISchedulingExecutor extends IExecutor {
	/**
	 * The OSGi service name for the scheduling executor service. This name
	 * can be used to obtain instances of the service.
	 * 
	 * @see BundleContext#getServiceReference(String)
	 */
	public static final String SERVICE_NAME = ISchedulingExecutor.class.getName();

	/**
	 * Schedules a runnable to execute at some defined time in the future.
	 * @param runnable The runnable to execute
	 * @param name A human-readable name for the task being executed
	 * @param delay The delay in milliseconds before the task should be executed
	 * @return A future that allows the caller to track progress of the execution
	 * and request cancelation.
	 */
	public IFuture schedule(IProgressRunnable runnable, String name, long delay);
}
