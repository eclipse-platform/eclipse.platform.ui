/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import java.util.EventListener;

/**
 * A plugin listener is notified of events related to the lifecycle of plug-ins. 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IPluginEvent
 * @see Platform#addPluginListener
 * @since 3.0
 * @deprecated This API will be removed before 3.0
 * Use {@link org.osgi.framework.BundleListener BundleListener} instead.
 */
public interface IPluginListener extends EventListener {
	/**
	 * Notifies this listener that changes in the lifecyle of
	 * one or more plug-ins have happened.
	 * <p>
	 * Note: This method is called asynchronously by the platform; 
	 * it is not intended to be called directly by clients.
	 *</p>
	 * @param events an array containing plug-in lifecycle events
	 * @see IPluginEvent
	 * @deprecated Use {@link org.osgi.framework.BundleListener#bundleChanged(BundleEvent) BundleListener#bundleChanged(BundleEvent)}
	 */
	public void pluginChanged(IPluginEvent[] events);
}
