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
public class FeaturePackaged extends Feature {

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
	public URL getRootURL() throws MalformedURLException, IOException, CoreException {
		
		// Extract the JAR file in the TEMP drive
		// and return the URL
		
		if (rootURL == null) {
			// install the Feature info into the TEMP drive
			SiteFile tempSite = (SiteFile)SiteManager.getTempSite();
			
			InputStream inStream = null;
			String[] names = getStorageUnitNames();
			if (names != null) {
				openFeature();
				for (int j = 0; j < names.length; j++) {
					if ((inStream = getInputStreamFor(names[j])) != null)
						 tempSite.storeFeatureInfo(getIdentifier(), names[j], inStream);
				}
				closeFeature();
			}
			//rootURL = new URL("jar", null, getURL().toExternalForm() + "!/");
			// get the path to the Feature
			rootURL = UpdateManagerUtils.getURL(tempSite.getURL(),SiteFile.INSTALL_FEATURE_PATH+getIdentifier().toString(),null);
		}
		return rootURL;
	}

	/**
	 * Constructor for DefaultPackagedFeature
	 */
	public FeaturePackaged(IFeature sourceFeature, ISite targetSite) throws CoreException {
		super(sourceFeature, targetSite);
	}

	/**
	 * Constructor for DefaultPackagedFeature
	 */
	public FeaturePackaged(URL url, ISite targetSite) {
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
			String filePath = UpdateManagerUtils.getPath(((Site) getSite()).getURL(getArchiveID(pluginEntry)));						
			open(filePath);
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

		// try to obtain the URL of the JAR file that contains the plugin entry from teh site.xml
		// if it doesn't exist, use the default one
		URL jarURL = ((Site) getSite()).getURL(getArchiveID(pluginEntry));
		String path = UpdateManagerUtils.getPath(jarURL);					
		String[] result = getJAREntries(path);

		return result;
	}

	/**
	 * Transfer feature.jar file locally
	 */
	private void transferLocally() throws CoreException {
		// install in DEFAULT PATH for feature
		// as we OWN the temp site

		try {
			String path = UpdateManagerUtils.getPath(getURL());			
			URL resolvedURL = UpdateManagerUtils.resolveAsLocal(getURL(),path);
			this.setURL(resolvedURL);

			// DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_INSTALL) {
				UpdateManagerPlugin.getPlugin().debug("the feature on TEMP file is :" + resolvedURL.toExternalForm());
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
		return entry.getIdentifier().toString() + JAR_EXTENSION;
	}

	/**
	 * @see AbstractFeature#getContentReferences()
	 */
	public String[] getArchives() {
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
	protected InputStream getInputStreamFor(String name) throws CoreException, IOException {
		URL siteURL = getSite().getURL();
		InputStream result = null;
		try {
			// ensure the file is local
			transferLocally();

			// teh feature must have a URL as 
			//it has been transfered locally
			String filePath = UpdateManagerUtils.getPath(getURL());						
			if (!(new File(filePath)).exists())
				throw new IOException("The File:" + filePath + "does not exist.");
			open(filePath);
			ZipEntry entry = currentOpenJarFile.getEntry(name);
			result = currentOpenJarFile.getInputStream(entry);

		} catch (IOException e) {
			throw new IOException("Error opening :" + name + " in feature archive:" + getIdentifier().toString() + "\r\n" + e.toString());
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames()
	 */
	protected String[] getStorageUnitNames() throws CoreException {

		// make sure the feature archive has been transfered locally
		transferLocally();

		// get the URL of the feature JAR file 
		// must exist as we tranfered it locally
		String path = UpdateManagerUtils.getPath(getURL());					
		String[] result = getJAREntries(path);

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

	/**
	 * @see AbstractFeature#close(IPluginEntry)
	 */
	protected void close(IPluginEntry entry) throws IOException {
		if (currentOpenJarFile != null)
			currentOpenJarFile.close();
	}

	/**
	 * @see AbstractFeature#closeFeature()
	 */
	public void closeFeature() throws IOException {
		if (currentOpenJarFile != null)
			currentOpenJarFile.close();
	}

	/**
	 * opens a JAR file or returns the one already opened 
	 * if teh path is the same.
	 */
	protected void open(String filePath) throws IOException {
		JarFile newJarFile = new JarFile(filePath);
		open(newJarFile);
	}

	/**
	 * opens a JAR file or returns the one already opened
	 * if teh path is the same.
	 */
	protected void open(JarFile newJarFile) throws IOException {

		// are we looking into teh same Jar file
		// or shoudl we close the previously opened one and open another one ?
		if (currentOpenJarFile != null) {
			if (!currentOpenJarFile.getName().equals(newJarFile.getName())) {
				currentOpenJarFile.close();
				currentOpenJarFile = newJarFile;
			} else {
				newJarFile.close();
			}
		} else {
			currentOpenJarFile = newJarFile;
		}
	}

	/**
	 * return the appropriate resource bundle for this feature
	 * Need to override as opening the JAR keeps it locked
	 * 
	 * baseclass + "_" + language1 + "_" + country1 + "_" + variant1 
	 * baseclass + "_" + language1 + "_" + country1 + "_" + variant1 + ".properties" 
	 * baseclass + "_" + language1 + "_" + country1 
	 * baseclass + "_" + language1 + "_" + country1 + ".properties" 
	 * baseclass + "_" + language1 
	 * baseclass + "_" + language1 + ".properties" 
	 * baseclass + "_" + language2 + "_" + country2 + "_" + variant2 
	 * baseclass + "_" + language2 + "_" + country2 + "_" + variant2 + ".properties" 
	 * baseclass + "_" + language2 + "_" + country2 
	 * baseclass + "_" + language2 + "_" + country2 + ".properties" 
	 * baseclass + "_" + language2 
	 * baseclass + "_" + language2 + ".properties" 
	 * baseclass 
	 * baseclass + ".properties" 
	 */
	public ResourceBundle getResourceBundle() throws IOException, CoreException {

		ResourceBundle result = null;
		String[] names = getStorageUnitNames();
		String base = FEATURE_FILE;

		// retrive names in teh JAR that starts with the basename
		// remove FEATURE_XML file
		List baseNames = new ArrayList();
		for (int i = 0; i < names.length; i++) {
			if (names[i].startsWith(base))
				baseNames.add(names[i]);
		}
		baseNames.remove(FEATURE_XML);

		// is there any file		
		if (!baseNames.isEmpty()) {

			Locale locale = Locale.getDefault();
			String lang1 = locale.getLanguage();
			String country1 = locale.getCountry();
			String variant1 = locale.getVariant();
			String[] attempt =
				new String[] {
					base + "_" + lang1 + "_" + country1 + "_" + variant1,
					base + "_" + lang1 + "_" + country1 + "_" + variant1 + ".properties",
					base + "_" + lang1 + "_" + country1,
					base + "_" + lang1 + "_" + country1 + ".properties",
					base + "_" + lang1,
					base + "_" + lang1 + ".properties",
					base,
					base + ".properties" };

			boolean found = false;
			int index = 0;
			while (!found && index < attempt.length) {
				if (baseNames.contains(attempt[index])) {
					result = new PropertyResourceBundle(getInputStreamFor(attempt[index]));
					found = true;
				}
				index++;
			}

		} // baseNames is empty

		if (result == null) {
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug("Cannot find resourceBundle for:" + base + " - " + Locale.getDefault().toString() + ":" + this.getURL().toExternalForm());
			}
		}
		return result;
	}
}