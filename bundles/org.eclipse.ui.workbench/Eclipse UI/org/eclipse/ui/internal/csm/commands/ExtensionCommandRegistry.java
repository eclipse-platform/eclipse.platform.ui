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

package org.eclipse.ui.internal.csm.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.registry.IConfigurationElement;
import org.eclipse.core.runtime.registry.IExtension;
import org.eclipse.core.runtime.registry.IExtensionDelta;
import org.eclipse.core.runtime.registry.IExtensionRegistry;
import org.eclipse.core.runtime.registry.IRegistryChangeEvent;
import org.eclipse.core.runtime.registry.IRegistryChangeListener;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

final class ExtensionCommandRegistry extends AbstractCommandRegistry {

	private List commandDefinitions;
	private List commandPatternBindingDefinitions;
	private IExtensionRegistry extensionRegistry;
	
	ExtensionCommandRegistry(IExtensionRegistry extensionRegistry) {
		if (extensionRegistry == null)
			throw new NullPointerException();
		
		this.extensionRegistry = extensionRegistry;

		this.extensionRegistry.addRegistryChangeListener(new IRegistryChangeListener() {
			public void registryChanged(IRegistryChangeEvent registryChangeEvent) {				
				IExtensionDelta[] extensionDeltas = registryChangeEvent.getExtensionDeltas(Persistence.PACKAGE_PREFIX, Persistence.PACKAGE_BASE);
				
				if (extensionDeltas.length != 0)
					try {
						load();
					} catch (IOException eIO) {
					}
			}
		});
		
		try {
			load();
		} catch (IOException eIO) {
		}
	}

	private void load()
		throws IOException {	
		if (commandDefinitions == null)
			commandDefinitions = new ArrayList();
		else 
			commandDefinitions.clear();

		if (commandPatternBindingDefinitions == null)
			commandPatternBindingDefinitions = new ArrayList();
		else 
			commandPatternBindingDefinitions.clear();		
				
		IConfigurationElement[] configurationElements = extensionRegistry.getConfigurationElementsFor(Persistence.PACKAGE_FULL);

		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement configurationElement = configurationElements[i];			
			String name = configurationElement.getName();

			if (Persistence.TAG_COMMAND.equals(name))
				readCommandDefinition(configurationElement);
			else if (Persistence.TAG_COMMAND_PATTERN_BINDING.equals(name))
				readCommandPatternBindingDefinition(configurationElement);			
		}

		boolean commandRegistryChanged = false;
			
		if (!commandDefinitions.equals(super.commandDefinitions)) {
			super.commandDefinitions = Collections.unmodifiableList(commandDefinitions);		
			commandRegistryChanged = true;
		}				

		if (!commandPatternBindingDefinitions.equals(super.commandPatternBindingDefinitions)) {
			super.commandPatternBindingDefinitions = Collections.unmodifiableList(commandPatternBindingDefinitions);		
			commandRegistryChanged = true;
		}		

		if (commandRegistryChanged)
			fireCommandRegistryChanged();
	}

	private String getPluginId(IConfigurationElement configurationElement) {
		String pluginId = null;	
	
		if (configurationElement != null) {	
			IExtension extension = configurationElement.getDeclaringExtension();
		
			if (extension != null)
				pluginId = extension.getParentIdentifier();
		}

		return pluginId;
	}

	private void readCommandDefinition(IConfigurationElement configurationElement) {
		ICommandDefinition commandDefinition = Persistence.readCommandDefinition(new ConfigurationElementMemento(configurationElement), getPluginId(configurationElement));
	
		if (commandDefinition != null)
			commandDefinitions.add(commandDefinition);	
	}
	
	private void readCommandPatternBindingDefinition(IConfigurationElement configurationElement) {
		ICommandPatternBindingDefinition commandPatternBindingDefinition = Persistence.readCommandPatternBindingDefinition(new ConfigurationElementMemento(configurationElement), getPluginId(configurationElement));
	
		if (commandPatternBindingDefinition != null)
			commandPatternBindingDefinitions.add(commandPatternBindingDefinition);	
	}	
}