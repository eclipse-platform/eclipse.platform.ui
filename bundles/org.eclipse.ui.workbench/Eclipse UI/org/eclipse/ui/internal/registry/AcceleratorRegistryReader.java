package org.eclipse.ui.internal.registry;

/**
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;

final class AcceleratorRegistryReader extends RegistryReader {
	
	private final static String ATTRIBUTE_CONFIGURATION_ID = "configurationId"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$
	private final static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	private final static String ATTRIBUTE_KEY = "key"; //$NON-NLS-1$
	private final static String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$
	private final static String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private final static String ATTRIBUTE_PARENT_CONFIGURATION = "parentConfiguration"; //$NON-NLS-1$
	private final static String ATTRIBUTE_PARENT_SCOPE = "parentScope"; //$NON-NLS-1$
	private final static String ATTRIBUTE_PLATFORM = "platform"; //$NON-NLS-1$
	private final static String ATTRIBUTE_SCOPE_ID = "scopeId"; //$NON-NLS-1$
	private final static String ELEMENT_ACCELERATOR = "accelerator"; //$NON-NLS-1$
	private final static String ELEMENT_ACCELERATOR_CONFIGURATION = "acceleratorConfiguration"; //$NON-NLS-1$
	private final static String ELEMENT_ACCELERATOR_SCOPE = "acceleratorScope"; //$NON-NLS-1$
	private final static String ELEMENT_ACCELERATOR_SET = "acceleratorSet"; //$NON-NLS-1$
	
	private AcceleratorRegistry acceleratorRegistry;
	private AcceleratorSet acceleratorSet;

	AcceleratorRegistryReader() {
		super();	
	}

	void read(IPluginRegistry registry, AcceleratorRegistry acceleratorRegistry) {
		this.acceleratorRegistry = acceleratorRegistry;
		readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_ACCELERATOR_CONFIGURATIONS);
		readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_ACCELERATOR_SCOPES);
		readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_ACCELERATOR_SETS);
	}

	protected boolean readElement(IConfigurationElement element) {
		String name = element.getName();
		
		if (ELEMENT_ACCELERATOR_CONFIGURATION.equals(name))
			return readAcceleratorConfiguration(element);
		
		if (ELEMENT_ACCELERATOR_SCOPE.equals(name))
			return readAcceleratorScope(element);
		
		if (ELEMENT_ACCELERATOR_SET.equals(name))
			return readAcceleratorSet(element);
		
		if (ELEMENT_ACCELERATOR.equals(name))
			return readAccelerator(element);
		
		return false;
	}

	private boolean readAcceleratorConfiguration(IConfigurationElement element) {
		String id = element.getAttribute(ATTRIBUTE_ID);
		String name = element.getAttribute(ATTRIBUTE_NAME);
		String description = element.getAttribute(ATTRIBUTE_DESCRIPTION);
		String parent = element.getAttribute(ATTRIBUTE_PARENT_CONFIGURATION);
			
		if (id == null)
			logMissingAttribute(element, ATTRIBUTE_ID);
		
		if (name == null)
			logMissingAttribute(element, ATTRIBUTE_NAME);
		
		if (description == null)
			logMissingAttribute(element, ATTRIBUTE_DESCRIPTION);

		String pluginId = element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
		AcceleratorConfiguration acceleratorConfiguration = new AcceleratorConfiguration(id, name, description, parent, pluginId);
		acceleratorRegistry.addConfiguration(acceleratorConfiguration);
		return true;
	}

	private boolean readAcceleratorScope(IConfigurationElement element) {
		String id = element.getAttribute(ATTRIBUTE_ID);
		String name = element.getAttribute(ATTRIBUTE_NAME);
		String description = element.getAttribute(ATTRIBUTE_DESCRIPTION);
		String parent = element.getAttribute(ATTRIBUTE_PARENT_SCOPE);
			
		if (id == null)
			logMissingAttribute(element, ATTRIBUTE_ID);
		
		if (name == null)
			logMissingAttribute(element, ATTRIBUTE_NAME);
		
		if (description == null)
			logMissingAttribute(element, ATTRIBUTE_DESCRIPTION);

		String pluginId = element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
		AcceleratorScope acceleratorScope = new AcceleratorScope(id, name, description, parent, pluginId);
		acceleratorRegistry.addScope(acceleratorScope);
		return true;		
	}

	private boolean readAcceleratorSet(IConfigurationElement element) {
		String configurationId = element.getAttribute(ATTRIBUTE_CONFIGURATION_ID);
		String scopeId = element.getAttribute(ATTRIBUTE_SCOPE_ID);
			
		if (configurationId == null)
			logMissingAttribute(element, ATTRIBUTE_CONFIGURATION_ID);
		
		if (scopeId == null)
			logMissingAttribute(element, ATTRIBUTE_SCOPE_ID);

		String pluginId = element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
		acceleratorSet = new AcceleratorSet(configurationId, scopeId,  pluginId);
		acceleratorRegistry.addSet(acceleratorSet);
		readElementChildren(element);
		acceleratorSet = null;
		return true;	
	}

	private boolean readAccelerator(IConfigurationElement element) {
		String id = element.getAttribute(ATTRIBUTE_ID);
		String key = element.getAttribute(ATTRIBUTE_KEY);
		String locale = element.getAttribute(ATTRIBUTE_LOCALE);
		String platform = element.getAttribute(ATTRIBUTE_PLATFORM);
		
		if (id == null)
			logMissingAttribute(element, ATTRIBUTE_ID);
		
		if (key == null)
			logMissingAttribute(element, ATTRIBUTE_KEY);	
		
		if (acceleratorSet != null)
			acceleratorSet.add(new Accelerator(id, key, locale, platform));

		return true;
	}	
}
