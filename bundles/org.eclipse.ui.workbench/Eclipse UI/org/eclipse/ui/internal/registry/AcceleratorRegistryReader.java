/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
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

	private boolean readAcceleratorConfiguration(IConfigurationElement element) {
		String id = element.getAttribute(ATTRIBUTE_ID);
		String name = element.getAttribute(ATTRIBUTE_NAME);
		String description = element.getAttribute(ATTRIBUTE_DESCRIPTION);
		String parentId = element.getAttribute(ATTRIBUTE_PARENT_CONFIGURATION);
			
		if (id == null)
			logMissingAttribute(element, ATTRIBUTE_ID);
		
		if (name == null)
			logMissingAttribute(element, ATTRIBUTE_NAME);
		
		if (description == null)
			logMissingAttribute(element, ATTRIBUTE_DESCRIPTION);

		String pluginId = getPluginId(element);
		AcceleratorConfiguration acceleratorConfiguration = new AcceleratorConfiguration(id, name, description, parentId, pluginId);
		acceleratorRegistry.addAcceleratorConfiguration(acceleratorConfiguration);
		return true;
	}

	private boolean readAcceleratorScope(IConfigurationElement element) {
		String id = element.getAttribute(ATTRIBUTE_ID);
		String name = element.getAttribute(ATTRIBUTE_NAME);
		String description = element.getAttribute(ATTRIBUTE_DESCRIPTION);
		String parentId = element.getAttribute(ATTRIBUTE_PARENT_SCOPE);
			
		if (id == null)
			logMissingAttribute(element, ATTRIBUTE_ID);
		
		if (name == null)
			logMissingAttribute(element, ATTRIBUTE_NAME);
		
		if (description == null)
			logMissingAttribute(element, ATTRIBUTE_DESCRIPTION);

		String pluginId = getPluginId(element);
		AcceleratorScope acceleratorScope = new AcceleratorScope(id, name, description, parentId, pluginId);
		acceleratorRegistry.addAcceleratorScope(acceleratorScope);
		return true;		
	}

	private boolean readAcceleratorSet(IConfigurationElement element) {
		String acceleratorConfigurationId = element.getAttribute(ATTRIBUTE_CONFIGURATION_ID);
		String acceleratorScopeId = element.getAttribute(ATTRIBUTE_SCOPE_ID);
			
		if (acceleratorConfigurationId == null)
			logMissingAttribute(element, ATTRIBUTE_CONFIGURATION_ID);
		
		if (acceleratorScopeId == null)
			logMissingAttribute(element, ATTRIBUTE_SCOPE_ID);

		String pluginId = getPluginId(element);
		acceleratorSet = new AcceleratorSet(acceleratorConfigurationId, acceleratorScopeId,  pluginId);
		acceleratorRegistry.addAcceleratorSet(acceleratorSet);
		readElementChildren(element);
		acceleratorSet = null;
		return true;	
	}

	private boolean readAccelerator(IConfigurationElement element) {
		String id = element.getAttribute(ATTRIBUTE_ID);
		String key = element.getAttribute(ATTRIBUTE_KEY);
		String locale = element.getAttribute(ATTRIBUTE_LOCALE);
		String platform = element.getAttribute(ATTRIBUTE_PLATFORM);

		if (key == null)
			logMissingAttribute(element, ATTRIBUTE_KEY);	
		
		if (acceleratorSet != null)
			acceleratorSet.addAccelerator(new Accelerator(id, key, locale, platform));

		return true;
	}	
}
