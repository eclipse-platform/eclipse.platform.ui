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

package org.eclipse.ui.internal.csm.activities;

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
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

final class PluginActivityRegistry extends AbstractActivityRegistry {

	private final class PluginRegistryReader extends RegistryReader {

		protected boolean readElement(IConfigurationElement element) {
			String name = element.getName();

			if (Persistence.TAG_ACTIVITY.equals(name))
				return readActivityDefinition(element);

			return true; // TODO return false;
		}		
	}

	private final static String TAG_ROOT = Persistence.PACKAGE_BASE;
	
	private List activityDefinitions;
	private IPluginRegistry pluginRegistry;
	private PluginRegistryReader pluginRegistryReader;
	
	PluginActivityRegistry(IPluginRegistry pluginRegistry) {
		if (pluginRegistry == null)
			throw new NullPointerException();
		
		this.pluginRegistry = pluginRegistry;
	}

	void load()
		throws IOException {	
		if (activityDefinitions == null)
			activityDefinitions = new ArrayList();
		else 
			activityDefinitions.clear();

		if (pluginRegistryReader == null)
			pluginRegistryReader = new PluginRegistryReader();

		pluginRegistryReader.readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, TAG_ROOT);
		boolean activityRegistryChanged = false;
			
		if (!activityDefinitions.equals(super.activityDefinitions)) {
			super.activityDefinitions = Collections.unmodifiableList(activityDefinitions);		
			activityRegistryChanged = true;
		}				
				
		if (activityRegistryChanged)
			fireActivityRegistryChanged();
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

	private boolean readActivityDefinition(IConfigurationElement element) {
		IActivityDefinition activityDefinition = Persistence.readActivityDefinition(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (activityDefinition != null)
			activityDefinitions.add(activityDefinition);	
		
		return true;
	}
}