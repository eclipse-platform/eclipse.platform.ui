package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

/**
 * Default implementation of a Feature Factory
 * Must be sublassed
 * @since.2.0
 */

public abstract class BaseFeatureFactory extends FeatureModelFactory implements IFeatureFactory {

	/*
	 * @see IFeatureFactory#createFeature(URL,ISite)
	 */
	public abstract IFeature createFeature(URL url, ISite site) throws CoreException;

	/*
	 * @see IFeatureFactory#createFeature(ISite)
	 */
	public IFeature createFeature(ISite site) throws CoreException {
		return createFeature(null/*URL*/, site);
	}

	/**
	 * return the appropriate resource bundle for this feature
	 */
	protected ResourceBundle getResourceBundle(URL url) throws IOException, CoreException {
		ResourceBundle bundle = null;
		try {
			ClassLoader l = new URLClassLoader(new URL[] { url }, null);
			bundle = ResourceBundle.getBundle(Feature.FEATURE_FILE, Locale.getDefault(), l);
		} catch (MissingResourceException e) {
			//ok, there is no bundle, keep it as null
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(e.getLocalizedMessage() + ":" + url.toExternalForm());
			}
		}
		return bundle;
	}


	/*
	 * @see FeatureModelFactory#createFeatureModel()
	 */
	public FeatureModel createFeatureModel() {
		return new Feature();
	}

	/*
	 * @see FeatureModelFactory#createInstallHandlerModel()
	 */
	public InstallHandlerModel createInstallHandlerModel() {
		return null;
	}

	/*
	 * @see FeatureModelFactory#createImportModel()
	 */
	public ImportModel createImportModel() {
		return new Import();
	}

	/*
	 * @see FeatureModelFactory#createPluginEntryModel()
	 */
	public PluginEntryModel createPluginEntryModel() {
		return new PluginEntry();
	}

	/*
	 * @see FeatureModelFactory#createNonPluginEntryModel()
	 */
	public NonPluginEntryModel createNonPluginEntryModel() {
		return new NonPluginEntry();
	}

	/*
	 * @see FeatureModelFactory#createContentGroupModel()
	 */
	public ContentGroupModel createContentGroupModel() {
		return null;
	}

	/*
	 * @see FeatureModelFactory#createURLEntryModel()
	 */
	public URLEntryModel createURLEntryModel() {
		return new URLEntry();
	}

}