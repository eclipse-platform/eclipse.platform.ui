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

/**
 * Registry change events describe changes to the registry. 
 * <p> 
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.0
 *
 */
public interface IRegistryChangeEvent {
	/** 
	 * Returns all extension deltas for all hosts. Returns an empty array if there are 
	 * no deltas in this event.
	 * 
	 * @return  all extension deltas 
	 */
	public IExtensionDelta[] getExtensionDeltas();	
	/** 
	 * Returns all extension deltas for the given host. Returns an empty array if there are
	 * no deltas in this event for any extension points provided by the given host. 
	 *
	 * @param hostId the host identifier 
	 * @return all extension deltas for the given host 
	 */
	public IExtensionDelta[] getExtensionDeltas(String hostId);
	/** 
	 * Returns all the extension deltas for the given host and extension point. Returns an 
	 * empty array if there are no deltas in this event for the given extension point.
	 *  
	 * @param hostId the host identifier
	 * @param extensionPoint the simple identifier of the 
	 * extension point (e.g. <code>"builders"</code>)
	 * @return all extension deltas for the given extension point
	 */
	public IExtensionDelta[] getExtensionDeltas(String hostId, String extensionPoint);
	/** 
	 * Returns the delta for the given host, extension point and extension. 
	 * Returns <code>null</code> if none exists in this event.
	 * 
	 * @param hostId the host identifier
	 * @param extensionPoint the simple identifier of the 
	 * extension point (e.g. <code>"builders"</code>)
	 * @param extension the unique identifier of the extension
	 */
	public IExtensionDelta getExtensionDelta(String hostId, String extensionPoint, String extension);	
}