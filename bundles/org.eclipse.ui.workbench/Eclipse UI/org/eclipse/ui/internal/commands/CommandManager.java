/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.ui.commands.IAction;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.IKeyBinding;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.commands.api.IActiveKeyConfigurationDefinition;
import org.eclipse.ui.internal.commands.api.ICategoryDefinition;
import org.eclipse.ui.internal.commands.api.ICommandDefinition;
import org.eclipse.ui.internal.commands.api.ICommandRegistry;
import org.eclipse.ui.internal.commands.api.ICommandRegistryEvent;
import org.eclipse.ui.internal.commands.api.ICommandRegistryListener;
import org.eclipse.ui.internal.commands.api.IContextBindingDefinition;
import org.eclipse.ui.internal.commands.api.IImageBindingDefinition;
import org.eclipse.ui.internal.commands.api.IKeyBindingDefinition;
import org.eclipse.ui.internal.commands.api.IKeyConfigurationDefinition;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.CharacterKey;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

public final class CommandManager implements ICommandManager {

	private final static String SEPARATOR = "_"; //$NON-NLS-1$

	private static CommandManager instance;

	public static CommandManager getInstance() {
		if (instance == null)
			instance = new CommandManager();
			
		return instance;
	}

	public static void validateCategoryDefinitions(Collection categoryDefinitions) {		
		Iterator iterator = categoryDefinitions.iterator();
			
		while (iterator.hasNext()) {
			ICategoryDefinition categoryDefinition = (ICategoryDefinition) iterator.next();
				
			if (categoryDefinition.getId() == null)
				iterator.remove();
		}
	}	

	public static void validateCommandDefinitions(Collection commandDefinitions) {			
		Iterator iterator = commandDefinitions.iterator();
		
		while (iterator.hasNext()) {
			ICommandDefinition commandDefinition = (ICommandDefinition) iterator.next();
			
			if (commandDefinition.getId() == null)
				iterator.remove();
		}
	}

	public static void validateContextBindingDefinitions(Collection contextBindingDefinitions) {		
		Iterator iterator = contextBindingDefinitions.iterator();
		
		while (iterator.hasNext()) {
			IContextBindingDefinition contextBindingDefinition = (IContextBindingDefinition) iterator.next();
			
			if (contextBindingDefinition.getCommandId() == null)
				iterator.remove();
		}
	}		
	
	public static void validateImageBindingDefinitions(Collection imageBindingDefinitions) {		
		Iterator iterator = imageBindingDefinitions.iterator();
		
		while (iterator.hasNext()) {
			IImageBindingDefinition imageBindingDefinition = (IImageBindingDefinition) iterator.next();
			
			// TODO
		}
	}		
	
	public static void validateKeyBindingDefinitions(Collection keyBindingDefinitions) {
		Iterator iterator = keyBindingDefinitions.iterator();
		
		while (iterator.hasNext()) {
			IKeyBindingDefinition keyBindingDefinition = (IKeyBindingDefinition) iterator.next();
			KeySequence keySequence = keyBindingDefinition.getKeySequence();
			
			if (keySequence == null || !validateKeySequence(keySequence))
				iterator.remove();
		}
	}	

	public static void validateKeyConfigurationDefinitions(Collection keyConfigurationDefinitions) {
		Iterator iterator = keyConfigurationDefinitions.iterator();
			
		while (iterator.hasNext()) {
			IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator.next();
				
			if (keyConfigurationDefinition.getId() == null)
				iterator.remove();
		}
	}	

	public static boolean validateKeySequence(KeySequence keySequence) {
		List keyStrokes = keySequence.getKeyStrokes();
		int size = keyStrokes.size();
			
		if (size == 0 || size > 4)
			return false;
		else 
			for (int i = 0; i < size; i++) {
				KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);	
	
				if (!validateKeyStroke(keyStroke))
					return false;
			}
			
		return true;
	}

	public static boolean validateKeyStroke(KeyStroke keyStroke) {
		return !keyStroke.getNaturalKey().equals(CharacterKey.getInstance('\0'));
	}

	private Map actionsById = new HashMap();	
	private List activeContextIds = Collections.EMPTY_LIST;
	private Set activeContextIdsAsSet = new HashSet();
	private String activeKeyConfigurationId = null;
	private String activeLocale = null;
	private String activePlatform = null;	
	private SortedMap categoriesById = new TreeMap();	
	private SortedMap categoryDefinitionsById = new TreeMap();
	private SortedMap commandDefinitionsById = new TreeMap();
	private ICommandManagerEvent commandManagerEvent;
	private List commandManagerListeners;
	private SortedMap commandsById = new TreeMap();	
	private SortedMap contextBindingsByCommandId = new TreeMap();	
	private SortedSet definedCategoryIds = new TreeSet();
	private SortedSet definedCommandIds = new TreeSet();
	private SortedSet definedKeyConfigurationIds = new TreeSet();	
	private Map imageBindingsByCommandId = new TreeMap();
	private Map keyBindingsByCommandId = new TreeMap();
	private KeyBindingMachine keyBindingMachine = new KeyBindingMachine();
	private SortedMap keyConfigurationDefinitionsById = new TreeMap();
	private SortedMap keyConfigurationsById = new TreeMap();	
	private PluginCommandRegistry pluginCommandRegistry;
	private SortedSet pluginContextBindingDefinitions = Util.EMPTY_SORTED_SET;
	private SortedSet pluginImageBindingDefinitions = Util.EMPTY_SORTED_SET;
	private SortedSet pluginKeyBindingDefinitions = Util.EMPTY_SORTED_SET;
	private PreferenceCommandRegistry preferenceCommandRegistry;
	private SortedSet preferenceContextBindingDefinitions = Util.EMPTY_SORTED_SET;
	private SortedSet preferenceImageBindingDefinitions = Util.EMPTY_SORTED_SET;
	private SortedSet preferenceKeyBindingDefinitions = Util.EMPTY_SORTED_SET;	

	private CommandManager() {
		String systemLocale = Locale.getDefault().toString();
		activeLocale = systemLocale != null ? systemLocale : Util.ZERO_LENGTH_STRING;
		String systemPlatform = SWT.getPlatform();
		activePlatform = systemPlatform != null ? systemPlatform : Util.ZERO_LENGTH_STRING;		
		
		if (pluginCommandRegistry == null)
			pluginCommandRegistry = new PluginCommandRegistry(Platform.getPluginRegistry());
			
		loadPluginCommandRegistry();		

		pluginCommandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
			public void commandRegistryChanged(ICommandRegistryEvent commandRegistryEvent) {
				readRegistry();
			}
		});

		if (preferenceCommandRegistry == null)
			preferenceCommandRegistry = new PreferenceCommandRegistry(WorkbenchPlugin.getDefault().getPreferenceStore());	

		loadPreferenceCommandRegistry();

		preferenceCommandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
			public void commandRegistryChanged(ICommandRegistryEvent commandRegistryEvent) {
				readRegistry();
			}
		});
		
		readRegistry();
	}

	public void addCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();
			
		if (commandManagerListeners == null)
			commandManagerListeners = new ArrayList();
		
		if (!commandManagerListeners.contains(commandManagerListener))
			commandManagerListeners.add(commandManagerListener);
	}

	public Map getActionsById() {
		return Collections.unmodifiableMap(actionsById);
	}

	public List getActiveContextIds() {
		return Collections.unmodifiableList(activeContextIds);
	}

	public String getActiveKeyConfigurationId() {		
		return activeKeyConfigurationId;
	}
	
	public String getActiveLocale() {
		return activeLocale;
	}
	
	public String getActivePlatform() {
		return activePlatform;
	}

	public ICategory getCategory(String categoryId) {
		if (categoryId == null)
			throw new NullPointerException();
			
		Category category = (Category) categoriesById.get(categoryId);
		
		if (category == null) {
			category = new Category(categoryId);
			updateCategory(category);
			categoriesById.put(categoryId, category);
		}
		
		return category;
	}

	public ICommand getCommand(String commandId) {
		if (commandId == null)
			throw new NullPointerException();
			
		Command command = (Command) commandsById.get(commandId);
		
		if (command == null) {
			command = new Command(commandId);
			updateCommand(command);
			commandsById.put(commandId, command);
		}
		
		return command;
	}

	public SortedSet getDefinedCategoryIds() {
		return Collections.unmodifiableSortedSet(definedCategoryIds);
	}
	
	public SortedSet getDefinedCommandIds() {
		return Collections.unmodifiableSortedSet(definedCommandIds);
	}
	
	public SortedSet getDefinedKeyConfigurationIds() {
		return Collections.unmodifiableSortedSet(definedKeyConfigurationIds);
	}

	public IKeyConfiguration getKeyConfiguration(String keyConfigurationId) {
		if (keyConfigurationId == null)
			throw new NullPointerException();
			
		KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
		
		if (keyConfiguration == null) {
			keyConfiguration = new KeyConfiguration(keyConfigurationId);
			updateKeyConfiguration(keyConfiguration);
			keyConfigurationsById.put(keyConfigurationId, keyConfiguration);
		}
		
		return keyConfiguration;
	}

	// TODO remove begin

	public Map getKeyBindingsByCommandId() {
		return keyBindingMachine.getKeyBindingsByCommandId();
	}
	
	public Map getKeyBindingsByCommandIdForMode() {
		return keyBindingMachine.getKeyBindingsByCommandIdForMode();
	}

	public Map getMatchesByKeySequence() {
		return keyBindingMachine.getMatchesByKeySequence();
	}

	public Map getMatchesByKeySequenceForMode() {
		return keyBindingMachine.getMatchesByKeySequenceForMode();
	}

	public KeySequence getMode() {
		return keyBindingMachine.getMode();	
	}
	
	public boolean setMode(KeySequence mode) {
		return keyBindingMachine.setMode(mode);
	}

	public String getKeyTextForCommand(String commandId) {
		String keyText = null;
		
		/* TODO
		ICommand command = getCommand(commandId);
		
		if (command.isDefined()) {
			try {
				SortedSet keyBindings = command.getKeyBindings();
	
				if (!keyBindings.isEmpty()) {
					IKeyBinding keyBinding = (IKeyBinding) keyBindings.first();
					KeySequence keySequence = keyBinding.getKeySequence();
					keyText = keySequence.format();
				}
			} catch (NotDefinedException eNotDefined) {				
			}
		}
		*/

		Map keyBindingsByCommandId = getKeyBindingsByCommandId();				
		SortedSet keyBindings = (SortedSet) keyBindingsByCommandId.get(commandId);
		
		if (keyBindings != null) {
			IKeyBinding keyBinding = (IKeyBinding) keyBindings.first();
		
			if (keyBinding != null)
				keyText = keyBinding.getKeySequence().format();
		}
			
		return keyText != null ? keyText : Util.ZERO_LENGTH_STRING;
	}
	
	public void reset() {
		loadPluginCommandRegistry();
		loadPreferenceCommandRegistry();
	}

	// TODO remove end

	public void removeCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();
			
		if (commandManagerListeners != null)
			commandManagerListeners.remove(commandManagerListener);
	}

	public void setActionsById(Map actionsById) {
		actionsById = Util.safeCopy(actionsById, String.class, IAction.class);	
	
		if (!Util.equals(actionsById, this.actionsById)) {	
			this.actionsById = actionsById;	
			fireCommandManagerChanged();
		}
	}

	public void setActiveContextIds(List activeContextIds) {
		activeContextIds = Util.safeCopy(activeContextIds, String.class);
		boolean commandManagerChanged = false;
		SortedSet updatedCommandIds = null;

		if (!this.activeContextIds.equals(activeContextIds)) {
			this.activeContextIds = activeContextIds;
			activeContextIdsAsSet = new HashSet(this.activeContextIds);			
			commandManagerChanged = true;			
			calculateKeyBindings();		
			updatedCommandIds = updateCommands(this.definedCommandIds);	
		}
		
		if (commandManagerChanged)
			fireCommandManagerChanged();

		if (updatedCommandIds != null)
			notifyCommands(updatedCommandIds);
	}

	public void setActiveLocale(String activeLocale) {
		boolean commandManagerChanged = false;
		SortedSet updatedCommandIds = null;

		if (!Util.equals(this.activeLocale, activeLocale)) {
			this.activeLocale = activeLocale;
			commandManagerChanged = true;			
			calculateImageBindings();
			calculateKeyBindings();		
			updatedCommandIds = updateCommands(this.definedCommandIds);	
		}
		
		if (commandManagerChanged)
			fireCommandManagerChanged();

		if (updatedCommandIds != null)
			notifyCommands(updatedCommandIds);
	}
	
	public void setActivePlatform(String activePlatform) {		
		boolean commandManagerChanged = false;
		SortedSet updatedCommandIds = null;

		if (!Util.equals(this.activePlatform, activePlatform)) {
			this.activePlatform = activePlatform;
			commandManagerChanged = true;			
			calculateImageBindings();
			calculateKeyBindings();			
			updatedCommandIds = updateCommands(this.definedCommandIds);	
		}
		
		if (commandManagerChanged)
			fireCommandManagerChanged();

		if (updatedCommandIds != null)
			notifyCommands(updatedCommandIds);
	}

	private void calculateContextBindings() {
		List contextBindingDefinitions = new ArrayList();		
		contextBindingDefinitions.addAll(pluginContextBindingDefinitions);
		contextBindingDefinitions.addAll(preferenceContextBindingDefinitions);
		contextBindingsByCommandId.clear();
		Iterator iterator = contextBindingDefinitions.iterator();
			
		while (iterator.hasNext()) {
			IContextBindingDefinition contextBindingDefinition = (IContextBindingDefinition) iterator.next();			
			String commandId = contextBindingDefinition.getCommandId();
			String contextId = contextBindingDefinition.getContextId();			
			SortedSet sortedSet = (SortedSet) contextBindingsByCommandId.get(commandId);
						
			if (sortedSet == null) {
				sortedSet = new TreeSet();
				contextBindingsByCommandId.put(commandId, sortedSet);						
			}
						
			sortedSet.add(new ContextBinding(contextId));
		}
	}

	private void calculateImageBindings() {
		String[] activeLocales = extend(getPath(activeLocale, SEPARATOR));
		String[] activePlatforms = extend(getPath(activePlatform, SEPARATOR));	
		// TODO
	}
	
	private void calculateKeyBindings() {
		List list = new ArrayList(this.activeContextIds);
	
		// TODO high priority. temporary fix for M3 for automatic inheritance of contexts for the specific case of the java editor scope. 
		if (list.contains("org.eclipse.jdt.ui.javaEditorScope") && !list.contains("org.eclipse.ui.textEditorScope"))
			list.add("org.eclipse.ui.textEditorScope");

		String[] activeContextIds = extend((String[]) list.toArray(new String[list.size()]));		
		String[] activeKeyConfigurationIds = extend(getKeyConfigurationIds(activeKeyConfigurationId, keyConfigurationDefinitionsById));
		String[] activeLocales = extend(getPath(activeLocale, SEPARATOR));
		String[] activePlatforms = extend(getPath(activePlatform, SEPARATOR));
		keyBindingMachine.setActiveContextIds(activeContextIds);
		keyBindingMachine.setActiveKeyConfigurationIds(activeKeyConfigurationIds);
		keyBindingMachine.setActiveLocales(activeLocales);
		keyBindingMachine.setActivePlatforms(activePlatforms);
		keyBindingMachine.setKeyBindings0(preferenceKeyBindingDefinitions);
		keyBindingMachine.setKeyBindings1(pluginKeyBindingDefinitions);		
		keyBindingsByCommandId = keyBindingMachine.getKeyBindingsByCommandId();
		
		/* TODO remove
		System.out.println("activeContextIds: " + Arrays.asList(activeContextIds));
		System.out.println("activeKeyConfigurationIds: " + Arrays.asList(activeKeyConfigurationIds));
		System.out.println("activeLocales: " + Arrays.asList(activeLocales));
		System.out.println("activePlatforms: " + Arrays.asList(activePlatforms));
		System.out.println("keyBindings0: " + preferenceKeyBindingDefinitions);
		System.out.println("keyBindings1: " + pluginKeyBindingDefinitions);
		System.out.println(keyBindingMachine.getMatchesByKeySequence());		
		System.out.println(keyBindingsByCommandId);
		*/
	}

	private String[] extend(String[] strings) {
		String[] strings2 = new String[strings.length + 1];
		System.arraycopy(strings, 0, strings2, 0, strings.length);		
		return strings2;
	}	

	private void fireCommandManagerChanged() {
		if (commandManagerListeners != null) {
			for (int i = 0; i < commandManagerListeners.size(); i++) {
				if (commandManagerEvent == null)
					commandManagerEvent = new CommandManagerEvent(this);
							
				((ICommandManagerListener) commandManagerListeners.get(i)).commandManagerChanged(commandManagerEvent);
			}				
		}		
	}
	
	private static String[] getKeyConfigurationIds(String keyConfigurationDefinitionId, Map keyConfigurationDefinitionsById) {
		List strings = new ArrayList();

		while (keyConfigurationDefinitionId != null) {	
			if (strings.contains(keyConfigurationDefinitionId))
				return new String[0];
				
			IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(keyConfigurationDefinitionId);
			
			if (keyConfigurationDefinition == null)
				return new String[0];
						
			strings.add(keyConfigurationDefinitionId);
			keyConfigurationDefinitionId = keyConfigurationDefinition.getParentId();
		}
	
		return (String[]) strings.toArray(new String[strings.size()]);
	}

	private static String[] getPath(String string, String separator) {
		if (string == null || separator == null)
			return new String[0];

		List strings = new ArrayList();
		StringBuffer stringBuffer = new StringBuffer();
		string = string.trim();
			
		if (string.length() > 0) {
			StringTokenizer stringTokenizer = new StringTokenizer(string, separator);
						
			while (stringTokenizer.hasMoreElements()) {
				if (stringBuffer.length() > 0)
					stringBuffer.append(separator);
						
				stringBuffer.append(((String) stringTokenizer.nextElement()).trim());
				strings.add(stringBuffer.toString());
			}
		}
		
		Collections.reverse(strings);		
		strings.add(Util.ZERO_LENGTH_STRING);
		return (String[]) strings.toArray(new String[strings.size()]);	
	}

	// TODO private
	public ICommandRegistry getPluginCommandRegistry() {
		return pluginCommandRegistry;
	}

	// TODO private
	public ICommandRegistry getPreferenceCommandRegistry() {
		return preferenceCommandRegistry;
	}

	private boolean inContext(Collection contextBindings) {
		if (contextBindings.isEmpty())
			return true;
		
		Iterator iterator = activeContextIds.iterator();
		
		while (iterator.hasNext()) {
			String activeContextId = (String) iterator.next();
			
			if (contextBindings.contains(new ContextBinding(activeContextId)));
				return true;			
		}
		
		return false;
	}

	private void loadPluginCommandRegistry() {
		try {
			pluginCommandRegistry.load();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}
	}

	private void loadPreferenceCommandRegistry() {
		try {
			preferenceCommandRegistry.load();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}		
	}

	private void notifyCategories(Collection categoryIds) {	
		Iterator iterator = categoryIds.iterator();
		
		while (iterator.hasNext()) {
			String categoryId = (String) iterator.next();					
			Category category = (Category) categoriesById.get(categoryId);
			
			if (category != null)
				category.fireCategoryChanged();
		}
	}

	private void notifyCommands(Collection commandIds) {	
		Iterator iterator = commandIds.iterator();
		
		while (iterator.hasNext()) {
			String commandId = (String) iterator.next();					
			Command command = (Command) commandsById.get(commandId);
			
			if (command != null)
				command.fireCommandChanged();
		}
	}

	private void notifyKeyConfigurations(Collection keyConfigurationIds) {	
		Iterator iterator = keyConfigurationIds.iterator();
		
		while (iterator.hasNext()) {
			String keyConfigurationId = (String) iterator.next();					
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
			
			if (keyConfiguration != null)
				keyConfiguration.fireKeyConfigurationChanged();
		}
	}

	private void readRegistry() {		
		List categoryDefinitions = new ArrayList();
		categoryDefinitions.addAll(pluginCommandRegistry.getCategoryDefinitions());
		categoryDefinitions.addAll(preferenceCommandRegistry.getCategoryDefinitions());		
		validateCategoryDefinitions(categoryDefinitions);
		SortedMap categoryDefinitionsById = new TreeMap(CategoryDefinition.categoryDefinitionsById(categoryDefinitions, false));
		SortedSet definedCategoryIds = new TreeSet(categoryDefinitionsById.keySet());				
		List commandDefinitions = new ArrayList();
		commandDefinitions.addAll(pluginCommandRegistry.getCommandDefinitions());
		commandDefinitions.addAll(preferenceCommandRegistry.getCommandDefinitions());
		validateCommandDefinitions(commandDefinitions);
		SortedMap commandDefinitionsById = new TreeMap(CommandDefinition.commandDefinitionsById(commandDefinitions, false));
		SortedSet definedCommandIds = new TreeSet(commandDefinitionsById.keySet());		
		List keyConfigurationDefinitions = new ArrayList();
		keyConfigurationDefinitions.addAll(pluginCommandRegistry.getKeyConfigurationDefinitions());
		keyConfigurationDefinitions.addAll(preferenceCommandRegistry.getKeyConfigurationDefinitions());
		validateKeyConfigurationDefinitions(keyConfigurationDefinitions);		
		SortedMap keyConfigurationDefinitionsById = new TreeMap(KeyConfigurationDefinition.keyConfigurationDefinitionsById(keyConfigurationDefinitions, false));
		SortedSet definedKeyConfigurationIds = new TreeSet(keyConfigurationDefinitionsById.keySet());						
		List activeKeyConfigurationDefinitions = new ArrayList();
		activeKeyConfigurationDefinitions.addAll(pluginCommandRegistry.getActiveKeyConfigurationDefinitions());
		activeKeyConfigurationDefinitions.addAll(preferenceCommandRegistry.getActiveKeyConfigurationDefinitions());
		String activeKeyConfigurationId = null;
		
		if (activeKeyConfigurationDefinitions.size() >= 1) {
			IActiveKeyConfigurationDefinition activeKeyConfigurationDefinition = (IActiveKeyConfigurationDefinition) activeKeyConfigurationDefinitions.get(activeKeyConfigurationDefinitions.size() - 1);
			activeKeyConfigurationId = activeKeyConfigurationDefinition.getKeyConfigurationId();
			
			if (activeKeyConfigurationId != null && !keyConfigurationDefinitionsById.containsKey(activeKeyConfigurationId))
				activeKeyConfigurationId = null;
		}

		boolean commandManagerChanged = false;

		if (!Util.equals(this.activeKeyConfigurationId, activeKeyConfigurationId)) {
			this.activeKeyConfigurationId = activeKeyConfigurationId;
			commandManagerChanged = true;
		}
		
		if (!this.definedCategoryIds.equals(definedCategoryIds)) {
			this.definedCategoryIds = definedCategoryIds;
			commandManagerChanged = true;	
		}

		if (!this.definedCommandIds.equals(definedCommandIds)) {
			this.definedCommandIds = definedCommandIds;
			commandManagerChanged = true;
		}

		if (!this.definedKeyConfigurationIds.equals(definedKeyConfigurationIds)) {
			this.definedKeyConfigurationIds = definedKeyConfigurationIds;
			commandManagerChanged = true;	
		}

		this.categoryDefinitionsById = categoryDefinitionsById;	
		this.commandDefinitionsById = commandDefinitionsById;
		this.keyConfigurationDefinitionsById = keyConfigurationDefinitionsById;
		pluginContextBindingDefinitions = new TreeSet(pluginCommandRegistry.getContextBindingDefinitions());
		validateContextBindingDefinitions(pluginContextBindingDefinitions);
		preferenceContextBindingDefinitions = new TreeSet(preferenceCommandRegistry.getContextBindingDefinitions());
		validateContextBindingDefinitions(preferenceContextBindingDefinitions);
		pluginImageBindingDefinitions = new TreeSet(pluginCommandRegistry.getImageBindingDefinitions());
		validateImageBindingDefinitions(pluginImageBindingDefinitions);
		preferenceImageBindingDefinitions = new TreeSet(preferenceCommandRegistry.getImageBindingDefinitions());
		validateImageBindingDefinitions(preferenceImageBindingDefinitions);
		pluginKeyBindingDefinitions = new TreeSet(pluginCommandRegistry.getKeyBindingDefinitions());
		validateKeyBindingDefinitions(pluginKeyBindingDefinitions);
		preferenceKeyBindingDefinitions = new TreeSet(preferenceCommandRegistry.getKeyBindingDefinitions());
		validateKeyBindingDefinitions(preferenceKeyBindingDefinitions);
		calculateContextBindings();
		calculateImageBindings();
		calculateKeyBindings();
		SortedSet updatedCategoryIds = updateCategories(this.definedCategoryIds);	
		SortedSet updatedCommandIds = updateCommands(this.definedCommandIds);	
		SortedSet updatedKeyConfigurationIds = updateKeyConfigurations(this.definedKeyConfigurationIds);	

		if (commandManagerChanged)
			fireCommandManagerChanged();

		if (updatedCategoryIds != null)
			notifyCategories(updatedCategoryIds);
		
		if (updatedCommandIds != null)
			notifyCommands(updatedCommandIds);

		if (updatedKeyConfigurationIds != null)
			notifyKeyConfigurations(updatedKeyConfigurationIds);
	}

	private SortedSet updateCategories(Collection categoryIds) {
		SortedSet updatedIds = new TreeSet();
		Iterator iterator = categoryIds.iterator();
		
		while (iterator.hasNext()) {
			String categoryId = (String) iterator.next();					
			Category category = (Category) categoriesById.get(categoryId);
			
			if (category != null && updateCategory(category))
				updatedIds.add(categoryId);			
		}
		
		return updatedIds;
	}

	private boolean updateCategory(Category category) {
		boolean updated = false;
		ICategoryDefinition categoryDefinition = (ICategoryDefinition) categoryDefinitionsById.get(category.getId());
		updated |= category.setDefined(categoryDefinition != null);
		updated |= category.setDescription(categoryDefinition != null ? categoryDefinition.getDescription() : null);
		updated |= category.setName(categoryDefinition != null ? categoryDefinition.getName() : null);
		return updated;		
	}

	private boolean updateCommand(Command command) {
		boolean updated = false;
		SortedSet contextBindings = (SortedSet) contextBindingsByCommandId.get(command.getId());
		updated |= command.setActive(contextBindings != null ? inContext(contextBindings) : true);
		ICommandDefinition commandDefinition = (ICommandDefinition) commandDefinitionsById.get(command.getId());
		updated |= command.setCategoryId(commandDefinition != null ? commandDefinition.getCategoryId() : null);
		updated |= command.setContextBindings(contextBindings != null ? contextBindings : Util.EMPTY_SORTED_SET);
		updated |= command.setDefined(commandDefinition != null);
		updated |= command.setDescription(commandDefinition != null ? commandDefinition.getDescription() : null);
		updated |= command.setHelpId(commandDefinition != null ? commandDefinition.getHelpId() : null);
		SortedSet imageBindings = (SortedSet) imageBindingsByCommandId.get(command.getId());
		updated |= command.setImageBindings(imageBindings != null ? imageBindings : Util.EMPTY_SORTED_SET);
		SortedSet keyBindings = (SortedSet) keyBindingsByCommandId.get(command.getId());
		updated |= command.setKeyBindings(keyBindings != null ? keyBindings : Util.EMPTY_SORTED_SET);
		updated |= command.setName(commandDefinition != null ? commandDefinition.getName() : null);
		return updated;
	}

	private SortedSet updateCommands(Collection commandIds) {
		SortedSet updatedIds = new TreeSet();
		Iterator iterator = commandIds.iterator();
		
		while (iterator.hasNext()) {
			String commandId = (String) iterator.next();					
			Command command = (Command) commandsById.get(commandId);
			
			if (command != null && updateCommand(command))
				updatedIds.add(commandId);			
		}
		
		return updatedIds;
	}
	
	private boolean updateKeyConfiguration(KeyConfiguration keyConfiguration) {
		boolean updated = false;
		updated |= keyConfiguration.setActive(Util.equals(keyConfiguration.getId(), activeKeyConfigurationId));
		IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(keyConfiguration.getId());
		updated |= keyConfiguration.setDefined(keyConfigurationDefinition != null);
		updated |= keyConfiguration.setDescription(keyConfigurationDefinition != null ? keyConfigurationDefinition.getDescription() : null);
		updated |= keyConfiguration.setName(keyConfigurationDefinition != null ? keyConfigurationDefinition.getName() : null);
		updated |= keyConfiguration.setParentId(keyConfigurationDefinition != null ? keyConfigurationDefinition.getParentId() : null);
		return updated;
	}

	private SortedSet updateKeyConfigurations(Collection keyConfigurationIds) {
		SortedSet updatedIds = new TreeSet();
		Iterator iterator = keyConfigurationIds.iterator();
		
		while (iterator.hasNext()) {
			String keyConfigurationId = (String) iterator.next();					
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
			
			if (keyConfiguration != null && updateKeyConfiguration(keyConfiguration))	
				updatedIds.add(keyConfigurationId);				
		}	
		
		return updatedIds;		
	}
}
