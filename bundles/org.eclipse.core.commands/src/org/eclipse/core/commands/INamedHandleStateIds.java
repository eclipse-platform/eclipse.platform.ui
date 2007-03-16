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

package org.eclipse.core.commands;

/**
 * <p>
 * State identifiers that are understood by named handle objects that implement
 * {@link IObjectWithState}.
 * </p>
 * <p>
 * Clients may implement or extend this class.
 * </p>
 * 
 * @since 3.2
 */
public interface INamedHandleStateIds {

	/**
	 * The state id used for overriding the description of a named handle
	 * object. This state's value must return a {@link String}.
	 */
	public static String DESCRIPTION = "DESCRIPTION"; //$NON-NLS-1$

	/**
	 * The state id used for overriding the name of a named handle object. This
	 * state's value must return a {@link String}.
	 */
	public static String NAME = "NAME"; //$NON-NLS-1$
}
