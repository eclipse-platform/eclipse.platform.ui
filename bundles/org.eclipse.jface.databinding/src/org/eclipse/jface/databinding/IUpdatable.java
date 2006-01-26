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
 * This interface is not intended to be implemented by clients. Instead,
 * create a subclass of the abstract base class {@link WritableUpdatable}.
 * </p>
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
public interface IUpdatable extends IReadable {
	
	/**
	 * Disposes of this updatable. This removes all remaining change listeners
	 * from this updatable, and deregisters any listeners this updatable object
	 * has registered on other (UI or model) objects.
	 */
	public void dispose();
	
	/**
	 * Returns true iff the updatable has been disposed.
	 * 
	 * @return true iff the updatable has been disposed
	 */
	public boolean isDisposed();
}
