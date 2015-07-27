/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.commands.internal.ICommandHelpService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.IUpdateService;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.menus.UIElement;

/**
 * <p>
 * Provides services related to the command architecture within the workbench.
 * This service can be used to access the set of commands and handlers.
 * </p>
 *
 * @since 3.1
 */
public final class CommandService implements ICommandService, IUpdateService {

	/**
	 * The preference key prefix for all handler state.
	 */
	private static final String PREFERENCE_KEY_PREFIX = "org.eclipse.ui.commands/state"; //$NON-NLS-1$

	/**
	 * Creates a preference key for the given piece of state on the given
	 * command.
	 *
	 * @param command
	 *            The command for which the preference key should be created;
	 *            must not be <code>null</code>.
	 * @param stateId
	 *            The identifier of the state for which the preference key
	 *            should be created; must not be <code>null</code>.
	 * @return A suitable preference key; never <code>null</code>.
	 */
	static final String createPreferenceKey(final Command command,
			final String stateId) {
		return PREFERENCE_KEY_PREFIX + '/' + command.getId() + '/' + stateId;
	}

	/**
	 * The command manager that supports this service. This value is never
	 * <code>null</code>.
	 */
	private final CommandManager commandManager;

	/**
	 * The persistence class for this command service.
	 */
	private final CommandPersistence commandPersistence;

	private IEclipseContext context;

	private ICommandHelpService commandHelpService;

	/**
	 * Constructs a new instance of <code>CommandService</code> using a
	 * command manager.
	 *
	 * @param commandManager
	 *            The command manager to use; must not be <code>null</code>.
	 */
	public CommandService(final CommandManager commandManager, IEclipseContext context) {
		if (commandManager == null) {
			throw new NullPointerException(
					"Cannot create a command service with a null manager"); //$NON-NLS-1$
		}
		this.commandManager = commandManager;
		this.commandPersistence = new CommandPersistence(commandManager);
		this.context = context;
		this.commandHelpService = context.get(ICommandHelpService.class);
	}

	@Override
	public final void addExecutionListener(final IExecutionListener listener) {
		commandManager.addExecutionListener(listener);
	}

	@Override
	public final void defineUncategorizedCategory(final String name,
			final String description) {
		commandManager.defineUncategorizedCategory(name, description);
	}

	@Override
	public final ParameterizedCommand deserialize(
			final String serializedParameterizedCommand)
			throws NotDefinedException, SerializationException {
		return commandManager.deserialize(serializedParameterizedCommand);
	}

	@Override
	public final void dispose() {
		commandPersistence.dispose();

		/*
		 * All state on all commands neeeds to be disposed. This is so that the
		 * state has a chance to persist any changes.
		 */
		final Command[] commands = commandManager.getAllCommands();
		for (int i = 0; i < commands.length; i++) {
			final Command command = commands[i];
			final String[] stateIds = command.getStateIds();
			for (int j = 0; j < stateIds.length; j++) {
				final String stateId = stateIds[j];
				final State state = command.getState(stateId);
				if (state instanceof PersistentState) {
					final PersistentState persistentState = (PersistentState) state;
					if (persistentState.shouldPersist()) {
						persistentState.save(PrefUtil
								.getInternalPreferenceStore(),
								createPreferenceKey(command, stateId));
					}
				}
			}
		}
		commandCallbacks = null;
	}

	@Override
	public final Category getCategory(final String categoryId) {
		return commandManager.getCategory(categoryId);
	}

	@Override
	public final Command getCommand(final String commandId) {
		return commandManager.getCommand(commandId);
	}

	@Override
	public final Category[] getDefinedCategories() {
		return commandManager.getDefinedCategories();
	}

	@Override
	public final Collection getDefinedCategoryIds() {
		return commandManager.getDefinedCategoryIds();
	}

	@Override
	public final Collection getDefinedCommandIds() {
		return commandManager.getDefinedCommandIds();
	}

	@Override
	public final Command[] getDefinedCommands() {
		return commandManager.getDefinedCommands();
	}

	@Override
	public Collection getDefinedParameterTypeIds() {
		return commandManager.getDefinedParameterTypeIds();
	}

	@Override
	public ParameterType[] getDefinedParameterTypes() {
		return commandManager.getDefinedParameterTypes();
	}

	/**
	 * @throws NotDefinedException
	 *             if the given command is not defined
	 */
	@Override
	public final String getHelpContextId(final Command command)
			throws NotDefinedException {
		return commandHelpService.getHelpContextId(command.getId(), context);
	}

	@Override
	public final String getHelpContextId(final String commandId)
			throws NotDefinedException {
		final Command command = getCommand(commandId);
		return getHelpContextId(command);
	}

	@Override
	public ParameterType getParameterType(final String parameterTypeId) {
		return commandManager.getParameterType(parameterTypeId);
	}

	@Override
	public final void readRegistry() {
		commandPersistence.reRead();
	}

	@Override
	public final void removeExecutionListener(final IExecutionListener listener) {
		commandManager.removeExecutionListener(listener);
	}

	@Override
	public final void setHelpContextId(final IHandler handler, final String helpContextId) {
		commandHelpService.setHelpContextId(handler, helpContextId);
	}

	/**
	 * This is a map of commandIds to a list containing currently registered
	 * callbacks, in the form of ICallbackReferences.
	 */
	private Map commandCallbacks = new HashMap();

	@Override
	public final void refreshElements(String commandId, Map filter) {
		Command cmd = getCommand(commandId);

		if (!cmd.isDefined() || !(cmd.getHandler() instanceof IElementUpdater)) {
			return;
		}
		final IElementUpdater updater = (IElementUpdater) cmd.getHandler();

		if (commandCallbacks == null) {
			return;
		}

		List callbackRefs = (List) commandCallbacks.get(commandId);
		if (callbackRefs == null) {
			return;
		}

		for (Iterator i = callbackRefs.iterator(); i.hasNext();) {
			final IElementReference callbackRef = (IElementReference) i.next();
			final Map parms = Collections.unmodifiableMap(callbackRef
					.getParameters());
			ISafeRunnable run = new ISafeRunnable() {
				@Override
				public void handleException(Throwable exception) {
					WorkbenchPlugin.log("Failed to update callback: "  //$NON-NLS-1$
							+ callbackRef.getCommandId(), exception);
				}

				@Override
				public void run() throws Exception {
					updater.updateElement(callbackRef.getElement(), parms);
				}
			};
			if (filter == null) {
				SafeRunner.run(run);
			} else {
				boolean match = true;
				for (Iterator j = filter.entrySet().iterator(); j.hasNext()
						&& match;) {
					Map.Entry parmEntry = (Map.Entry) j.next();
					Object value = parms.get(parmEntry.getKey());
					if (!parmEntry.getValue().equals(value)) {
						match = false;
					}
				}
				if (match) {
					SafeRunner.run(run);
				}
			}
		}
	}

	@Override
	public final IElementReference registerElementForCommand(
			ParameterizedCommand command, UIElement element)
			throws NotDefinedException {
		if (!command.getCommand().isDefined()) {
			throw new NotDefinedException(
					"Cannot define a callback for undefined command " //$NON-NLS-1$
							+ command.getCommand().getId());
		}
		if (element == null) {
			throw new NotDefinedException("No callback defined for command " //$NON-NLS-1$
					+ command.getCommand().getId());
		}

		ElementReference ref = new ElementReference(command.getId(), element,
				command.getParameterMap());
		registerElement(ref);
		return ref;
	}

	@Override
	public void registerElement(IElementReference elementReference) {
		List parameterizedCommands = (List) commandCallbacks
				.get(elementReference.getCommandId());
		if (parameterizedCommands == null) {
			parameterizedCommands = new ArrayList();
			commandCallbacks.put(elementReference.getCommandId(),
					parameterizedCommands);
		}
		parameterizedCommands.add(elementReference);

		// If the active handler wants to update the callback, it can do
		// so now
		Command command = getCommand(elementReference.getCommandId());
		if (command.isDefined()) {
			if (command.getHandler() instanceof IElementUpdater) {
				((IElementUpdater) command.getHandler()).updateElement(
						elementReference.getElement(), elementReference
								.getParameters());
			}
		}
	}

	@Override
	public void unregisterElement(IElementReference elementReference) {
		if (commandCallbacks == null)
			return;
		List parameterizedCommands = (List) commandCallbacks
				.get(elementReference.getCommandId());
		if (parameterizedCommands != null) {
			parameterizedCommands.remove(elementReference);
			if (parameterizedCommands.isEmpty()) {
				commandCallbacks.remove(elementReference.getCommandId());
			}
		}
	}

	/**
	 * @return Returns the commandPersistence.
	 */
	public CommandPersistence getCommandPersistence() {
		return commandPersistence;
	}

	@Override
	public Runnable registerElementForUpdate(ParameterizedCommand parameterizedCommand,
			final MItem item) {
		UIElement element = new UIElement(context.get(IWorkbench.class)) {

			@Override
			public void setText(String text) {
				item.setLabel(text);
			}

			@Override
			public void setTooltip(String text) {
				item.setTooltip(text);
			}

			@Override
			public void setIcon(ImageDescriptor desc) {
				item.setIconURI(MenuHelper.getIconURI(desc, context));
			}

			@Override
			public void setDisabledIcon(ImageDescriptor desc) {
				item.getTransientData().put(IPresentationEngine.DISABLED_ICON_IMAGE_KEY,
						MenuHelper.getIconURI(desc, context));
			}

			@Override
			public void setHoverIcon(ImageDescriptor desc) {
				// ignored
			}

			@Override
			public void setChecked(boolean checked) {
				item.setSelected(checked);
			}
		};

		try {
			final IElementReference reference = registerElementForCommand(parameterizedCommand,
					element);
			return new Runnable() {
				@Override
				public void run() {
					unregisterElement(reference);
				}
			};
		} catch (NotDefinedException e) {
			WorkbenchPlugin.log(e);
		}
		return null;
	}
}
