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
package org.eclipse.core.runtime.registry;

import java.util.EventListener;

/**
 * A registry change listener is notified of changes to extensions points in the 
 * registry.
 * These changes arise from subsequent manipulation of the registry after 
 * it was initially created.
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @since 3.0
 */
public interface IRegistryChangeListener extends EventListener {
	/**
	 * Notifies this listener that some registry changes are happening, or have 
	 * already happened.
	 * <p>
	 * The supplied event gives details. This event object (and the deltas in it) is valid 
	 * only for the duration of the invocation of this method.
	 * </p>
	 * <p>
	 * Note: This method is called by the platform; it is not intended
	 * to be called directly by clients.
	 * </p>
	 * <p>
	 * Note that during registry change event notification, further changes
	 * to the registry are disallowed.
	 * </p>
	 * 
	 * @param event the registry change event
	 * @see IExtensionDelta
	*/
	public void registryChanged(IRegistryChangeEvent event);
}
