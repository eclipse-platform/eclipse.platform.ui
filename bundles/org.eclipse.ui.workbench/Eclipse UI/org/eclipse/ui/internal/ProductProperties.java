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

import org.eclipse.core.runtime.IProduct;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.branding.IProductConstants;

/**
 * A class that converts the strings returned by
 * <code>org.eclipse.core.runtime.IProduct.getProperty</code> to the
 * appropriate class. This implementation is tightly bound to the properties
 * provided in IProductConstants. Clients adding their own properties could
 * choose to subclass this.
 * 
 * @see org.eclipse.ui.branding.IProductConstants
 * @since 3.0
 */
public abstract class ProductProperties extends BrandingProperties implements
        IProductConstants {

	/**
	 * The application name, used to initialize the SWT Display.  This
	 * value is distinct from the string displayed in the application
	 * title bar.
	 * <p>
	 * E.g., On motif, this can be used to set the name used for
	 * resource lookup.
	 * </p>
	 * @see org.eclipse.swt.widgets.Display#setAppName
	 */
	public static String getAppName(IProduct product) {
	    String property = product.getProperty(APP_NAME);
	    return property == null ? "" : property; //$NON-NLS-1$
	}

	/**
	 * The text to show in an "about" dialog for this product.
	 * Products designed to run "headless" typically would not
	 * have such text.
	 */
	public static String getAboutText(IProduct product) {
	    String property = product.getProperty(ABOUT_TEXT);
	    return property == null ? "" : property; //$NON-NLS-1$
	}

	/**
	 * An image which can be shown in an "about" dialog for this
	 * product. Products designed to run "headless" typically would not 
	 * have such an image.
	 * <p>
     * A full-sized product image (no larger than 500x330 pixels) is
     * shown without the "aboutText" blurb.  A half-sized product image
     * (no larger than 250x330 pixels) is shown with the "aboutText"
     * blurb beside it.
     */
	public static ImageDescriptor getAboutImage(IProduct product) {
	    return getImage(product.getProperty(ABOUT_IMAGE), product.getDefiningBundle());
	}

	/**
	 * An array of one or more images to be used for this product.  The
	 * expectation is that the array will contain the same image rendered
	 * at different sizes (16x16 and 32x32).  
	 * Products designed to run "headless" typically would not have such images.
	 * <p>
	 * If this property is given, then it supercedes <code>WINDOW_IMAGE</code>.
	 * </p>
	 */
	public static ImageDescriptor[] getWindowImages(IProduct product) {
	    String property = product.getProperty(WINDOW_IMAGES);

	    // for compatibility with pre-3.0 plugins that may still use WINDOW_IMAGE
	    if(property == null)
	        property = product.getProperty(WINDOW_IMAGE);

	    return getImages(property, product.getDefiningBundle());
	}
}
