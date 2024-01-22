/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.branding;

/**
 * These constants define the set of properties that the UI expects to be
 * available via <code>IBundleGroup.getProperty(String)</code>.
 *
 * @since 3.0
 * @see org.eclipse.core.runtime.IBundleGroup#getProperty(String)
 */
public interface IBundleGroupConstants {

	/**
	 * The text to show in an "about features" dialog.
	 */
	String ABOUT_TEXT = "aboutText"; //$NON-NLS-1$

	/**
	 * An image which can be shown in an "about features" dialog (32x32).
	 * <p>
	 * The value is a fully qualified valid URL.
	 * </p>
	 */
	String FEATURE_IMAGE = "featureImage"; //$NON-NLS-1$

	/**
	 * A help reference for the feature's tips and tricks page (optional).
	 */
	String TIPS_AND_TRICKS_HREF = "tipsAndTricksHref"; //$NON-NLS-1$

	/**
	 * The feature's welcome page (special XML-based format).
	 * <p>
	 * The value is a fully qualified valid URL.
	 * </p>
	 * Products designed to run "headless" typically would not have such a page.
	 */
	String WELCOME_PAGE = "welcomePage"; //$NON-NLS-1$

	/**
	 * The id of a perspective in which to show the welcome page (optional).
	 */
	String WELCOME_PERSPECTIVE = "welcomePerspective"; //$NON-NLS-1$

	/**
	 * The URL of the license page for the feature (optional).
	 * <p>
	 * The value is a fully qualified valid URL.
	 * </p>
	 */
	String LICENSE_HREF = "licenseHref"; //$NON-NLS-1$

	/**
	 * The feature's branding bundle id (optional).
	 *
	 * @since 3.5
	 */
	String BRANDING_BUNDLE_ID = "brandingBundleId"; //$NON-NLS-1$

	/**
	 * The feature's branding bundle version (optional).
	 *
	 * @since 3.5
	 */
	String BRANDING_BUNDLE_VERSION = "brandingBundleVersion"; //$NON-NLS-1$
}
