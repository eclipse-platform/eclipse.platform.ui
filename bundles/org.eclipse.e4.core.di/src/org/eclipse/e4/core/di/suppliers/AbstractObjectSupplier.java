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
abstract public class AbstractObjectSupplier {

	/**
	 * The OSGi service name for an object provider service. This name can be used to obtain
	 * instances of the service.
	 * 
	 * @see BundleContext#getServiceReference(String)
	 */
	public static final String SERVICE_NAME = AbstractObjectSupplier.class.getName();

	/**
	 * An OSGi service property used to indicate the context key this function should be registered
	 * in.
	 * 
	 * @see BundleContext#getServiceReference(String)
	 */
	public static final String SERVICE_CONTEXT_KEY = "dependency.injection.annotation"; //$NON-NLS-1$

	/**
	 * @param descriptor
	 * @param requestor
	 * @param track <code>true</code> if requestor needs to be updated on changes in dependent objects,
	 * <code>false</code> otherwise
	 * @param group <code>true</code> if requestor supports batched updates, <code>false</code> otherwise
	 * @return object
	 */
	abstract public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group);

	// TBD make Supplier and SupplierMulti; move bilerplate multi <-> single code into injector?
	abstract public Object[] get(IObjectDescriptor[] descriptors, IRequestor requestor, boolean track, boolean group);

	public AbstractObjectSupplier() {
		// placeholder
	}
	// TBD add dispose()
}
