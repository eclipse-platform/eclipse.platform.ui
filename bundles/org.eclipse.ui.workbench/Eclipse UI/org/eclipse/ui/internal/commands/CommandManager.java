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
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.ui.commands.IAction;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICategoryEvent;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandEvent;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.commands.IKeyConfigurationEvent;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

public final class CommandManager implements ICommandManager {

	public final static String SEPARATOR = "_"; //$NON-NLS-1$
	
	static boolean isKeyConfigurationDefinitionChildOf(String ancestor, String id, Map keyConfigurationDefinitionsById) {
		Collection visited = new HashSet();

		while (id != null && !visited.contains(id)) {
			IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(id);				
			visited.add(id);

			if (keyConfigurationDefinition != null && Util.equals(id = keyConfigurationDefinition.getParentId(), ancestor))
				return true;
		}

		return false;
	}

	static void validateActivityBindingDefinitions(Collection activityBindingDefinitions) {		
		Iterator iterator = activityBindingDefinitions.iterator();
		
		while (iterator.hasNext()) {
			IActivityBindingDefinition activityBindingDefinition = (IActivityBindingDefinition) iterator.next();
			
			if (activityBindingDefinition.getCommandId() == null)
				iterator.remove();
		}
	}		
	
	static void validateImageBindingDefinitions(Collection imageBindingDefinitions) {		
		Iterator iterator = imageBindingDefinitions.iterator();
		
		while (iterator.hasNext()) {
			IImageBindingDefinition imageBindingDefinition = (IImageBindingDefinition) iterator.next();
			
			if (imageBindingDefinition.getCommandId() == null || imageBindingDefinition.getImageUri() == null)
				iterator.remove();
		}
	}		
	
	static void validateKeySequenceBindingDefinitions(Collection keySequenceBindingDefinitions) {
		Iterator iterator = keySequenceBindingDefinitions.iterator();
		
		while (iterator.hasNext()) {
			IKeySequenceBindingDefinition keySequenceBindingDefinition = (IKeySequenceBindingDefinition) iterator.next();
			KeySequence keySequence = keySequenceBindingDefinition.getKeySequence();
			
			if (keySequenceBindingDefinition.getCommandId() == null || keySequence == null || !validateKeySequence(keySequence))
				iterator.remove();
		}
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
	
	private Set activeActivityIds = new HashSet();
	// TODO does this have any use anymore?
	private String activeKeyConfigurationId = null;
	private String activeLocale = null;
	private String activePlatform = null;
	private Map activityBindingsByCommandId = new HashMap();		
	private Map categoriesById = new WeakHashMap();
	private Set categoriesWithListeners = new HashSet();
	private Map categoryDefinitionsById = new HashMap();	
	private Map commandDefinitionsById = new HashMap();	
	private List commandManagerListeners;
	private ICommandRegistry commandRegistry;		
	private Map commandsById = new WeakHashMap();
	private Set commandsWithListeners = new HashSet();
	private Set definedCategoryIds = new HashSet();
	private Set definedCommandIds = new HashSet();
	private Set definedKeyConfigurationIds = new HashSet();
	private Map imageBindingsByCommandId = new HashMap();		
	private Map keyConfigurationsById = new WeakHashMap();
	private Set keyConfigurationsWithListeners = new HashSet();
	private Map keyConfigurationDefinitionsById = new HashMap();		
	private Map keySequenceBindingsByCommandId = new HashMap();
	private IMutableCommandRegistry mutableCommandRegistry;		
	
	// TODO review begin	
	private Map actionsById = new HashMap();	
	private Map commandIdsByActionIds = new HashMap();
	private KeySequenceBindingMachine keySequenceBindingMachine = new KeySequenceBindingMachine();	
	// TODO review end
	
	public CommandManager() {
		this(new ExtensionCommandRegistry(Platform.getExtensionRegistry()), new PreferenceCommandRegistry(WorkbenchPlugin.getDefault().getPreferenceStore()));
	}

	public CommandManager(ICommandRegistry commandRegistry, IMutableCommandRegistry mutableCommandRegistry) {
		if (commandRegistry == null || mutableCommandRegistry == null)
			throw new NullPointerException();

		this.commandRegistry = commandRegistry;
		this.mutableCommandRegistry = mutableCommandRegistry;
		String systemLocale = Locale.getDefault().toString();
		activeLocale = systemLocale != null ? systemLocale : Util.ZERO_LENGTH_STRING;
		String systemPlatform = SWT.getPlatform();
		activePlatform = systemPlatform != null ? systemPlatform : Util.ZERO_LENGTH_STRING;	
		
		this.commandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
			public void commandRegistryChanged(ICommandRegistryEvent commandRegistryEvent) {
				readRegistry();
			}
		});

		this.mutableCommandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
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

	public Set getActiveActivityIds() {
		return Collections.unmodifiableSet(activeActivityIds);
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
			category = new Category(categoriesWithListeners, categoryId);
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
			command = new Command(commandsWithListeners, commandId);
			updateCommand(command);
			commandsById.put(commandId, command);
		}
		
		return command;
	}

	public Set getDefinedCategoryIds() {
		return Collections.unmodifiableSet(definedCategoryIds);
	}
	
	public Set getDefinedCommandIds() {
		return Collections.unmodifiableSet(definedCommandIds);
	}

	public Set getDefinedKeyConfigurationIds() {
		return Collections.unmodifiableSet(definedKeyConfigurationIds);
	}

	public IKeyConfiguration getKeyConfiguration(String keyConfigurationId) {
		if (keyConfigurationId == null)
			throw new NullPointerException();
			
		KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
		
		if (keyConfiguration == null) {
			keyConfiguration = new KeyConfiguration(keyConfigurationsWithListeners, keyConfigurationId);
			updateKeyConfiguration(keyConfiguration);
			keyConfigurationsById.put(keyConfigurationId, keyConfiguration);
		}
		
		return keyConfiguration;
	}	

	public KeySequence getMode() {
		return keySequenceBindingMachine.getMode();	
	}	

	public void removeCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();
			
		if (commandManagerListeners != null)
			commandManagerListeners.remove(commandManagerListener);
	}

	public void setActiveActivityIds(Set activeActivityIds) {
		activeActivityIds = Util.safeCopy(activeActivityIds, String.class);
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;

		if (!this.activeActivityIds.equals(activeActivityIds)) {
			this.activeActivityIds = activeActivityIds;
			commandManagerChanged = true;
			calculateKeySequenceBindings();	
			commandEventsByCommandId = updateCommands(commandsById.keySet());	
		}
		
		if (commandManagerChanged)
			fireCommandManagerChanged(new CommandManagerEvent(this, true, false, false, false, false, false, false, false));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);	
	}	
	
	public void setActiveKeyConfigurationId(String activeKeyConfigurationId) {
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;
		Map keyConfigurationEventsByKeyConfigurationId = null;

		if (!Util.equals(this.activeKeyConfigurationId, activeKeyConfigurationId)) {
			this.activeKeyConfigurationId = activeKeyConfigurationId;
			commandManagerChanged = true;
			calculateKeySequenceBindings();	
			commandEventsByCommandId = updateCommands(commandsById.keySet());	
			keyConfigurationEventsByKeyConfigurationId = updateKeyConfigurations(keyConfigurationsById.keySet());	
		}
		
		if (commandManagerChanged)
			fireCommandManagerChanged(new CommandManagerEvent(this, false, true, false, false, false, false, false, false));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);		
		
		if (keyConfigurationEventsByKeyConfigurationId != null)
			notifyKeyConfigurations(keyConfigurationEventsByKeyConfigurationId);	
	}	

	public void setActiveLocale(String activeLocale) {
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;

		if (!Util.equals(this.activeLocale, activeLocale)) {
			this.activeLocale = activeLocale;
			commandManagerChanged = true;
			calculateImageBindings();
			calculateKeySequenceBindings();	
			commandEventsByCommandId = updateCommands(commandsById.keySet());	
		}
		
		if (commandManagerChanged)
			fireCommandManagerChanged(new CommandManagerEvent(this, false, false, true, false, false, false, false, false));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);		
	}		

	public void setActivePlatform(String activePlatform) {
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;

		if (!Util.equals(this.activePlatform, activePlatform)) {
			this.activePlatform = activePlatform;
			commandManagerChanged = true;
			calculateImageBindings();
			calculateKeySequenceBindings();	
			commandEventsByCommandId = updateCommands(commandsById.keySet());	
		}
		
		if (commandManagerChanged)
			fireCommandManagerChanged(new CommandManagerEvent(this, false, false, false, true, false, false, false, false));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);		
	}		

	public void setActionsById(Map actionsById) {
		actionsById = Util.safeCopy(actionsById, String.class, IAction.class);	
	
		if (!Util.equals(actionsById, this.actionsById)) {	
			this.actionsById = actionsById;
			
			// TODO begin temporary (?)
			for (Iterator iterator = this.actionsById.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String commandId = (String) entry.getKey();
				IAction action = (IAction) entry.getValue();
				
				if (commandId != null && action instanceof ActionHandler) {						
					ActionHandler actionHandler = (ActionHandler) action;
					org.eclipse.jface.action.IAction jfaceAction = (org.eclipse.jface.action.IAction) actionHandler.getAction();	
					
					if (jfaceAction != null) {
						String actionId = jfaceAction.getId();
						
						if (actionId != null)
							commandIdsByActionIds.put(actionId, commandId);
					}
				}				
			}
			// TODO end temporary
		}
	}
	
	public void setMode(KeySequence mode) {
		if (keySequenceBindingMachine.setMode(mode))
			fireCommandManagerChanged(new CommandManagerEvent(this, false, false, false, false, false, false, false, true));
	}

	static String[] extend(String[] strings) {
		String[] strings2 = new String[strings.length + 1];
		System.arraycopy(strings, 0, strings2, 0, strings.length);		
		return strings2;
	}		

	private void calculateImageBindings() {
		String[] activeLocales = extend(getPath(activeLocale, SEPARATOR));
		String[] activePlatforms = extend(getPath(activePlatform, SEPARATOR));	
		// TODO
	}
	
	private void calculateKeySequenceBindings() {
		List list = new ArrayList(this.activeActivityIds);
		
		// TODO high priority. temporary fix for M3 for automatic inheritance of activities for the specific case of the java editor scope. 
		if (list.contains("org.eclipse.jdt.ui.javaEditorScope") && !list.contains("org.eclipse.ui.textEditorScope"))
			list.add("org.eclipse.ui.textEditorScope");

		String[] activeActivityIds = extend((String[]) list.toArray(new String[list.size()]));		
		String[] activeKeyConfigurationIds = extend(getKeyConfigurationIds(activeKeyConfigurationId, keyConfigurationDefinitionsById));
		String[] activeLocales = extend(getPath(activeLocale, SEPARATOR));
		String[] activePlatforms = extend(getPath(activePlatform, SEPARATOR));
		keySequenceBindingMachine.setActiveActivityIds(activeActivityIds);
		keySequenceBindingMachine.setActiveKeyConfigurationIds(activeKeyConfigurationIds);
		keySequenceBindingMachine.setActiveLocales(activeLocales);
		keySequenceBindingMachine.setActivePlatforms(activePlatforms);
		keySequenceBindingsByCommandId = keySequenceBindingMachine.getKeySequenceBindingsByCommandId();
	}		
	
	static String[] getKeyConfigurationIds(String keyConfigurationDefinitionId, Map keyConfigurationDefinitionsById) {
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

	static String[] getPath(String string, String separator) {
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
	
	private void fireCommandManagerChanged(ICommandManagerEvent commandManagerEvent) {
		if (commandManagerEvent == null)
			throw new NullPointerException();
		
		if (commandManagerListeners != null)
			for (int i = 0; i < commandManagerListeners.size(); i++)
				((ICommandManagerListener) commandManagerListeners.get(i)).commandManagerChanged(commandManagerEvent);
	}

	private void notifyCategories(Map categoryEventsByCategoryId) {	
		for (Iterator iterator = categoryEventsByCategoryId.entrySet().iterator(); iterator.hasNext();) {	
			Map.Entry entry = (Map.Entry) iterator.next();			
			String categoryId = (String) entry.getKey();
			ICategoryEvent categoryEvent = (ICategoryEvent) entry.getValue();
			Category category = (Category) categoriesById.get(categoryId);
			
			if (category != null)
				category.fireCategoryChanged(categoryEvent);
		}
	}	
	
	private void notifyCommands(Map commandEventsByCommandId) {	
		for (Iterator iterator = commandEventsByCommandId.entrySet().iterator(); iterator.hasNext();) {	
			Map.Entry entry = (Map.Entry) iterator.next();			
			String commandId = (String) entry.getKey();
			ICommandEvent commandEvent = (ICommandEvent) entry.getValue();
			Command command = (Command) commandsById.get(commandId);
			
			if (command != null)
				command.fireCommandChanged(commandEvent);
		}
	}

	private void notifyKeyConfigurations(Map keyConfigurationEventsByKeyConfigurationId) {	
		for (Iterator iterator = keyConfigurationEventsByKeyConfigurationId.entrySet().iterator(); iterator.hasNext();) {	
			Map.Entry entry = (Map.Entry) iterator.next();			
			String keyConfigurationId = (String) entry.getKey();
			IKeyConfigurationEvent keyConfigurationEvent = (IKeyConfigurationEvent) entry.getValue();
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
			
			if (keyConfiguration != null)
				keyConfiguration.fireKeyConfigurationChanged(keyConfigurationEvent);
		}
	}	
	
	private void readRegistry() {
		Collection categoryDefinitions = new ArrayList();
		categoryDefinitions.addAll(commandRegistry.getCategoryDefinitions());				
		categoryDefinitions.addAll(mutableCommandRegistry.getCategoryDefinitions());				
		Map categoryDefinitionsById = new HashMap(CategoryDefinition.categoryDefinitionsById(categoryDefinitions, false));

		for (Iterator iterator = categoryDefinitionsById.values().iterator(); iterator.hasNext();) {
			ICategoryDefinition categoryDefinition = (ICategoryDefinition) iterator.next();
			String name = categoryDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}
		
		Collection commandDefinitions = new ArrayList();
		commandDefinitions.addAll(commandRegistry.getCommandDefinitions());				
		commandDefinitions.addAll(mutableCommandRegistry.getCommandDefinitions());				
		Map commandDefinitionsById = new HashMap(CommandDefinition.commandDefinitionsById(commandDefinitions, false));

		for (Iterator iterator = commandDefinitionsById.values().iterator(); iterator.hasNext();) {
			ICommandDefinition commandDefinition = (ICommandDefinition) iterator.next();
			String name = commandDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}

		Collection keyConfigurationDefinitions = new ArrayList();
		keyConfigurationDefinitions.addAll(commandRegistry.getKeyConfigurationDefinitions());				
		keyConfigurationDefinitions.addAll(mutableCommandRegistry.getKeyConfigurationDefinitions());				
		Map keyConfigurationDefinitionsById = new HashMap(KeyConfigurationDefinition.keyConfigurationDefinitionsById(keyConfigurationDefinitions, false));

		for (Iterator iterator = keyConfigurationDefinitionsById.values().iterator(); iterator.hasNext();) {
			IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator.next();
			String name = keyConfigurationDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}		

		for (Iterator iterator = commandDefinitionsById.values().iterator(); iterator.hasNext();) {
			ICommandDefinition commandDefinition = (ICommandDefinition) iterator.next();
			String categoryId = commandDefinition.getCategoryId();
				
			if (categoryId != null && !categoryDefinitionsById.containsKey(categoryId))
				iterator.remove();
		}

		for (Iterator iterator = keyConfigurationDefinitionsById.keySet().iterator(); iterator.hasNext();)
			if (!isKeyConfigurationDefinitionChildOf(null, (String) iterator.next(), keyConfigurationDefinitionsById))
				iterator.remove();

		// TODO should the active key configuration change if a call to setActivityKeyConfigurationId was explicitly made already?
		List activeKeyConfigurationDefinitions = new ArrayList();
		activeKeyConfigurationDefinitions.addAll(commandRegistry.getActiveKeyConfigurationDefinitions());
		activeKeyConfigurationDefinitions.addAll(mutableCommandRegistry.getActiveKeyConfigurationDefinitions());
		String activeKeyConfigurationId = null;
		
		if (!activeKeyConfigurationDefinitions.isEmpty()) {
			IActiveKeyConfigurationDefinition activeKeyConfigurationDefinition = (IActiveKeyConfigurationDefinition) activeKeyConfigurationDefinitions.get(activeKeyConfigurationDefinitions.size() - 1);
			activeKeyConfigurationId = activeKeyConfigurationDefinition.getKeyConfigurationId();
			
			if (activeKeyConfigurationId != null && !keyConfigurationDefinitionsById.containsKey(activeKeyConfigurationId))
				activeKeyConfigurationId = null;
		}			

		this.categoryDefinitionsById = categoryDefinitionsById;
		this.commandDefinitionsById = commandDefinitionsById;
		this.keyConfigurationDefinitionsById = keyConfigurationDefinitionsById;		
		boolean activeKeyConfigurationIdChanged = false;	
		
		if (!Util.equals(this.activeKeyConfigurationId, activeKeyConfigurationId)) {
			this.activeKeyConfigurationId = activeKeyConfigurationId;
			activeKeyConfigurationIdChanged = true;
		}		
		
		boolean definedCategoryIdsChanged = false;			
		Set definedCategoryIds = new HashSet(categoryDefinitionsById.keySet());		

		if (!definedCategoryIds.equals(this.definedCategoryIds)) {
			this.definedCategoryIds = definedCategoryIds;
			definedCategoryIdsChanged = true;	
		}
		
		boolean definedCommandIdsChanged = false;			
		Set definedCommandIds = new HashSet(commandDefinitionsById.keySet());		

		if (!definedCommandIds.equals(this.definedCommandIds)) {
			this.definedCommandIds = definedCommandIds;
			definedCommandIdsChanged = true;	
		}

		boolean definedKeyConfigurationIdsChanged = false;
		Set definedKeyConfigurationIds = new HashSet(keyConfigurationDefinitionsById.keySet());		

		if (!definedKeyConfigurationIds.equals(this.definedKeyConfigurationIds)) {
			this.definedKeyConfigurationIds = definedKeyConfigurationIds;
			definedKeyConfigurationIdsChanged = true;	
		}
		
		List activityBindingDefinitions = new ArrayList();
		activityBindingDefinitions.addAll(commandRegistry.getActivityBindingDefinitions());
		activityBindingDefinitions.addAll(mutableCommandRegistry.getActivityBindingDefinitions());
		validateActivityBindingDefinitions(activityBindingDefinitions);
		Map activityBindingsByCommandId = new HashMap();
		
		for (Iterator iterator = activityBindingDefinitions.iterator(); iterator.hasNext();) {
			IActivityBindingDefinition activityBindingDefinition = (IActivityBindingDefinition) iterator.next();					
			String activityId = activityBindingDefinition.getActivityId();			
			String commandId = activityBindingDefinition.getCommandId();
			SortedSet sortedSet = (SortedSet) activityBindingsByCommandId.get(commandId);
			
			if (sortedSet == null) {
				sortedSet = new TreeSet();
				activityBindingsByCommandId.put(commandId, sortedSet);						
			}
			
			sortedSet.add(new ActivityBinding(activityId));
		}
		
		this.activityBindingsByCommandId = activityBindingsByCommandId;				
		List commandRegistryImageBindingDefinitions = new ArrayList(commandRegistry.getImageBindingDefinitions());
		validateImageBindingDefinitions(commandRegistryImageBindingDefinitions);		
		List mutableCommandRegistryImageBindingDefinitions = new ArrayList(mutableCommandRegistry.getImageBindingDefinitions());
		validateImageBindingDefinitions(mutableCommandRegistryImageBindingDefinitions);
		// TODO prepare imageBindingMachine with ImageBindingDefinitions
		calculateImageBindings();		
		List commandRegistryKeySequenceBindingDefinitions = new ArrayList(commandRegistry.getKeySequenceBindingDefinitions());
		validateKeySequenceBindingDefinitions(commandRegistryKeySequenceBindingDefinitions);		
		List mutableCommandRegistryKeySequenceBindingDefinitions = new ArrayList(mutableCommandRegistry.getKeySequenceBindingDefinitions());
		validateKeySequenceBindingDefinitions(mutableCommandRegistryKeySequenceBindingDefinitions);		
		keySequenceBindingMachine.setKeySequenceBindings0(commandRegistryKeySequenceBindingDefinitions);
		keySequenceBindingMachine.setKeySequenceBindings1(mutableCommandRegistryKeySequenceBindingDefinitions);		
		calculateKeySequenceBindings();
		Map categoryEventsByCategoryId = updateCategories(categoriesById.keySet());	
		Map commandEventsByCommandId = updateCommands(commandsById.keySet());	
		Map keyConfigurationEventsByKeyConfigurationId = updateKeyConfigurations(keyConfigurationsById.keySet());	
		
		if (activeKeyConfigurationIdChanged || definedCategoryIdsChanged || definedCommandIdsChanged || definedKeyConfigurationIdsChanged)
			fireCommandManagerChanged(new CommandManagerEvent(this, false, activeKeyConfigurationIdChanged, false, false, definedCategoryIdsChanged, definedCommandIdsChanged, definedKeyConfigurationIdsChanged, false));

		if (categoryEventsByCategoryId != null)
			notifyCategories(categoryEventsByCategoryId);		

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);
		
		if (keyConfigurationEventsByKeyConfigurationId != null)
			notifyKeyConfigurations(keyConfigurationEventsByKeyConfigurationId);
	}

	private Map updateCategories(Collection categoryIds) {
		Map categoryEventsByCategoryId = new TreeMap();
		
		for (Iterator iterator = categoryIds.iterator(); iterator.hasNext();) {		
			String categoryId = (String) iterator.next();					
			Category category = (Category) categoriesById.get(categoryId);
			
			if (category != null) {
				ICategoryEvent categoryEvent = updateCategory(category);
				
				if (categoryEvent != null)
					categoryEventsByCategoryId.put(categoryId, categoryEvent);
			}
		}
		
		return categoryEventsByCategoryId;			
	}	
	
	private ICategoryEvent updateCategory(Category category) {
		ICategoryDefinition categoryDefinition = (ICategoryDefinition) categoryDefinitionsById.get(category.getId());
		boolean definedChanged = category.setDefined(categoryDefinition != null);
		boolean descriptionChanged = category.setDescription(categoryDefinition != null ? categoryDefinition.getDescription() : null);		
		boolean nameChanged = category.setName(categoryDefinition != null ? categoryDefinition.getName() : null);
		
		if (definedChanged || descriptionChanged || nameChanged )
			return new CategoryEvent(category, definedChanged, descriptionChanged, nameChanged); 
		else 
			return null;
	}
	
	private ICommandEvent updateCommand(Command command) {
		SortedSet activityBindings = (SortedSet) activityBindingsByCommandId.get(command.getId());		
		boolean activeChanged = command.setActive(activityBindings != null ? isActive(activityBindings) : true);		
		// TODO list to sortedset in api?
		boolean activityBindingsChanged = command.setActivityBindings(activityBindings != null ? new ArrayList(activityBindings) : Collections.EMPTY_LIST);		
		ICommandDefinition commandDefinition = (ICommandDefinition) commandDefinitionsById.get(command.getId());
		boolean categoryIdChanged = command.setCategoryId(commandDefinition != null ? commandDefinition.getCategoryId() : null);				
		boolean definedChanged = command.setDefined(commandDefinition != null);
		boolean descriptionChanged = command.setDescription(commandDefinition != null ? commandDefinition.getDescription() : null);		
		SortedSet imageBindings = (SortedSet) imageBindingsByCommandId.get(command.getId());
		// TODO list to sortedset in api?
		boolean imageBindingsChanged = command.setImageBindings(imageBindings != null ? new ArrayList(imageBindings) : Collections.EMPTY_LIST);		
		SortedSet keySequenceBindings = (SortedSet) keySequenceBindingsByCommandId.get(command.getId());
		// TODO list to sortedset in api?
		boolean keySequenceBindingsChanged = command.setKeySequenceBindings(keySequenceBindings != null ? new ArrayList(keySequenceBindings) : Collections.EMPTY_LIST);		
		boolean nameChanged = command.setName(commandDefinition != null ? commandDefinition.getName() : null);

		if (activeChanged || activityBindingsChanged || categoryIdChanged || definedChanged || descriptionChanged || imageBindingsChanged || keySequenceBindingsChanged || nameChanged)
			return new CommandEvent(command, activeChanged, activityBindingsChanged, categoryIdChanged, definedChanged, descriptionChanged, imageBindingsChanged, keySequenceBindingsChanged, nameChanged); 
		else 
			return null;
	}

	private Map updateCommands(Collection commandIds) {
		Map commandEventsByCommandId = new TreeMap();
		
		for (Iterator iterator = commandIds.iterator(); iterator.hasNext();) {		
			String commandId = (String) iterator.next();					
			Command command = (Command) commandsById.get(commandId);
			
			if (command != null) {
				ICommandEvent commandEvent = updateCommand(command);
				
				if (commandEvent != null)
					commandEventsByCommandId.put(commandId, commandEvent);
			}
		}
		
		return commandEventsByCommandId;			
	}	
	
	private IKeyConfigurationEvent updateKeyConfiguration(KeyConfiguration keyConfiguration) {
		boolean activeChanged = keyConfiguration.setActive(Util.equals(activeKeyConfigurationId, keyConfiguration.getId()));		
		IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(keyConfiguration.getId());
		boolean definedChanged = keyConfiguration.setDefined(keyConfigurationDefinition != null);
		boolean descriptionChanged = keyConfiguration.setDescription(keyConfigurationDefinition != null ? keyConfigurationDefinition.getDescription() : null);		
		boolean nameChanged = keyConfiguration.setName(keyConfigurationDefinition != null ? keyConfigurationDefinition.getName() : null);
		boolean parentIdChanged = keyConfiguration.setParentId(keyConfigurationDefinition != null ? keyConfigurationDefinition.getParentId() : null);				
		
		if (activeChanged || definedChanged || descriptionChanged || nameChanged || parentIdChanged)
			return new KeyConfigurationEvent(keyConfiguration, activeChanged, definedChanged, descriptionChanged, nameChanged, parentIdChanged); 
		else 
			return null;
	}

	private Map updateKeyConfigurations(Collection keyConfigurationIds) {
		Map keyConfigurationEventsByKeyConfigurationId = new TreeMap();
		
		for (Iterator iterator = keyConfigurationIds.iterator(); iterator.hasNext();) {		
			String keyConfigurationId = (String) iterator.next();					
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
			
			if (keyConfiguration != null) {
				IKeyConfigurationEvent keyConfigurationEvent = updateKeyConfiguration(keyConfiguration);
				
				if (keyConfigurationEvent != null)
					keyConfigurationEventsByKeyConfigurationId.put(keyConfigurationId, keyConfigurationEvent);
			}
		}
		
		return keyConfigurationEventsByKeyConfigurationId;			
	}

	public Map getPartialMatches(KeySequence keySequence) {
		Map map = new HashMap(keySequenceBindingMachine.getMatchesByKeySequenceForMode(keySequence));
		
		for (Iterator iterator = map.values().iterator(); iterator.hasNext();) {
			Map.Entry entry	= (Map.Entry) iterator.next();
			entry.setValue(((Match) entry.getValue()).getCommandId());
		}		
		
		return Collections.unmodifiableMap(map);
	}
	
	public String getPerfectMatch(KeySequence keySequence) {
		Match match = (Match) keySequenceBindingMachine.getMatchesByKeySequence().get(keySequence);
		return match != null ? match.getCommandId() : null;
	}

	public boolean isPartialMatch(KeySequence keySequence) {
		return !getPartialMatches(keySequence).isEmpty();
	}
	
	public boolean isPerfectMatch(KeySequence keySequence) {
		return getPerfectMatch(keySequence) != null;
	}


	
	
	public Map getMatchesByKeySequenceForMode() {
		return keySequenceBindingMachine.getMatchesByKeySequenceForMode();
	}

	
	
	public ICommandRegistry getCommandRegistry() {
		return commandRegistry;
	}

	IMutableCommandRegistry getMutableCommandRegistry() {
		return mutableCommandRegistry;
	}
	
	private boolean isActive(Collection activityBindings) {
		if (activityBindings.isEmpty())
			return true;
		
		Iterator iterator = activeActivityIds.iterator();
		
		while (iterator.hasNext()) {
			String activeActivityId = (String) iterator.next();
			
			if (activityBindings.contains(new ActivityBinding(activeActivityId)));
				return true;			
		}
		
		return false;
	}
	
	/* TODO			
		HashSet categoryIdsReferencedByCommandDefinitions = new HashSet();

		for (Iterator iterator = commandDefinitionsById.values().iterator(); iterator.hasNext();) {
			ICommandDefinition commandDefinition = (ICommandDefinition) iterator.next();
			String categoryId = commandDefinition.getCategoryId();
			String name = commandDefinition.getName();

			if (name == null || name.length() == 0 || categoryId != null && !categoryDefinitionsById.containsKey(categoryId))
				iterator.remove();
			else {
				String commandId = commandDefinition.getId();
				Set commandIds = (Set) commandIdsByCategoryId.get(categoryId);
			
				if (commandIds == null) {
					commandIds = new HashSet();
					commandIdsByCategoryId.put(categoryId, commandIds);
				}
			
				commandIds.add(commandId);
				categoryIdsReferencedByCommandDefinitions.add(categoryId);				
			}
		}
		
		categoryDefinitionsById.keySet().retainAll(categoryIdsReferencedByCommandDefinitions);			 
	*/
}
