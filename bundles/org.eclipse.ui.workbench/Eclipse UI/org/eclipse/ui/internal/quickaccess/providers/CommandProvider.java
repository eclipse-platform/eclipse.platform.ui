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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.quickaccess.QuickAccessMessages;
import org.eclipse.ui.internal.quickaccess.QuickAccessProvider;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
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

	Map<String, String> idToFqn;

	public CommandProvider() {
		idToCommand = Collections.synchronizedMap(new HashMap<>());
		idToFqn = Collections.synchronizedMap(new HashMap<>());
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
			if (commandService == null) {
				return null;
			}

			// initialize all the workbench commands to fqn.
			if (idToFqn.isEmpty()) {
				parseWorkbenchCommands();
			}

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

	/**
	 * Query all the commands contributions on the workbench and fill idToFqn map.
	 * FQN = plug-in id/command id
	 */
	private void parseWorkbenchCommands() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(IWorkbenchRegistryConstants.EXTENSION_COMMANDS);
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (IWorkbenchRegistryConstants.TAG_COMMAND.equals(element.getName())) {
					String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
					// plugin-id/command-id.
					String qualifiedName = element.getContributor().getName() + "/" + id; //$NON-NLS-1$
					idToFqn.put(id, qualifiedName);
				}
			}
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
						if (excludeWithActivitySupport(pc)) {
							continue;
						}
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

	private boolean excludeWithActivitySupport(ParameterizedCommand pc) {
		if (!WorkbenchActivityHelper.isFiltering()) {
			return false;
		}

		// fetch qualified name: plugin-id/command-id.
		String commandId = getQualifiedCommandId(pc.getCommand());

		if (commandId == null) {
			return false;
		}

		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
		IIdentifier identifier = workbenchActivitySupport.getActivityManager().getIdentifier(commandId);
		return !identifier.isEnabled();
	}

	/**
	 * Returns the qualified name for the given command id. That is Plgin-contributr
	 * id/command id. This is useful in asking asking activity support if the
	 * command is is enabled or not.
	 *
	 * @param command
	 * @return returns the qualified command id. i.e. contributor plug-in/command
	 *         id. It may return null.
	 */
	private String getQualifiedCommandId(Command command) {
		String cmdId = command.getId();
		// This is an auto generated command for Actions. They have qualified path
		// already. i.e. bundle-id/command-id
		if (cmdId.startsWith(IWorkbenchRegistryConstants.AUTOGENERATED_PREFIX)) {
			return cmdId.substring(IWorkbenchRegistryConstants.AUTOGENERATED_PREFIX.length());
		}

		return idToFqn.get(cmdId);
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
