/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import org.eclipse.core.internal.runtime.InternalPlatform;

/**
 * An abstract superclass implementing the <code>IAdaptable</code>
 * interface. <code>getAdapter</code> invocations are directed
 * to the platform's adapter manager.
 * <p>
 * Note: In situations where it would be awkward to subclass this
 * class, the same affect can be achieved simply by implementing
 * the <code>IAdaptable</code> interface and explicitly forwarding
 * the <code>getAdapter</code> request to the platform's 
 * adapter manager. The method would look like:
 * <pre>
 *     public Object getAdapter(Class adapter) {
 *         return Platform.getAdapterManager().getAdapter(this, adapter);
 *     }
 * </pre>
 * </p>
 * <p>
 * Clients may subclass.
 * </p>
 *
 * @see Platform#getAdapterManager()
 * @see IAdaptable
 */
public abstract class PlatformObject implements IAdaptable {
	/**
	 * Constructs a new platform object.
	 */
	public PlatformObject() {
		super();
	}

	/**
	 * Returns an object which is an instance of the given class
	 * associated with this object. Returns <code>null</code> if
	 * no such object can be found.
	 * <p>
	 * This implementation of the method declared by <code>IAdaptable</code>
	 * passes the request along to the platform's adapter manager; roughly
	 * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
	 * Subclasses may override this method (however, if they do so, they
	 * should invoke the method on their superclass to ensure that the
	 * Platform's adapter manager is consulted).
	 * </p>
	 *
	 * @param adapter the class to adapt to
	 * @return the adapted object or <code>null</code>
	 * @see IAdaptable#getAdapter(Class)
	 * @see Platform#getAdapterManager()
	 */
	public Object getAdapter(Class adapter) {
		return InternalPlatform.getDefault().getAdapterManager().getAdapter(this, adapter);
	}
}
