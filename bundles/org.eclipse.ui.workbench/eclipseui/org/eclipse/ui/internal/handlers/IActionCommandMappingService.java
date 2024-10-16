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

package org.eclipse.ui.internal.handlers;

/**
 * <p>
 * A service which holds mappings between retarget action identifiers and
 * command identifiers (aka: action definition ids).
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 *
 * @since 3.2
 */
public interface IActionCommandMappingService {

	/**
	 * Returns the command identifier corresponding to the given action identifier,
	 * if any.
	 *
	 * @param actionId The identifier of the retarget action for which the command
	 *                 identifier should be retrieved; must not be
	 *                 <code>null</code>.
	 * @return The identifier of the corresponding command; <code>null</code> if
	 *         none.
	 */
	String getCommandId(String actionId);

	/**
	 * Maps an action identifier to a command identifier. This is used for retarget
	 * action, so that global action handlers can be registered with the correct
	 * command.
	 *
	 * @param actionId  The identifier of the retarget action; must not be
	 *                  <code>null</code>.
	 * @param commandId The identifier of the command; must not be <code>null</code>
	 */
	void map(String actionId, String commandId);

	String getGeneratedCommandId(String targetId, String actionId);
}
