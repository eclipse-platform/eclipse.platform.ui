/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.core.commands.common;

/**
 * <p>
 * An object that is unique identifiable based on the combination of its class
 * and its identifier.
 * </p>
 *
 * @see HandleObject
 * @since 3.2
 */
public interface IIdentifiable {

	/**
	 * Returns the identifier for this object.
	 *
	 * @return The identifier; never <code>null</code>.
	 */
	String getId();
}

