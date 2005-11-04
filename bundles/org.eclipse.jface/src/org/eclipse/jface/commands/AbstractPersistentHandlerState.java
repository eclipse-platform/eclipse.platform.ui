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

package org.eclipse.jface.commands;

import org.eclipse.core.commands.AbstractHandlerState;

/**
 * <p>
 * An abstract implementation of {@link IPersistableHandlerState}. This is a
 * handler state that might (or might not) be persisted.
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
public abstract class AbstractPersistentHandlerState extends
		AbstractHandlerState implements IPersistableHandlerState {

	/**
	 * Whether this state should be persisted.
	 */
	private boolean persisted;

	/**
	 * Whether this state should be persisted. Subclasses should check this
	 * method before loading or saving.
	 * 
	 * @return <code>true</code> if this state should be persisted;
	 *         <code>false</code> otherwise.
	 */
	public final boolean isPersisted() {
		return persisted;
	}

	/**
	 * Sets whether this state should be persisted.
	 * 
	 * @param persisted
	 *            Whether this state should be persisted.
	 */
	public final void setPersisted(final boolean persisted) {
		this.persisted = persisted;
	}
}
