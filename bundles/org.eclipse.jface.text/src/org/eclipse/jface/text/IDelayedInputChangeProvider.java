/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.text;


/**
 * A delayed input change provider notifies the registered
 * {@link IInputChangedListener} about input changes that occur after the normal
 * operation of the provider.
 * <p>
 * Clients can implement that interface and its extension interfaces.</p>
 *
 * @since 3.4
 */
public interface IDelayedInputChangeProvider {

	/**
	 * Sets or clears the delayed input change listener.
	 *
	 * @param inputChangeListener the new delayed input change listener, or
	 *        <code>null</code> if none
	 * @since 3.4
	 */
	void setDelayedInputChangeListener(IInputChangedListener inputChangeListener);
}
