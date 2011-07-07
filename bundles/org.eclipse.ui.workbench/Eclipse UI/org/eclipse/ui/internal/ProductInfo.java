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

import org.eclipse.core.runtime.IProduct;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Stores information about the product.  This class replaces the old AboutInfo.
 * The product information is available as strings, but is needed as URLs, etc.
 * This class manages that translation.
 * @since 3.0
 */
public class ProductInfo {
    private IProduct product;

    private String productName;

    private String appName;

    private ImageDescriptor[] windowImages;

    private ImageDescriptor aboutImage;

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
        if (productName == null && product != null) {
			productName = product.getName();
		}
        return productName;
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
        if (appName == null && product != null) {
			appName = ProductProperties.getAppName(product);
		}
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
        if (aboutImage == null && product != null) {
			aboutImage = ProductProperties.getAboutImage(product);
		}
        return aboutImage;
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
        if (windowImages == null && product != null) {
			windowImages = ProductProperties.getWindowImages(product);
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
        if (aboutText == null && product != null) {
			aboutText = ProductProperties.getAboutText(product);
		}
        return aboutText;
    }
}
