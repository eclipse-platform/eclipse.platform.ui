/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The branding properties are retrieved as strings, but often used as other
 * types (e.g., <code>java.net.URL</code>s. This class provides some utility
 * functions for converting the string values to these well known classes. This
 * may be subclassed by clients that use more than just these few types.
 */
public abstract class BrandingProperties {

    public static URL getUrl(String property) {
        try {
            if (property != null)
                return new URL(property);
        } catch (MalformedURLException e) {
            // do nothing
        }

        return null;
    }

    public static ImageDescriptor getImage(String property) {
        URL url = getUrl(property);
        return url == null ? null : ImageDescriptor.createFromURL(url);
    }

    /**
     * Returns a array of URL for the given property or <code>null</code>.
     * The property value should be a comma separated list of urls, tokens for
     * which the product cannot build an url will have a null entry.
     * 
     * @param property
     *            value of a property that contains a comma-separated list of
     *            product relative urls
     * @return a URL for the given property, or <code>null</code>
     */
    public static URL[] getURLs(String property) {
        if(property == null)
            return null;

        StringTokenizer tokens = new StringTokenizer(property, ","); //$NON-NLS-1$
        ArrayList array = new ArrayList(10);
        while (tokens.hasMoreTokens())
            try {
                array.add(new URL(tokens.nextToken().trim()));
            } catch (IOException e) {
                // do nothing
            }

        return (URL[]) array.toArray(new URL[array.size()]);
    }

    /**
     * Returns an array of image descriptors for the given property, or
     * <code>null</code>. The property value should be a comma separated list
     * of image paths.
     * 
     * @param property
     *            value of a property that contains a comma-separated list of
     *            product relative urls describing images
     * @return an array of image descriptors for the given property, or
     *         <code>null</code>
     */
    public static ImageDescriptor[] getImages(String property) {
        URL[] urls = getURLs(property);
        if (urls == null || urls.length <= 0)
            return null;

        ImageDescriptor[] images = new ImageDescriptor[urls.length];
        for (int i = 0; i < images.length; ++i)
            images[i] = ImageDescriptor.createFromURL(urls[i]);

        return images;
    }
}
