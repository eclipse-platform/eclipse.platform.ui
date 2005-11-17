/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.jface.bindings.BindingManager;

/**
 * This class allows clients to broker instances of <code>ICommandManager</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 */
public final class CommandManagerFactory {

	/**
	 * Creates a new instance of <code>IMutableCommandManager</code>.
	 * 
	 * @param bindingManager
	 *            The binding manager providing support for the command manager;
	 *            must not be <code>null</code>.
	 * @param commandManager
	 *            The command manager providing support for this command
	 *            manager; must not be <code>null</code>.
	 * @param contextManager
	 *            The context manager for this command manager; must not be
	 *            <code>null</code>.
	 * @return a new instance of <code>IMutableCommandManager</code>. Clients
	 *         should not make assumptions about the concrete implementation
	 *         outside the contract of the interface. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public static CommandManagerLegacyWrapper getCommandManagerWrapper(
			final BindingManager bindingManager,
			final CommandManager commandManager,
			final ContextManager contextManager) {
		return new CommandManagerLegacyWrapper(bindingManager, commandManager,
				contextManager);
	}

	private CommandManagerFactory() {
	}
}
