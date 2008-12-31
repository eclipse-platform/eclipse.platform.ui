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

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;

/**
 * Objects that are adaptable to <code>IIterable</code> can be used
 * as the default variable in an iterate expression.
 *
 * @see IAdaptable
 * @see IAdapterManager
 *
 * @since 3.3
 */
public interface IIterable {

	/**
	 * Returns an iterator to iterate over the elements.
	 *
	 * @return an iterator
	 */
	public Iterator iterator();
}