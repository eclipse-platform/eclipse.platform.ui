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
//TODO: missing javadoc for params and returns
public interface IRegistryChangeEvent {
	/** 
	 * Returns all extension deltas for all hosts.
	 * 
	 * @return  all extension deltas 
	 */
	public IExtensionDelta[] getExtensionDeltas();	
	/** 
	 * Returns all extension deltas for the given host. An extension delta is 
	 * related to a host if it reports additions/removals of an extension to any 
	 * of the hosts's extension points.  
	 * 
	 * @return all extension deltas for the given host 
	 */
	public IExtensionDelta[] getExtensionDeltas(String hostId);
	/** 
	 * Returns all the extension deltas for the given host and extension point.
	 */
	public IExtensionDelta[] getExtensionDeltas(String hostId, String extensionPoint);
	/** 
	 * Returns the delta for the given host, extension point and extension. Returns null if none.
	 */
	public IExtensionDelta getExtensionDelta(String hostId, String extensionPoint, String extension);
	
}
