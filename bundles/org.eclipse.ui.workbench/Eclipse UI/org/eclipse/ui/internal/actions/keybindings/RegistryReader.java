/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions.keybindings;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.actions.Label;

final class RegistryReader extends org.eclipse.ui.internal.registry.RegistryReader {
	
	private final static String ATTRIBUTE_CONFIGURATION_ID = "configurationId"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$
	private final static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	private final static String ATTRIBUTE_KEY = "key"; //$NON-NLS-1$
	private final static String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$
	private final static String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private final static String ATTRIBUTE_PARENT_CONFIGURATION = "parentConfiguration"; //$NON-NLS-1$
	private final static String ATTRIBUTE_PARENT_SCOPE = "parentScope"; //$NON-NLS-1$
	private final static String ATTRIBUTE_PLATFORM = "platform"; //$NON-NLS-1$
	private final static String ATTRIBUTE_RANK = "rank"; //$NON-NLS-1$
	private final static String ATTRIBUTE_SCOPE_ID = "scopeId"; //$NON-NLS-1$
	private final static String ELEMENT_ACCELERATOR = "accelerator"; //$NON-NLS-1$
	private final static String ELEMENT_ACCELERATOR_CONFIGURATION = "acceleratorConfiguration"; //$NON-NLS-1$
	private final static String ELEMENT_ACCELERATOR_SCOPE = "acceleratorScope"; //$NON-NLS-1$
	private final static String ELEMENT_ACCELERATOR_SET = "acceleratorSet"; //$NON-NLS-1$
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$
	
	private String configuration;
	private Registry registry;
	private String scope;
	
	RegistryReader() {
		super();	
	}

	void read(IPluginRegistry pluginRegistry, Registry registry) {
		this.registry = registry;

		if (this.registry != null) {
			readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_ACCELERATOR_CONFIGURATIONS);
			readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_ACCELERATOR_SCOPES);
			readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_ACCELERATOR_SETS);
		}
	}

	protected boolean readElement(IConfigurationElement element) {
		String name = element.getName();

		if (ELEMENT_ACCELERATOR.equals(name))
			return readAccelerator(element);
		
		if (ELEMENT_ACCELERATOR_CONFIGURATION.equals(name))
			return readAcceleratorConfiguration(element);
		
		if (ELEMENT_ACCELERATOR_SCOPE.equals(name))
			return readAcceleratorScope(element);
		
		if (ELEMENT_ACCELERATOR_SET.equals(name))
			return readAcceleratorSet(element);
				
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

	private boolean readAccelerator(IConfigurationElement element) {
		if (configuration == null || scope == null)
			return false;

		String id = element.getAttribute(ATTRIBUTE_ID);
		String key = element.getAttribute(ATTRIBUTE_KEY);

		if (key == null) {
			logMissingAttribute(element, ATTRIBUTE_KEY);
			return true;
		}	

		List keySequences = KeyManager.parseKeySequences(key);
		
		if (keySequences.size() <= 0)
			return true;

		String locale = element.getAttribute(ATTRIBUTE_LOCALE);

		if (locale == null)
			locale = ZERO_LENGTH_STRING;

		String platform = element.getAttribute(ATTRIBUTE_PLATFORM);

		if (platform == null)
			platform = ZERO_LENGTH_STRING;

		String plugin = getPlugin(element);		
		int rank = 0;
		
		try {
			rank = Integer.valueOf(element.getAttribute(ATTRIBUTE_RANK)).intValue();		
		} catch (NumberFormatException eNumberFormat) {
		}
			
		Iterator iterator = keySequences.iterator();
		
		while (iterator.hasNext()) {
			KeySequence keySequence = (KeySequence) iterator.next();			
			Binding binding = Binding.create(id, configuration, keySequence, plugin, rank, scope);	
			registry.addRegionalBinding(RegionalBinding.create(binding, locale, platform));
		}

		return true;
	}

	private boolean readAcceleratorConfiguration(IConfigurationElement element) {
		String description = element.getAttribute(ATTRIBUTE_DESCRIPTION);
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

		String parent = element.getAttribute(ATTRIBUTE_PARENT_CONFIGURATION);
		String plugin = getPlugin(element);
		Configuration configuration = Configuration.create(Label.create(description, null, id, name), parent, plugin);
		registry.addConfiguration(configuration);
		return true;
	}

	private boolean readAcceleratorScope(IConfigurationElement element) {
		String description = element.getAttribute(ATTRIBUTE_DESCRIPTION);
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
			
		String parent = element.getAttribute(ATTRIBUTE_PARENT_SCOPE);
		String plugin = getPlugin(element);		
		Scope scope = Scope.create(Label.create(description, null, id, name), parent, plugin);
		registry.addScope(scope);
		return true;		
	}

	private boolean readAcceleratorSet(IConfigurationElement element) {
		configuration = element.getAttribute(ATTRIBUTE_CONFIGURATION_ID);
		scope = element.getAttribute(ATTRIBUTE_SCOPE_ID);
			
		if (configuration == null) {
			logMissingAttribute(element, ATTRIBUTE_CONFIGURATION_ID);
			return true;
		}
		
		if (scope == null) {
			logMissingAttribute(element, ATTRIBUTE_SCOPE_ID);
			return true;
		}

		readElementChildren(element);
		configuration = null;
		scope = null;
		return true;	
	}
}