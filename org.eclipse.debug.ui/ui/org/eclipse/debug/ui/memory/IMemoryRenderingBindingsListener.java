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
 * A rendering bindings listener is notified of changes in the renderings provided by
 * a rendering bindings provider. When bindings change, a memory block may be bound
 * to a different set of renderings.
 * <p>
 * Clients who wish to detect changes to a dynamic bindings provider should
 * implement this interface and register as a listener with the
 * {@link org.eclipse.debug.ui.memory.IMemoryRenderingManager}.
 * </p>
 * @since 3.1
 * @see IMemoryRenderingBindingsProvider#addListener
 * @see IMemoryRenderingBindingsProvider#removeListener
 */
public interface IMemoryRenderingBindingsListener {
	
	/**
	 * Notification that the bindings provided by a rendering bindings
	 * provider have changed.
	 */
	void memoryRenderingBindingsChanged();

}
