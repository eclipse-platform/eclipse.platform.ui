/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.commands;

/**
 * <p>
 * A listener to changes in the state of a handler.
 * </p>
 * <p>
 * Clients may implement, but must not extend this interface.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public interface IHandlerStateListener {

	/**
	 * Handles a change to the value in a handler state.
	 * 
	 * @param state
	 *            The state that has changed; never <code>null</code>. The
	 *            value for this state has been updated to the new value.
	 * @param oldValue
	 *            The old value; may be anything.
	 */
	public void handleStateChange(IHandlerState state, Object oldValue);
}
