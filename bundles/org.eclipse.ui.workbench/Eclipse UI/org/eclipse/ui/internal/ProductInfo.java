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
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.branding.IProductConstants;

/**
 * Stores information about the product.  This class replaces the old AboutInfo.
 * The product information is available as strings, but is needed as URLs, etc.
 * This class manages that translation
 * @since 3.0
 */
public class ProductInfo {
	private IProduct product;

	private String appName;
	private ImageDescriptor[] windowImages;
	private ImageDescriptor aboutImage;
	private ImageDescriptor featureImage;
	private String aboutText;

	public ProductInfo(IProduct product) {
		this.product = product;
	}

	/**
	 * Returns the product name or <code>null</code>.
	 * This is shown in the window title and the About action.
	 *
	 * @return the product name, or <code>null</code>
	 */
	public String getProductName() {
		return product != null ? product.getName() : null;
	}

	/**
	 * Returns the application name or <code>null</code>. Note this is never
	 * shown to the user.  It is used to initialize the SWT Display.
	 * <p>
	 * On Motif, for example, this can be used to set the name used
	 * for resource lookup.
	 * </p>
	 *
	 * @return the application name, or <code>null</code>
	 * 
	 * @see org.eclipse.swt.widgets.Display#setAppName
	 */
	public String getAppName() {
		if(appName == null)
			appName = getProperty(IProductConstants.APP_NAME);
		return appName;
	}

	/**
	 * Returns the descriptor for an image which can be shown in an "about" dialog 
	 * for this product. Products designed to run "headless" typically would not 
	 * have such an image.
	 * 
	 * @return the descriptor for an about image, or <code>null</code> if none
	 */
	public ImageDescriptor getAboutImage() {
		if(aboutImage == null) {
			String prop = getProperty(IProductConstants.ABOUT_IMAGE);
			aboutImage = getImage(prop);
		}

		return aboutImage;
	}

	/**
	 * Returns the descriptor for an image which can be shown in an "about features" 
	 * dialog. Products designed to run "headless" typically would not have such an image.
	 * 
	 * @return the descriptor for a feature image, or <code>null</code> if none
	 */
	public ImageDescriptor getFeatureImage() {
		return featureImage;
	}

	/**
	 * Return an array of image descriptors for the window images to use for
	 * this product. The expectations is that the elements will be the same
	 * image rendered at different sizes. Products designed to run "headless"
	 * typically would not have such images.
	 * 
	 * @return an array of the image descriptors for the window images, or
	 *         <code>null</code> if none
	 */
	public ImageDescriptor[] getWindowImages() {
		if(windowImages == null) {
			windowImages = getImages(IProductConstants.WINDOW_IMAGES);

			// just in case the provider is still using the old label
			if(windowImages == null)
				windowImages = getImages(IProductConstants.WINDOW_IMAGE);
		}

		return windowImages;
	}

	/**
	 * Returns the text to show in an "about" dialog for this product.
	 * Products designed to run "headless" typically would not have such text.
	 * 
	 * @return the about text, or <code>null</code> if none
	 */
	public String getAboutText() {
		if(aboutText == null)
			aboutText = getProperty(IProductConstants.ABOUT_TEXT);
		return aboutText;
	}

	/**
	 * Returns the value mapped to the argument key of this product.  Return
	 * <code>null</code> if the key is not contained by this product.
	 * @param key the name of the property to lookup
	 * @return
	 */
	private String getProperty(String key) {
		return product != null ? product.getProperty(key) : null;
	}

	/**
	 * Returns an URL for the given key, or <code>null</code>.
	 * 
	 * @return a URL for the given value, or <code>null</code>
	 */
	private URL getURL(String key) {
		String value = getProperty(key);
		if (value == null)
			return null;

		try {
			return new URL(value);
		}
		catch(IOException e) {
			return null;
		}
	}

	/**
	 * Returns a array of URL for the given key, or <code>null</code>. The
	 * property value should be a comma separated list of urls, tokens for
	 * which the product cannot build an url will have a null entry.
	 * 
	 * @param key
	 *            name of a property that contains a comma-separated list of
	 *            product relative urls
	 * @return a URL for the given key, or <code>null</code>
	 */
	private URL[] getURLs(String key) {
		String value = getProperty(key);
		if(value == null)
			return null;

		StringTokenizer tokens = new StringTokenizer(value, ","); //$NON-NLS-1$
		ArrayList array = new ArrayList(10);
		while (tokens.hasMoreTokens()) {
			String str = tokens.nextToken().trim();
			try {
				URL url = new URL(str);
				array.add(url);
			}
			catch(IOException e) {
				// do nothing
			}
		}

		URL[] urls = new URL[array.size()];
		array.toArray(urls);
		return urls;
	}

	/**
	 * Returns an image descriptor for the given key, or <code>null</code>.
	 * 
	 * @return an image descriptor for the given key, or <code>null</code>
	 */
	private ImageDescriptor getImage(String key) {
		URL url = getURL(key);
		return url == null ? null : ImageDescriptor.createFromURL(url);
	}

	/**
	 * Returns an array of image descriptors for the given key, or <code>null</code>.
	 * The property value should be a comma separated list of image paths.
	 *
	 * @param key name of the property containing the requested images
	 * @return an array of image descriptors for the given key, or <code>null</code>
	 */
	private ImageDescriptor[] getImages(String key) {
		URL[] urls = getURLs(key);
		if (urls == null || urls.length <= 0)
			return null;

		ImageDescriptor[] images = new ImageDescriptor[urls.length];
		for (int i = 0; i < images.length; ++i)
			images[i] = ImageDescriptor.createFromURL(urls[i]);

		return images;
	}
}
