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

/**
 * A context function encapsulates evaluation of some code within an {@link IEclipseContext}.
 * The result of the function must be derived purely from the provided arguments and
 * context objects, and must be free from side-effects other than the function's return value.
 * In particular, the function must be idempotent - subsequent invocations of the same function
 * with the same inputs must produce the same result.
 * <p>
 * A common use for context functions is as a place holder for an object that has not yet
 * been created. These place holders can be stored as values in an {@link IEclipseContext}, 
 * allowing the concrete value they represent to be computed lazily only when requested.
 * </p>
 * 
 * @see IEclipseContext#set(String, Object)
 * @noimplement This interface is not intended to be implemented by clients. Function
 * implementations must subclass {@link ContextFunction} instead.
 */
public interface IContextFunction {
	/**
	 * Evaluates the function based on the provided arguments and context to produce
	 * a consistent result.
	 * 
	 * @param context The context in which to perform the value computation.
	 * @param arguments The arguments required to compute the value, or
	 * <code>null</code> if no arguments are applicable
	 * @return The concrete value.
	 */
	public Object compute(IEclipseContext context, Object[] arguments);

}