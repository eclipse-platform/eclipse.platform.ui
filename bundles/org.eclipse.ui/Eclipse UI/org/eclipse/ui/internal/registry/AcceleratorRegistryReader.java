package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Locale;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;

import org.eclipse.swt.SWT;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * This class is used to read accelerator configurations, accelerator scopes,
 * and accelerator sets (as well as the accelerators they contain, in certain cases)
 * from the platform registry and stores them in the accelerator registry. An
 * accelerator set's accelerators are only read if the set belongs to the active
 * accelerator configuration or the default accelerator configuration. 
 */
public class AcceleratorRegistryReader extends RegistryReader{	
	private static final String TAG_ACCEL_CONFIG = "acceleratorConfiguration"; //$NON-NLS-1$
	private static final String TAG_ACCEL_SCOPE = "acceleratorScope"; //$NON-NLS-1$
	private static final String TAG_ACCEL_SET = "acceleratorSet"; //$NON-NLS-1$
	private static final String TAG_ACCELERATOR = "accelerator"; //$NON-NLS-1$
	
	private static final String CONFIG_ATT_ID = "id"; //$NON-NLS-1$
	private static final String CONFIG_ATT_NAME = "name"; //$NON-NLS-1$
	private static final String CONFIG_ATT_DESCRIPTION = "description"; //$NON-NLS-1$
	
	private static final String SCOPE_ATT_ID = "id"; //$NON-NLS-1$
	private static final String SCOPE_ATT_NAME = "name"; //$NON-NLS-1$
	private static final String SCOPE_ATT_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String SCOPE_ATT_PARENT_SCOPE = "parentScope"; //$NON-NLS-1$
	
	private static final String SET_ATT_CONFIG_ID = "configurationId"; //$NON-NLS-1$
	private static final String SET_ATT_SCOPE_ID = "scopeId"; //$NON-NLS-1$
	
	private static final String ACCEL_ATT_ID = "id"; //$NON-NLS-1$
	private static final String ACCEL_ATT_KEY = "key"; //$NON-NLS-1$
	private static final String ACCEL_ATT_LOCALE = "locale"; //$NON-NLS-1$
	private static final String ACCEL_ATT_PLATFORM = "platform"; //$NON-NLS-1$
 
 	private static final String PLATFORM = SWT.getPlatform();
 	private static final String EMPTY = ""; //$NON-NLS-1$
 	
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
		acceleratorSet = acceleratorRegistry.getSet(configurationId,scopeId,pluginId);
		if(acceleratorSet == null) {
			acceleratorSet = new AcceleratorSet(configurationId, scopeId,  pluginId);
			acceleratorRegistry.addSet(acceleratorSet);
		}
		readElementChildren(element);
		return true;	
	}
	/*
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
		Accelerator oldAcc = acceleratorSet.getAccelerator(id);
		Accelerator newAcc = new Accelerator(id, key, locale, platform);
		int newAccMatchValue = computeValue(newAcc);
		if(newAccMatchValue < 0)
			return true;
		if(oldAcc == null) {
			acceleratorSet.add(newAcc);
		} else {
			int oldAccMatchValue = computeValue(oldAcc);
			if(oldAccMatchValue < newAccMatchValue) {
				acceleratorSet.removeAccelerator(oldAcc);
				acceleratorSet.add(newAcc);
			}
		}
		return true;
	}
	/*
	 * Return a value representing how well acc has match platform and locale.
	 * Return -1 if acc should not be include in the current platform, ie, if the
	 * platform and/or locale are specified and don't match.
	 * Return a number between 0 and 7 if platform and/or locale match.
	 */
	private int computeValue(Accelerator acc) {
		int result = 0;
		Locale defaultLocale = Locale.getDefault();
		if(defaultLocale.toString().equals(acc.getLocale())) {
			result = result + 3;
		} else {
			String[] localeArray = parseLocale(acc.getLocale());
			//Language
			if(localeArray[0].equals(defaultLocale.getLanguage()))
				result++;
			else if(localeArray[0] != EMPTY)
				return -1;
			//Country
			if(localeArray[1].equals(defaultLocale.getCountry()))
				result++;
			else if(localeArray[1] != EMPTY)
				return -1;
			//Variant
			if(localeArray[2].equals(defaultLocale.getVariant()))
				result++;
			else if(localeArray[2] != EMPTY)
				return -1;	
		}
		//Platform
		if(PLATFORM.equals(acc.getPlatform()))
			result = result + 4;
		else if(!Accelerator.DEFAULT_PLATFORM.equals(acc.getPlatform()))
			return -1;
		return result;
	}
	/*
	 * Return a new String[3]{language,country,variant} 
	 */
	private String[] parseLocale(String locale) {
		//Parse language
		String localeArray[] = {EMPTY,EMPTY,EMPTY};
		if(Accelerator.DEFAULT_LOCALE.equals(locale))
			return localeArray;
			
		int index = locale.indexOf("_"); //$NON-NLS-1$
		if(index < 0) {
			localeArray[0] = locale;
			return localeArray;
		} else if(index >= 0) {
			localeArray[0] = locale.substring(0,index);
		}
		if(index + 1 >= locale.length())
			return localeArray;
		//Parse country
		int newIndex = locale.indexOf("_",index + 1); //$NON-NLS-1$
		if(newIndex < 0) {
			localeArray[1] = locale.substring(index + 1);
			return localeArray;
		} else if(newIndex > 0) {
			localeArray[1] = locale.substring(index + 1,newIndex);
		}
		index = newIndex;
		if(index + 1 >= locale.length())
			return localeArray;
		//Parse variant
		newIndex = locale.indexOf("_",index + 1); //$NON-NLS-1$
		if(newIndex < 0) {
			localeArray[2] = locale.substring(index + 1);
			return localeArray;
		} else if(newIndex > 0) {
			localeArray[2] = locale.substring(index + 1,newIndex);
		}
		return localeArray;
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
