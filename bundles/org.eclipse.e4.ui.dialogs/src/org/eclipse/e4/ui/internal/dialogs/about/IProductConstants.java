/*******************************************************************************
 * Copyright (c) 2004, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.dialogs.about;

/**
 * These constants define the set of properties that the UI expects to be
 * available via <code>IProduct.getProperty(String)</code>.
 *
 * @since 3.0
 * @see org.eclipse.core.runtime.IProduct#getProperty(String)
 */
public interface IProductConstants {
	/**
	 * The SWT application name, used to initialize the SWT Display.
	 * <p>
	 * This value is used to refer to the application in .XDefaults files on X
	 * server based window systems such as Motif.
	 * </p>
	 * <p>
	 * To obtain a human-readable name for the product, use
	 * <code>IProduct.getName()</code>.
	 * </p>
	 *
	 * @see org.eclipse.swt.widgets.Display#setAppName
	 */
	public static final String APP_NAME = "appName"; //$NON-NLS-1$

	/**
	 * The text to show in an "about" dialog for this product. Products designed
	 * to run "headless" typically would not have such text.
	 */
	public static final String ABOUT_TEXT = "aboutText"; //$NON-NLS-1$

	/**
	 * An image which can be shown in an "about" dialog for this product.
	 * Products designed to run "headless" typically would not have such an
	 * image.
	 * <p>
	 * The value is either a fully qualified valid URL or a path relative to the
	 * product's defining bundle.
	 * </p>
	 * <p>
	 * A full-sized product image (no larger than 500x330 pixels) is shown
	 * without the "aboutText" blurb. A half-sized product image (no larger than
	 * 250x330 pixels) is shown with the "aboutText" blurb beside it.
	 * </p>
	 */
	public static final String ABOUT_IMAGE = "aboutImage"; //$NON-NLS-1$

	/**
	 * An array of one or more images to be used for this product. The
	 * expectation is that the array will contain the same image rendered at
	 * different sizes (16x16 and 32x32). Products designed to run "headless"
	 * typically would not have such images.
	 * <p>
	 * The value is a comma-separated list of paths, where each path is either a
	 * fully qualified valid URL or a path relative to the product's defining
	 * bundle.
	 * </p>
	 * <p>
	 * If this property is given, then it supercedes <code>WINDOW_IMAGE</code>.
	 * </p>
	 * <p>
	 * It is recommended that products use <code>WINDOW_IMAGES</code> rather
	 * than <code>WINDOW_IMAGE</code>, and specify both a 16x16 image and a
	 * 32x32 image, to ensure that different sizes of the image are available
	 * for different uses in the OS. For example, on Windows, the 16x16 image is
	 * used in the corner of the window and in the task tray, but the 32x32
	 * image is used in the Alt+Tab application switcher.
	 * </p>
	 */
	public static final String WINDOW_IMAGES = "windowImages"; //$NON-NLS-1$

	/**
	 * The product's welcome page (special XML-based format).
	 * <p>
	 * The value is either a fully qualified valid URL or a path relative to the
	 * product's defining bundle.
	 * </p>
	 * <p>
	 * Products designed to run "headless" typically would not have such a page.
	 * Use of this property is discouraged in 3.0, the new
	 * <code>org.eclipse.ui.e4.intro</code> extension point should be used
	 * instead.
	 * </p>
	 */
	public static final String WELCOME_PAGE = "welcomePage"; //$NON-NLS-1$

}
