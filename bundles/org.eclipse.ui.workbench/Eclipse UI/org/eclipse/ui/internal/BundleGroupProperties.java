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

import java.net.URL;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.branding.IBundleGroupConstants;

/**
 * A class that converts the strings returned by
 * <code>org.eclipse.core.runtime.IBundleGroup.getProperty</code> to the
 * appropriate class. This implementation is tightly bound to the properties
 * provided in IBundleGroupConstants. Clients adding their own properties could
 * choose to subclass this.
 * 
 * @see org.eclipse.ui.branding.IBundleGroupConstants
 * @since 3.0
 */
public abstract class BundleGroupProperties extends BrandingProperties
        implements IBundleGroupConstants {

    /**
     * An image which can be shown in an "about features" dialog (32x32).
     */
    public static ImageDescriptor getFeatureImage(IBundleGroup bundleGroup) {
        return getImage(bundleGroup.getProperty(FEATURE_IMAGE), null);
    }

    /**
     * The URL to an image which can be shown in an "about features" dialog (32x32).
     */
    public static URL getFeatureImageUrl(IBundleGroup bundleGroup) {
        return getUrl(bundleGroup.getProperty(FEATURE_IMAGE), null);
    }

    /**
     * A help reference for the feature's tips and tricks page (optional).
     */
    public static String getTipsAndTricksHref(IBundleGroup bundleGroup) {
        return bundleGroup.getProperty(TIPS_AND_TRICKS_HREF);
    }

    /**
     * A URL for the feature's welcome page (special XML-based format) ($nl$/
     * prefix to permit locale-specific translations of entire file). Products
     * designed to run "headless" typically would not have such a page.
     */
    public static URL getWelcomePageUrl(IBundleGroup bundleGroup) {
        return getUrl(bundleGroup.getProperty(WELCOME_PAGE), null);
    }

    /**
     * The id of a perspective in which to show the welcome page (optional).
     */
    public static String getWelcomePerspective(IBundleGroup bundleGroup) {
        String property = bundleGroup.getProperty(WELCOME_PERSPECTIVE);
        return property == null ? "" : property; //$NON-NLS-1$
    }

    /**
     * A URL for the feature's license page.
     */
    public static URL getLicenseUrl(IBundleGroup bundleGroup) {
        return getUrl(bundleGroup.getProperty(LICENSE_HREF), null);
    }
}
