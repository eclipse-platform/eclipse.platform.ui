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

import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.IRunAndTrack;

/**
 * A context is used to isolate application code from its dependencies on an
 * application framework or container. This helps avoid building in dependencies
 * on a specific framework that inhibit reuse of the application code.
 * Fundamentally a context supplies values (either data objects or services),
 * and allows values to be set.
 * <p>
 * While a context appears superficially to be a Map, it may in fact compute
 * values for requested keys dynamically rather than simply retrieving a stored
 * value.
 * </p>
 * <p>
 * Contexts may have a parent context, and may delegate lookup of a value to
 * their parent. Whether a value is computed or stored in this context or a
 * parent context is an implementation detail that clients need not be concerned
 * with. Parent contexts cannot be modified by a child context.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEclipseContext {

	/**
	 * Returns whether this context or a parent has a value stored for the given
	 * name.
	 * 
	 * @param name
	 *            The name being queried
	 * @return <code>true</code> if this context has computed a value for the
	 *         given name, and <code>false</code> otherwise.
	 */
	public boolean containsKey(String name);

	/**
	 * Returns the context value associated with the given name. Returns
	 * <code>null</code> if no such value is defined or computable by this
	 * context, or if the assigned value is <code>null</code>.
	 * <p>
	 * If the value associated with this name is an {@link IContextFunction},
	 * this method will evaluate
	 * {@link IContextFunction#compute(IEclipseContext, Object[])} with zero
	 * arguments.
	 * </p>
	 * 
	 * @param name
	 *            The name of the value to return
	 * @return An object corresponding to the given name, or <code>null</code>
	 */
	public Object get(String name);

	/**
	 * Returns the context value associated with the given name, or
	 * <code>null</code> if no such value is defined or computable by this
	 * context.
	 * <p>
	 * If the value associated with this name is an {@link IContextFunction},
	 * this method will evaluate
	 * {@link IContextFunction#compute(IEclipseContext, Object[])} with the
	 * provided arguments.
	 * </p>
	 * 
	 * @param name
	 *            The name of the value to return
	 * @return An object corresponding to the given name, or <code>null</code>
	 */
	public Object get(String name, Object[] arguments);

	/**
	 * TODO Remove this. It should be up to the context implementation to decide
	 * whether to retrieve a local value or delegate the computation to a parent
	 * or elsewhere.
	 */
	public Object getLocal(String name);

	/**
	 * Removes the given name and any corresponding value from this context.
	 * <p>
	 * Removal can never affect a parent context, so it is possible that a
	 * subsequent call to {@link #get(String)} with the same name will return a
	 * non-null result, due to a value being stored in a parent context.
	 * </p>
	 * 
	 * @param name
	 *            The name to remove
	 */
	public void remove(String name);

	/**
	 * Executes a runnable within this context. If the runnable accesses any
	 * values in this context during its execution, the runnable will be
	 * executed again after any of those values change. Only the context values
	 * accessed during the most recent invocation of the runnable are tracked.
	 * <p>
	 * This method allows a client to keep some external state synchronized with
	 * one or more values in this context. For this synchronization to work
	 * correctly, the runnable should perform its synchronization purely as a
	 * function of values in the context. If the runnable relies on mutable
	 * values that are external to the context, it will not be updated correctly
	 * when that external state changes.
	 * </p>
	 * <p>
	 * The runnable is not guaranteed to be executed synchronously with the
	 * context change that triggers its execution. Thus the context state at the
	 * time of the runnable's execution may not match the context state at the
	 * time of the relevant context change.
	 * </p>
	 * <p>
	 * The runnable does not need to be explicitly unregistered from this
	 * context when it is no longer interested in tracking changes. If a
	 * subsequent invocation of this runnable does not access any values in this
	 * context, it is automatically unregistered from change tracking on this
	 * context. Thus a provided runnable should be implemented to return
	 * immediately when change tracking is no longer needed.
	 * </p>
	 * 
	 * @param runnable
	 *            The runnable to execute and register for change tracking
	 */
	public void runAndTrack(final Runnable runnable);

	/**
	 * Executes a runnable within this context. This is a more advanced variant
	 * of {@link #runAndTrack(Runnable)} that provides additional information to
	 * the runnable. Apart from the additional data passed to the runnable, this
	 * method has the same contract as {@link #runAndTrack(Runnable)}.
	 * 
	 * @param runnable
	 *            The runnable to execute and register for change tracking
	 * @param args
	 *            Argument to be supplied to the runnable each time it executes.
	 * @see IRunAndTrack
	 */
	public void runAndTrack(final IRunAndTrack runnable, Object[] args);

	/**
	 * Sets a value to be associated with a given name in this context. The
	 * value may be an arbitrary object, or it may be an
	 * {@link IContextFunction}. In the case of a function, subsequent
	 * invocations of {@link #get(String)} with the same name will invoke
	 * {@link IContextFunction#compute(IEclipseContext, Object[])} to obtain the
	 * value. The value may be <code>null</code>.
	 * <p>
	 * Removal can never affect a parent context, so it is possible that a
	 * subsequent call to {@link #get(String)} with the same name will return a
	 * non-null result, due to a value being stored in a parent context.
	 * </p>
	 * 
	 * @param name
	 *            The name to store a value for
	 * @param value
	 *            The value to be stored, or a {@link ContextFunction} that can
	 *            return the stored value.
	 */
	public void set(String name, Object value);
}
