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
	private String getPath(IPluginEntry pluginEntry) throws IOException,CoreException {
		String result = null;

		// get the URL of the Archive file that contains the plugin entry
		ISiteContentProvider provider = feature.getSite().getSiteContentProvider();
		URL fileURL = provider.getArchiveReference(getArchiveID(pluginEntry));
		result = fileURL.getFile();

		// return the list of all subdirectories
		if (!result.endsWith(File.separator))
			result += File.separator;
		File pluginDir = new File(result);
		if (!pluginDir.exists())
			throw new IOException(Policy.bind("FeatureExecutableContentProvider.FileDoesNotExist",result)); //$NON-NLS-1$ //$NON-NLS-2$

		return result;
	}

	/**
	 * return the path for the DefaultFeature
	 */
	private String getFeaturePath() throws IOException {
		String result = feature.getURL().getFile();


		// return the list of all subdirectories
		if (!(result.endsWith(File.separator) || result.endsWith("/"))) //$NON-NLS-1$
			result += File.separator;
		File pluginDir = new File(result);
		if (!pluginDir.exists())
			throw new IOException(Policy.bind("FeatureExecutableContentProvider.FileDoesNotExist",result)); //$NON-NLS-1$ //$NON-NLS-2$

		return result;
	}

	/**
	 * return all the files under the directory
	 */
	private List getFiles(File dir) throws IOException {
		List result = new ArrayList();

		if (!dir.isDirectory())
			throw new IOException(dir.getPath() + Policy.bind("FeatureExecutableContentProvider.InvalidDirectory",dir.getAbsolutePath())); //$NON-NLS-1$

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
	 * the default ID is plugins/pluginId_pluginVer 
		*/
	private String getArchiveID(IPluginEntry entry) {
		return Site.DEFAULT_PLUGIN_PATH + entry.getVersionedIdentifier().toString();
	}

	/**
	 * return the archive ID for a plugin
	 * The id is based on the feature
	 * the default ID is features/pluginId_pluginVer/
		*/
	private String getArchiveID(INonPluginEntry entry) {
		String nonPluginBaseID = Site.DEFAULT_FEATURE_PATH + feature.getVersionedIdentifier().toString()+"/"; //$NON-NLS-1$
		return nonPluginBaseID + entry.getIdentifier().toString();
	}
	
	/*
	 * @see IFeatureContentProvider#getVerifier()
	 */
	public IVerifier getVerifier() throws CoreException {
		return null;
	}
	
	/*
	 * @see IFeatureContentProvider#getFeatureManifestReference()
	 */
	public ContentReference getFeatureManifestReference(InstallMonitor monitor) throws CoreException {
		ContentReference result = null;
		try {
			result = new ContentReference(null, new URL(getURL(), Feature.FEATURE_XML));
			
		} catch (MalformedURLException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("FeatureExecutableContentProvider.UnableToCreateURLFor",getURL().toExternalForm()+" "+Feature.FEATURE_XML), e); //$NON-NLS-1$ //$NON-NLS-2$
			throw new CoreException(status);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getArchiveReferences()
	 */
	public ContentReference[] getArchiveReferences(InstallMonitor monitor) throws CoreException {
		// executable feature does not contain archives
		return new ContentReference[0];
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryArchiveReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryArchiveReferences(IPluginEntry pluginEntry, InstallMonitor monitor) throws CoreException {
		ContentReference[] result = new ContentReference[1];
		try {
			result[0] = new ContentReference(getArchiveID(pluginEntry),new File(getPath(pluginEntry)));
		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("FeatureExecutableContentProvider.UnableToRetrievePluginEntry", pluginEntry.getVersionedIdentifier().toString()), e); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getNonPluginEntryArchiveReferences(INonPluginEntry)
	 */
	public ContentReference[] getNonPluginEntryArchiveReferences(INonPluginEntry nonPluginEntry, InstallMonitor monitor) throws CoreException {
		ContentReference[] result = new ContentReference[1];
		try {
			// get the URL of the Archive file that contains the plugin entry
			ISiteContentProvider provider = feature.getSite().getSiteContentProvider();
			URL fileURL = provider.getArchiveReference(getArchiveID(nonPluginEntry));
			String fileString = fileURL.getFile();

			File nonPluginData = new File(fileString);
			if (!nonPluginData.exists())
				throw new IOException(Policy.bind("FeatureExecutableContentProvider.FileDoesNotExist",fileString)); //$NON-NLS-1$ //$NON-NLS-2$

			result[0] = new ContentReference(nonPluginEntry.getIdentifier(),nonPluginData.toURL());
		} catch (Exception e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("FeatureExecutableContentProvider.UnableToRetrieveNonPluginEntry", nonPluginEntry.getIdentifier().toString()), e); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getFeatureEntryArchiveReferences()
	 */
	public ContentReference[] getFeatureEntryArchiveReferences(InstallMonitor monitor) throws CoreException {
		ContentReference[] contentReferences = new ContentReference[1];
		contentReferences[0] = new ContentReference(null,getURL());
		return contentReferences;
	}

	/*
	 * @see IFeatureContentProvider#getFeatureEntryArchivesContentReferences()
	 */
	public ContentReference[] getFeatureEntryContentReferences(InstallMonitor monitor) throws CoreException {
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
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("FeatureExecutableContentProvider.UnableToRetrieveFeatureEntryContentRef",feature.getVersionedIdentifier().toString()), e); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryContentReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryContentReferences(IPluginEntry pluginEntry, InstallMonitor monitor) throws CoreException {

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
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("FeatureExecutableContentProvider.UnableToRetriveArchiveContentRef") + pluginEntry.getVersionedIdentifier().toString(), e); //$NON-NLS-1$
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