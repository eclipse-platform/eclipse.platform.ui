/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.commands.Command;
import org.eclipse.commands.CommandManager;
import org.eclipse.commands.contexts.ContextManager;
import org.eclipse.commands.misc.NotDefinedException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.BindingManagerEvent;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.ui.commands.CommandManagerEvent;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

/**
 * Provides support for the old <code>ICommandManager</code> interface.
 * 
 * @since 3.0
 */
public final class MutableCommandManager implements
        org.eclipse.commands.ICommandManagerListener, IBindingManagerListener,
        IMutableCommandManager {

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
     * @since 3.1
     */
    private final BindingManager bindingManager;

    /**
     * The command manager that provides functionality for this workbench
     * command manager. This value will never be <code>null</code>.
     * @since 3.1
     */
    private final CommandManager commandManager;

    private List commandManagerListeners;

    private ICommandRegistry commandRegistry;

    /**
     * The context manager that provides functionality for this workbench
     * command manager. This value will never be <code>null</code>.
     * @since 3.1
     */
    private final ContextManager contextManager;

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
    public MutableCommandManager(final BindingManager bindingManager,
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
    public MutableCommandManager(final ICommandRegistry commandRegistry,
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
        BindingManager.DEBUG = true;
        this.commandManager = commandManager;
        Command.DEBUG_COMMAND_EXECUTION = true;
        Command.DEBUG_HANDLERS = true;
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
        this.commandManager.addCommandManagerListener(this);
        this.bindingManager.addBindingManagerListener(this);

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
    public void bindingManagerChanged(BindingManagerEvent event) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.commands.ICommandManagerListener#commandManagerChanged(org.eclipse.commands.CommandManagerEvent)
     */
    public void commandManagerChanged(
            org.eclipse.commands.CommandManagerEvent commandManagerEvent) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub
        return null;
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
        // TODO What to do here?
        // return commandManager.getDefinedHandlers();
        return null;
    }

    public Set getDefinedKeyConfigurationIds() {
        return bindingManager.getDefinedSchemeIds();
    }

    /**
     * Builds the collection of key binding definitions that are relevant for
     * the current locale, platform and key configuration. This set is further
     * reduced based on cancelling definitions (i.e., definitions with the same
     * key sequence and context as another definition). Key binding definitions
     * with no category name or no command name are also removed from the set.
     * 
     * @return The collection of key binding definitions for the current
     *         environment (disregarding context). The collection may be empty,
     *         but is never <code>null</code>. It contains only instances of
     *         <code>KeySequenceBindingDefinition</code>.
     * @since 3.1
     */
    final Collection getKeyBindings() {
        // TODO What to do here?
        return null;
    }

    public IKeyConfiguration getKeyConfiguration(String keyConfigurationId) {
        final Scheme scheme = bindingManager.getScheme(keyConfigurationId);
        return new SchemeWrapper(scheme, bindingManager);
    }

    IMutableCommandRegistry getMutableCommandRegistry() {
        return mutableCommandRegistry;
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
                    .getDescription());
        }

        // Copy in the key configuration definitions.
        Collection keyConfigurationDefinitions = new ArrayList();
        keyConfigurationDefinitions.addAll(commandRegistry
                .getKeyConfigurationDefinitions());
        keyConfigurationDefinitions.addAll(mutableCommandRegistry
                .getKeyConfigurationDefinitions());
        Map keyConfigurationDefinitionsById = new HashMap(
                KeyConfigurationDefinition.keyConfigurationDefinitionsById(
                        keyConfigurationDefinitions, false));
        for (Iterator iterator = keyConfigurationDefinitionsById.values()
                .iterator(); iterator.hasNext();) {
            KeyConfigurationDefinition keyConfigurationDefinition = (KeyConfigurationDefinition) iterator
                    .next();
            String name = keyConfigurationDefinition.getName();
            if (name == null || name.length() == 0)
                iterator.remove();
        }
        for (Iterator iterator = keyConfigurationDefinitionsById.keySet()
                .iterator(); iterator.hasNext();)
            if (!isKeyConfigurationDefinitionChildOf(null, (String) iterator
                    .next(), keyConfigurationDefinitionsById))
                iterator.remove();
        final Iterator schemeDefinitionItr = keyConfigurationDefinitionsById
                .values().iterator();
        while (schemeDefinitionItr.hasNext()) {
            final KeyConfigurationDefinition schemeDefinition = (KeyConfigurationDefinition) schemeDefinitionItr
                    .next();
            final String schemeId = schemeDefinition.getId();
            final Scheme scheme = bindingManager.getScheme(schemeId);
            scheme.define(schemeDefinition.getName(), schemeDefinition
                    .getDescription(), schemeDefinition.getParentId());
        }

        // Set up the active scheme.
        try {
            bindingManager
                    .setActiveScheme("org.eclipse.ui.defaultAcceleratorConfiguration"); //$NON-NLS-1$
        } catch (final NotDefinedException e) {
            // Oh, well....
        }

        // Copy in the key bindings.
        List commandRegistryKeySequenceBindingDefinitions = new ArrayList(
                commandRegistry.getKeySequenceBindingDefinitions());
        validateKeySequenceBindingDefinitions(commandRegistryKeySequenceBindingDefinitions);
        List mutableCommandRegistryKeySequenceBindingDefinitions = new ArrayList(
                mutableCommandRegistry.getKeySequenceBindingDefinitions());
        validateKeySequenceBindingDefinitions(mutableCommandRegistryKeySequenceBindingDefinitions);
        final Set bindings = new HashSet();
        final Iterator registryBindingItr = commandRegistryKeySequenceBindingDefinitions
                .iterator();
        while (registryBindingItr.hasNext()) {
            final KeySequenceBindingDefinition definition = (KeySequenceBindingDefinition) registryBindingItr
                    .next();
            try {
                final org.eclipse.jface.bindings.keys.KeySequence keySequence = org.eclipse.jface.bindings.keys.KeySequence
                        .getInstance(definition.getKeySequence().toString());
                final String commandId = definition.getCommandId();
                final String schemeId = definition.getKeyConfigurationId();
                final String contextId = definition.getContextId();
                final String locale = definition.getLocale();
                final String platform = definition.getPlatform();
                bindings.add(new KeyBinding(keySequence, commandId, schemeId,
                        contextId, locale, platform, null, Binding.SYSTEM));
            } catch (final ParseException e) {
                // Ignore this binding.
            }
        }
        final Iterator preferenceBindingItr = mutableCommandRegistryKeySequenceBindingDefinitions
                .iterator();
        while (preferenceBindingItr.hasNext()) {
            final KeySequenceBindingDefinition definition = (KeySequenceBindingDefinition) preferenceBindingItr
                    .next();
            try {
                final org.eclipse.jface.bindings.keys.KeySequence keySequence = org.eclipse.jface.bindings.keys.KeySequence
                        .getInstance(definition.getKeySequence().toString());
                final String commandId = definition.getCommandId();
                final String schemeId = definition.getKeyConfigurationId();
                final String contextId = definition.getContextId();
                final String locale = definition.getLocale();
                final String platform = definition.getPlatform();
                bindings.add(new KeyBinding(keySequence, commandId, schemeId,
                        contextId, locale, platform, null, Binding.USER));
            } catch (final ParseException e) {
                // Ignore this binding.
            }
        }
        bindingManager.setBindings(bindings);
    }

    public void removeCommandManagerListener(
            ICommandManagerListener commandManagerListener) {
        if (commandManagerListener == null)
            throw new NullPointerException();
        if (commandManagerListeners != null)
            commandManagerListeners.remove(commandManagerListener);
    }

    public void setActiveContextIds(Map activeContextIds) {
        contextManager.setActiveContextIds(activeContextIds.keySet());
    }

    public void setActiveKeyConfigurationId(String activeKeyConfigurationId) {
        try {
            bindingManager.setActiveScheme(activeKeyConfigurationId);
        } catch (final NotDefinedException e) {
            // The key configuration is not defined, so do nothing.
        }
    }

    public void setActiveLocale(String activeLocale) {
        bindingManager.setLocale(activeLocale);
    }

    public void setActivePlatform(String activePlatform) {
        bindingManager.setPlatform(activePlatform);
    }

    public void setHandlersByCommandId(Map handlersByCommandId) {
        // TODO Implement.
    }
}