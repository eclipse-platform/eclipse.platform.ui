/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

/**
 *
 * Mixin interface for IObservables that observe other objects.
 *
 * @since 1.0
 *
 */
@FunctionalInterface
public interface IObserving {

	/**
	 * Returns the observed object, or <code>null</code> if this observing
	 * object does not currently observe an object.
	 *
	 * @return the observed object, or <code>null</code>
	 */
	public Object getObserved();

}
