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
package org.eclipse.ui.internal.registry.experimental;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * @since 3.1
 */
public interface IConfigurationElementTracker {
	
	/**
	 * Constant for strong (normal) reference holding.
	 * 
	 * Value <code>1</code>.
	 */
	public static final int REF_STRONG = 1; 

	/**
	 * Constant for soft reference holding.
	 * 
	 * Value <code>2</code>.
	 */
	public static final int REF_SOFT = 2;
	
	/**
	 * Constant for weak reference holding.
	 * 
	 * Value <code>3</code>.
	 */
	public static final int REF_WEAK = 3;
	
	public void registerAdditionHandler(IConfigurationElementAdditionHandler handler);
	public void unregisterAdditionHandler(IConfigurationElementAdditionHandler handler);
	public void registerRemovalHandler(IConfigurationElementRemovalHandler handler);
	public void unregisterRemovalHandler(IConfigurationElementRemovalHandler handler);
	
	/**
	 * Registers an object such that when the paired configuration element is no 
	 * longer valid then the reference to the object is cleared.
	 * @param element
	 * @param object 
	 * @param referenceType
	 */
	public void registerObject(IConfigurationElement element, Object object, int referenceType);
	
	/**
	 * Get all elements bound to the provided configuration element.
	 * 
	 * @param element
	 * @return the elements
	 */
	public Object [] getObjects(IConfigurationElement element);
}
