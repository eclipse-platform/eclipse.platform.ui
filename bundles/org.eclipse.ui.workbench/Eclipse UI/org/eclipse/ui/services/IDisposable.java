/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.services;

/**
 * <p>
 * The interface that should be implemented by services that make themselves
 * available through the <code>IAdaptable</code> mechanism. This is the
 * interface that drives the majority of services provided at the workbench
 * level.
 * </p>
 * <p>
 * A service has life-cycle. When the constructor completes, the service must be
 * fully functional. When it comes time for the service to go away, then the
 * service will receive a {@link #dispose()} call. At this point, the service
 * must release all resources and detach all listeners. A service can only be
 * disposed once; it cannot be reused.
 * </p>
 * <p>
 * This interface has nothing to do with OSGi services.
 * </p>
 * <p>
 * This interface can be extended or implemented by clients.
 * </p>
 * 
 * @since 3.2
 */
public interface IDisposable {

	/**
	 * Disposes of this service. All resources must be freed. All listeners must
	 * be detached. Dispose will only be called once during the life cycle of a
	 * service.
	 */
	public void dispose();
}
