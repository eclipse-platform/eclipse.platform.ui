/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public interface IObserving {

	/**
	 * Returns the observed object, or <code>null</code> if this observing
	 * object does not currently observe an object.
	 * 
	 * @return the observed object, or <code>null</code>
	 */
	public Object getObserved();

}
