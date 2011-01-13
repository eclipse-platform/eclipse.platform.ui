/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;

// There is no replacement for PackageAdmin#getBundles()
@SuppressWarnings("deprecation")
public class BundleTranslationProvider extends TranslationService {

	/**
	 * The schema identifier used for Eclipse platform references
	 */
	final private static String PLATFORM_SCHEMA = "platform"; //$NON-NLS-1$
	final private static String PLUGIN_SEGMENT = "/plugin/"; //$NON-NLS-1$
	final private static String FRAGMENT_SEGMENT = "/fragment/"; //$NON-NLS-1$

	/**
	 * Prefix for keys to be translated
	 */
	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$

	/**
	 * Prefix that aborts translation
	 */
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$	

	@Override
	public String translate(String key, String contributorURI) {
		Bundle bundle = getBundle(contributorURI);
		if (bundle == null)
			return key;
		BundleLocalization localizationService = ServicesActivator.getDefault()
				.getLocalizationService();
		if (localizationService == null)
			return key;
		// TBD locale might contain extra information, such as calendar specification
		// that might need to be removed.
		ResourceBundle resourceBundle = localizationService.getLocalization(bundle, locale);
		return getResourceString(key, resourceBundle);
	}

	private Bundle getBundle(String contributorURI) {
		if (contributorURI == null)
			return null;
		URI uri;
		try {
			uri = new URI(contributorURI);
		} catch (URISyntaxException e) {
			LogService logService = ServicesActivator.getDefault().getLogService();
			if (logService != null)
				logService.log(LogService.LOG_ERROR, "Invalid contributor URI: " + contributorURI); //$NON-NLS-1$
			return null;
		}
		if (!PLATFORM_SCHEMA.equals(uri.getScheme()))
			return null; // not implemented
		String bundleName = uri.getPath();
		if (bundleName.startsWith(PLUGIN_SEGMENT))
			bundleName = bundleName.substring(PLUGIN_SEGMENT.length());
		else if (bundleName.startsWith(FRAGMENT_SEGMENT))
			bundleName = bundleName.substring(FRAGMENT_SEGMENT.length());
		PackageAdmin packageAdmin = ServicesActivator.getDefault().getPackageAdmin();
		Bundle[] bundles = packageAdmin.getBundles(bundleName, null);
		if (bundles == null)
			return null;
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	public String getResourceString(String value, ResourceBundle resourceBundle) {
		String s = value.trim();
		if (!s.startsWith(KEY_PREFIX, 0))
			return s;
		if (s.startsWith(KEY_DOUBLE_PREFIX, 0))
			return s.substring(1);

		int ix = s.indexOf(' ');
		String key = ix == -1 ? s : s.substring(0, ix);
		String dflt = ix == -1 ? s : s.substring(ix + 1);

		if (resourceBundle == null)
			return dflt;

		try {
			return resourceBundle.getString(key.substring(1));
		} catch (MissingResourceException e) {
			// this will avoid requiring a bundle access on the next lookup
			return dflt;
		}
	}

}
