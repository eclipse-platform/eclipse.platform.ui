/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.api.observable;

/**
 * @since 3.2
 * 
 */
public interface IObservable {

	/**
	 * @param listener
	 */
	public void addChangeListener(IChangeListener listener);

	/**
	 * @param listener
	 */
	public void removeChangeListener(IChangeListener listener);

	/**
	 * @param listener
	 */
	public void addStaleListener(IStaleListener listener);

	/**
	 * @param listener
	 */
	public void removeStaleListener(IStaleListener listener);

	/**
	 * @return true if this observable's state is stale and will change soon.
	 * 
	 * @TrackedGetterO
	 */
	public boolean isStale();
	
	/**
	 * 
	 */
	public void dispose();
}
