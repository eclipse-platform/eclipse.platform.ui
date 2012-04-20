/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.commands;

import javax.annotation.PostConstruct;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.e4.core.commands.internal.CommandServiceImpl;
import org.eclipse.e4.core.commands.internal.HandlerServiceCreationFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * Provide the command and handler service as an add-on. Must be instantiated against the
 * application level context.
 */
public class CommandServiceAddon {
	@PostConstruct
	public void init(IEclipseContext context) {
		// global command service. There can be only one ... per application :-)
		CommandManager manager = new CommandManager();
		context.set(CommandManager.class, manager);
		CommandServiceImpl service = ContextInjectionFactory
				.make(CommandServiceImpl.class, context);
		context.set(ECommandService.class, service);

		// handler service - a mediator service
		context.set(EHandlerService.class.getName(), new HandlerServiceCreationFunction());
	}
}
