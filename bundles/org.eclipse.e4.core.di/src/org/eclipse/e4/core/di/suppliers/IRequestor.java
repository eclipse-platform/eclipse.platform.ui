/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.di.suppliers;

import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.InjectionException;

/**
 * Requestor represents an atomary operation performed during the injection.
 * Injecting a field, or calling an injected method are examples of such
 * operations.
 * <p>
 * When an injector is asked to do a task, it splits work into a set of
 * requestors. Requestors are passed to relevant object suppliers so that 
 * requestors can be executed when values in the supplier change. (For instance,
 * an object supplier that provided the value for the injected field, is expected
 * to execute requestor again when it detects change in the injected value).
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRequestor {
	/**
	 * Call this method to perform requestor's task. 
	 * <p>
	 * Call this method when a dependent value changed
	 * <p> 
	 * @return result of the task
	 * @throws InjectionException if exception occurred while performing this task
	 */
	public Object execute() throws InjectionException;

	/**
	 * The injector that created this requestor.
	 * @return the injector that created this requestor
	 */
	public IInjector getInjector();

	/**
	 * The injected object that initiated this request
	 */
	public Object getRequestingObject();

	/**
	 * The primary object supplier used in the original injection
	 * @return primary object supplier
	 */
	public AbstractObjectSupplier getPrimarySupplier();

	/**
	 * Determines if the requestor needs to be called whenever one of 
	 * the dependent object changes.
	 * @return <code>true</code> if requestor needs to be updated on 
	 * changes in dependent objects, <code>false</code> otherwise
	 */
	public boolean shouldTrack();

	/**
	 * Determines if requestor updates can be batches.
	 * @return <code>true</code> if requestor supports batched updates,
	 * <code>false</code> otherwise
	 */
	public boolean shouldGroupUpdates();
}
