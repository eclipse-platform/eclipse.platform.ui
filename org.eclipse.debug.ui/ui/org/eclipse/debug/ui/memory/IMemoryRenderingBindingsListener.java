/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.ui.memory;

/** 
 * The listener will be notified when the rendering bindings provided by
 * the provider has changed.  When changed, a memory block has been bound to a 
 * different set of renderings.
 * 
 * Clients who wish to detect changes to a dynamic binding provider should
 * implement this interface.
 * 
 * @since 3.1
 *
 * @see IMemoryRenderingBindingsProvider#addListener
 * @see IMemoryRenderingBindingsProvider#removeListener
 */
public interface IMemoryRenderingBindingsListener {
	
	/**
	 * Memory rendering binding is changed.  
	 */
	void memoryRenderingBindingsChanged();

}
