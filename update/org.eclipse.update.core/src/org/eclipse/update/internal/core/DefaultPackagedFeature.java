package org.eclipse.update.internal.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.AbstractSite;
import org.eclipse.update.core.Assert;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.UpdateManagerPlugin;
import org.eclipse.update.core.VersionedIdentifier;

public class DefaultPackagedFeature extends AbstractFeature {

	private JarFile currentOpenJarFile = null;

	public static final String JAR_EXTENSION = ".jar";
	
	
	/**
	 * @see IFeature#getRootURL()
	 * In general, the Root URL is the URL of teh Feature
	 * 
	 * The RootURL is used to calculate relative URL for teh feature
	 * In case of a file feature, you can just append teh relative path
	 * to the URL of teh feature
	 * 
	 * In case of a JAR file, you cannot *just* append the file 
	 * You have to transfrom the URL
	 * 
	 */
	public URL getRootURL() {
		URL rootURL = null;
		try {
			rootURL = new URL("jar",null,getURL().toExternalForm()+"!/");
		} catch (MalformedURLException e){
			//FIXME:
			e.printStackTrace();
		}
		return rootURL;
	}
	

	/**
	 * Constructor for DefaultPackagedFeature
	 */
	public DefaultPackagedFeature(IFeature sourceFeature, ISite targetSite) {
		super(sourceFeature, targetSite);
	}

	/**
	 * Constructor for DefaultPackagedFeature
	 */
	public DefaultPackagedFeature(
		VersionedIdentifier identifier,
		ISite targetSite) {
		super(identifier, targetSite);
	}

	/**
	 * @see AbstractFeature#getContentReferenceToInstall(IPluginEntry[])
	 */
	public String[] getContentReferenceToInstall(IPluginEntry[] pluginsToInstall) {
		String[] names = null;
		if (pluginsToInstall != null) {
			names = new String[pluginsToInstall.length];
			for (int i = 0; i < pluginsToInstall.length; i++) {
				names[i] = getArchiveID(pluginsToInstall[i]);
			}
		}
		return names;
	}

	/**
	 * @see AbstractFeature#getInputStreamFor(String)
	 */
	public InputStream getInputStreamFor(IPluginEntry pluginEntry, String name) {
		URL siteURL = getSite().getURL();
		InputStream result = null;
		try {
			// default			
			String filePath =
				siteURL.getPath()
					+ AbstractSite.DEFAULT_PLUGIN_PATH
					+ getArchiveID(pluginEntry);
			URL fileURL =
				((AbstractSite) getSite()).getArchiveURLfor(getArchiveID(pluginEntry));
			if (fileURL != null) {
				// has to be local, file ?
				filePath = fileURL.getPath();
			}

			if (currentOpenJarFile != null) {
				if (!currentOpenJarFile.getName().equals(filePath)) {
					currentOpenJarFile.close();
					currentOpenJarFile = new JarFile(filePath);
				} else {
					// same file do nothing
				}
			} else {
				currentOpenJarFile = new JarFile(filePath);
			}

			if (!(new File(filePath)).exists())
				throw new IOException("The File:" + filePath + "does not exist.");
			ZipEntry entry = currentOpenJarFile.getEntry(name);
			result = currentOpenJarFile.getInputStream(entry);
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		} catch (IOException e) {
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames(IPluginEntry)
	 */
	public String[] getStorageUnitNames(IPluginEntry pluginEntry) {
		URL siteURL = getSite().getURL();
		JarFile jarFile = null;
		String[] result = null;
		try {
			// get the URL of the JAR file that contains teh plugin entry
			URL jarURL =
				((AbstractSite) getSite()).getArchiveURLfor(getArchiveID(pluginEntry));
			if (jarURL == null) {
				// default path
				jarURL =
					new URL(siteURL.getProtocol(),siteURL.getHost(),siteURL.getPath()+AbstractSite.DEFAULT_PLUGIN_PATH + getArchiveID(pluginEntry));
			}
			jarFile = new JarFile(jarURL.getPath());
			result = new String[jarFile.size()];
			Enumeration enum = jarFile.entries();
			int loop = 0;
			while (enum.hasMoreElements()) {
				ZipEntry nextEntry = (ZipEntry) enum.nextElement();
				result[loop] = (String) nextEntry.getName();
				loop++;
			}
			jarFile.close();
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		} catch (IOException e) {
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getFeatureInputStream()
	 * The feature url is pointing at the JAR
	 * download the JAR in the TEMP dir 
	 * Change the URL to be JAR url jar:file://<filename>!/
	 * and get the feature.xml
	 */
	public InputStream getFeatureInputStream() throws IOException {
		transferLocally();
		return super.getFeatureInputStream();
	}

	/**
	 * Transfer feature.jar file locally
	 */
	private void transferLocally() throws IOException {
		// install in DEFAULT PATH for feature
		// as we OWN the temp site
		String newFile = 
				AbstractSite.DEFAULT_FEATURE_PATH
				+ getIdentifier().toString()
				+ JAR_EXTENSION;
		URL resolvedURL = UpdateManagerUtils.resolveAsLocal(getURL(),newFile);		this.setURL(resolvedURL);

		// DEBUG:
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_INSTALL){
			System.out.println("the feature on TEMP file is :"+newFile);					
		}
	}

	/**
	 * return the archive ID for a plugin
	 */
	private String getArchiveID(IPluginEntry entry) {
		return entry.getIdentifier().toString() + JAR_EXTENSION;
	}

	/**
	 * @see AbstractFeature#getContentReferences()
	 */
	public String[] getContentReferences() {
		String[] names = new String[getPluginEntryCount()];
		for (int i = 0; i < getPluginEntryCount(); i++) {
			names[i] = getArchiveID(getPluginEntries()[i]);
		}
		return names;
	}

	/**
	 * @see AbstractFeature#isInstallable()
	 */
	public boolean isInstallable() {
		return true;
	}

	/**
	 * @see AbstractFeature#getInputStreamFor(String)
	 */
	protected InputStream getInputStreamFor(String name) {
		URL siteURL = getSite().getURL();
		InputStream result = null;

		try {
			transferLocally();

			// default			
			String filePath =
				siteURL.getPath()
					+ AbstractSite.DEFAULT_FEATURE_PATH
					+ getIdentifier().toString()
					+ JAR_EXTENSION;
			URL fileURL = getURL();
			if (fileURL != null) {
				// has to be local, file ?
				filePath = fileURL.getPath();
			}

			if (currentOpenJarFile != null) {
				if (!currentOpenJarFile.getName().equals(filePath)) {
					currentOpenJarFile.close();
					currentOpenJarFile = new JarFile(filePath);
				} else {
					// same file do nothing
				}
			} else {
				currentOpenJarFile = new JarFile(filePath);
			}

			if (!(new File(filePath)).exists())
				throw new IOException("The File:" + filePath + "does not exist.");
			ZipEntry entry = currentOpenJarFile.getEntry(name);
			result = currentOpenJarFile.getInputStream(entry);
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		} catch (IOException e) {
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames()
	 */
	protected String[] getStorageUnitNames() {
		URL siteURL = getSite().getURL();
		JarFile jarFile = null;
		String[] result = null;

		try {

			transferLocally();

			// get the URL of the feature JAR file
			URL jarURL = getURL();
			if (jarURL == null) {
				// default path
				jarURL =
					new URL(
						siteURL,
						AbstractSite.DEFAULT_FEATURE_PATH + getIdentifier().toString() + JAR_EXTENSION);
			}
			jarFile = new JarFile(jarURL.getPath());
			result = new String[jarFile.size()];
			Enumeration enum = jarFile.entries();
			int loop = 0;
			while (enum.hasMoreElements()) {
				ZipEntry nextEntry = (ZipEntry) enum.nextElement();
				result[loop] = (String) nextEntry.getName();
				loop++;
			}
			jarFile.close();
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		} catch (IOException e) {
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

}