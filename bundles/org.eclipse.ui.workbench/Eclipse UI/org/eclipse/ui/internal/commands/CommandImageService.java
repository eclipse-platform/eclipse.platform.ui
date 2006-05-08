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
package org.eclipse.ui.internal.commands;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.commands.ICommandService;

/**
 * <p>
 * Provides services related to the command architecture within the workbench.
 * This service can be used to access the set of commands and handlers.
 * </p>
 * 
 * @since 3.2
 */
public final class CommandImageService implements ICommandImageService {

	/**
	 * The command image manager that supports this service. This value is never
	 * <code>null</code>.
	 */
	private final CommandImageManager commandImageManager;

	/**
	 * The class providing persistence for this service.
	 */
	private final CommandImagePersistence commandImagePersistence;

	/**
	 * Constructs a new instance of <code>CommandService</code> using a
	 * command image manager.
	 * 
	 * @param commandImageManager
	 *            The command image manager to use; must not be
	 *            <code>null</code>.
	 * @param commandService
	 *            The workbench command service; must not be <code>null</code>.
	 *            This is used for checking whether a command is defined when
	 *            reading the registry.
	 */
	public CommandImageService(final CommandImageManager commandImageManager,
			final ICommandService commandService) {
		if (commandImageManager == null) {
			throw new NullPointerException(
					"Cannot create a command image service with a null manager"); //$NON-NLS-1$
		}
		if (commandService == null) {
			throw new NullPointerException(
					"Cannot create a command image service with a null command service"); //$NON-NLS-1$
		}
		this.commandImageManager = commandImageManager;
		this.commandImagePersistence = new CommandImagePersistence(
				commandImageManager, commandService);
	}

	public final void bind(final String commandId, final int type,
			final String style, final ImageDescriptor descriptor) {
		commandImageManager.bind(commandId, type, style, descriptor);
	}

	public final void bind(final String commandId, final int type,
			final String style, final URL url) {
		commandImageManager.bind(commandId, type, style, url);
	}

	public final void dispose() {
		commandImagePersistence.dispose();
	}

	public final String generateUnusedStyle(final String commandId) {
		return commandImageManager.generateUnusedStyle(commandId);
	}

	public final ImageDescriptor getImageDescriptor(final String commandId) {
		return commandImageManager.getImageDescriptor(commandId);
	}

	public final ImageDescriptor getImageDescriptor(final String commandId,
			final int type) {
		return commandImageManager.getImageDescriptor(commandId, type);
	}

	public final ImageDescriptor getImageDescriptor(final String commandId,
			final int type, final String style) {
		return commandImageManager.getImageDescriptor(commandId, type, style);
	}

	public final ImageDescriptor getImageDescriptor(final String commandId,
			final String style) {
		return commandImageManager.getImageDescriptor(commandId, style);
	}

	public final void readRegistry() {
		commandImagePersistence.read();
	}
}
