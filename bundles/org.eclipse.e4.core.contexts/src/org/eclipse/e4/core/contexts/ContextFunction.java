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
package org.eclipse.e4.core.contexts;

/**
 * The base class for all computed value implementations. Clients may subclass
 * this class. See the class comment of {@link IContextFunction} for specific
 * rules that must be followed by function implementations.
 * <p>
 * This class is intended to be subclassed by clients.
 * </p>
 * @see IContextFunction
 */
public abstract class ContextFunction implements IContextFunction {

	/**
	 * Constructs a new instance of the context function
	 */
	public ContextFunction() {
		// placeholder
	}

	/**
	 * @deprecated {@link IContextFunction}'s compute() was changed to take the context key
	 */
	public Object compute(IEclipseContext context) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object compute(IEclipseContext context, String contextKey) {
		// call into now-deprecated method to maintain backwards compatibility
		return compute(context);
	}

}
