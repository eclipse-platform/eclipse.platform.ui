/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.CRC32;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;

/**
 * The information within this object is obtained from the about INI file.
 * This file resides within an install configurations directory and must be a 
 * standard java property file.  
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * </p>
 */
public final class AboutInfo {
	private final static int BYTE_ARRAY_SIZE = 2048;

	private String featureId;
	private String versionId = ""; //$NON-NLS-1$
	private String featurePluginLabel;
	private String providerName;
	private String appName;
	private ImageDescriptor[] windowImages;
	private ImageDescriptor aboutImage;
	private ImageDescriptor featureImage;
	private String aboutText;
	private URL welcomePageURL;
	private String welcomePerspective;
	private String tipsAndTricksHref;
	private URL featureImageURL;
	private Long featureImageCRC;
	private boolean calculatedImageCRC = false;

	/*
	 * Create a new about info for a feature with the given id.
	 */
	/* package */ AboutInfo(String featureId) {
		super();
		this.featureId = featureId;
	}

	/**
	 * Returns the configuration information for the feature with the 
	 * given id.
	 * 
	 * @param featureId the feature id
	 * @param versionId the version id (of the feature)
	 * @param pluginId the plug-in id
	 * @return the configuration information for the feature
	 */
	public static AboutInfo readFeatureInfo(String featureId, String versionId, String pluginId) {
		Assert.isNotNull(featureId);
		Assert.isNotNull(versionId);
		Assert.isNotNull(pluginId);

		IProduct product = Platform.getProduct();
		IBundleGroup bundleGroup = getBundleGroup(featureId);
		if (product == null || bundleGroup == null)
		    return null;

		AboutInfo info = new AboutInfo(featureId);
		info.versionId = versionId;

		info.appName = ProductProperties.getAppName(product);
		info.aboutText = ProductProperties.getAboutText(product);
		info.aboutImage = ProductProperties.getAboutImage(product);
		info.windowImages = ProductProperties.getWindowImages(product);

		info.featurePluginLabel = bundleGroup.getName();
		info.providerName = bundleGroup.getProviderName();
		info.featureImage = BundleGroupProperties.getFeatureImage(bundleGroup);
		info.featureImageURL = BundleGroupProperties.getFeatureImageUrl(bundleGroup);
		info.welcomePageURL = BundleGroupProperties.getWelcomePageUrl(bundleGroup);
		info.welcomePerspective = BundleGroupProperties.getWelcomePerspective(bundleGroup);
		info.tipsAndTricksHref = BundleGroupProperties.getTipsAndTricksHref(bundleGroup);

		return info;
	}
	
	private static IBundleGroup getBundleGroup(String id) {
	    if(id == null)
	        return null;

	    IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
	    for(int p = 0; p < providers.length; ++p) {
	        IBundleGroup[] groups = providers[p].getBundleGroups();
	        for(int g = 0; g < groups.length; ++g)
	            if(id.equals(groups[g].getIdentifier()))
	                return groups[g];
	    }
	    
	    return null;
	}
	
	/**
	 * Returns the descriptor for an image which can be shown in an "about" dialog 
	 * for this product. Products designed to run "headless" typically would not 
	 * have such an image.
	 * 
	 * @return the descriptor for an about image, or <code>null</code> if none
	 */
	public ImageDescriptor getAboutImage() {
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
	 * Returns the simple name of the feature image file.
	 * 
	 * @return the simple name of the feature image file,
	 * or <code>null</code> if none
	 */
	public String getFeatureImageName() {
		if (featureImageURL != null) {
			IPath path = new Path(featureImageURL.getPath());
			return path.lastSegment();
		} else {
			return null;
		}
	}

	/**
	 * Returns the CRC of the feature image as supplied in the properties file.
	 * 
	 * @return the CRC of the feature image, or <code>null</code> if none
	 */
	public Long getFeatureImageCRC() {
		if (!calculatedImageCRC && featureImageURL != null) {
			featureImageCRC = calculateFeatureImageCRC();
			calculatedImageCRC = true;
		}
		return featureImageCRC;
	}

	/**
	 * Calculate a CRC for the feature image
	 */
	private Long calculateFeatureImageCRC() {
		if (featureImageURL == null)
			return null;
			
		// Get the image bytes
		InputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = featureImageURL.openStream();	
			out = new ByteArrayOutputStream();
			byte[] buffer = new byte[BYTE_ARRAY_SIZE];
			int readResult = BYTE_ARRAY_SIZE;
			while (readResult == BYTE_ARRAY_SIZE) {
				readResult = in.read(buffer);
				if (readResult > 0) 
					out.write(buffer, 0, readResult);
			}
			byte[] contents = out.toByteArray();
			// Calculate the crc
			CRC32 crc = new CRC32();
			crc.update(contents);
			return new Long(crc.getValue());
		} catch (IOException e) {
			return null;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					// ignore errors on close
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// ignore errors on close
				}
		}
	}	
		
	/**
	 * Returns a label for the feature plugn, or <code>null</code>.
	 */
	public String getFeatureLabel() {
		return featurePluginLabel;
	}

	/**
	 * Returns the id for this feature.
	 * 
	 * @return the feature id
	 */
	public String getFeatureId() {
		return featureId;
	}
	
	/**
	 * Returns the text to show in an "about" dialog for this product.
	 * Products designed to run "headless" typically would not have such text.
	 * 
	 * @return the about text, or <code>null</code> if none
	 */
	public String getAboutText() {
		return aboutText;
	}

	/**
	 * Returns the application name or <code>null</code>.
	 * Note this is never shown to the user.
	 * It is used to initialize the SWT Display.
	 * <p>
	 * On Motif, for example, this can be used
	 * to set the name used for resource lookup.
	 * </p>
	 *
	 * @return the application name, or <code>null</code>
	 * 
	 * @see org.eclipse.swt.widgets.Display#setAppName
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * Returns the product name or <code>null</code>.
	 * This is shown in the window title and the About action.
	 *
	 * @return the product name, or <code>null</code>
	 */
	public String getProductName() {
		return featurePluginLabel;
	}

	/**
	 * Returns the provider name or <code>null</code>.
	 *
	 * @return the provider name, or <code>null</code>
	 */
	public String getProviderName() {
		return providerName;
	}

	/**
	 * Returns the feature version id.
	 *
	 * @return the version id of the feature
	 */
	public String getVersionId() {
		return versionId;
	}

	/**
	 * Returns a <code>URL</code> for the welcome page.
	 * Products designed to run "headless" typically would not have such an page.
	 * 
	 * @return the welcome page, or <code>null</code> if none
	 */
	public URL getWelcomePageURL() {
		return welcomePageURL;
	}

	/**
	 * Returns the ID of a perspective in which to show the welcome page.
	 * May be <code>null</code>.
	 * 
	 * @return the welcome page perspective id, or <code>null</code> if none
	 */
	public String getWelcomePerspectiveId() {
		return welcomePerspective;
	}

	/**
	 * Returns a <code>String</code> for the tips and trick href.
	 * 
	 * @return the tips and tricks href, or <code>null</code> if none
	 */
	public String getTipsAndTricksHref() {
		return tipsAndTricksHref;
	}

	/**
	 * Return an array of image descriptors for the window images to use for
	 * this product. The expectations is that the elements will be the same
	 * image rendered at different sizes. Products designed to run "headless"
	 * typically would not have such images.
	 * 
	 * @return an array of the image descriptors for the window images, or
	 *         <code>null</code> if none
	 * @since 3.0
	 */
	public ImageDescriptor[] getWindowImages() {
		return windowImages;
	}
}
