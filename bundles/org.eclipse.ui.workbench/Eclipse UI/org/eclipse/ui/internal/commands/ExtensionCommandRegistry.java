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

import org.eclipse.core.runtime.registry.IConfigurationElement;
import org.eclipse.core.runtime.registry.IExtension;
import org.eclipse.core.runtime.registry.IExtensionDelta;
import org.eclipse.core.runtime.registry.IExtensionRegistry;
import org.eclipse.core.runtime.registry.IRegistryChangeEvent;
import org.eclipse.core.runtime.registry.IRegistryChangeListener;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

public final class ExtensionCommandRegistry extends AbstractCommandRegistry {

	private List activeKeyConfigurationDefinitions;
	private List activityBindingDefinitions;
	private List categoryDefinitions;
	private List commandDefinitions;
	private IExtensionRegistry extensionRegistry;
	private List imageBindingDefinitions;
	private List keyConfigurationDefinitions;
	private List keySequenceBindingDefinitions;

	public ExtensionCommandRegistry(IExtensionRegistry extensionRegistry) {
		if (extensionRegistry == null)
			throw new NullPointerException();

		this.extensionRegistry = extensionRegistry;

		this
			.extensionRegistry
			.addRegistryChangeListener(new IRegistryChangeListener() {
			public void registryChanged(IRegistryChangeEvent registryChangeEvent) {
				IExtensionDelta[] extensionDeltas =
					registryChangeEvent.getExtensionDeltas(
						Persistence.PACKAGE_PREFIX,
						Persistence.PACKAGE_BASE);

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

	private String getPluginId(IConfigurationElement configurationElement) {
		String pluginId = null;

		if (configurationElement != null) {
			IExtension extension = configurationElement.getDeclaringExtension();

			if (extension != null)
				pluginId = extension.getParentIdentifier();
		}

		return pluginId;
	}

	private void load() throws IOException {
		if (activeKeyConfigurationDefinitions == null)
			activeKeyConfigurationDefinitions = new ArrayList();
		else
			activeKeyConfigurationDefinitions.clear();

		if (activityBindingDefinitions == null)
			activityBindingDefinitions = new ArrayList();
		else
			activityBindingDefinitions.clear();

		if (categoryDefinitions == null)
			categoryDefinitions = new ArrayList();
		else
			categoryDefinitions.clear();

		if (commandDefinitions == null)
			commandDefinitions = new ArrayList();
		else
			commandDefinitions.clear();

		if (imageBindingDefinitions == null)
			imageBindingDefinitions = new ArrayList();
		else
			imageBindingDefinitions.clear();

		if (keyConfigurationDefinitions == null)
			keyConfigurationDefinitions = new ArrayList();
		else
			keyConfigurationDefinitions.clear();

		if (keySequenceBindingDefinitions == null)
			keySequenceBindingDefinitions = new ArrayList();
		else
			keySequenceBindingDefinitions.clear();

		IConfigurationElement[] configurationElements =
			extensionRegistry.getConfigurationElementsFor(
				Persistence.PACKAGE_FULL);

		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement configurationElement =
				configurationElements[i];
			String name = configurationElement.getName();

			if (Persistence.TAG_ACTIVE_KEY_CONFIGURATION.equals(name))
				readActiveKeyConfigurationDefinition(configurationElement);
			else if (Persistence.TAG_ACTIVITY_BINDING.equals(name))
				readActivityBindingDefinition(configurationElement);
			else if (Persistence.TAG_CATEGORY.equals(name))
				readCategoryDefinition(configurationElement);
			else if (Persistence.TAG_COMMAND.equals(name))
				readCommandDefinition(configurationElement);
			else if (Persistence.TAG_IMAGE_BINDING.equals(name))
				readImageBindingDefinition(configurationElement);
			else if (Persistence.TAG_KEY_CONFIGURATION.equals(name))
				readKeyConfigurationDefinition(configurationElement);
			else if (Persistence.TAG_KEY_SEQUENCE_BINDING.equals(name))
				readKeySequenceBindingDefinition(configurationElement);
		}

		boolean commandRegistryChanged = false;

		if (!activeKeyConfigurationDefinitions
			.equals(super.activeKeyConfigurationDefinitions)) {
			super.activeKeyConfigurationDefinitions =
				Collections.unmodifiableList(activeKeyConfigurationDefinitions);
			commandRegistryChanged = true;
		}

		if (!activityBindingDefinitions
			.equals(super.activityBindingDefinitions)) {
			super.activityBindingDefinitions =
				Collections.unmodifiableList(activityBindingDefinitions);
			commandRegistryChanged = true;
		}

		if (!categoryDefinitions.equals(super.categoryDefinitions)) {
			super.categoryDefinitions =
				Collections.unmodifiableList(categoryDefinitions);
			commandRegistryChanged = true;
		}

		if (!commandDefinitions.equals(super.commandDefinitions)) {
			super.commandDefinitions =
				Collections.unmodifiableList(commandDefinitions);
			commandRegistryChanged = true;
		}

		if (!imageBindingDefinitions.equals(super.imageBindingDefinitions)) {
			super.imageBindingDefinitions =
				Collections.unmodifiableList(imageBindingDefinitions);
			commandRegistryChanged = true;
		}

		if (!keyConfigurationDefinitions
			.equals(super.keyConfigurationDefinitions)) {
			super.keyConfigurationDefinitions =
				Collections.unmodifiableList(keyConfigurationDefinitions);
			commandRegistryChanged = true;
		}

		if (!keySequenceBindingDefinitions
			.equals(super.keySequenceBindingDefinitions)) {
			super.keySequenceBindingDefinitions =
				Collections.unmodifiableList(keySequenceBindingDefinitions);
			commandRegistryChanged = true;
		}

		if (commandRegistryChanged)
			fireCommandRegistryChanged();
	}

	private void readActiveKeyConfigurationDefinition(IConfigurationElement configurationElement) {
		IActiveKeyConfigurationDefinition activeKeyConfigurationDefinition =
			Persistence.readActiveKeyConfigurationDefinition(
				new ConfigurationElementMemento(configurationElement),
				getPluginId(configurationElement));

		if (activeKeyConfigurationDefinition != null)
			activeKeyConfigurationDefinitions.add(
				activeKeyConfigurationDefinition);
	}

	private void readActivityBindingDefinition(IConfigurationElement configurationElement) {
		IContextBindingDefinition activityBindingDefinition =
			Persistence.readActivityBindingDefinition(
				new ConfigurationElementMemento(configurationElement),
				getPluginId(configurationElement));

		if (activityBindingDefinition != null)
			activityBindingDefinitions.add(activityBindingDefinition);
	}

	private void readCategoryDefinition(IConfigurationElement configurationElement) {
		ICategoryDefinition categoryDefinition =
			Persistence.readCategoryDefinition(
				new ConfigurationElementMemento(configurationElement),
				getPluginId(configurationElement));

		if (categoryDefinition != null)
			categoryDefinitions.add(categoryDefinition);
	}

	private void readCommandDefinition(IConfigurationElement configurationElement) {
		ICommandDefinition commandDefinition =
			Persistence.readCommandDefinition(
				new ConfigurationElementMemento(configurationElement),
				getPluginId(configurationElement));

		if (commandDefinition != null)
			commandDefinitions.add(commandDefinition);
	}

	private void readImageBindingDefinition(IConfigurationElement configurationElement) {
		IImageBindingDefinition imageBinding =
			Persistence.readImageBindingDefinition(
				new ConfigurationElementMemento(configurationElement),
				getPluginId(configurationElement));

		if (imageBinding != null)
			imageBindingDefinitions.add(imageBinding);
	}

	private void readKeyConfigurationDefinition(IConfigurationElement configurationElement) {
		IKeyConfigurationDefinition keyConfigurationDefinition =
			Persistence.readKeyConfigurationDefinition(
				new ConfigurationElementMemento(configurationElement),
				getPluginId(configurationElement));

		if (keyConfigurationDefinition != null)
			keyConfigurationDefinitions.add(keyConfigurationDefinition);
	}

	private void readKeySequenceBindingDefinition(IConfigurationElement configurationElement) {
		IKeySequenceBindingDefinition keySequenceBindingDefinition =
			Persistence.readKeySequenceBindingDefinition(
				new ConfigurationElementMemento(configurationElement),
				getPluginId(configurationElement));

		if (keySequenceBindingDefinition != null)
			keySequenceBindingDefinitions.add(keySequenceBindingDefinition);
	}
}