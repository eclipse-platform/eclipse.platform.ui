/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.help.ui.internal;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.help.ILiveHelpAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * Executes a serialized parameterized command using the workbench command
 * service. This class is intended to be called by the
 * <code>executeCommand</code> function in <code>livehelp.js</code> (defined
 * in <code>org.eclipse.help</code> plugin).
 * 
 * @since 3.2
 */
public class ExecuteCommandAction implements ILiveHelpAction {

	/**
	 * Stores the serialized command for execution when the <code>run</code>
	 * method is called.
	 */
	private String serializedCommand;

	public void setInitializationString(String data) {
		serializedCommand = data;
	}

	public void run() {

		if (serializedCommand == null) {
			// No command to execute!
			return;
		}

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				executeSerializedCommand();
			}
		});

	}

	private void executeSerializedCommand() {
		try {
			ICommandService commandService = getCommandService();
			ParameterizedCommand command = commandService.deserialize(serializedCommand);
			command.executeWithChecks(null, null);
		} catch (CommandException ex) {
			// silently ignore error
		}
	}

	private static ICommandService getCommandService() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Object serviceObject = workbench.getAdapter(ICommandService.class);
		if (serviceObject != null) {
			ICommandService service = (ICommandService) serviceObject;
			return service;
		}
		return null;
	}

}
