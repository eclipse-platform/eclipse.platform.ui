package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.model.FeatureModel;
import org.eclipse.update.core.model.FeatureModelFactory;
import org.eclipse.update.core.model.ImportModel;
import org.eclipse.update.core.model.InstallHandlerEntryModel;
import org.eclipse.update.core.model.NonPluginEntryModel;
import org.eclipse.update.core.model.PluginEntryModel;
import org.eclipse.update.core.model.URLEntryModel;
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

	/**
	 * return the appropriate resource bundle for this feature
	 */
	protected ResourceBundle getResourceBundle(URL url) throws IOException, CoreException {
		if (url == null)
			return null;
			
		ResourceBundle bundle = null;
		try {
			ClassLoader l = new URLClassLoader(new URL[] { url }, null);
			bundle = ResourceBundle.getBundle(Feature.FEATURE_FILE, Locale.getDefault(), l);
		} catch (MissingResourceException e) {
			//ok, there is no bundle, keep it as null
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(e.getLocalizedMessage() + ":" + url.toExternalForm()); //$NON-NLS-1$
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
	 * @see FeatureModelFactory#createInstallHandlerEntryModel()
	 */
	public InstallHandlerEntryModel createInstallHandlerEntryModel() {
		return new InstallHandlerEntry();
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
	 * @see FeatureModelFactory#createURLEntryModel()
	 */
	public URLEntryModel createURLEntryModel() {
		return new URLEntry();
	}

}