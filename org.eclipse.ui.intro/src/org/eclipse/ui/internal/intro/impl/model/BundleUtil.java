/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.eclipse.ui.internal.intro.impl.util.StringUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * Bundle convenience methods.
 */
public class BundleUtil {

    public static Bundle getBundleFromConfigurationElement(
            IConfigurationElement cfg) {
        return Platform.getBundle(cfg.getNamespace());
    }

    /**
     * Utility method to validate the state of a bundle. Log invalid bundles to
     * log file.
     */
    public static boolean bundleHasValidState(Bundle bundle) {
        if (bundle == null || bundle.getState() == Bundle.UNINSTALLED
                || bundle.getState() == Bundle.INSTALLED) {

            if (bundle == null)
                Log.error("Intro tried accessing a NULL bundle.", null); //$NON-NLS-1$
            else {
                String msg = StringUtil
                    .concat("Intro tried accessing Bundle: ", getBundleHeader( //$NON-NLS-1$
                        bundle, Constants.BUNDLE_NAME), " vendor: ", //$NON-NLS-1$
                        getBundleHeader(bundle, Constants.BUNDLE_VENDOR),
                        " bundle state: ", String.valueOf(bundle.getState())).toString(); //$NON-NLS-1$
                Log.error(msg, null);
            }
            return false;
        }

        return true;
    }

    /**
     * Retrieves the given key from the bundle header.
     * 
     * @param bundle
     * @param key
     * @return
     */
    public static String getBundleHeader(Bundle bundle, String key) {
        return (String) bundle.getHeaders().get(key);
    }

    /**
     * Returns the fully qualified location of the passed resource string from
     * the declaring plugin. If the plugin is not defined, or file could not be
     * loaded from the plugin, the resource is returned as is.
     * 
     * @param resource
     * @return
     */
    public static String getPluginLocation(String resource,
            IConfigurationElement element) {
        Bundle bundle = getBundleFromConfigurationElement(element);
        return getResolvedBundleLocation(resource, bundle);
    }

    public static String getResolvedBundleLocation(String resource,
            Bundle bundle) {
        // quick exits.
        if (resource == null)
            return null;

        if (bundle == null || !bundleHasValidState(bundle))
            return resource;

        URL localLocation = null;
        try {
            // we need to perform a 'resolve' on this URL.
            localLocation = Platform.find(bundle, new Path(resource));
            if (localLocation == null) {
                // localLocation can be null if the passed resource could not
                // be found relative to the plugin. log fact, return resource,
                // as is.
                String msg = StringUtil.concat("Could not find resource: ", //$NON-NLS-1$
                    resource, " in ", getBundleHeader( //$NON-NLS-1$
                        bundle, Constants.BUNDLE_NAME)).toString();
                Log.warning(msg);
                return resource;
            }
            localLocation = Platform.asLocalURL(localLocation);
            return localLocation.toExternalForm();
        } catch (Exception e) {
            String msg = StringUtil.concat("Failed to load resource: ", //$NON-NLS-1$
                resource, " from ", getBundleHeader(bundle, //$NON-NLS-1$
                    Constants.BUNDLE_NAME)).toString();
            Log.error(msg, e);
            return resource;
        }
    }

    /**
     * Returns the fully qualified location of the passed resource string from
     * the passed plugin id. If the file could not be loaded from the plugin,
     * the resource is returned as is.
     * 
     * @param resource
     * @return
     */
    public static String getResolvedBundleLocation(String resource,
            String pluginId) {
        Bundle bundle = Platform.getBundle(pluginId);
        return getResolvedBundleLocation(resource, bundle);
    }

    /**
     * Get the absolute path of the given bundle, in the form
     * file:/path_to_plugin
     * 
     * @param bundle
     * @return
     */
    public static String getResolvedBundleLocation(Bundle bundle) {
        try {
            URL bundleLocation = bundle.getEntry(""); //$NON-NLS-1$
            if (bundleLocation == null)
                return null;
            bundleLocation = Platform.asLocalURL(bundleLocation);
            return bundleLocation.toExternalForm();
        } catch (IllegalStateException e) {
            Log.error("Failed to access bundle: " //$NON-NLS-1$
                    + bundle.getSymbolicName(), e);
            return null;
        } catch (IOException e) {
            Log.error("Failed to resolve URL path for bundle: " //$NON-NLS-1$
                    + bundle.getSymbolicName(), e);
            return null;
        }
    }

    /**
     * Get the absolute path of the bundle with id <code>bundleId</code>. If
     * no such bundle is found, return null.
     * 
     * @param bundleId
     * @return
     */
    public static String getResolvedBundleLocation(String bundleId) {
        Bundle bundle = Platform.getBundle(bundleId);
        if (bundle == null)
            return null;
        return getResolvedBundleLocation(bundle);
    }

}
