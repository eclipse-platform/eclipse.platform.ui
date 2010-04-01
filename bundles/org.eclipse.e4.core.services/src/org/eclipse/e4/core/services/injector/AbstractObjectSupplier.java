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
package org.eclipse.e4.core.services.injector;

/**
 * This interface describes an "object supplier" - something that knows how to instantiate objects
 * corresponding to the descriptor. NOTE: This is a preliminary form; this API will change.
 */
abstract public class AbstractObjectSupplier {

	final protected IInjector injector;

	// TBD remove?
	abstract public Object get(IObjectDescriptor descriptor, IRequestor requestor);

	abstract public Object[] get(IObjectDescriptor[] descriptors, IRequestor requestor);

	public AbstractObjectSupplier(IInjector injector) {
		this.injector = injector;
	}
}
