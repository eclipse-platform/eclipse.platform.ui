/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

/**
 * Registry change events describe changes to the extension registry. 
 * <p> 
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.0
 * @see IExtensionRegistry
 * @see IRegistryChangeListener
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
	 * Returns all extension deltas for the given namespace. Returns an empty array if there are
	 * no deltas in this event for any extension points provided in the given namespace. 
	 * 
	 * @param namespace the namespace for the extension deltas 
	 * @return all extension deltas for the given namespace 
	 */
	public IExtensionDelta[] getExtensionDeltas(String namespace);

	/** 
	 * Returns all the extension deltas for the given namespace and extension point. Returns an 
	 * empty array if there are no deltas in this event for the given extension point.
	 *  
	 * @param namespace the namespace for the extension point
	 * @param extensionPoint the simple identifier of the 
	 * extension point (e.g. <code>"builders"</code>)
	 * @return all extension deltas for the given extension point
	 */
	public IExtensionDelta[] getExtensionDeltas(String namespace, String extensionPoint);

	/** 
	 * Returns the delta for the given namespace, extension point and extension. 
	 * Returns <code>null</code> if none exists in this event.
	 * 
	 * @param namespace the namespace for the extension point
	 * @param extensionPoint the simple identifier of the 
	 * extension point (e.g. <code>"builders"</code>)
	 * @param extension the unique identifier of the extension
	 * @return the extension delta, or <code>null</code>
	 */
	public IExtensionDelta getExtensionDelta(String namespace, String extensionPoint, String extension);
}
