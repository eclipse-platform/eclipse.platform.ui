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
package org.eclipse.ui.branding;

/**
 * These constants define the set of properties that the UI expects to
 * be available via <code>IProduct.getProperty(String)</code>.
 * 
 * @since 3.0
 * @see org.eclipse.core.runtime.IProduct#getProperty(String)
 */
public interface IProductConstants {
	/**
	 * The SWT application name, used to initialize the SWT Display.
	 * <p>  
	 * This value is used to refer to the application in .XDefaults
	 * files on X server based window systems such as Motif.
	 * </p>
	 * <p>
	 * To obtain a human-readable name for the product, use
	 * <code>IProduct.getName()</code>.
	 * </p>
	 * @see org.eclipse.swt.widgets.Display#setAppName
	 */
	public static final String APP_NAME = "appName"; //$NON-NLS-1$

	/**
	 * The text to show in an "about" dialog for this product.
	 * Products designed to run "headless" typically would not
	 * have such text.
	 */
	public static final String ABOUT_TEXT = "aboutText"; //$NON-NLS-1$

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
	public static final String ABOUT_IMAGE = "aboutImage"; //$NON-NLS-1$

	/**
	 * An image to be used as the window icon for this product (16x16).  
	 * Products designed to run "headless" typically would not have such an image.
	 * <p>
	 * If the <code>WINDOW_IMAGES</code> property is given, then it supercedes
	 * this one.
	 * </p>
	 */
	public static final String WINDOW_IMAGE = "windowImage"; //$NON-NLS-1$

	/**
	 * An array of one or more images to be used for this product.  The
	 * expectation is that the array will contain the same image rendered
	 * at different sizes (16x16 and 32x32).  
	 * Products designed to run "headless" typically would not have such images.
	 * <p>
	 * If this property is given, then it supercedes <code>WINDOW_IMAGE</code>.
	 * </p>
	 */
	public static final String WINDOW_IMAGES = "windowImages"; //$NON-NLS-1$

	/**
     * Location of the product's welcome page (special XML-based format), either
     * a fully qualified valid URL or a path relative to the product's defining
     * bundle. Products designed to run "headless" typically would not have such
     * a page. Use of this property is discouraged in 3.0, the new
     * org.eclipse.ui.intro extension point should be used instead.
     */
	public static final String WELCOME_PAGE = "welcomePage"; //$NON-NLS-1$
}
