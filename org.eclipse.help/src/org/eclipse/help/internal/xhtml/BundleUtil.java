/***************************************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * Bundle convenience methods.
 */
public class BundleUtil {


	private static String NL_TAG = "$nl$/"; //$NON-NLS-1$

	/*
	 * Util method to return an URL to a plugin relative resource.
	 */
	public static URL getResourceAsURL(String resource, String pluginId) {
		Bundle bundle = Platform.getBundle(pluginId);
		URL localLocation = localLocation = Platform.find(bundle, new Path(resource));
		return localLocation;
	}


	public static Bundle getBundleFromConfigurationElement(IConfigurationElement cfg) {
		return Platform.getBundle(cfg.getNamespace());
	}


	/**
	 * Get the resource location, but do not force an $nl$ on it.
	 * 
	 * @param resource
	 * @param element
	 * @return
	 */
	public static String getResourceLocation(String resource, IConfigurationElement element) {
		Bundle bundle = getBundleFromConfigurationElement(element);
		return getResolvedResourceLocation(resource, bundle, false);
	}



	public static String getResolvedResourceLocation(String resource, Bundle bundle, boolean forceNLResolve) {
		// quick exits.
		if (resource == null)
			return null;

		if (bundle == null || !bundleHasValidState(bundle))
			return resource;

		URL localLocation = null;
		try {
			// we need to resolve this URL.
			String copyResource = resource;
			if (forceNLResolve && !copyResource.startsWith(NL_TAG)) {
				if (copyResource.startsWith("/") //$NON-NLS-1$
						|| copyResource.startsWith("\\")) //$NON-NLS-1$
					copyResource = resource.substring(1);
				copyResource = NL_TAG + copyResource;
			}
			IPath resourcePath = new Path(copyResource);
			localLocation = Platform.find(bundle, resourcePath);
			if (localLocation == null) {
				// localLocation can be null if the passed resource could not
				// be found relative to the plugin. log fact, return resource,
				// as is.
				String msg = "Could not find resource: " + //$NON-NLS-1$
						resource + " in " + getBundleHeader( //$NON-NLS-1$
								bundle, Constants.BUNDLE_NAME);
				HelpPlugin.logWarning(msg);
				return resource;
			}
			localLocation = Platform.asLocalURL(localLocation);
			return localLocation.toExternalForm();
		} catch (Exception e) {
			String msg = "Failed to load resource: " + //$NON-NLS-1$
					resource + " from " + getBundleHeader(bundle, //$NON-NLS-1$
							Constants.BUNDLE_NAME);
			HelpPlugin.logError(msg, e);
			return resource;
		}
	}

	public static boolean bundleHasValidState(Bundle bundle) {
		if (bundle == null || bundle.getState() == Bundle.UNINSTALLED
				|| bundle.getState() == Bundle.INSTALLED) {

			if (bundle == null)
				HelpPlugin.logError("Help  tried accessing a NULL bundle.", null); //$NON-NLS-1$
			else {
				String msg = "Help tried accessing Bundle: " + getBundleHeader( //$NON-NLS-1$
						bundle, Constants.BUNDLE_NAME)
						+ " vendor: " + //$NON-NLS-1$
						getBundleHeader(bundle, Constants.BUNDLE_VENDOR)
						+ " bundle state: " + String.valueOf(bundle.getState()); //$NON-NLS-1$
				HelpPlugin.logError(msg, null);
			}
			return false;
		}

		return true;
	}

	public static String getBundleHeader(Bundle bundle, String key) {
		return (String) bundle.getHeaders().get(key);
	}
}
