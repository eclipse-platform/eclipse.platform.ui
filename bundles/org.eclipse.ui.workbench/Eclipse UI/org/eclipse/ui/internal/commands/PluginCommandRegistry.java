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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.commands.api.IActiveKeyConfigurationDefinition;
import org.eclipse.ui.internal.commands.api.ICategoryDefinition;
import org.eclipse.ui.internal.commands.api.ICommandDefinition;
import org.eclipse.ui.internal.commands.api.IContextBindingDefinition;
import org.eclipse.ui.internal.commands.api.IImageBindingDefinition;
import org.eclipse.ui.internal.commands.api.IKeyBindingDefinition;
import org.eclipse.ui.internal.commands.api.IKeyConfigurationDefinition;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

final class PluginCommandRegistry extends AbstractCommandRegistry {

	private final class PluginRegistryReader extends RegistryReader {

		protected boolean readElement(IConfigurationElement element) {
			String name = element.getName();

			if (Persistence.TAG_ACTIVE_KEY_CONFIGURATION.equals(name))
				return readActiveKeyConfigurationDefinition(element);

			if (Persistence.TAG_CATEGORY.equals(name))
				return readCategoryDefinition(element);

			if (Persistence.TAG_COMMAND.equals(name))
				return readCommandDefinition(element);

			if (Persistence.TAG_CONTEXT_BINDING.equals(name))
				return readContextBindingDefinition(element);

			if (Persistence.TAG_IMAGE_BINDING.equals(name))
				return readImageBindingDefinition(element);

			if (Persistence.TAG_KEY_BINDING.equals(name))
				return readKeyBindingDefinition(element);

			if (Persistence.TAG_KEY_CONFIGURATION.equals(name))
				return readKeyConfigurationDefinition(element);

			return true; // TODO return false;
		}		
	}

	private final static String TAG_ROOT = Persistence.PACKAGE_BASE;
	
	private List activeKeyConfigurationDefinitions;
	private List categoryDefinitions; 
	private List commandDefinitions; 
	private List contextBindingDefinitions;
	private List imageBindingDefinitions;
	private List keyBindingDefinitions;
	private List keyConfigurationDefinitions;	
	private IPluginRegistry pluginRegistry;
	private PluginRegistryReader pluginRegistryReader;
	
	PluginCommandRegistry(IPluginRegistry pluginRegistry) {
		if (pluginRegistry == null)
			throw new NullPointerException();
		
		this.pluginRegistry = pluginRegistry;
	}

	void load()
		throws IOException {
		if (activeKeyConfigurationDefinitions == null)
			activeKeyConfigurationDefinitions = new ArrayList();
		else 
			activeKeyConfigurationDefinitions.clear();		
	
		if (categoryDefinitions == null)
			categoryDefinitions = new ArrayList();
		else 
			categoryDefinitions.clear();
		
		if (commandDefinitions == null)
			commandDefinitions = new ArrayList();
		else 
			commandDefinitions.clear();

		if (contextBindingDefinitions == null)
			contextBindingDefinitions = new ArrayList();
		else 
			contextBindingDefinitions.clear();

		if (imageBindingDefinitions == null)
			imageBindingDefinitions = new ArrayList();
		else 
			imageBindingDefinitions.clear();
		
		if (keyBindingDefinitions == null)
			keyBindingDefinitions = new ArrayList();
		else 
			keyBindingDefinitions.clear();

		if (keyConfigurationDefinitions == null)
			keyConfigurationDefinitions = new ArrayList();
		else 
			keyConfigurationDefinitions.clear();

		if (pluginRegistryReader == null)
			pluginRegistryReader = new PluginRegistryReader();

		pluginRegistryReader.readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, TAG_ROOT);		
		boolean commandRegistryChanged = false;
			
		if (!activeKeyConfigurationDefinitions.equals(super.activeKeyConfigurationDefinitions)) {
			super.activeKeyConfigurationDefinitions = Collections.unmodifiableList(activeKeyConfigurationDefinitions);			
			commandRegistryChanged = true;
		}	

		if (!categoryDefinitions.equals(super.categoryDefinitions)) {
			super.categoryDefinitions = Collections.unmodifiableList(categoryDefinitions);			
			commandRegistryChanged = true;
		}	
				
		if (!commandDefinitions.equals(super.commandDefinitions)) {
			super.commandDefinitions = Collections.unmodifiableList(commandDefinitions);			
			commandRegistryChanged = true;
		}	
				
		if (!contextBindingDefinitions.equals(super.contextBindingDefinitions)) {
			super.contextBindingDefinitions = Collections.unmodifiableList(contextBindingDefinitions);			
			commandRegistryChanged = true;
		}	
			
		if (!imageBindingDefinitions.equals(super.imageBindingDefinitions)) {
			super.imageBindingDefinitions = Collections.unmodifiableList(imageBindingDefinitions);			
			commandRegistryChanged = true;
		}	
				
		if (!keyBindingDefinitions.equals(super.keyBindingDefinitions)) {
			super.keyBindingDefinitions = Collections.unmodifiableList(keyBindingDefinitions);			
			commandRegistryChanged = true;
		}	
				
		if (!keyConfigurationDefinitions.equals(super.keyConfigurationDefinitions)) {
			super.keyConfigurationDefinitions = Collections.unmodifiableList(keyConfigurationDefinitions);			
			commandRegistryChanged = true;
		}	
				
		if (commandRegistryChanged)
			fireCommandRegistryChanged();
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

	private boolean readActiveKeyConfigurationDefinition(IConfigurationElement element) {
		IActiveKeyConfigurationDefinition activeKeyConfigurationDefinition = Persistence.readActiveKeyConfigurationDefinition(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (activeKeyConfigurationDefinition != null)
			activeKeyConfigurationDefinitions.add(activeKeyConfigurationDefinition);	
		
		return true;
	}

	private boolean readCategoryDefinition(IConfigurationElement element) {
		ICategoryDefinition categoryDefinition = Persistence.readCategoryDefinition(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (categoryDefinition != null)
			categoryDefinitions.add(categoryDefinition);	
		
		return true;
	}
	
	private boolean readCommandDefinition(IConfigurationElement element) {
		ICommandDefinition commandDefinition = Persistence.readCommandDefinition(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (commandDefinition != null)
			commandDefinitions.add(commandDefinition);	
		
		return true;
	}

	private boolean readContextBindingDefinition(IConfigurationElement element) {
		IContextBindingDefinition contextBindingDefinition = Persistence.readContextBindingDefinition(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (contextBindingDefinition != null)
			contextBindingDefinitions.add(contextBindingDefinition);	
		
		return true;
	}

	private boolean readImageBindingDefinition(IConfigurationElement element) {
		IImageBindingDefinition imageBinding = Persistence.readImageBindingDefinition(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (imageBinding != null)
			imageBindingDefinitions.add(imageBinding);	
		
		return true;
	}

	private boolean readKeyBindingDefinition(IConfigurationElement element) {
		IKeyBindingDefinition keyBindingDefinition = Persistence.readKeyBindingDefinition(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (keyBindingDefinition != null)
			keyBindingDefinitions.add(keyBindingDefinition);	
		
		return true;
	}
	
	private boolean readKeyConfigurationDefinition(IConfigurationElement element) {
		IKeyConfigurationDefinition keyConfigurationDefinition = Persistence.readKeyConfigurationDefinition(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (keyConfigurationDefinition != null)
			keyConfigurationDefinitions.add(keyConfigurationDefinition);	
		
		return true;
	}
}