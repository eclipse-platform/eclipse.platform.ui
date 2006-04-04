/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.utils;

/**
 * A string pool participant is used for sharing strings between several
 * unrelated parties.  Typically a single <code>StringPool</code> instance
 * will be created, and a group of participants will be asked to store their
 * strings in the pool.  This allows participants to share equal strings 
 * without creating explicit dependencies between each other.
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @see StringPool
 * @since 3.1
 */
public interface IStringPoolParticipant {
	/**
	 * Instructs this participant to share its strings in the provided
	 * pool.
	 */
	public void shareStrings(StringPool pool);
}
