package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.net.*;
import java.util.*;

import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.core.URLEncoder;

/**
 * Default implementation of a Site Factory
 * Must be sublassed
 * @since.2.0
 */

public abstract class BaseSiteFactory extends SiteModelFactory implements ISiteFactory {

	/*
	 * @see ISiteFactory#createSite(URL, boolean)
	 */
	public abstract ISite createSite(URL url, boolean forceCreation) throws IOException, InvalidSiteTypeException, ParsingException;

	/**
	 * return the appropriate resource bundle for this site
	 */
	protected ResourceBundle getResourceBundle(URL url) {
		ResourceBundle bundle = null;

		try {
			URL resolvedURL = URLEncoder.encode(url);
			ClassLoader l = new URLClassLoader(new URL[] { resolvedURL }, null);
			bundle = ResourceBundle.getBundle(Site.SITE_FILE, Locale.getDefault(), l);
		} catch (MissingResourceException e) {
			//ok, there is no bundle, keep it as null
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(e.getLocalizedMessage() + ":" + url.toExternalForm()); //$NON-NLS-1$
			}

			// do not attempt if URL like protocol://....#....
			if (url.getRef() == null) {
				// the URL may point ot a file.. attempt to use the parent directory
				String file = url.getFile();
				if (!file.endsWith("/")) { //$NON-NLS-1$
					try {
						int index = file.lastIndexOf('/');
						if (index != -1) {
							file = file.substring(0, index + 1);
							url = new URL(url.getProtocol(), url.getHost(), url.getPort(), file);
							URL resolvedURL = URLEncoder.encode(url);
							ClassLoader l = new URLClassLoader(new URL[] { resolvedURL }, null);
							bundle = ResourceBundle.getBundle(Site.SITE_FILE, Locale.getDefault(), l);
						}
					} catch (MalformedURLException e1) {
						if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
							UpdateManagerPlugin.getPlugin().debug(Policy.bind("BaseSiteFactory.CannotRetriveParentDirectory", url.toExternalForm()));  //$NON-NLS-1$
						}
					} catch (MissingResourceException e2) { //ok, there is no bundle, keep it as null
						//DEBUG:
						if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
							UpdateManagerPlugin.getPlugin().debug(e2.getLocalizedMessage() + ":" + url.toExternalForm());  //$NON-NLS-1$
						}
					}
				}
			}
		} catch (MalformedURLException e1) {
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(Policy.bind("BaseSiteFactory.CannotEncodeURL", url.toExternalForm()));  //$NON-NLS-1$
			}
		}

		return bundle;
	}

	/*
	 * @see SiteModelFactory#createSiteMapModel()
	*/
	public SiteModel createSiteMapModel() {
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
	public CategoryModel createSiteCategoryModel() {
		return new Category();
	}

}