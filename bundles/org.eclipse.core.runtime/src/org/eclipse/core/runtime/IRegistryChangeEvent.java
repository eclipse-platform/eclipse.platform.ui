/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;
/**
 * Registry change events describe changes to the registry. 
 * <p> 
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.0
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
	 * Returns all extension deltas for the given element. Returns an empty array if there are
	 * no deltas in this event for any extension points provided by the given element. 
	 * 
	 * @param elementId the element identifier 
	 * @return all extension deltas for the given element 
	 */
	public IExtensionDelta[] getExtensionDeltas(String elementId);
	/** 
	 * Returns all the extension deltas for the given element and extension point. Returns an 
	 * empty array if there are no deltas in this event for the given extension point.
	 *  
	 * @param elementId the element identifier
	 * @param extensionPoint the simple identifier of the 
	 * extension point (e.g. <code>"builders"</code>)
	 * @return all extension deltas for the given extension point
	 */
	public IExtensionDelta[] getExtensionDeltas(String elementId, String extensionPoint);
	/** 
	 * Returns the delta for the given element, extension point and extension. 
	 * Returns <code>null</code> if none exists in this event.
	 * 
	 * @param elementId the element identifier
	 * @param extensionPoint the simple identifier of the 
	 * extension point (e.g. <code>"builders"</code>)
	 * @param extension the unique identifier of the extension
	 * @return the extension delta, or <code>null</code>
	 */
	public IExtensionDelta getExtensionDelta(String elementId, String extensionPoint, String extension);
}