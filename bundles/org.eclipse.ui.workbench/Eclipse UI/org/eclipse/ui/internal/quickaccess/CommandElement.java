/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @since 3.3
 * 
 */
public class CommandElement extends QuickAccessElement {

	private static final String separator = " - "; //$NON-NLS-1$

	private ParameterizedCommand command;

	private String id;

	/* package */CommandElement(ParameterizedCommand command, String id,
			CommandProvider commandProvider) {
		super(commandProvider);
		this.id = id;
		this.command = command;
	}

	public void execute() {
		Object o = getProvider();
		if (o instanceof CommandProvider) {
			CommandProvider provider = (CommandProvider) o;
			if (provider.getHandlerService()!=null) {
				try {
					provider.getHandlerService().executeCommandInContext(
							command, null, provider.getContextSnapshot());
				} catch (Exception ex) {
					StatusUtil.handleStatus(ex, StatusManager.SHOW
							| StatusManager.LOG);
				}
				return;
			}
		}
		
		// let's try the old fashioned way
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			IHandlerService handlerService = (IHandlerService) window
					.getWorkbench().getService(IHandlerService.class);
			try {
				handlerService.executeCommand(command, null);
			} catch (Exception ex) {
				StatusUtil.handleStatus(ex, StatusManager.SHOW
						| StatusManager.LOG);
			}
		}
	}

	public String getId() {
		return id;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getLabel() {
		try {
			Command nestedCommand = command.getCommand();
			if (nestedCommand != null && nestedCommand.getDescription() != null
					&& nestedCommand.getDescription().length() != 0) {
				return command.getName() + separator
						+ nestedCommand.getDescription();
			}
			return command.getName();
		} catch (NotDefinedException e) {
			return command.toString();
		}
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CommandElement other = (CommandElement) obj;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		return true;
	}
}
