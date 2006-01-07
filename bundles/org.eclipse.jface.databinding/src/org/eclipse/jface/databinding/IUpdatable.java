/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding;

/**
 * An updatable supports listening to its changes.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public interface IUpdatable {

	/**
	 * Add the given change listener to this updatable. Has no effect if an
	 * identical listener is already registered.
	 * <p>
	 * Change listeners are informed about state changes that affect the value
	 * or structure of this updatable object.
	 * </p>
	 * 
	 * @param changeListener
	 */
	public void addChangeListener(IChangeListener changeListener);

	/**
	 * Removes a change listener from this updatable. Has no effect if an
	 * identical listener is not registered.
	 * @param changeListener 
	 */
	public void removeChangeListener(IChangeListener changeListener);

	/**
	 * Disposes of this updatable. This removes all remaining change listeners
	 * from this updatable.
	 */
	public void dispose();
}
