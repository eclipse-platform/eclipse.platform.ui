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
package org.eclipse.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.zip.CRC32;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.IniFileReader;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * The information within this object is obtained from the about INI file.
 * This file resides within an install configurations directory and must be a 
 * standard java property file.  
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public final class AboutInfo {
	public final static String INI_FILENAME = "about.ini"; //$NON-NLS-1$
	public final static String PROPERTIES_FILENAME = "about.properties"; //$NON-NLS-1$
	public final static String MAPPINGS_FILENAME = "about.mappings"; //$NON-NLS-1$
	
	private final static int BYTE_ARRAY_SIZE = 2048;

	private String featureId;
	private PluginVersionIdentifier versionId;
	private IPluginDescriptor pluginDescriptor;
	private String featurePluginLabel;
	private String providerName;
	private String appName;
	private ImageDescriptor windowImage;
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
	 * Constructs a new instance of the about info.
	 */
	private AboutInfo(String featureId, PluginVersionIdentifier versionId) {
		super();
		this.featureId = featureId;
		this.versionId = versionId;
	}

	/**
	 * Creates and loades the about information for the specified feature.
	 * 
	 * @param featureId the feature id to read the about information from, or <code>null</code>
	 * @param versionId the version id of the feature, or <code>null</code>
	 * @return the initialized about information for the specified feature
	 */
	public final static AboutInfo create(String featureId, PluginVersionIdentifier versionId) throws WorkbenchException {
		AboutInfo info = new AboutInfo(featureId, versionId);
		if (featureId != null) {
			IniFileReader reader = new IniFileReader(featureId, INI_FILENAME, PROPERTIES_FILENAME, MAPPINGS_FILENAME);
			IStatus status = reader.load();
			if (!status.isOK()) {
				throw new WorkbenchException(status);
			}
			Hashtable runtimeMappings  = new Hashtable();
			String featureVersion = info.getVersion();
			if (featureVersion == null)
				featureVersion = WorkbenchMessages.getString("AboutInfo.NoVersion"); //$NON-NLS-1$
			runtimeMappings.put("{featureVersion}", featureVersion); //$NON-NLS-1$

			info.pluginDescriptor = reader.getPluginDescriptor();
			info.featurePluginLabel = reader.getFeaturePluginLabel();
			info.providerName = reader.getProviderName();
			info.appName = reader.getString("appName", true, runtimeMappings); //$NON-NLS-1$
			info.aboutText = reader.getString("aboutText", true, runtimeMappings); //$NON-NLS-1$
			info.windowImage = reader.getImage("windowImage"); //$NON-NLS-1$
			info.aboutImage = reader.getImage("aboutImage"); //$NON-NLS-1$
			info.featureImage = reader.getImage("featureImage"); //$NON-NLS-1$
			info.featureImageURL = reader.getURL("featureImage"); //$NON-NLS-1$
			info.welcomePageURL = reader.getURL("welcomePage"); //$NON-NLS-1$
			info.welcomePerspective = reader.getString("welcomePerspective", false, runtimeMappings); //$NON-NLS-1$
			info.tipsAndTricksHref = reader.getString("tipsAndTricksHref", false, runtimeMappings); //$NON-NLS-1$
		}
		return info;
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
	 * Returns the name of the feature image as supplied in the properties file.
	 * 
	 * @return the name of the feature image, or <code>null</code> if none
	 */
	public String getFeatureImageName() {
		if (featureImageURL != null)
			return featureImageURL.getFile();
		else
			return null;
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
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
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
	 * Returns the ID for this feature, or <code>null</code>
	 * 
	 * @return the feature ID, or <code>null</code>
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
	 * Returns the descriptor for the corresponding plug-in of this feature.
	 * 
	 * @return the plug-in descriptor or <code>null</code> if none found
	 */
	public IPluginDescriptor getPluginDescriptor() {
		return pluginDescriptor;
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
	 * Returns the version as text or <code>null</code>.
	 *
	 * @return the version as text, or <code>null</code>
	 */
	public String getVersion() {
		if (versionId == null)
			return null;
		return versionId.toString();
	}

	/**
	 * Returns the plug-in version identifier or <code>null</code>.
	 *
	 * @return the plug-in version identifier, or <code>null</code>
	 */
	public PluginVersionIdentifier getVersionId() {
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
	 * Returns the image descriptor for the window image to use for this product.
	 * Products designed to run "headless" typically would not have such an image.
	 * 
	 * @return the image descriptor for the window image, or <code>null</code> if none
	 */
	public ImageDescriptor getWindowImage() {
		return windowImage;
	}
}
