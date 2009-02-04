/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.core.services.context;

import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.e4.core.services.context.spi.AbstractContext;
import org.eclipse.e4.core.services.context.spi.IComputedValue;

/**
 * A context is used to isolate application code from an application framework. This
 * helps avoid building in dependencies on a specific framework that inhibit reuse
 * of the application code. Fundamentally a context supplies values (either data
 * objects or services), and allows values to be set. 
 * <p>
 * While a context appears superficially to be a Map, it may in fact compute values
 * for requested keys dynamically rather than simply retrieving a stored value.
 * 
 * @noimplement This interface is not intended to be implemented by clients. Context
 * implementations must subclass {@link AbstractContext} instead.
 */
public interface IEclipseContext {

	/**
	 * TODO Remove this. It should be up to the context implementation to decide
	 * whether to retrieve a local value or delegate the computation to a parent or elsewhere.
	 */
	public Object getLocal(String name);
	
	/**
	 * Returns the context value associated with the given name, or <code>null</code>
	 * if no such value is defined or computable by this context.
	 * @param name The name of the value to return
	 * @return An object corresponding to the given name, or <code>null</code>
	 */
	public Object get(String name);
	
	public Object get(String name, String[] arguments);

	/**
	 * Sets a value to be associated with a given name in this context. The value may
	 * be an arbitrary object, or it may be a {@link ComputedValue}. In the case of 
	 * a computed value, subsequent invocations of {@link #get(String)} with the
	 * same name will invoke {@link IComputedValue#compute(IEclipseContext, String[])} if
	 * required to obtain the value.
	 * 
	 * @param name The name to store a value for
	 * @param value The value to be stored, or a {@link ComputedValue} that can return
	 * the stored value.
	 */
	public void set(String name, Object value);
	
	public void unset(String name);
	
	/**
	 * Returns whether this context has a computed value stored for the given name.
	 * @param name The name being queried
	 * @return <code>true</code> if this context has computed a value for the given
	 * name, and <code>false</code> otherwise.
	 */
	public boolean isSet(String name);
	
	// TBD should this be a part of IEclipseContext or a separate convenience method?
	public void runAndTrack(final Runnable runnable, String name);
	
	// TBD add newChild()
	// TBD add dispose() ?
}
