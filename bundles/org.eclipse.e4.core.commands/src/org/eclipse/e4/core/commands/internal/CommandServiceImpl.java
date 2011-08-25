/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.commands.internal;

import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;

/**
 *
 */
public class CommandServiceImpl implements ECommandService {

	private CommandManager commandManager;

	@Inject
	public void setManager(CommandManager m) {
		commandManager = m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.commands.ECommandService#createCommand(org.eclipse.core.commands.Command,
	 * java.util.Map)
	 */
	public ParameterizedCommand createCommand(String id, Map parameters) {
		Command command = getCommand(id);
		if (command == null) {
			return null;
		}
		return ParameterizedCommand.generateCommand(command, parameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.commands.ECommandService#defineCategory(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public Category defineCategory(String id, String name, String description) {
		Category cat = commandManager.getCategory(id);
		if (!cat.isDefined()) {
			cat.define(name, description);
		}
		return cat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.commands.ECommandService#defineCommand(java.lang.String,
	 * java.lang.String, java.lang.String, org.eclipse.core.commands.Category)
	 */
	public Command defineCommand(String id, String name, String description, Category category,
			IParameter[] parameters) {
		Command cmd = commandManager.getCommand(id);
		if (!cmd.isDefined()) {
			cmd.define(name, description, category, parameters);
		}
		return cmd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.commands.ECommandService#getCategory(java.lang.String)
	 */
	public Category getCategory(String categoryId) {
		return commandManager.getCategory(categoryId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.commands.ECommandService#getCommand(java.lang.String)
	 */
	public Command getCommand(String commandId) {
		return commandManager.getCommand(commandId);
	}

}
