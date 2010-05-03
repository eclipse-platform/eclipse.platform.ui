/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.di.suppliers;

import org.osgi.framework.BundleContext;

/**
 * This interface describes an "object supplier" - something that knows how to instantiate objects
 * corresponding to the descriptor. NOTE: This is a preliminary form; this API will change.
 */
abstract public class ExtendedObjectSupplier {

	/**
	 * The OSGi service name for an object provider service. This name can be used to obtain
	 * instances of the service.
	 * 
	 * @see BundleContext#getServiceReference(String)
	 */
	public static final String SERVICE_NAME = ExtendedObjectSupplier.class.getName();

	/**
	 * An OSGi service property used to indicate the context key this function should be registered
	 * in.
	 * 
	 * @see BundleContext#getServiceReference(String)
	 */
	public static final String SERVICE_CONTEXT_KEY = "dependency.injection.annotation"; //$NON-NLS-1$

	public ExtendedObjectSupplier() {
		// placeholder
	}

	/**
	 * This method is called by the dependency injection mechanism to obtain an object corresponding
	 * to the object descriptor. If the supplier is asked to track changes, it should notify requestor 
	 * whenever it detects a change that would result in a different result produced by this method.
	 * @param descriptor descriptor of the object requested by the requestor
	 * @param requestor the originator of this request
	 * @param track <code>true</code> if the object suppliers should notify requestor of
	 * changes to the returned objects; <code>false</code> otherwise
	 * @param group <code>true</code> if the change notifications can be grouped; 
	 * <code>false</code> otherwise
	 * @return object corresponding to the object descriptor
	 */
	abstract public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group);

}
