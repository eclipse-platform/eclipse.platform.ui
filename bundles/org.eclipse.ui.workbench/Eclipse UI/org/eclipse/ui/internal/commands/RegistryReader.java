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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.IContextBinding;
import org.eclipse.ui.commands.IImageBinding;
import org.eclipse.ui.commands.IKeyBinding;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

final class RegistryReader extends org.eclipse.ui.internal.registry.RegistryReader {

	private final static String TAG_ROOT = Persistence.PACKAGE_BASE;

	private List categories;	
	private List commands;
	private List contextBindings;
	private List imageBindings;
	private List keyBindings;
	private List keyConfigurations;
	private IPluginRegistry pluginRegistry;
	private List unmodifiableCategories;
	private List unmodifiableCommands;
	private List unmodifiableContextBindings;
	private List unmodifiableImageBindings;
	private List unmodifiableKeyBindings;
	private List unmodifiableKeyConfigurations;
	
	RegistryReader(IPluginRegistry pluginRegistry) {
		super();	
		this.pluginRegistry = pluginRegistry;
		unmodifiableCategories = Collections.EMPTY_LIST;
		unmodifiableCommands = Collections.EMPTY_LIST;
		unmodifiableContextBindings = Collections.EMPTY_LIST;
		unmodifiableImageBindings = Collections.EMPTY_LIST;		
		unmodifiableKeyBindings = Collections.EMPTY_LIST;
		unmodifiableKeyConfigurations = Collections.EMPTY_LIST;
	}

	List getCategories() {
		return unmodifiableCategories;
	}
	
	List getCommands() {
		return unmodifiableCommands;
	}

	List getContextBindings() {
		return unmodifiableContextBindings;
	}
	
	List getImageBindings() {
		return unmodifiableImageBindings;
	}

	List getKeyBindings() {
		return unmodifiableKeyBindings;
	}

	List getKeyConfigurations() {
		return unmodifiableKeyConfigurations;
	}
	
	void load() {
		if (categories == null)
			categories = new ArrayList();
		else 
			categories.clear();
			
		if (commands == null)
			commands = new ArrayList();
		else 
			commands.clear();

		if (contextBindings == null)
			contextBindings = new ArrayList();
		else 
			contextBindings.clear();

		if (imageBindings == null)
			imageBindings = new ArrayList();
		else 
			imageBindings.clear();
			
		if (keyBindings == null)
			keyBindings = new ArrayList();
		else 
			keyBindings.clear();

		if (keyConfigurations == null)
			keyConfigurations = new ArrayList();
		else 
			keyConfigurations.clear();

		if (pluginRegistry != null)	
			readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, TAG_ROOT);
			
		unmodifiableCategories = Collections.unmodifiableList(new ArrayList(categories));
		unmodifiableCommands = Collections.unmodifiableList(new ArrayList(commands));
		unmodifiableContextBindings = Collections.unmodifiableList(new ArrayList(contextBindings));
		unmodifiableImageBindings = Collections.unmodifiableList(new ArrayList(imageBindings));
		unmodifiableKeyBindings = Collections.unmodifiableList(new ArrayList(keyBindings));
		unmodifiableKeyConfigurations = Collections.unmodifiableList(new ArrayList(keyConfigurations));
	}

	protected boolean readElement(IConfigurationElement element) {
		String name = element.getName();

		if (Persistence.TAG_CATEGORY.equals(name))
			return readCategory(element);

		if (Persistence.TAG_COMMAND.equals(name))
			return readCommand(element);

		if (Persistence.TAG_CONTEXT_BINDING.equals(name))
			return readContextBinding(element);

		if (Persistence.TAG_IMAGE_BINDING.equals(name))
			return readImageBinding(element);

		if (Persistence.TAG_KEY_BINDING.equals(name))
			return readKeyBinding(element);

		if (Persistence.TAG_KEY_CONFIGURATION.equals(name))
			return readKeyConfiguration(element);

		return true; // TODO
		//return false;
	}

	private String getPluginId(IConfigurationElement element) {
		String pluginId = null;	
	
		if (element != null) {	
			IExtension extension = element.getDeclaringExtension();
		
			if (extension != null) {
				IPluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
			
				if (pluginDescriptor != null) 
					pluginId = pluginDescriptor.getUniqueIdentifier();				
			}
		}

		return pluginId;
	}

	private boolean readCategory(IConfigurationElement element) {
		ICategory category = Persistence.readCategory(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (category != null)
			categories.add(category);	
		
		return true;
	}
	
	private boolean readCommand(IConfigurationElement element) {
		ICommand command = Persistence.readCommand(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (command != null)
			commands.add(command);	
		
		return true;
	}

	private boolean readContextBinding(IConfigurationElement element) {
		IContextBinding contextBinding = Persistence.readContextBinding(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (contextBinding != null)
			contextBindings.add(contextBinding);	
		
		return true;
	}

	private boolean readImageBinding(IConfigurationElement element) {
		IImageBinding imageBinding = Persistence.readImageBinding(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (imageBinding != null)
			imageBindings.add(imageBinding);	
		
		return true;
	}

	private boolean readKeyBinding(IConfigurationElement element) {
		IKeyBinding keyBinding = Persistence.readKeyBinding(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (keyBinding != null)
			keyBindings.add(keyBinding);	
		
		return true;
	}
	
	private boolean readKeyConfiguration(IConfigurationElement element) {
		IKeyConfiguration keyConfiguration = Persistence.readKeyConfiguration(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (keyConfiguration != null)
			keyConfigurations.add(keyConfiguration);	
		
		return true;
	}
}