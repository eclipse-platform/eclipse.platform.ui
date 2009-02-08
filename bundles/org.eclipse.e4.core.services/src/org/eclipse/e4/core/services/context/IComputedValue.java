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

import org.eclipse.e4.core.services.context.spi.ComputedValue;

/**
 * A computed value instance is a place holder for an object that has not yet
 * been created. These place holders can be stored as values in an {@link IEclipseContext}, 
 * allowing the concrete value they represent to be computed lazily only when requested.
 * 
 * @noimplement This interface is not intended to be implemented by clients. Computed
 * value implementations must subclass {@link ComputedValue} instead.
 */
public interface IComputedValue {
	/**
	 * Computes and returns the concrete value that this place holder represents
	 * 
	 * @param context The context in which to perform the value computation.
	 * @param arguments The arguments required to compute the value, or
	 * <code>null</code> if no arguments are applicable
	 * @return The concrete value.
	 */
	public Object compute(IEclipseContext context, Object[] arguments);

}