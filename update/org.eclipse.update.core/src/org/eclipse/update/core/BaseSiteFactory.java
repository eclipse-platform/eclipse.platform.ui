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
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.FeatureReference;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

/**
 * Default implementation of a Site Factory
 * Must be sublassed
 * @since.2.0
 */

public abstract class BaseSiteFactory extends SiteModelFactory implements ISiteFactory {

	/*
	 * @see ISiteFactory#createSite(URL)
	 */
	public abstract ISite createSite(URL url) throws CoreException, InvalidSiteTypeException;


	/**
	 * return the appropriate resource bundle for this feature
	 */
	protected ResourceBundle getResourceBundle(URL url) throws IOException, CoreException {
		ResourceBundle bundle = null;
		try {
			ClassLoader l = new URLClassLoader(new URL[] { url }, null);
			bundle = ResourceBundle.getBundle(Site.SITE_FILE, Locale.getDefault(), l);
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
	 * @see SiteModelFactory#createSiteMapModel()
	 */
	public SiteMapModel createSiteMapModel() {
		return new Site();
	}
	/*
	 * @see SiteModelFactory#createFeatureReferenceModel()
	 */
	public FeatureReferenceModel createFeatureReferenceModel() {
		return new FeatureReference();
	}

	/*
	 * @see SiteModelFactory#createArchiveReferenceModel()
	 */
	public ArchiveReferenceModel createArchiveReferenceModel() {
		return new ArchiveReference();
	}

	/*
	 * @see SiteModelFactory#createURLEntryModel()
	 */
	public URLEntryModel createURLEntryModel() {
		return new URLEntry();
	}

	/*
	 * @see SiteModelFactory#createSiteCategoryModel()
	 */
	public SiteCategoryModel createSiteCategoryModel() {
		return new Category();
	}

}
