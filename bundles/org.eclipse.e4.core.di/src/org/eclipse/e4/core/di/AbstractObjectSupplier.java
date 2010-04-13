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
package org.eclipse.e4.core.di;


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

	abstract public Object get(IObjectDescriptor descriptor, IRequestor requestor);

	// TBD make Supplier and SupplierMulti; move bilerplate multi <-> single code into injector?
	abstract public Object[] get(IObjectDescriptor[] descriptors, IRequestor requestor);

	public AbstractObjectSupplier() {
		// placeholder
	}
	// TBD add dispose()
}
