/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
package org.eclipse.jface.text.contentassist;

import org.eclipse.core.commands.IHandler;


/**
 * Extends {@link org.eclipse.jface.text.contentassist.IContentAssistant} with
 * the following function:
 * <ul>
 * <li>allows to get a handler for the given command identifier</li>
 * </ul>
 *
 * @since 3.4
 */
public interface IContentAssistantExtension4 {

	/**
	 * Returns the handler for the given command identifier.
	 * <p>
	 * The same handler instance will be returned when called a more than once
	 * with the same command identifier.
	 * </p>
	 *
	 * @param commandId the command identifier
	 * @return the handler for the given command identifier
	 * @throws IllegalArgumentException if the command is not supported by this
	 *             content assistant
	 * @throws IllegalStateException if called when this content assistant is
	 *             uninstalled
	 */
	IHandler getHandler(String commandId);

}
