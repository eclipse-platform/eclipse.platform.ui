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

package org.eclipse.ui.internal.activities;

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

final class ExtensionActivityRegistry extends AbstractActivityRegistry {

	private List activityDefinitions;
	private IExtensionRegistry extensionRegistry;
	private List patternBindingDefinitions;

	ExtensionActivityRegistry(IExtensionRegistry extensionRegistry) {
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

	private void load() throws IOException {
		if (activityDefinitions == null)
			activityDefinitions = new ArrayList();
		else
			activityDefinitions.clear();

		if (patternBindingDefinitions == null)
			patternBindingDefinitions = new ArrayList();
		else
			patternBindingDefinitions.clear();

		IConfigurationElement[] configurationElements = extensionRegistry.getConfigurationElementsFor(Persistence.PACKAGE_FULL);

		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement configurationElement = configurationElements[i];
			String name = configurationElement.getName();

			if (Persistence.TAG_ACTIVITY.equals(name))
				readActivityDefinition(configurationElement);
			else if (Persistence.TAG_PATTERN_BINDING.equals(name))
				readPatternBindingDefinition(configurationElement);
		}

		boolean activityRegistryChanged = false;

		if (!activityDefinitions.equals(super.activityDefinitions)) {
			super.activityDefinitions = Collections.unmodifiableList(activityDefinitions);
			activityRegistryChanged = true;
		}

		if (!patternBindingDefinitions.equals(super.patternBindingDefinitions)) {
			super.patternBindingDefinitions = Collections.unmodifiableList(patternBindingDefinitions);
			activityRegistryChanged = true;
		}

		if (activityRegistryChanged)
			fireActivityRegistryChanged();
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

	private void readActivityDefinition(IConfigurationElement configurationElement) {
		IActivityDefinition activityDefinition =
			Persistence.readActivityDefinition(new ConfigurationElementMemento(configurationElement), getPluginId(configurationElement));

		if (activityDefinition != null)
			activityDefinitions.add(activityDefinition);
	}

	private void readPatternBindingDefinition(IConfigurationElement configurationElement) {
		IPatternBindingDefinition patternBindingDefinition =
			Persistence.readPatternBindingDefinition(new ConfigurationElementMemento(configurationElement), getPluginId(configurationElement));

		if (patternBindingDefinition != null)
			patternBindingDefinitions.add(patternBindingDefinition);
	}
}