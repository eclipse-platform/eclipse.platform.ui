/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheetCommand;

/**
 * Execute a command defined in a cheatsheet
 */

public class CommandRunner {
	
	private ICommandService getCommandService() {
		IWorkbench wb =	PlatformUI.getWorkbench(); 
		if (wb != null) {
			Object serviceObject = wb.getAdapter(ICommandService.class);
		    if (serviceObject != null) {
			    ICommandService service = (ICommandService)serviceObject;
			    return service;
		    }
		}
		return null;
	}
	
	/**
	 * Attempt to execute a command 
	 * @param command a CheatSheetCommand created by the parser
	 * @return OK_STATUS if the command completes withour error, otherwise
	 * an error status
	 */
	public IStatus executeCommand(CheatSheetCommand command) {
		ICommandService service = getCommandService();
		if (service == null) {
			return new Status
			(IStatus.ERROR, 
			ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, 0,
			Messages.ERROR_COMMAND_SERVICE_UNAVAILABLE, null);
		}

		ParameterizedCommand selectedCommand;
		try {
			selectedCommand = service.deserialize(command.getSerialization());
			selectedCommand.executeWithChecks(null, null);
		} catch (NotDefinedException e) {
			String message = NLS.bind(Messages.ERROR_COMMAND_ID_NOT_FOUND, (new Object[] {command.getSerialization()}));
			return new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, e);		
		} catch (SerializationException e) {
			return commandFailureStatus(e);
		} catch (ExecutionException e) {
			return commandFailureStatus(e);
		} catch (NotEnabledException e) {
			return commandFailureStatus(e);
		} catch (NotHandledException e) {
			return commandFailureStatus(e);
		} catch (Exception e) {
			return commandFailureStatus(e);
		}
		
		return Status.OK_STATUS;
	}
	
	private IStatus commandFailureStatus(Exception exception) {
		return new Status
		(IStatus.ERROR, 
		ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, 0,
		Messages.ERROR_COMMAND_ERROR_STATUS, exception);
	}

}
