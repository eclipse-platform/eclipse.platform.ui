/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     ARTAL Technologies <simon.chemouil@artal.fr> - Bug 293044 added keybindings display 
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.menus.CommandMessages;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @since 3.3
 * 
 */
public class CommandElement extends QuickAccessElement {

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

	/**
	 * Returns a formatted string describes this command.
	 * 
	 * @return a description of the command of this element
	 * @since 3.6
	 */
	public String getCommand() {
		final StringBuffer label = new StringBuffer();

		try {
			Command nestedCommand = command.getCommand();
			label.append(command.getName());
			if (nestedCommand != null && nestedCommand.getDescription() != null
					&& nestedCommand.getDescription().length() != 0) {
				label.append(separator).append(nestedCommand.getDescription());
			}
		} catch (NotDefinedException e) {
			label.append(command.toString());
		}

		return label.toString();
	}

	public String getLabel() {
		String command = getCommand();
		String binding = getBinding();
		if (binding != null) {
			return NLS.bind(CommandMessages.Tooltip_Accelerator, command, binding);
		}
		return command;
	}

	/**
	 * Returns a formatted string that can be used to invoke this element's
	 * command. <code>null</code> may be returned if a binding cannot be found.
	 * 
	 * @return the string keybinding for invoking this element's command, may be
	 *         <code>null</code>
	 * @since 3.6
	 */
	public String getBinding() {
		BindingService service = (BindingService) PlatformUI.getWorkbench().getService(
				IBindingService.class);
		TriggerSequence[] triggerSeq = service.getBindingManager()
				.getActiveBindingsDisregardingContextFor(command);
		if (triggerSeq != null && triggerSeq.length > 0) {
			return triggerSeq[0].format();
		}
		return null;
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
