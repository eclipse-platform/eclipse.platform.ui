/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.commands.contexts.ContextManagerEvent;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.BindingManagerEvent;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.ui.commands.CommandManagerEvent;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

/**
 * Provides support for the old <code>ICommandManager</code> interface.
 * 
 * @since 3.1
 */
public final class CommandManagerWrapper implements ICommandManager,
		org.eclipse.core.commands.ICommandManagerListener,
		IBindingManagerListener, IContextManagerListener {

	/**
	 * Whether commands should print out information about which handlers are
	 * being executed. Change this value if you want console output on command
	 * execution.
	 */
	public static boolean DEBUG_COMMAND_EXECUTION = false;

	/**
	 * Whether commands should print out information about handler changes.
	 * Change this value if you want console output when commands change
	 * handlers.
	 */
	public static boolean DEBUG_HANDLERS = false;

	/**
	 * Which command should print out debugging information. Change this value
	 * if you want to only here when a command with a particular identifier
	 * changes its handler.
	 */
	public static String DEBUG_HANDLERS_COMMAND_ID = null;

	static boolean isKeyConfigurationDefinitionChildOf(String ancestor,
			String id, Map keyConfigurationDefinitionsById) {
		Collection visited = new HashSet();
		while (id != null && !visited.contains(id)) {
			KeyConfigurationDefinition keyConfigurationDefinition = (KeyConfigurationDefinition) keyConfigurationDefinitionsById
					.get(id);
			visited.add(id);
			if (keyConfigurationDefinition != null
					&& Util.equals(id = keyConfigurationDefinition
							.getParentId(), ancestor))
				return true;
		}
		return false;
	}

	static boolean validateKeySequence(KeySequence keySequence) {
		if (keySequence == null)
			return false;
		List keyStrokes = keySequence.getKeyStrokes();
		int size = keyStrokes.size();
		if (size == 0 || size > 4 || !keySequence.isComplete())
			return false;
		return true;
	}

	static void validateKeySequenceBindingDefinitions(
			Collection keySequenceBindingDefinitions) {
		Iterator iterator = keySequenceBindingDefinitions.iterator();
		while (iterator.hasNext()) {
			KeySequenceBindingDefinition keySequenceBindingDefinition = (KeySequenceBindingDefinition) iterator
					.next();
			String keyConfigurationId = keySequenceBindingDefinition
					.getKeyConfigurationId();
			KeySequence keySequence = keySequenceBindingDefinition
					.getKeySequence();
			if (keyConfigurationId == null || keySequence == null
					|| !validateKeySequence(keySequence))
				iterator.remove();
		}
	}

	/**
	 * The JFace binding machine that provides binding support for this
	 * workbench mutable command manager. This value will never be
	 * <code>null</code>.
	 * 
	 * @since 3.1
	 */
	private final BindingManager bindingManager;

	/**
	 * The command manager that provides functionality for this workbench
	 * command manager. This value will never be <code>null</code>.
	 * 
	 * @since 3.1
	 */
	private final CommandManager commandManager;

	private List commandManagerListeners;

	private ICommandRegistry commandRegistry;

	/**
	 * The context manager that provides functionality for this workbench
	 * command manager. This value will never be <code>null</code>.
	 * 
	 * @since 3.1
	 */
	private final ContextManager contextManager;

	/**
	 * The set of handlers defined in XML. This set is never <code>null</code>.
	 * It may be empty if there are either no handler submissions defined in XML
	 * or if the method <code>readRegistry</code> has not yet been called.
	 */
	private final Set definedHandlers = new HashSet();

	private IMutableCommandRegistry mutableCommandRegistry;

	/**
	 * Constructs a new instance of <code>MutableCommandManager</code>. The
	 * registry readers are constructed automatically at this time.
	 * 
	 * @param bindingManager
	 *            The binding manager providing support for the command manager;
	 *            must not be <code>null</code>.
	 * @param commandManager
	 *            The command manager providing support for this command
	 *            manager; must not be <code>null</code>.
	 * @param contextManager
	 *            The context manager to provide context support to this
	 *            manager. This value must not be <code>null</code>.
	 */
	public CommandManagerWrapper(final BindingManager bindingManager,
			final CommandManager commandManager,
			final ContextManager contextManager) {
		this(new ExtensionCommandRegistry(Platform.getExtensionRegistry()),
				new PreferenceCommandRegistry(WorkbenchPlugin.getDefault()
						.getPreferenceStore()), bindingManager, commandManager,
				contextManager);
	}

	/**
	 * Constructs a new instance of <code>MutableCommandManager</code>. The
	 * binding manager and command manager providing support for this manager
	 * are constructed at this time.
	 * 
	 * @param commandRegistry
	 *            The plug-in registry from which the commands should be read;
	 *            must not be <code>null</code>.
	 * @param mutableCommandRegistry
	 *            The preference registry from which preferences should be read;
	 *            must not be <code>null</code>.
	 * @param bindingManager
	 *            The binding manager providing support for the command manager;
	 *            must not be <code>null</code>.
	 * @param commandManager
	 *            The command manager providing support for this command
	 *            manager; must not be <code>null</code>.
	 * @param contextManager
	 *            The context manager to provide context support to this
	 *            manager. This value must not be <code>null</code>.
	 * 
	 */
	public CommandManagerWrapper(final ICommandRegistry commandRegistry,
			final IMutableCommandRegistry mutableCommandRegistry,
			final BindingManager bindingManager,
			final CommandManager commandManager,
			final ContextManager contextManager) {
		if (contextManager == null) {
			throw new NullPointerException(
					"The context manager cannot be null."); //$NON-NLS-1$
		}

		if ((commandRegistry == null) || (mutableCommandRegistry == null)) {
			throw new NullPointerException("The registries must not be null."); //$NON-NLS-1$
		}

		this.commandRegistry = commandRegistry;
		this.mutableCommandRegistry = mutableCommandRegistry;
		this.bindingManager = bindingManager;
		this.commandManager = commandManager;
		this.contextManager = contextManager;

		this.commandRegistry
				.addCommandRegistryListener(new ICommandRegistryListener() {
					public void commandRegistryChanged(
							CommandRegistryEvent commandRegistryEvent) {
						readRegistry();
					}
				});
		this.mutableCommandRegistry
				.addCommandRegistryListener(new ICommandRegistryListener() {
					public void commandRegistryChanged(
							CommandRegistryEvent commandRegistryEvent) {
						readRegistry();
					}
				});

		// Make sure to read in the registry.
		readRegistry();
	}

	public final void addCommandManagerListener(
			final ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null) {
			throw new NullPointerException("Cannot add a null listener."); //$NON-NLS-1$
		}

		if (commandManagerListeners == null) {
			commandManagerListeners = new ArrayList();
			this.commandManager.addCommandManagerListener(this);
			this.bindingManager.addBindingManagerListener(this);
			this.contextManager.addContextManagerListener(this);
		}

		if (!commandManagerListeners.contains(commandManagerListener)) {
			commandManagerListeners.add(commandManagerListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.IBindingManagerListener#bindingManagerChanged(org.eclipse.jface.bindings.BindingManagerEvent)
	 */
	public final void bindingManagerChanged(final BindingManagerEvent event) {
		final boolean schemeDefinitionsChanged = event.isSchemeDefined()
				|| event.isSchemeUndefined();
		final Set previousSchemes;
		if (schemeDefinitionsChanged) {
			previousSchemes = new HashSet();
			final Scheme scheme = event.getScheme();
			final Scheme[] definedSchemes = event.getManager()
					.getDefinedSchemes();
			final int definedSchemesCount = definedSchemes.length;
			for (int i = 0; i < definedSchemesCount; i++) {
				final Scheme definedScheme = definedSchemes[0];
				if ((definedScheme == scheme) && (event.isSchemeDefined())) {
					continue; // skip this one, it was just defined.
				}
				previousSchemes.add(definedSchemes[0].getId());
			}
			if (!event.isSchemeDefined()) {
				previousSchemes.add(scheme.getId());
			}
		} else {
			previousSchemes = null;
		}

		fireCommandManagerChanged(new CommandManagerEvent(this, false, event
				.hasActiveSchemeChanged(), event.hasLocaleChanged(), event
				.hasPlatformChanged(), false, false, event.isSchemeDefined()
				|| event.isSchemeUndefined(), null, null, previousSchemes));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.commands.ICommandManagerListener#commandManagerChanged(org.eclipse.commands.CommandManagerEvent)
	 */
	public final void commandManagerChanged(
			final org.eclipse.core.commands.CommandManagerEvent event) {
		// Figure out the set of previous category identifiers.
		final boolean categoryIdsChanged = event.isCategoryIdChanged();
		final Set previousCategoryIds;
		if (categoryIdsChanged) {
			previousCategoryIds = new HashSet(commandManager
					.getDefinedCategoryIds());
			final String categoryId = event.getCategoryId();
			if (event.isCategoryIdAdded()) {
				previousCategoryIds.remove(categoryId);
			} else {
				previousCategoryIds.add(categoryId);
			}
		} else {
			previousCategoryIds = null;
		}

		// Figure out the set of previous command identifiers.
		final boolean commandIdsChanged = event.isCommandIdChanged();
		final Set previousCommandIds;
		if (commandIdsChanged) {
			previousCommandIds = new HashSet(commandManager
					.getDefinedCommandIds());
			final String commandId = event.getCommandId();
			if (event.isCommandIdAdded()) {
				previousCommandIds.remove(commandId);
			} else {
				previousCommandIds.add(commandId);
			}
		} else {
			previousCommandIds = null;
		}
		
		fireCommandManagerChanged(new CommandManagerEvent(this, false, false,
				false, false, categoryIdsChanged, commandIdsChanged, false,
				previousCategoryIds, previousCommandIds, null));
	}

	public final void contextManagerChanged(final ContextManagerEvent event) {
		fireCommandManagerChanged(new CommandManagerEvent(this, event
				.haveActiveContextsChanged(), false, false, false, false,
				false, false, null, null, null));
	}

	private void fireCommandManagerChanged(
			CommandManagerEvent commandManagerEvent) {
		if (commandManagerEvent == null)
			throw new NullPointerException();
		if (commandManagerListeners != null)
			for (int i = 0; i < commandManagerListeners.size(); i++)
				((ICommandManagerListener) commandManagerListeners.get(i))
						.commandManagerChanged(commandManagerEvent);
	}

	public Set getActiveContextIds() {
		return contextManager.getActiveContextIds();
	}

	public String getActiveKeyConfigurationId() {
		final Scheme scheme = bindingManager.getActiveScheme();
		if (scheme != null) {
			return scheme.getId();
		}

		/*
		 * TODO This is possibly a breaking change. The id should be non-null,
		 * and presumably, a real scheme id.
		 */
		return Util.ZERO_LENGTH_STRING;
	}

	public String getActiveLocale() {
		return bindingManager.getLocale();
	}

	public String getActivePlatform() {
		return bindingManager.getPlatform();
	}

	public ICategory getCategory(String categoryId) {
		// TODO Provide access to the categories.
		// return new CategoryWrapper(commandManager.getCategory(categoryId));
		return null;
	}

	public ICommand getCommand(String commandId) {
		final Command command = commandManager.getCommand(commandId);
		return new CommandWrapper(command, bindingManager);
	}

	/**
	 * Returns the plug-in registry for this command manager.
	 * 
	 * @return The command registry; never <code>null</code>.
	 */
	public ICommandRegistry getCommandRegistry() {
		return commandRegistry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandManager#getDefinedCategoryIds()
	 */
	public Set getDefinedCategoryIds() {
		return commandManager.getDefinedCategoryIds();
	}

	public Set getDefinedCommandIds() {
		return commandManager.getDefinedCommandIds();
	}

	/**
	 * An accessor for those handlers that have been defined in XML.
	 * 
	 * @return The handlers defined in XML; never <code>null</code>, but may
	 *         be empty.
	 */
	public Set getDefinedHandlers() {
		return definedHandlers;
	}

	public Set getDefinedKeyConfigurationIds() {
		final Set definedIds = new HashSet();
		final Scheme[] schemes = bindingManager.getDefinedSchemes();
		for (int i = 0; i < schemes.length; i++) {
			definedIds.add(schemes[i].getId());
		}
		return definedIds;
	}

	public IKeyConfiguration getKeyConfiguration(String keyConfigurationId) {
		final Scheme scheme = bindingManager.getScheme(keyConfigurationId);
		return new SchemeWrapper(scheme, bindingManager);
	}

	public Map getPartialMatches(KeySequence keySequence) {
		try {
			final org.eclipse.jface.bindings.keys.KeySequence sequence = org.eclipse.jface.bindings.keys.KeySequence
					.getInstance(keySequence.toString());
			final Map partialMatches = bindingManager
					.getPartialMatches(sequence);
			final Map returnValue = new HashMap();
			final Iterator matchItr = partialMatches.entrySet().iterator();
			while (matchItr.hasNext()) {
				final Map.Entry entry = (Map.Entry) matchItr.next();
				final TriggerSequence trigger = (TriggerSequence) entry
						.getKey();
				if (trigger instanceof org.eclipse.jface.bindings.keys.KeySequence) {
					final org.eclipse.jface.bindings.keys.KeySequence triggerKey = (org.eclipse.jface.bindings.keys.KeySequence) trigger;
					returnValue.put(KeySequence.getInstance(triggerKey
							.toString()), entry.getValue());
				}
			}
			return returnValue;
		} catch (final ParseException e) {
			return new HashMap();
		} catch (final org.eclipse.ui.keys.ParseException e) {
			return new HashMap();
		}
	}

	public String getPerfectMatch(KeySequence keySequence) {
		try {
			final org.eclipse.jface.bindings.keys.KeySequence sequence = org.eclipse.jface.bindings.keys.KeySequence
					.getInstance(keySequence.toString());
			return bindingManager.getPerfectMatch(sequence);
		} catch (final ParseException e) {
			return null;
		}
	}

	public boolean isPartialMatch(KeySequence keySequence) {
		try {
			final org.eclipse.jface.bindings.keys.KeySequence sequence = org.eclipse.jface.bindings.keys.KeySequence
					.getInstance(keySequence.toString());
			return bindingManager.isPartialMatch(sequence);
		} catch (final ParseException e) {
			return false;
		}
	}

	public boolean isPerfectMatch(KeySequence keySequence) {
		try {
			final org.eclipse.jface.bindings.keys.KeySequence sequence = org.eclipse.jface.bindings.keys.KeySequence
					.getInstance(keySequence.toString());
			return bindingManager.isPerfectMatch(sequence);
		} catch (final ParseException e) {
			return false;
		}
	}

	private void readRegistry() {
		// Copy in the category definitions.
		final Collection categoryDefinitions = new ArrayList();
		categoryDefinitions.addAll(commandRegistry.getCategoryDefinitions());
		categoryDefinitions.addAll(mutableCommandRegistry
				.getCategoryDefinitions());
		final Map categoryDefinitionsById = new HashMap(CategoryDefinition
				.categoryDefinitionsById(categoryDefinitions, false));
		Iterator categoryDefinitionItr = categoryDefinitionsById.values()
				.iterator();
		while (categoryDefinitionItr.hasNext()) {
			final CategoryDefinition categoryDefinition = (CategoryDefinition) categoryDefinitionItr
					.next();
			final String name = categoryDefinition.getName();
			if ((name != null) && (name.length() > 0)) {
				final String categoryId = categoryDefinition.getId();
				final Category category = commandManager
						.getCategory(categoryId);
				category.define(categoryDefinition.getName(),
						categoryDefinition.getDescription());
			}
		}

		// Copy in the command definitions.
		Collection commandDefinitions = new ArrayList();
		commandDefinitions.addAll(commandRegistry.getCommandDefinitions());
		commandDefinitions.addAll(mutableCommandRegistry
				.getCommandDefinitions());
		Map commandDefinitionsById = new HashMap(CommandDefinition
				.commandDefinitionsById(commandDefinitions, false));
		for (Iterator iterator = commandDefinitionsById.values().iterator(); iterator
				.hasNext();) {
			CommandDefinition commandDefinition = (CommandDefinition) iterator
					.next();
			String name = commandDefinition.getName();
			if (name == null || name.length() == 0)
				iterator.remove();
		}
		final Iterator commandDefinitionItr = commandDefinitionsById.values()
				.iterator();
		while (commandDefinitionItr.hasNext()) {
			final CommandDefinition commandDefinition = (CommandDefinition) commandDefinitionItr
					.next();
			final String commandId = commandDefinition.getId();
			final Command command = commandManager.getCommand(commandId);
			command.define(commandDefinition.getName(), commandDefinition
					.getDescription(), commandManager
					.getCategory(commandDefinition.getCategoryId()));
		}

		// Copy in the handler submissions
		definedHandlers.clear();
		definedHandlers.addAll(commandRegistry.getHandlers());
		definedHandlers.addAll(mutableCommandRegistry.getHandlers());
	}

	public void removeCommandManagerListener(
			ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null) {
			throw new NullPointerException("Cannot remove a null listener"); //$NON-NLS-1$
		}
		
		if (commandManagerListeners != null) {
			commandManagerListeners.remove(commandManagerListener);
			if (commandManagerListeners.isEmpty()) {
				commandManagerListeners = null;
				this.commandManager.removeCommandManagerListener(this);
				this.bindingManager.removeBindingManagerListener(this);
				this.contextManager.removeContextManagerListener(this);
			}
		}
	}

	/**
	 * Updates the handlers for a block of commands all at once.
	 * 
	 * @param handlersByCommandId
	 *            The map of command identifier (<code>String</code>) to
	 *            handler (<code>IHandler</code>).
	 */
	public final void setHandlersByCommandId(final Map handlersByCommandId) {
		final Iterator entryItr = handlersByCommandId.entrySet().iterator();
		while (entryItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) entryItr.next();
			final String commandId = (String) entry.getKey();

			// Convert the handler to the right kind of handler.
			final Object value = entry.getValue();
			final IHandler handler;
			if (value instanceof IHandler) {
				handler = (IHandler) value;
			} else if (value == null) {
				handler = null;
			} else {
				handler = new LegacyHandlerWrapper(
						(org.eclipse.ui.commands.IHandler) value);
			}

			// Set the handler on the command.
			final Command command = commandManager.getCommand(commandId);
			command.setHandler(handler);
		}
	}
}