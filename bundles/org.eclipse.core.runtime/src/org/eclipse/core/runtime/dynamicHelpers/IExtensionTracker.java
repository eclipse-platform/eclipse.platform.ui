/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.dynamicHelpers;

import org.eclipse.core.runtime.IExtension;

/**
 * An extension tracker keeps associations between extensions and their derived objects on an extension basis.
 * All extensions being added in a tracker will automatically be removed when the extension is uninstalled from the registry.
 * Users interested in extension removal can register a handler that will let them know when an object is being removed.
 * 
 * The extension tracker can be filled automatically registering a IExtensionAdditionHandler.
 *
 * This API is EXPERIMENTAL and provided as early access.
 * @since 3.1
 */
public interface IExtensionTracker {
	
	/**
	 * Constant for strong (normal) reference holding.
	 * 
	 * Value <code>1</code>.
	 */
	public static final int REF_STRONG = ReferenceHashSet.HARD; 

	/**
	 * Constant for soft reference holding.
	 * 
	 * Value <code>2</code>.
	 */
	public static final int REF_SOFT = ReferenceHashSet.SOFT;
	
	/**
	 * Constant for weak reference holding.
	 * 
	 * Value <code>3</code>.
	 */
	public static final int REF_WEAK = ReferenceHashSet.WEAK;
	
	public void registerAdditionHandler(IExtensionAdditionHandler handler);
	public void unregisterAdditionHandler(IExtensionAdditionHandler handler);
	
	public void registerRemovalHandler(IExtensionRemovalHandler handler);
	public void unregisterRemovalHandler(IExtensionRemovalHandler handler);
	
	public void registerObject(IExtension extension, Object object, int referenceType);
	
    public void unregisterObject(IExtension extension, Object object);
    
    public Object[] unregisterObject(IExtension extension);
    
	public Object[] getObjects(IExtension extension);
	
    public void close();
}
