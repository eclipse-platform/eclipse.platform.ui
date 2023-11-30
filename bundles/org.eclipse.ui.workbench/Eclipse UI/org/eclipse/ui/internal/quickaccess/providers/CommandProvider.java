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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 476045
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess.providers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.quickaccess.QuickAccessMessages;
import org.eclipse.ui.internal.quickaccess.QuickAccessProvider;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * @since 3.3
 */
public class CommandProvider extends QuickAccessProvider {

	private IEclipseContext context;
	private ExpressionContext evaluationContext;

	public void setContext(IEclipseContext context) {
		reset();
		this.context = context;
		this.evaluationContext = new ExpressionContext(context);
	}

	private final Map<String, CommandElement> idToCommand;
	private IHandlerService handlerService;
	private ICommandService commandService;
	private EHandlerService ehandlerService;
	private ICommandImageService commandImageService;

	private boolean allCommandsRetrieved;

	public CommandProvider() {
		idToCommand = Collections.synchronizedMap(new HashMap<>());
	}

	@Override
	public String getId() {
		return "org.eclipse.ui.commands"; //$NON-NLS-1$
	}

	@Override
	public QuickAccessElement findElement(String id, String filterText) {
		retrieveCommand(id);
		return idToCommand.get(id);
	}

	@Override
	public QuickAccessElement[] getElements() {
		if (!allCommandsRetrieved) {
			ICommandService commandService = getCommandService();
			Collection<String> commandIds = commandService.getDefinedCommandIds();
			for (String commandId : commandIds) {
				retrieveCommand(commandId);
			}
			allCommandsRetrieved = true;
		}
		synchronized (idToCommand) {
			return idToCommand.values().stream().toArray(QuickAccessElement[]::new);
		}
	}

	private void retrieveCommand(final String currentCommandId) {
		boolean commandRetrieved = idToCommand.containsKey(currentCommandId);
		if (!commandRetrieved) {
			ICommandService commandService = getCommandService();
			EHandlerService ehandlerService = getEHandlerService();

			final Command command = commandService.getCommand(currentCommandId);
			ParameterizedCommand pcmd = new ParameterizedCommand(command, null);
			if (command != null && ehandlerService.canExecute(pcmd, context)) {
				try {
					Collection<ParameterizedCommand> combinations = ParameterizedCommand.generateCombinations(command);
					for (ParameterizedCommand pc : combinations) {
						String id = pc.serialize();
						synchronized (idToCommand) {
							idToCommand.put(id, new CommandElement(pc, id, this));
						}
					}
				} catch (final NotDefinedException e) {
					// It is safe to just ignore undefined commands.
				}
			}
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	@Override
	public String getName() {
		return QuickAccessMessages.QuickAccess_Commands;
	}

	EHandlerService getEHandlerService() {
		if (ehandlerService == null) {
			if (context != null) {
				ehandlerService = context.get(EHandlerService.class);
			} else {
				ehandlerService = PlatformUI.getWorkbench().getService(EHandlerService.class);
			}
		}
		return ehandlerService;
	}

	ICommandService getCommandService() {
		if (commandService == null) {
			if (context != null) {
				commandService = context.get(ICommandService.class);
			} else {
				commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
			}
		}
		return commandService;
	}

	IHandlerService getHandlerService() {
		if (handlerService == null) {
			if (context != null) {
				handlerService = context.get(IHandlerService.class);
			} else {
				handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
			}
		}
		return handlerService;
	}

	public ICommandImageService getCommandImageService() {
		if (commandImageService == null) {
			if (context != null) {
				commandImageService = context.get(ICommandImageService.class);
			} else {
				commandImageService = PlatformUI.getWorkbench().getService(ICommandImageService.class);
			}
		}
		return commandImageService;
	}

	public IEvaluationContext getEvaluationContext() {
		return evaluationContext;
	}

	@Override
	protected void doReset() {
		allCommandsRetrieved = false;
		synchronized (idToCommand) {
			idToCommand.clear();
		}
		evaluationContext = null;
		context = null;
	}

	@Override
	public boolean requiresUiAccess() {
		return true;
	}
}
