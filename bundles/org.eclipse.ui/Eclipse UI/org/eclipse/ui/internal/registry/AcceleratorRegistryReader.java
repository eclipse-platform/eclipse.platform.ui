package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;

import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * This class is used to read accelerator configurations, accelerator scopes,
 * and accelerator sets (as well as the accelerators they contain, in certain cases)
 * from the platform registry and stores them in the accelerator registry. An
 * accelerator set's accelerators are only read if the set belongs to the active
 * accelerator configuration or the default accelerator configuration. 
 */
public class AcceleratorRegistryReader extends RegistryReader{	
	private static final String TAG_ACCEL_CONFIG = "acceleratorConfiguration";
	private static final String TAG_ACCEL_SCOPE = "acceleratorScope";
	private static final String TAG_ACCEL_SET = "acceleratorSet";
	private static final String TAG_ACCELERATOR = "accelerator";
	
	private static final String CONFIG_ATT_ID = "id";
	private static final String CONFIG_ATT_NAME = "name";
	private static final String CONFIG_ATT_DESCRIPTION = "description";
	
	private static final String SCOPE_ATT_ID = "id";
	private static final String SCOPE_ATT_NAME = "name";
	private static final String SCOPE_ATT_DESCRIPTION = "description";
	private static final String SCOPE_ATT_PARENT_SCOPE = "parentScope";
	
	private static final String SET_ATT_CONFIG_ID = "configurationId";
	private static final String SET_ATT_SCOPE_ID = "scopeId";
	
	private static final String ACCEL_ATT_ID = "id";
	private static final String ACCEL_ATT_KEY = "key";
	private static final String ACCEL_ATT_LOCALE = "locale";
	private static final String ACCEL_ATT_PLATFORM = "platform";
	
	private AcceleratorRegistry acceleratorRegistry;
	private AcceleratorSet acceleratorSet;

	/* (non-Javadoc)
	 * Method declared in RegistryReader.
	 */	
	protected boolean readElement(IConfigurationElement element) {
		String name = element.getName();
		if (name.equals(TAG_ACCEL_CONFIG))
			return readConfiguration(element);
		if (name.equals(TAG_ACCEL_SCOPE))
			return readScope(element);
		if (name.equals(TAG_ACCEL_SET))
			return readSet(element);
		if (name.equals(TAG_ACCELERATOR))
			return readAccelerator(element);
		return false;
	}

	/**
	 * Reads an element if it is an acclerator configuration, and stores it in
	 * the accelerator registry.
	 */	
	private boolean readConfiguration(IConfigurationElement element) {
		String id = element.getAttribute(CONFIG_ATT_ID);
		String name = element.getAttribute(CONFIG_ATT_NAME);
		String description = element.getAttribute(CONFIG_ATT_DESCRIPTION);
			
		if (id==null) {
			logMissingAttribute(element, CONFIG_ATT_ID);
		}
		if (name==null) {
			logMissingAttribute(element, CONFIG_ATT_NAME);
		}
		if (description==null) {
			logMissingAttribute(element, CONFIG_ATT_DESCRIPTION);
		}
	
		AcceleratorConfiguration a = new AcceleratorConfiguration(id, name, description);
		acceleratorRegistry.addConfiguration(a);
		return true;
	}

	/**
	 * Reads an element if it is an acclerator scope, and stores it in
	 * the accelerator registry.
	 */
	private boolean readScope(IConfigurationElement element) {
		String id = element.getAttribute(SCOPE_ATT_ID);
		String name = element.getAttribute(SCOPE_ATT_NAME);
		String description = element.getAttribute(SCOPE_ATT_DESCRIPTION);
		String parentScope = element.getAttribute(SCOPE_ATT_PARENT_SCOPE);
			
		if (id==null) {
			logMissingAttribute(element, SCOPE_ATT_ID);
		}
		if (name==null) {
			logMissingAttribute(element, SCOPE_ATT_NAME);
		}
		if (description==null) {
			logMissingAttribute(element, SCOPE_ATT_DESCRIPTION);
		}

		AcceleratorScope a = new AcceleratorScope(id, name, description, parentScope);
		acceleratorRegistry.addScope(a);
		return true;		
	}

	/**
	 * Reads an element if it is an acclerator set, and stores it in
	 * the accelerator registry. If the set belongs to the active or
	 * default accelerator configuration, the set's accelerators are
	 * also read and stored in the accelerator registry.
	 */	
	private boolean readSet(IConfigurationElement element) {
		String configurationId = element.getAttribute(SET_ATT_CONFIG_ID);
		String scopeId = element.getAttribute(SET_ATT_SCOPE_ID);
			
		if (configurationId==null) {
			logMissingAttribute(element, SET_ATT_CONFIG_ID);
		}
		if (scopeId==null) {
			logMissingAttribute(element, SET_ATT_SCOPE_ID);
		}

		String pluginId = element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
		acceleratorSet = new AcceleratorSet(configurationId, scopeId,  pluginId);
		acceleratorRegistry.addSet(acceleratorSet);
		
		String configId = acceleratorSet.getConfigurationId();
//		if(configId == IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID)
			readElementChildren(element);
//		if(configId == keyBindingService.getActiveAcceleratorConfigurationId())		
//			readElementChildren(element);

		return true;	
	}

	/**
	 * Reads an element if it is an accelerator, and stores it in it's accelerator
	 * set in the accelerator registry.
	 */	
	private boolean readAccelerator(IConfigurationElement element) {
		String id = element.getAttribute(ACCEL_ATT_ID);
		String key = element.getAttribute(ACCEL_ATT_KEY);
		String locale = element.getAttribute(ACCEL_ATT_LOCALE);
		String platform = element.getAttribute(ACCEL_ATT_PLATFORM);
		
		if (id==null) {
			logMissingAttribute(element, ACCEL_ATT_ID);
		}
		if (key==null) {
			logMissingAttribute(element, ACCEL_ATT_KEY);	
		}
		
		Accelerator accelerator = new Accelerator(id, key, locale, platform);
		acceleratorSet.add(accelerator);		
		return true;	
	}
	
	/**
	 * Reads from the plugin registry and stores results in the accelerator
	 * registry.
	 */	
	public void read(IPluginRegistry registry, AcceleratorRegistry out) {
		acceleratorRegistry = out;
		readRegistry(registry, IWorkbenchConstants.PLUGIN_ID, IWorkbenchConstants.PL_ACCELERATOR_CONFIGURATIONS);
		readRegistry(registry, IWorkbenchConstants.PLUGIN_ID, IWorkbenchConstants.PL_ACCELERATOR_SCOPES);
		readRegistry(registry, IWorkbenchConstants.PLUGIN_ID, IWorkbenchConstants.PL_ACCELERATOR_SETS);
	}
}
