package org.eclipse.update.internal.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.AbstractSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISiteChangedListener;
import org.eclipse.update.core.VersionedIdentifier;

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
	 * @deprecated
	 */
	private String getPath() {
		org.eclipse.update.core.Assert.isTrue(false,"SHOULD NOT BE CALLED");
		if (path == null) {
			path = getURL().getPath();
			if (path.startsWith(File.separator))
				path = path.substring(1);
		}
		return path;
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
			
			

			UpdateManagerUtils.resolveAsLocal(inStream, pluginPath + File.separator + contentKey);


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
			UpdateManagerUtils.resolveAsLocal(inStream, featurePath + File.separator + contentKey);

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