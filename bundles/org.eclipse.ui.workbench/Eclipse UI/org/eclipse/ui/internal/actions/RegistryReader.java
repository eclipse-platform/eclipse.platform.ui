/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;

final class RegistryReader extends org.eclipse.ui.internal.registry.RegistryReader {

	private final static String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$
	private final static String ATTRIBUTE_ICON = "icon"; //$NON-NLS-1$
	private final static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	private final static String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private final static String ELEMENT_ACTION_DEFINITION = "actionDefinition"; //$NON-NLS-1$
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$
	
	private Registry registry;
	
	RegistryReader() {
		super();	
	}

	void read(IPluginRegistry pluginRegistry, Registry registry) {
		this.registry = registry;

		if (this.registry != null) {
			readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_ACTION_DEFINITIONS);
		}
	}

	protected boolean readElement(IConfigurationElement element) {
		String name = element.getName();
		
		if (ELEMENT_ACTION_DEFINITION.equals(name))
			return readActionDefinition(element);
		
		return false;
	}

	private String getPlugin(IConfigurationElement element) {
		String plugin = null;	
		
		if (element != null) {	
			IExtension extension = element.getDeclaringExtension();
			
			if (extension != null) {
				IPluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
				
				if (pluginDescriptor != null) 
					plugin = pluginDescriptor.getUniqueIdentifier();				
			}
		}

		return plugin;
	}

	private boolean readActionDefinition(IConfigurationElement element) {
		String description = element.getAttribute(ATTRIBUTE_DESCRIPTION);
		String icon = element.getAttribute(ATTRIBUTE_ICON);
		String id = element.getAttribute(ATTRIBUTE_ID);

		if (id == null) {
			logMissingAttribute(element, ATTRIBUTE_ID);
			return true;
		}

		String name = element.getAttribute(ATTRIBUTE_NAME);
		
		if (name == null) {
			logMissingAttribute(element, ATTRIBUTE_NAME);
			return true;
		}
		
		String plugin = getPlugin(element);
		Action action = Action.create(Label.create(description, icon, id, name), plugin);			
		registry.addAction(action);
		return true;
	}
}
