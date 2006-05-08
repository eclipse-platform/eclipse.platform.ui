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
public class BundleGroupProperties extends BrandingProperties implements
        IBundleGroupConstants {

    private final IBundleGroup bundleGroup;

    private ImageDescriptor featureImageDescriptor;

    private URL featureImageUrl;

    private String tipsAndTricksHref;

    private URL welcomePageUrl;

    private String welcomePerspective;

    private URL licenseUrl;

    private String featureLabel;

    private String featureId;

    private String providerName;

    private String versionId;

    /**
     * This instance will return properties from the given bundle group.  The properties are
     * retrieved in a lazy fashion and cached for later retrieval.
     * @param bundleGroup must not be null
     */
    public BundleGroupProperties(IBundleGroup bundleGroup) {
        if (bundleGroup == null) {
			throw new IllegalArgumentException();
		}
        this.bundleGroup = bundleGroup;
    }

    /**
     * An image which can be shown in an "about features" dialog (32x32).
     */
    public ImageDescriptor getFeatureImage() {
        if (featureImageDescriptor == null) {
			featureImageDescriptor = getFeatureImage(bundleGroup);
		}
        return featureImageDescriptor;
    }

    /**
     * The URL to an image which can be shown in an "about features" dialog (32x32).
     */
    public URL getFeatureImageUrl() {
        if (featureImageUrl == null) {
			featureImageUrl = getFeatureImageUrl(bundleGroup);
		}
        return featureImageUrl;
    }

    /**
     * A help reference for the feature's tips and tricks page (optional).
     */
    public String getTipsAndTricksHref() {
        if (tipsAndTricksHref == null) {
			tipsAndTricksHref = getTipsAndTricksHref(bundleGroup);
		}
        return tipsAndTricksHref;
    }

    /**
     * A URL for the feature's welcome page (special XML-based format) ($nl$/
     * prefix to permit locale-specific translations of entire file). Products
     * designed to run "headless" typically would not have such a page.
     */
    public URL getWelcomePageUrl() {
        if (welcomePageUrl == null) {
			welcomePageUrl = getWelcomePageUrl(bundleGroup);
		}
        return welcomePageUrl;
    }

    /**
     * The id of a perspective in which to show the welcome page (optional).
     */
    public String getWelcomePerspective() {
        if (welcomePerspective == null) {
			welcomePerspective = getWelcomePerspective(bundleGroup);
		}
        return welcomePerspective;
    }

    /**
     * A URL for the feature's license page.
     */
    public URL getLicenseUrl() {
        if (licenseUrl == null) {
			licenseUrl = getLicenseUrl(bundleGroup);
		}
        return licenseUrl;
    }

    /**
     * Returns a label for the feature plugn, or <code>null</code>.
     */
    public String getFeatureLabel() {
        if (featureLabel == null) {
			featureLabel = getFeatureLabel(bundleGroup);
		}
        return featureLabel;
    }

    /**
     * Returns the id for this bundleGroup.
     */
    public String getFeatureId() {
        if (featureId == null) {
			featureId = getFeatureId(bundleGroup);
		}
        return featureId;
    }

    /**
     * Returns the provider name.
     */
    public String getProviderName() {
        if (providerName == null) {
			providerName = getProviderName(bundleGroup);
		}
        return providerName;
    }

    /**
     * Returns the feature version id.
     */
    public String getFeatureVersion() {
        if (versionId == null) {
			versionId = getFeatureVersion(bundleGroup);
		}
        return versionId;
    }

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
        return property == null ? null : property;
    }

    /**
     * A URL for the feature's license page.
     */
    public static URL getLicenseUrl(IBundleGroup bundleGroup) {
        return getUrl(bundleGroup.getProperty(LICENSE_HREF), null);
    }

    /**
     * Returns a label for the feature plugn, or <code>null</code>.
     */
    public static String getFeatureLabel(IBundleGroup bundleGroup) {
        return bundleGroup.getName();
    }

    /**
     * Returns the id for this bundleGroup.
     */
    public static String getFeatureId(IBundleGroup bundleGroup) {
        return bundleGroup.getIdentifier();
    }

    /**
     * Returns the provider name.
     */
    public static String getProviderName(IBundleGroup bundleGroup) {
        return bundleGroup.getProviderName();
    }

    /**
     * Returns the feature version id.
     */
    public static String getFeatureVersion(IBundleGroup bundleGroup) {
        return bundleGroup.getVersion();
    }
}
