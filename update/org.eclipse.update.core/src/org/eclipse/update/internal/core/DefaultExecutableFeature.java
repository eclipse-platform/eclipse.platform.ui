package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.*; 

/**
 * feature that is executable
 */

public class DefaultExecutableFeature extends AbstractFeature {
	
	/**
	 * 
	 */
	private URL rootURL;

	/**
	 * Constructor for DefaultExecutableFeature
	 */
	public DefaultExecutableFeature(IFeature sourceFeature, ISite targetSite) throws CoreException {
		super(sourceFeature, targetSite);
	}

	/**
	 * Constructor for DefaultExecutableFeature
	 */
	public DefaultExecutableFeature(URL url,	ISite targetSite) {
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
	 * @see AbstractFeature#getInputStreamFor(String)
	 */
	public InputStream getInputStreamFor(IPluginEntry pluginEntry,String name) throws CoreException {
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

			if (!(new File(filePath)).exists())
				throw new IOException("The File:" + filePath + "does not exist.");
			File entry = new File(filePath,name);
			result = new FileInputStream(entry);
		} catch (Exception e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,id,IStatus.OK,"Error during retrieving the Stream of :"+name+" in plugin:"+pluginEntry.getIdentifier().toString(),e);
			throw new CoreException(status);
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames(IPluginEntry)
	 */
	public String[] getStorageUnitNames(IPluginEntry pluginEntry) throws CoreException {
		URL siteURL = getSite().getURL();
		String[] result = null;
		try {
			// get the URL of the Archive file that contains the plugin entry
			URL fileURL = 	((AbstractSite) getSite()).getArchiveURLfor(getArchiveID(pluginEntry));

			// if doesn't exist, get default path			
			if (fileURL == null) {
				fileURL =	new URL(siteURL, AbstractSite.DEFAULT_PLUGIN_PATH + getArchiveID(pluginEntry));
			}
			
			// return the list of all subdirectories
			File pluginDir = new File(fileURL.getFile());
			result = new String[pluginDir.list().length];
			for (int i = 0; i<pluginDir.list().length;i++){
				result[i] = pluginDir.list()[i];
			}
			//FIXME: check validity of algorithm and create testcase

		} catch (MalformedURLException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,id,IStatus.OK,"Error retrieving archive names for:"+pluginEntry.getIdentifier().toString(),e);
			throw new CoreException(status);		
		}
		return result;
	}
	/**
	 * @see AbstractFeature#getContentReferences()
	 */
	public String[] getContentReferences() throws CoreException {
		String[] names = new String[getPluginEntryCount()];
		for (int i = 0; i < getPluginEntryCount(); i++) {
			names[i] = getArchiveID(getPluginEntries()[i]);
		}
		return names;
	}

	/**
	 * @see AbstractFeature#isExecutable()
	 */
	public boolean isExecutable() {
		return true;
	}

	/**
	 * @see IFeature#getRootURL()
	 * Make sure the rotURL ends with '/' as it shoudl be a directory
	 */
	public URL getRootURL()  throws MalformedURLException {
		if (rootURL==null){
			rootURL = getURL();
			if (!rootURL.getPath().endsWith("/")){
				rootURL = new URL(getURL().getProtocol(),getURL().getHost(),getURL().getPath()+"/");
			}
		}
		return rootURL;
	}
	/**
	 * @see AbstractFeature#getInputStreamFor(String)
	 */
	protected InputStream getInputStreamFor(String name) {
		// TODO:
		return null;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames()
	 */
	protected String[] getStorageUnitNames() {
		// TODO:
		return null;
	}
	
	/**
	 * return the archive ID for a plugin
	 */
	private String getArchiveID(IPluginEntry entry) {
		//TODO: ?
		return entry.getIdentifier().toString() ;
	}

}

