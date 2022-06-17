/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     ARTAL Technologies <simon.chemouil@artal.fr> - Bug 293044 added keybindings display
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 476045
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess.providers;

import java.util.Objects;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.menus.CommandMessages;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @since 3.3
 *
 */
public class CommandElement extends QuickAccessElement {

	private ParameterizedCommand command;

	private String id;

	private CommandProvider provider;

	/* package */ CommandElement(ParameterizedCommand command, String id, CommandProvider commandProvider) {
		this.provider = commandProvider;
		this.id = id;
		this.command = command;
	}

	@Override
	public void execute() {
		if (provider != null) {
			if (provider.getHandlerService() != null && provider.getEvaluationContext() != null) {
				try {
					provider.getHandlerService().executeCommandInContext(command, null,
							provider.getEvaluationContext());
				} catch (Exception ex) {
					StatusUtil.handleStatus(ex, StatusManager.SHOW | StatusManager.LOG);
				}
				return;
			}
		}

		// let's try the old fashioned way
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IHandlerService handlerService = window.getWorkbench().getService(IHandlerService.class);
			try {
				handlerService.executeCommand(command, null);
			} catch (Exception ex) {
				StatusUtil.handleStatus(ex, StatusManager.SHOW | StatusManager.LOG);
			}
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		ICommandImageService imgService = provider.getCommandImageService();
		return (imgService == null) ? null : imgService.getImageDescriptor(getId());
	}

	/**
	 * Returns a formatted string describes this command.
	 *
	 * @return a description of the command of this element
	 * @since 3.6
	 */
	public String getCommand() {
		final StringBuilder label = new StringBuilder();

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

	@Override
	public String getLabel() {
		String command = getCommand();
		String binding = getBinding();
		if (binding != null) {
			return NLS.bind(CommandMessages.Tooltip_Accelerator, command, binding);
		}
		return command;
	}

	/**
	 * Returns a formatted string that can be used to invoke this element's command.
	 * <code>null</code> may be returned if a binding cannot be found.
	 *
	 * @return the string keybinding for invoking this element's command, may be
	 *         <code>null</code>
	 * @since 3.6
	 */
	public String getBinding() {
		BindingService service = (BindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		TriggerSequence[] triggerSeq = service.getBindingManager().getActiveBindingsDisregardingContextFor(command);
		if (triggerSeq != null && triggerSeq.length > 0) {
			return triggerSeq[0].format();
		}
		return null;
	}

	@Override
	public String getSortLabel() {
		try {
			return command.getName();
		} catch (NotDefinedException e) {
			return command.toString();
		}
	}

	@Override
	public String getMatchLabel() {
		String defaultMatchLabel = super.getMatchLabel();
		try {
			return defaultMatchLabel + ' ' + command.getCommand().getCategory().getName();
		} catch (NotDefinedException e) {
			return defaultMatchLabel;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(command);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CommandElement other = (CommandElement) obj;
		return Objects.equals(command, other.command);
	}
}
