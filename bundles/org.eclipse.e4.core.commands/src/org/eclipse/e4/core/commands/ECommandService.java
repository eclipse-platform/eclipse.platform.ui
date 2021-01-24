/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
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
 *     Daniel Kruegler <daniel.kruegler@gmail.com> - Bug 493459
 *******************************************************************************/
package org.eclipse.e4.core.commands;

import java.util.Map;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.ParameterizedCommand;

/**
 * This service allows access to the command subsystem.
 *
 * A <code>command</code> is an abstract notion of a user action like "copy" and
 * "paste". A handler is an object that executes the actual code when the
 * command is activated.
 *
 * <p>
 * Many handlers may be associated with a command (e.g. "copy") because each
 * handler typically only handles the command when in a specific context. An
 * example of different contexts is the use of the "copy" command in a text
 * editor versus in a table. In each case the command is the same, but the code
 * to handle the case in that context is very different.
 * <p>
 * You should not implement this service, an implementation is provided by
 * Eclipse.
 * <p>
 * It is usually not needed to use this service in your programs because command
 * creation is done by Eclipse using the application model. However, in some
 * cases it may be needed to programmatically create commands, or activate a
 * handler using the <code>EHandlerService</code>
 * <p>
 * Example usage:
 *
 * <pre>
 * <code>
 * &#64;inject  ECommandService cs;
 * &#64;inject  EHandlerService hs;
 *
 * Command command = cs.getCommand(commandId);
 * if (command.isDefined()) {
 *	Map<String, Object> parameters = new HashMap<String, Object>();
 *	parameters.put("parm1", "hello, world");
 *	ParameterizedCommand parmCmd = cs.createCommand(commandId, parameters);
 *	if (hs.canExecute(parmCmd)) {
 *		hs.executeHandler(parmCmd);
 *	}
 *	else {logger.error("Cannot execute command");}
 * }
 * else {logger.error("Command is not defined");}
 * </code>
 * </pre>
 *
 * @since 1.0
 * @see EHandlerService
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ECommandService {
	/**
	 * Allows to create an instance of an existing command based on the id parameter
	 *
	 * @param id         - Command to create
	 * @param parameters - Map of the parameters of the command or null
	 * @return ParameterizedCommand - created command or null
	 */
	public ParameterizedCommand createCommand(String id, Map<String, ?> parameters);

	/**
	 * Allows to create an instance of an existing command based on the id parameter
	 * which does not require parameters Delegates to createCommand(String,Map)
	 * passing in null as Map parameter
	 *
	 * @param id - Command to create
	 * @return ParameterizedCommand - created command or null
	 */
	public ParameterizedCommand createCommand(String id);

	/**
	 * @param id          - Category to define
	 * @param name        - The name of this category; must not be <code>null</code>
	 * @param description - The description for this category; may be
	 *                    <code>null</code>
	 * @return the defined category
	 * @noreference
	 */
	public Category defineCategory(String id, String name, String description);

	/**
	 * @param id          - Command to define
	 * @param name        - The name of this command; must not be <code>null</code>
	 * @param description - The description for this command; may be
	 *                    <code>null</code>
	 * @param category    - The category for this command; must not be
	 *                    <code>null</code>
	 * @param parameters  - The parameters understood by this command. This value
	 *                    may be either <code>null</code> or empty if the command
	 *                    does not accept parameters
	 * @return the defined command
	 * @noreference
	 */
	public Command defineCommand(String id, String name, String description, Category category,
			IParameter[] parameters);

	/**
	 * @param id            - Command to define
	 * @param name          - The name of this command; must not be
	 *                      <code>null</code>
	 * @param description   - The description for this command; may be
	 *                      <code>null</code>
	 * @param category      - The category for this command; must not be
	 *                      <code>null</code>
	 * @param parameters    - The parameters understood by this command. This value
	 *                      may be either <code>null</code> or empty if the command
	 *                      does not accept parameters
	 * @param helpContextId - The identifier of the help context to associate with
	 *                      this command; may be <code>null</code> if this command
	 *                      does not have any help associated with it
	 * @return the defined command
	 * @noreference
	 */
	public Command defineCommand(String id, String name, String description, Category category,
			IParameter[] parameters,String helpContextId);

	/**
	 * Get category for id.
	 *
	 * @param categoryId - The category id
	 * @return the category for id
	 */
	public Category getCategory(String categoryId);

	/**
	 * Get command for id.
	 *
	 * @param commandId - The command id
	 * @return the command for id
	 */
	public Command getCommand(String commandId);
}
