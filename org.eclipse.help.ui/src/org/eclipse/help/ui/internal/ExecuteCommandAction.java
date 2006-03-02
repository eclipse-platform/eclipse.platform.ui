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
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.ILiveHelpAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
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
				forceDialogsOnTop();
				executeSerializedCommand();
			}
		});

	}
	
	/**
	 * This was introduced to work around the behavior described in
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=130206
	 */
	private void forceDialogsOnTop() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Shell windowShell=null;
		
		Shell[] shells = display.getShells();
		for (int i=0; i<shells.length; i++) {
			Object data = shells[i].getData();
			if (data!=null && data instanceof IWorkbenchWindow) {
				windowShell=shells[i];
				break;
			}
		}
		
		if (windowShell!=null) {
			windowShell.forceActive();
			if (Platform.getWS().equals(Platform.WS_WIN32)) {
				// feature in Windows. Without this code,
				// the window will only flash in the launch bar.
				windowShell.setVisible(false);
				windowShell.setMinimized(true);
				windowShell.setVisible(true);
				windowShell.setMinimized(false);
			}
		}
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
