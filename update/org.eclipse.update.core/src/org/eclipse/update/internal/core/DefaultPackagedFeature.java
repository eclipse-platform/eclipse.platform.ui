package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * Parse the default feature.xml
 */
public class DefaultPackagedFeature extends AbstractFeature {

	private JarFile currentOpenJarFile = null;

	private URL rootURL;

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
	public URL getRootURL() throws MalformedURLException {
		if (rootURL == null) {
			rootURL = new URL("jar", null, getURL().toExternalForm() + "!/");
		}
		return rootURL;
	}

	/**
	 * Constructor for DefaultPackagedFeature
	 */
	public DefaultPackagedFeature(IFeature sourceFeature, ISite targetSite) throws CoreException {
		super(sourceFeature, targetSite);
	}

	/**
	 * Constructor for DefaultPackagedFeature
	 */
	public DefaultPackagedFeature(URL url, ISite targetSite) {
		super(url, targetSite);
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
	 * @see AbstractFeature#getInputStreamFor(IPluginEntry,String)
	 */
	public InputStream getInputStreamFor(IPluginEntry pluginEntry, String name) throws CoreException {
		URL siteURL = getSite().getURL();
		InputStream result = null;

		try {
			// check if the site.xml had a coded URL for this plugin or if we
			// should look in teh default place to find it: <site>+/plugins/+archiveId
			String filePath = ((AbstractSite) getSite()).getURL(getArchiveID(pluginEntry)).getPath();

			// are we looking into teh same Jar file
			// or shoudl we close the previously opened one and open another one ?
			if (currentOpenJarFile != null) {
				if (!currentOpenJarFile.getName().equals(filePath)) {
					currentOpenJarFile.close();
					currentOpenJarFile = new JarFile(filePath);
				}
			} else {
				currentOpenJarFile = new JarFile(filePath);
			}

			if (!(new File(filePath)).exists())
				throw new IOException("The File:" + filePath + "does not exist.");
			ZipEntry entry = currentOpenJarFile.getEntry(name);
			result = currentOpenJarFile.getInputStream(entry);
		} catch (Exception e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error opening :" + name + " in plugin archive:" + pluginEntry.getIdentifier().toString(), e);
			throw new CoreException(status);
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames(IPluginEntry)
	 */
	public String[] getStorageUnitNames(IPluginEntry pluginEntry) throws CoreException {
		URL siteURL = getSite().getURL();
		JarFile jarFile = null;
		String[] result = null;
		// try to obtain the URL of the JAR file that contains the plugin entry from teh site.xml
		// if it doesn't exist, use the default one
		URL jarURL = ((AbstractSite) getSite()).getURL(getArchiveID(pluginEntry));
		result = getJAREntries(jarURL.getPath());

		return result;
	}

	/**
	 * @see AbstractFeature#getFeatureInputStream()
	 * The feature url is pointing at the JAR
	 * download the JAR in the TEMP dir 
	 * Change the URL to be JAR url jar:file://<filename>!/
	 * and get the feature.xml
	 */
	public InputStream getFeatureInputStream() throws CoreException, IOException {
		transferLocally();
		return super.getFeatureInputStream();
	}

	/**
	 * Transfer feature.jar file locally
	 */
	private void transferLocally() throws CoreException {
		// install in DEFAULT PATH for feature
		// as we OWN the temp site
		// attempt to preserve name

		try {
			URL resolvedURL = UpdateManagerUtils.resolveAsLocal(getURL(), getURL().getPath());
			if (!resolvedURL.equals(getURL())){
				this.setURL(resolvedURL);

				// DEBUG:
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_INSTALL) {
					UpdateManagerPlugin.getPlugin().debug("the feature on TEMP file is :" + resolvedURL.toExternalForm());
				}
			}
		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error transfering feature to TEMP directory", e);
			throw new CoreException(status);
		}
	}

	/**
	 * return the archive ID for a plugin
	 */
	private String getArchiveID(IPluginEntry entry) {
		return entry.getIdentifier().toString()+JAR_EXTENSION;
	}

	/**
	 * @see AbstractFeature#getContentReferences()
	 */
	public String[] getContentReferences() {
		String[] names = new String[getPluginEntryCount()];
		IPluginEntry[] entries = getPluginEntries();
		for (int i = 0; i < getPluginEntryCount(); i++) {
			names[i] = getArchiveID(entries[i]);
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
	protected InputStream getInputStreamFor(String name) throws CoreException {
		URL siteURL = getSite().getURL();
		InputStream result = null;
		try {
			// ensure the file is local
			transferLocally();

			// teh feature must have a URL as 
			//it has been transfered locally
			String filePath = getURL().getPath();
			
			// ensure we close the previous JAR file 
			if (currentOpenJarFile != null) {
				if (!currentOpenJarFile.getName().equals(filePath)) {
					currentOpenJarFile.close();
					currentOpenJarFile = new JarFile(filePath);
				}
			} else {
				currentOpenJarFile = new JarFile(filePath);
			}

			if (!(new File(filePath)).exists())
				throw new IOException("The File:" + filePath + "does not exist.");
			ZipEntry entry = currentOpenJarFile.getEntry(name);
			result = currentOpenJarFile.getInputStream(entry);

		} catch (Exception e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error opening :" + name + " in feature archive:" + getIdentifier().toString(), e);
			throw new CoreException(status);
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames()
	 */
	protected String[] getStorageUnitNames() throws CoreException {
		URL siteURL = getSite().getURL();
		JarFile jarFile = null;
		String[] result = null;

		//FIXME: delete obsolete try/catch block
		//try {
			// make sure the feature archive has been transfered locally
			transferLocally();

			// get the URL of the feature JAR file 
			// must exist as we tranfered it locally
			URL jarURL = getURL();
			result = getJAREntries(jarURL.getPath());

/*		} catch (MalformedURLException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error during Install", e);
			throw new CoreException(status);
		}*/
		return result;
	}

	/**
	 * return the list of entries in the JAR file
	 * Do not retrun Directory entries
	 * 
	 * do not get directories only entry as the directories will
	 * be created when teh fils will be created
	 * it was difficult to obtain a correct URL for a Directory inside a JAR
	 * because of the last '\' in the entry
	 */
	private String[] getJAREntries(String path) throws CoreException {
		String[] result = new String[0];
		try {
			JarFile jarFile = new JarFile(path);
			List list = new ArrayList();
			Enumeration enum = jarFile.entries();
			int loop = 0;
			while (enum.hasMoreElements()) {
				ZipEntry nextEntry = (ZipEntry) enum.nextElement();
				if (!nextEntry.isDirectory()) {
					list.add(nextEntry.getName());
					loop++;
				}
			}
			jarFile.close();

			// set the result			
			if (loop > 0 && !list.isEmpty()) {
				result = new String[loop];
				list.toArray(result);
			}
		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error opening JAR file:" + path, e);
			throw new CoreException(status);
		}
		return result;
	}

}