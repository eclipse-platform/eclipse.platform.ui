/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.core.commands;

import java.util.Map;

/**
 * <p>
 * The parameters for a command. This interface will only be consulted if the
 * parameters need to be displayed to the user. Otherwise, they will be ignored.
 * </p>
 *
 * @since 3.1
 */
public interface IParameterValues {

	/**
	 * Returns a map keyed by externalized names for parameter values. These
	 * names should be human-readable, and are generally for display to the user
	 * in a user interface of some sort. The values should be actual values that
	 * will be interpreted by the handler for the command.
	 *
	 * @return A map of the externalizable name of the parameter value (<code>String</code>)
	 *         to the actual value of the parameter (<code>String</code>).
	 */
	@SuppressWarnings("rawtypes")
	public Map getParameterValues();
}
