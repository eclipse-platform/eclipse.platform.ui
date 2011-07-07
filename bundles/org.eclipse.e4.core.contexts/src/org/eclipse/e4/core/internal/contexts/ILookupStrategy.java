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
package org.eclipse.e4.core.internal.contexts;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * A context strategy for looking up values that are not currently defined
 * in a context. The lookup strategy is consulted by the context after
 * looking for an already defined local value, but before delegating lookup
 * to a parent context.
 */
public interface ILookupStrategy {
	/**
	 * Looks up a value for the given name to be associated with the given context.
	 * @param name The name of the context value to look up
	 * @param context The context in which the lookup occurred
	 * @return The value for the given name, or <code>null</code> if no
	 * corresponding value could be found. The value may be an
	 * {@link ContextFunction}.
	 */
	public Object lookup(String name, IEclipseContext context);

	/**
	 * Returns whether this strategy is able to return a value for the given name.
	 * There is no guarantee that the result will predict a future invocation
	 * of {@link #lookup(String, IEclipseContext)} on this same strategy,
	 * due to the possibility of concurrent changes in this lookup strategy's
	 * search scope.
	 * 
	 * @param name The name of the context value to look up
	 * @param context The context in which the lookup occurred
	 * @return <code>true</code> if this strategy is able to return a 
	 * value for the given name, and <code>false</code> otherwise.
	 */
	public boolean containsKey(String name, IEclipseContext context);

	/**
	 * Disposes of this object. If this object is already disposed this method
	 * will have no effect.
	 */
	public void dispose();
}
