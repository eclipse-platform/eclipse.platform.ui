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

package org.eclipse.ui.internal.chris.roles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.internal.roles.api.IRoleDefinition;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

final class PluginRoleRegistry extends AbstractRoleRegistry {

	private final class PluginRegistryReader extends RegistryReader {

		protected boolean readElement(IConfigurationElement element) {
			String name = element.getName();

			if (Persistence.TAG_ROLE.equals(name))
				return readRoleDefinition(element);

			return true; // TODO return false;
		}		
	}

	private final static String TAG_ROOT = Persistence.PACKAGE_BASE;
	
	private List roleDefinitions;
	private IPluginRegistry pluginRegistry;
	private PluginRegistryReader pluginRegistryReader;
	
	PluginRoleRegistry(IPluginRegistry pluginRegistry) {
		if (pluginRegistry == null)
			throw new NullPointerException();
		
		this.pluginRegistry = pluginRegistry;
	}

	void load()
		throws IOException {	
		if (roleDefinitions == null)
			roleDefinitions = new ArrayList();
		else 
			roleDefinitions.clear();

		if (pluginRegistryReader == null)
			pluginRegistryReader = new PluginRegistryReader();

		pluginRegistryReader.readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, TAG_ROOT);
		boolean roleRegistryChanged = false;
			
		if (!roleDefinitions.equals(super.roleDefinitions)) {
			super.roleDefinitions = Collections.unmodifiableList(roleDefinitions);		
			roleRegistryChanged = true;
		}				
				
		if (roleRegistryChanged)
			fireRoleRegistryChanged();
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

	private boolean readRoleDefinition(IConfigurationElement element) {
		IRoleDefinition roleDefinition = Persistence.readRoleDefinition(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (roleDefinition != null)
			roleDefinitions.add(roleDefinition);	
		
		return true;
	}
}