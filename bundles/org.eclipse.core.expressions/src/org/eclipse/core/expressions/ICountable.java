/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.expressions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;

/**
 * Objects that are adaptable to <code>ICountable</code> can be used
 * as the default variable in a count expression.
 *
 * @see IAdaptable
 * @see IAdapterManager
 *
 * @since 3.3
 */
public interface ICountable {

	/**
	 * Returns the number of elements.
	 *
	 * @return the number of elements
	 */
	public int count();
}