package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.IFeatureFactory;
import org.eclipse.update.core.Utilities;

/**
 * Manages FeatureFactory extension point
 */
public final class FeatureTypeFactory {

	private static FeatureTypeFactory inst;
	private Map factories;

	private static final String SIMPLE_EXTENSION_ID = "featureTypes";
	//$NON-NLS-1$	

	/*
	 * hide constructor
	 */
	private FeatureTypeFactory() {
	}

	/*
	 * Singleton pattern
	 */
	public static FeatureTypeFactory getInstance() {
		if (inst == null)
			inst = new FeatureTypeFactory();
		return inst;
	}

	/*
	 * return the factory for the associated type
	 */
	public IFeatureFactory getFactory(String type) throws CoreException {
		//
		Object instance = getFactories().get(type);
		if (instance == null) {
			instance = createFactoryFor(type);
			getFactories().put(type, instance);
		}
		return (IFeatureFactory) instance;
	}

	/*
	 * creates a factory for the associated type and cache it 
	 */
	private IFeatureFactory createFactoryFor(String type) throws CoreException {
		IFeatureFactory result = null;

		String pluginID =
			UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		IConfigurationElement[] elements =
			pluginRegistry.getConfigurationElementsFor(pluginID, SIMPLE_EXTENSION_ID, type);

		if (elements == null || elements.length == 0) {
			throw Utilities.newCoreException(
					Policy.bind("FeatureTypeFactory.UnableToFindFeatureFactory", type),
					null);
			//$NON-NLS-1$
		} 

		IConfigurationElement element = elements[0];
		result = (IFeatureFactory) element.createExecutableExtension("class");
			//$NON-NLS-1$
		return result;
	}

	/*
	 * 
	 */
	private Map getFactories() {
		if (factories == null)
			factories = new HashMap();
		return factories;
	}
}