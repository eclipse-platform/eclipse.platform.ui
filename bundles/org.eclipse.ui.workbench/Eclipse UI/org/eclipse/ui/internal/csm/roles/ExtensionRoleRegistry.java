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

package org.eclipse.ui.internal.csm.roles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.registry.IConfigurationElement;
import org.eclipse.core.runtime.registry.IExtension;
import org.eclipse.core.runtime.registry.IExtensionRegistry;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

final class ExtensionRoleRegistry extends AbstractRoleRegistry {

	private List activityBindingDefinitions;
	private IExtensionRegistry extensionRegistry;
	private List roleDefinitions;
	
	ExtensionRoleRegistry(IExtensionRegistry extensionRegistry) {
		if (extensionRegistry == null)
			throw new NullPointerException();
		
		this.extensionRegistry = extensionRegistry;
	}

	void load()
		throws IOException {	
		if (activityBindingDefinitions == null)
			activityBindingDefinitions = new ArrayList();
		else 
			activityBindingDefinitions.clear();

		if (roleDefinitions == null)
			roleDefinitions = new ArrayList();
		else 
			roleDefinitions.clear();		
				
		IConfigurationElement[] configurationElements = extensionRegistry.getConfigurationElementsFor(Persistence.PACKAGE_FULL);

		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement configurationElement = configurationElements[i];			
			String name = configurationElement.getName();

			if (Persistence.TAG_ACTIVITY_BINDING.equals(name))
				readActivityBindingDefinition(configurationElement);
			else if (Persistence.TAG_ROLE.equals(name))
				readRoleDefinition(configurationElement);			
		}

		boolean roleRegistryChanged = false;
			
		if (!activityBindingDefinitions.equals(super.activityBindingDefinitions)) {
			super.activityBindingDefinitions = Collections.unmodifiableList(activityBindingDefinitions);		
			roleRegistryChanged = true;
		}				

		if (!roleDefinitions.equals(super.roleDefinitions)) {
			super.roleDefinitions = Collections.unmodifiableList(roleDefinitions);		
			roleRegistryChanged = true;
		}		
		
		if (roleRegistryChanged)
			fireRoleRegistryChanged();
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

	private void readActivityBindingDefinition(IConfigurationElement configurationElement) {
		IActivityBindingDefinition activityBindingDefinition = Persistence.readActivityBindingDefinition(new ConfigurationElementMemento(configurationElement), getPluginId(configurationElement));
	
		if (activityBindingDefinition != null)
			activityBindingDefinitions.add(activityBindingDefinition);	
	}
	
	private void readRoleDefinition(IConfigurationElement configurationElement) {
		IRoleDefinition roleDefinition = Persistence.readRoleDefinition(new ConfigurationElementMemento(configurationElement), getPluginId(configurationElement));
	
		if (roleDefinition != null)
			roleDefinitions.add(roleDefinition);	
	}
}