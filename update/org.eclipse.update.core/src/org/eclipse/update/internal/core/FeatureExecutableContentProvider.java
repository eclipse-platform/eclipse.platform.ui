package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * Default implementation of an Executable DefaultFeature Content Provider
 */

public class FeatureExecutableContentProvider extends FeatureContentProvider {

	/**
	 * the feature 
	 */
	private IFeature feature;

	/**
	 * Constructor for DefaultExecutableFeature
	 */
	public FeatureExecutableContentProvider(URL url) {
		super(url);
	}

	/**
	 * return the path for a pluginEntry
	 */
	private String getPath(IPluginEntry pluginEntry) throws Exception {
		String result = null;

		// get the URL of the Archive file that contains the plugin entry
		ISiteContentProvider provider = feature.getSite().getSiteContentProvider();
		URL fileURL = provider.getArchiveReference(getArchiveID(pluginEntry));
		result = UpdateManagerUtils.getPath(fileURL);

		// return the list of all subdirectories
		if (!result.endsWith(File.separator))
			result += File.separator;
		File pluginDir = new File(result);
		if (!pluginDir.exists())
			throw new IOException("The File:" + result + "does not exist.");

		return result;
	}

	/**
	 * return the path for the DefaultFeature
	 */
	private String getFeaturePath() throws IOException {
		String result = UpdateManagerUtils.getPath(feature.getURL());
		;

		// return the list of all subdirectories
		if (!(result.endsWith(File.separator) || result.endsWith("/")))
			result += File.separator;
		File pluginDir = new File(result);
		if (!pluginDir.exists())
			throw new IOException("The File:" + result + "does not exist.");

		return result;
	}

	/**
	 * return all the files under the directory
	 */
	private List getFiles(File dir) throws IOException {
		List result = new ArrayList();

		if (!dir.isDirectory())
			throw new IOException(dir.getPath() + " is not a valid directory");

		File[] files = dir.listFiles();
		if (files != null) // be careful since it can be null
			for (int i = 0; i < files.length; ++i) {
				if (files[i].isDirectory()) {
					result.addAll(getFiles(files[i]));
				} else {
					result.add(files[i]);
				}
			}
		return result;
	}

	/**
	 * return the archive ID for a plugin
	 * The id is based on the feature
	 * the default ID is plugins/pluginId_pluginVer or
	 * the default ID is fragments/pluginId_pluginVer or
		*/
	private String getArchiveID(IPluginEntry entry) {
		//FIXME: fragments
		String type = (entry.isFragment()) ? Site.DEFAULT_FRAGMENT_PATH : Site.DEFAULT_PLUGIN_PATH;
		return type + entry.getIdentifier().toString();
	}

	/**
	 * return the archive ID for a plugin
	 * The id is based on the feature
	 * the default ID is plugins/pluginId_pluginVer or
	 * the default ID is fragments/pluginId_pluginVer or
		*/
	private String getArchiveID(INonPluginEntry entry) {
		String nonPluginBaseID = Site.DEFAULT_FEATURE_PATH + feature.getVersionIdentifier().toString()+"/";
		return nonPluginBaseID + entry.getIdentifier().toString();
	}
	/*
	 * @see IFeatureContentProvider#getFeatureManifestReference()
	 */
	public ContentReference getFeatureManifestReference() throws CoreException {
		ContentReference result = null;
		try {
			result = new ContentReference(null, new URL(getURL(), Feature.FEATURE_XML));
			
		} catch (MalformedURLException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "cannot create URL for :"+getURL().toExternalForm()+" "+Feature.FEATURE_XML, e);
			throw new CoreException(status);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getArchiveReferences()
	 */
	public ContentReference[] getArchiveReferences() throws CoreException {
		return null;
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryArchiveReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryArchiveReferences(IPluginEntry pluginEntry) throws CoreException {
		ContentReference[] result = new ContentReference[1];
		try {
			// get the URL of the Archive file that contains the plugin entry
			ISiteContentProvider provider = feature.getSite().getSiteContentProvider();
			String archiveID = getArchiveID(pluginEntry);
			URL fileURL = provider.getArchiveReference(archiveID);
			String fileString = UpdateManagerUtils.getPath(fileURL);

			// return the list of all subdirectories
			if (!fileString.endsWith(File.separator))
				fileString += File.separator;
			File pluginDir = new File(fileString);
			if (!pluginDir.exists())
				throw new IOException("The File:" + fileString + "does not exist.");

			result[0] = new ContentReference(archiveID,pluginDir.toURL());
		} catch (Exception e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error retrieving archive names for:" + pluginEntry.getIdentifier().toString(), e);
			throw new CoreException(status);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getNonPluginEntryArchiveReferences(INonPluginEntry)
	 */
	public ContentReference[] getNonPluginEntryArchiveReferences(INonPluginEntry nonPluginEntry) throws CoreException {
		ContentReference[] result = new ContentReference[1];
		try {
			// get the URL of the Archive file that contains the plugin entry
			ISiteContentProvider provider = feature.getSite().getSiteContentProvider();
			URL fileURL = provider.getArchiveReference(getArchiveID(nonPluginEntry));
			String fileString = UpdateManagerUtils.getPath(fileURL);

			File nonPluginData = new File(fileString);
			if (!nonPluginData.exists())
				throw new IOException("The File:" + fileString + " does not exist.");

			result[0] = new ContentReference(nonPluginEntry.getIdentifier(),nonPluginData.toURL());
		} catch (Exception e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error retrieving archive references:" + nonPluginEntry.getIdentifier().toString(), e);
			throw new CoreException(status);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getFeatureEntryArchiveReferences()
	 */
	public ContentReference[] getFeatureEntryArchiveReferences() throws CoreException {
		ContentReference[] contentReferences = new ContentReference[1];
		contentReferences[1] = new ContentReference(null,getURL());
		return contentReferences;
	}

	/*
	 * @see IFeatureContentProvider#getFeatureEntryArchivesContentReferences()
	 */
	public ContentReference[] getFeatureEntryContentReferences() throws CoreException {
		ContentReference[] result = new ContentReference[0];
		try {
			File featureDir = new File(getFeaturePath());
			List files = getFiles(featureDir);
			result = new ContentReference[files.size()];
			for (int i = 0; i < result.length; i++) {
				File currentFile = (File) files.get(i);
				result[i] = new ContentReference(currentFile.getName(),currentFile.toURL());
			}
		} catch (Exception e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error retrieving archive content references for:" + feature.getVersionIdentifier().toString(), e);
			throw new CoreException(status);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryContentReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryContentReferences(IPluginEntry pluginEntry) throws CoreException {

		ContentReference[] result = new ContentReference[0];

		try {
			// return the list of all subdirectories
			File pluginDir = new File(getPath(pluginEntry));
			List files = getFiles(pluginDir);
			result = new ContentReference[files.size()];
			for (int i = 0; i < result.length; i++) {
				File currentFile = (File) files.get(i);
				result[i] = new ContentReference(null,currentFile.toURL());
			}
		} catch (Exception e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error retrieving archive content references for:" + pluginEntry.getIdentifier().toString(), e);
			throw new CoreException(status);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#setFeature(IFeature)
	 */
	public void setFeature(IFeature feature) {
		this.feature = feature;
	}

}