/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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
package org.eclipse.e4.core.commands;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * This service enables actions on handlers.
 *
 * A handler is an object which has methods that are annotated with
 * <code>@CanExecute</code> (optional) and <code>@Execute</code>. A handler is
 * associated with a <code>Command</code> which is an abstract notion of a user
 * action like "copy" and "paste".
 * <p>
 * Many handlers may be associated with a command (e.g. "copy") because each
 * handler typically only handles the command when in a specific context.
 * <p>
 * An example of different contexts, is the use of the "copy" command in a text
 * editor versus in a table. In every case the command is the same, but the code
 * executed is very different.
 * <p>
 * You should not implement this interface, an implementation is provided by
 * Eclipse.
 * <p>
 * It is usually not needed to use this service in your programs because handler
 * activation is done by Eclipse using binding contexts that are evaluated when
 * a part is made active. Execution is done by Eclipse when a user presses a
 * button or uses a context menu. However, in some cases it may be needed to
 * programmatically manipulate handler activation, or execute the handler that
 * is active for a give command.
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
 * @see ECommandService
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface EHandlerService {

	/**
	 * Makes the passed <code>handler</code> active for the command with the passed
	 * <code>commandId</code>.
	 *
	 * @param commandId Must not be <code>null</code>
	 * @param handler   Must not be <code>null</code>
	 *
	 */
	public void activateHandler(String commandId, Object handler);

	/**
	 * Deactivates the passed <code>handler</code> from the command with the passed
	 * id.
	 *
	 * @param commandId Must not be <code>null</code>
	 * @param handler   Must not be <code>null</code>
	 */
	public void deactivateHandler(String commandId, Object handler);

	/**
	 * Executes the active handler of the passed <code>command</code>.
	 *
	 * @param command Must not be <code>null</code>
	 *
	 * @return the return value of the handler, could be null
	 */
	public Object executeHandler(ParameterizedCommand command);

	/**
	 * Tests if the active handler associated with the passed <code>command</code>
	 * can be executed.
	 *
	 * @param command Must not be <code>null</code>
	 * @return true of the handler can be executed, false if it cannot be executed
	 *         or if no handler is active for the passed command.
	 */
	public boolean canExecute(ParameterizedCommand command);

	/**
	 * Execute a handler for the command.
	 *
	 * @param command       Must not be <code>null</code>
	 * @param staticContext Must not be <code>null</code>. You must dispose your
	 *                      context when you are done.
	 * @return the command return value.
	 */
	public Object executeHandler(ParameterizedCommand command, IEclipseContext staticContext);

	/**
	 * Check if a command can be executed.
	 *
	 * @param command       Must not be <code>null</code>.
	 * @param staticContext Must not be <code>null</code>. You must dispose your
	 *                      context when you are done.
	 * @return true if the command can be executed.
	 */
	public boolean canExecute(ParameterizedCommand command, IEclipseContext staticContext);
}
