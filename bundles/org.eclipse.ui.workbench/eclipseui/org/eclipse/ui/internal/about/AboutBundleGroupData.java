/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.ui.internal.about;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.branding.IBundleGroupConstants;

/**
 * A small class to manage the information related to IBundleGroup's.
 *
 * @since 3.0
 */
public class AboutBundleGroupData extends AboutData {
	private IBundleGroup bundleGroup;

	private URL licenseUrl;

	private URL featureImageUrl;

	private Long featureImageCrc;

	private ImageDescriptor featureImage;

	public AboutBundleGroupData(IBundleGroup bundleGroup) {
		super(bundleGroup.getProviderName(), bundleGroup.getName(), bundleGroup.getVersion(),
				bundleGroup.getIdentifier());
		this.bundleGroup = bundleGroup;
	}

	public IBundleGroup getBundleGroup() {
		return bundleGroup;
	}

	public URL getLicenseUrl() {
		if (licenseUrl == null) {
			licenseUrl = getURL(bundleGroup.getProperty(IBundleGroupConstants.LICENSE_HREF));
		}

		return licenseUrl;
	}

	public URL getFeatureImageUrl() {
		if (featureImageUrl == null) {
			featureImageUrl = getURL(bundleGroup.getProperty(IBundleGroupConstants.FEATURE_IMAGE));
		}
		return featureImageUrl;
	}

	public ImageDescriptor getFeatureImage() {
		if (featureImage == null) {
			featureImage = getImage(getFeatureImageUrl());
		}
		return featureImage;
	}

	public Long getFeatureImageCrc() {
		if (featureImageCrc != null) {
			return featureImageCrc;
		}

		URL url = getFeatureImageUrl();
		if (url == null) {
			return null;
		}

		// Get the image bytes
		CRC32 checksum = new CRC32();
		try (InputStream in = new CheckedInputStream(url.openStream(), checksum)) {
			// the contents don't matter, the read just needs a place to go
			in.readAllBytes();

			featureImageCrc = Long.valueOf(checksum.getValue());
			return featureImageCrc;

		} catch (IOException e) {
			return null;
		}
	}

	public String getAboutText() {
		return bundleGroup.getProperty(IBundleGroupConstants.ABOUT_TEXT);
	}
}
