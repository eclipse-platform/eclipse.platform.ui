package org.eclipse.update.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.ISiteFactory;
import org.eclipse.update.core.Utilities;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * 
 */
public final class SiteTypeFactory {
	

	/**
	 * extension point ID
	 */
	public static final String SIMPLE_EXTENSION_ID = "siteTypes";	 //$NON-NLS-1$
	

	private static SiteTypeFactory inst;
	
	private Map factories;

	/**
	 * hide ctr 
	 */
	private SiteTypeFactory() {
	}

	public static SiteTypeFactory getInstance() {
		if (inst == null)
			inst = new SiteTypeFactory();
		return inst;
	}


	/**
	 * return the factory for the type
	 */
	public ISiteFactory getFactory(String type) throws CoreException {
			//
			Object instance = getFactories().get(type);
			if (instance==null) {
				instance = createFactoryFor(type);
				getFactories().put(type,instance);
			}
			return (ISiteFactory) instance;
	}

	/**
	 * 
	 */
	private ISiteFactory createFactoryFor(String type) throws CoreException {
		ISiteFactory result = null;
		
		String pluginID = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		IConfigurationElement[] elements = pluginRegistry.getConfigurationElementsFor(pluginID,SIMPLE_EXTENSION_ID,type);
		if (elements==null || elements.length==0){
			throw Utilities.newCoreException(Policy.bind("SiteTypeFactory.UnableToFindSiteFactory",type),null); //$NON-NLS-1$
		} else {
			IConfigurationElement element = elements[0];
			result = (ISiteFactory)element.createExecutableExtension("class"); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Gets the actories.
	 * @return Returns a Map
	 */
	private Map getFactories() {
		if (factories==null) factories = new HashMap();
			return factories;
	}

	/**
	 * Sets the actories.
	 * @param actories The actories to set
	 */
	private void setFactories(Map factories) {
		factories = factories;
	}

}