package org.eclipse.update.internal.core;

import java.io.*;
import java.net.URL;

import org.eclipse.update.core.*;

public class FileSite extends URLSite {

	private String path;
	public static final String INSTALL_FEATURE_PATH = "install/features/";

	/**
	 * Constructor for FileSite
	 */
	public FileSite(URL siteReference) {
		super(siteReference);
	}

	/**
	 * @see AbstractSite#createExecutableFeature(IFeature)
	 */
	public AbstractFeature createExecutableFeature(IFeature sourceFeature) {
		return new DefaultExecutableFeature(sourceFeature, this);
	}

	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(
		IPluginEntry pluginEntry,
		String contentKey,
		InputStream inStream) {
		try {

			// TEST: what if SiteURL is not valid ? 

			File pluginSite = new File(getURL().getPath() + DEFAULT_PLUGIN_PATH);
			if (!pluginSite.exists()) {
				if (!CREATE_PATH) {
					//FIXME: Serviceability
					throw new IOException("The Path:" + pluginSite.toString() + "does not exist.");
				} else {
					//FIXME: Serviceability
					if (!pluginSite.mkdirs())
						throw new IOException("Cannot create:" + pluginSite.toString());
				}
			}

			String pluginPath =
				getURL().getPath() + DEFAULT_PLUGIN_PATH + pluginEntry.getIdentifier().toString();
			
			

			UpdateManagerUtils.copyToLocal(inStream, pluginPath + File.separator + contentKey);


		} catch (IOException e) {
			//FIXME: 
			e.printStackTrace();
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * store Feature files
	 */
	public void storeFeatureInfo(
		VersionedIdentifier featureIdentifier,
		String contentKey,
		InputStream inStream) {
		try {

			// TEST: what if SiteURL is not valid ? 
			File featureSite = new File(getURL().getPath() + INSTALL_FEATURE_PATH);
			if (!featureSite.exists()) {
				if (!CREATE_PATH) {
					//FIXME: Serviceability
					throw new IOException("The Path:" + featureSite.toString() + "does not exist.");
				} else {
					//FIXME: Serviceability
					if (!featureSite.mkdirs())
						throw new IOException("Cannot create:" + featureSite.toString());
				}
			}

			String featurePath = getURL().getPath() + INSTALL_FEATURE_PATH + featureIdentifier.toString();
			UpdateManagerUtils.copyToLocal(inStream, featurePath + File.separator + contentKey);

		} catch (IOException e) {
			//FIXME: 
			e.printStackTrace();
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {
			}
		}

	}

}