/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Instantiate an action that will execute the command.
 * <p>
 * This is a legacy bridge class, and should not be used outside of the
 * framework. Please use menu contributions to display a command in a menu or
 * toolbar.
 * </p>
 * <p>
 * <b>Note:</b> Clients my instantiate, but they must not subclass.
 * </p>
 * 
 * @since 3.3
 */
public class CommandAction extends Action {

	private IHandlerService handlerService = null;

	private String commandId = null;

	private ParameterizedCommand parameterizedCommand = null;

	public CommandAction(String commandIdIn, IServiceLocator serviceLocator) {
		commandId = commandIdIn;
		init(serviceLocator);
	}

	/**
	 * Creates the action backed by a parameterized command. The parameterMap
	 * must contain only all required parameters, and may contain the optional
	 * parameters.
	 * 
	 * @param commandIdIn
	 *            the command id. Must not be <code>null</code>.
	 * @param parameterMap
	 *            the parameter map. May be <code>null</code>.
	 * @param serviceLocator
	 *            The service locator that is closest in lifecycle to this
	 *            action.
	 */
	public CommandAction(String commandIdIn, Map parameterMap,
			IServiceLocator serviceLocator) {
		this(commandIdIn, serviceLocator);
		if (parameterMap != null) {
			ICommandService commandService = (ICommandService) serviceLocator
					.getService(ICommandService.class);
			createCommand(commandService, parameterMap);
		}
	}

	/**
	 * Build a command from the executable extension information.
	 * 
	 * @param commandService
	 *            to get the Command object
	 * @param parameterMap
	 */
	private void createCommand(ICommandService commandService, Map parameterMap) {
		try {
			Command cmd = commandService.getCommand(commandId);
			if (!cmd.isDefined()) {
				WorkbenchPlugin.log("Command " + commandId + " is undefined"); //$NON-NLS-1$//$NON-NLS-2$
				return;
			}
			ArrayList parameters = new ArrayList();
			Iterator i = parameterMap.keySet().iterator();
			while (i.hasNext()) {
				String parmName = (String) i.next();
				IParameter parm = cmd.getParameter(parmName);
				if (parm == null) {
					WorkbenchPlugin.log("Invalid parameter \'" + parmName //$NON-NLS-1$
							+ "\' for command " + commandId); //$NON-NLS-1$
					return;
				}
				parameters.add(new Parameterization(parm, (String) parameterMap
						.get(parmName)));
			}
			parameterizedCommand = new ParameterizedCommand(cmd,
					(Parameterization[]) parameters
							.toArray(new Parameterization[parameters.size()]));
		} catch (NotDefinedException e) {
			WorkbenchPlugin.log(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
	 */
	public void dispose() {
		// not important for command ID, maybe for command though.
		handlerService = null;
		parameterizedCommand = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(Event event) {
		if (handlerService == null) {
			WorkbenchPlugin.log("Cannot run " + commandId //$NON-NLS-1$
					+ " before command action has been initialized"); //$NON-NLS-1$
			return;
		}
		try {
			if (parameterizedCommand != null) {
				handlerService.executeCommand(parameterizedCommand, event);
			} else if (commandId != null) {
				handlerService.executeCommand(commandId, event);
			}
		} catch (Exception e) {
			WorkbenchPlugin.log(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		// hopefully this is never called
		runWithEvent(null);
	}

	private void init(IServiceLocator serviceLocator) {
		if (handlerService != null) {
			// already initialized
			return;
		}
		handlerService = (IHandlerService) serviceLocator
				.getService(IHandlerService.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getActionDefinitionId()
	 */
	public String getActionDefinitionId() {
		// This is deliberatly a no-op, since feeding a CommandAction back
		// into the command/handler system would cause a stack overflow,
		// which I've been told is bad.
		return null;
	}
}
