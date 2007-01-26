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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.3
 * 
 */
public class CommandAction extends Action {

	private IHandlerService handlerService = null;
	private String commandId = null;

	public CommandAction(String commandIdIn, IWorkbenchWindow window) {
		commandId = commandIdIn;
		init(window);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
	 */
	public void dispose() {
		// not important for command ID, maybe for command though.
		commandId = null;
		handlerService = null;
	}

	public void run() {
		if (handlerService == null) {
			// what, no handler service ... no problem
			return;
		}
		try {
			if (commandId != null) {
				handlerService.executeCommand(commandId, null);
			}

		} // else there is no command for this delegate
		catch (Exception e) {
			WorkbenchPlugin.log(e);
		}
	}

	public void init(IWorkbenchWindow window) {
		if (handlerService != null) {
			// already initialized
			return;
		}

		handlerService = (IHandlerService) window
				.getService(IHandlerService.class);
	}
}
