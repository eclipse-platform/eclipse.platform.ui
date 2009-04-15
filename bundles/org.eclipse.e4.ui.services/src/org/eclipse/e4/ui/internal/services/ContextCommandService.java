/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.services;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.ui.services.ECommandService;
import org.eclipse.e4.ui.services.IServiceConstants;

public class ContextCommandService implements ECommandService {
	public static class CommandHandlerFunction extends ContextFunction {
		private String commandId;

		public CommandHandlerFunction(String commandId) {
			this.commandId = commandId;
		}

		@Override
		public Object compute(IEclipseContext context, Object[] arguments) {
			IEclipseContext childContext = (IEclipseContext) context
					.getLocal(IServiceConstants.ACTIVE_CHILD);
			if (childContext != null) {
				return childContext.get(commandId, arguments);
			}
			if (arguments.length > 0 && "handler".equals(arguments[0])) {
				return context.get(HANDLER_PREFIX + commandId);
			}
			return context.get(CMD_PREFIX + commandId);
		}

	}

	private static final String HANDLER_PREFIX = "HDL_";
	private static final String CMD_PREFIX = "CMD_";
	private static final String CMD_CAT_PREFIX = "CMD_CAT_";

	private IEclipseContext context;

	private CommandManager commandManager;

	public ContextCommandService(IEclipseContext context) {
		this.context = context;
		commandManager = (CommandManager) context.get(CommandManager.class
				.getName());
	}

	public Category getCategory(String categoryId) {
		Category cat = (Category) context.get(CMD_CAT_PREFIX + categoryId);
		if (cat == null) {
			cat = commandManager.getCategory(CMD_CAT_PREFIX + categoryId);
			context.set(CMD_CAT_PREFIX + categoryId, cat);
		}
		return cat;
	}

	public Command getCommand(String commandId) {
		Command cmd = (Command) context.get(CMD_PREFIX + commandId);
		if (cmd == null) {
			cmd = commandManager.getCommand(commandId);
			context.set(CMD_PREFIX + commandId, cmd);
			context.set(commandId, new CommandHandlerFunction(commandId));
		}
		return cmd;
	}
}
