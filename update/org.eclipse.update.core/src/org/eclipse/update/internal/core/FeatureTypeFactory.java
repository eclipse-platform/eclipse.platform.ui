package org.eclipse.update.internal.core;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.IFeatureFactory;
import org.eclipse.update.internal.core.Policy;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * 
 */
public final class FeatureTypeFactory {

	private static FeatureTypeFactory inst;
	private Map factories;
	
	private static final String SIMPLE_EXTENSION_ID = "featureTypes"; //$NON-NLS-1$	

	/**
	 * hide ctr 
	 */
	private FeatureTypeFactory() {
	}

	public static FeatureTypeFactory getInstance() {
		if (inst == null)
			inst = new FeatureTypeFactory();
		return inst;
	}


	/**
	 * return the factory for the type
	 */
	public IFeatureFactory getFactory(String type) throws CoreException {
			//
			Object instance = getFactories().get(type);
			if (instance==null) {
				instance = createFactoryFor(type);
				getFactories().put(type,instance);
			}
			return (IFeatureFactory) instance;
	}

	/**
	 * 
	 */
	private IFeatureFactory createFactoryFor(String type) throws CoreException {
		IFeatureFactory result = null;
		
		String pluginID = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		IConfigurationElement[] elements = pluginRegistry.getConfigurationElementsFor(pluginID,SIMPLE_EXTENSION_ID,type);
		if (elements==null || elements.length==0){
			IStatus status = new Status(IStatus.ERROR,pluginID,IStatus.OK,Policy.bind("FeatureTypeFactory.UnableToFindFeatureFactory",type),null); //$NON-NLS-1$
			throw new CoreException(status);
		} else {
			IConfigurationElement element = elements[0];
			result = (IFeatureFactory)element.createExecutableExtension("class"); //$NON-NLS-1$
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