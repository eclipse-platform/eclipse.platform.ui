/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;

/**
 * The branding properties are retrieved as strings, but often used as other
 * types (e.g., <code>java.net.URL</code>s. This class provides some utility
 * functions for converting the string values to these well known classes. This
 * may be subclassed by clients that use more than just these few types.
 */
public abstract class BrandingProperties {

    /**
     * Create an url from the argument absolute or relative path. The bundle
     * parameter is used as the base for relative paths and is allowed to be
     * null.
     * 
     * @param value
     *            the absolute or relative path
     * @param definingBundle
     *            bundle to be used for relative paths (may be null)
     * @return
     */
    public static URL getUrl(String value, Bundle definingBundle) {
        try {
            if (value != null) {
				return new URL(value);
			}
        } catch (MalformedURLException e) {
            if (definingBundle != null) {
				return Platform.find(definingBundle, new Path(value));
			}
        }

        return null;
    }

    /**
     * Create a descriptor from the argument absolute or relative path to an
     * image file. bundle parameter is used as the base for relative paths and
     * is allowed to be null.
     * 
     * @param value
     *            the absolute or relative path
     * @param definingBundle
     *            bundle to be used for relative paths (may be null)
     * @return
     */
    public static ImageDescriptor getImage(String value, Bundle definingBundle) {
        URL url = getUrl(value, definingBundle);
        return url == null ? null : ImageDescriptor.createFromURL(url);
    }

    /**
     * Returns a array of URL for the given property or <code>null</code>.
     * The property value should be a comma separated list of urls (either
     * absolute or relative to the argument bundle). Tokens that do not
     * represent a valid url will be represented with a null entry in the
     * returned array.
     * 
     * @param value
     *            value of a property that contains a comma-separated list of
     *            product relative urls
     * @param definingBundle
     *            bundle to be used as base for relative paths (may be null)
     * @return a URL for the given property, or <code>null</code>
     */
    public static URL[] getURLs(String value, Bundle definingBundle) {
        if (value == null) {
			return null;
		}

        StringTokenizer tokens = new StringTokenizer(value, ","); //$NON-NLS-1$
        ArrayList array = new ArrayList(10);
        while (tokens.hasMoreTokens()) {
			array.add(getUrl(tokens.nextToken().trim(), definingBundle));
		}

        return (URL[]) array.toArray(new URL[array.size()]);
    }

    /**
     * Returns an array of image descriptors for the given property, or
     * <code>null</code>. The property value should be a comma separated list
     * of image paths. Each path should either be absolute or relative to the
     * optional bundle parameter.
     * 
     * @param value
     *            value of a property that contains a comma-separated list of
     *            product relative urls describing images
     * @param definingBundle
     *            bundle to be used for relative paths (may be null)
     * @return an array of image descriptors for the given property, or
     *         <code>null</code>
     */
    public static ImageDescriptor[] getImages(String value,
            Bundle definingBundle) {
        URL[] urls = getURLs(value, definingBundle);
        if (urls == null || urls.length <= 0) {
			return null;
		}

        ImageDescriptor[] images = new ImageDescriptor[urls.length];
        for (int i = 0; i < images.length; ++i) {
			images[i] = ImageDescriptor.createFromURL(urls[i]);
		}

        return images;
    }
}
