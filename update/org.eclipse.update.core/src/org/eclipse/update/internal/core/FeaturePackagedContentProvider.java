package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.zip.ZipException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.ContentReference;
import org.eclipse.update.core.Feature;
import org.eclipse.update.core.FeatureContentProvider;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.INonPluginEntry;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.InstallMonitor;
import org.eclipse.update.core.JarContentReference;
import org.eclipse.update.core.Site;
import org.eclipse.update.core.*;

/**
 * Parse the default feature.xml
 */
public class FeaturePackagedContentProvider extends FeatureContentProvider {

	private ContentReference localManifest = null;
	private ContentReference[] localFeatureFiles = new ContentReference[0];

	public static final String JAR_EXTENSION = ".jar";

	public static final FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(FeaturePackagedContentProvider.JAR_EXTENSION);
		}
	};

	/**
	 * Constructor 
	 */
	public FeaturePackagedContentProvider(URL url) {
		super(url);
	}

	/**
	 * return the archive ID for a plugin
	 */
	private String getPluginEntryArchiveID(IPluginEntry entry) {
		String type = (entry.isFragment()) ? Site.DEFAULT_FRAGMENT_PATH : Site.DEFAULT_PLUGIN_PATH;
		return type + entry.getVersionIdentifier().toString() + JAR_EXTENSION;
	}

	/**
	 * @see AbstractFeature#getArchiveID()
	 */
	public String[] getFeatureEntryArchiveID() {
		String[] names = new String[feature.getPluginEntryCount()];
		IPluginEntry[] entries = feature.getPluginEntries();
		for (int i = 0; i < feature.getPluginEntryCount(); i++) {
			names[i] = getPluginEntryArchiveID(entries[i]);
		}
		return names;
	}

	/*
	 * @see IFeatureContentProvider#getFeatureManifestReference()
	 */
	public ContentReference getFeatureManifestReference(InstallMonitor monitor) throws CoreException {

		// check to see if we already have local copy of the manifest
		if (localManifest != null)
			return localManifest;

		ContentReference result = null;
		ContentReference[] featureArchiveReference = getFeatureEntryArchiveReferences(monitor);
		try {
			// force feature archive to local. This content provider always assumes exactly 1 archive file (index [0])		
			JarContentReference featureJarReference = (JarContentReference) asLocalReference(featureArchiveReference[0], null);

			// we need to unpack archive locally for UI browser references to be resolved correctly
			localFeatureFiles = unpack(featureJarReference, null, monitor); // unpack and cache references
			result = null;
			for (int i = 0; i < localFeatureFiles.length; i++) {
				// find the manifest in the unpacked feature files
				if (localFeatureFiles[i].getIdentifier().equals(Feature.FEATURE_XML)) {
					result = localFeatureFiles[i];
					localManifest = result; // cache reference to manifest
					break;
				}
			}
			if (result == null)
				throw newCoreException("Error retrieving manifest file in  feature :" + featureArchiveReference[0].getIdentifier(), null);
		} catch (IOException e) {
			throw newCoreException("Error retrieving manifest file in  feature :" + featureArchiveReference[0].getIdentifier(), e);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getArchiveReferences()
	 */
	public ContentReference[] getArchiveReferences(InstallMonitor monitor) throws CoreException {
		IPluginEntry[] entries = feature.getPluginEntries();
		INonPluginEntry[] nonEntries = feature.getNonPluginEntries();
		List listAllContentRef = new ArrayList();
		ContentReference[] allContentRef = new ContentReference[0];

		// feature
		listAllContentRef.addAll(Arrays.asList(getFeatureEntryArchiveReferences(monitor)));

		// plugins
		for (int i = 0; i < entries.length; i++) {
			listAllContentRef.addAll(Arrays.asList(getPluginEntryArchiveReferences(entries[i], monitor)));
		}

		// non plugins
		for (int i = 0; i < nonEntries.length; i++) {
			listAllContentRef.addAll(Arrays.asList(getNonPluginEntryArchiveReferences(nonEntries[i], monitor)));
		}

		if (listAllContentRef.size() > 0) {
			allContentRef = new ContentReference[listAllContentRef.size()];
			listAllContentRef.toArray(allContentRef);
		}

		return allContentRef;
	}

	/*
	 * @see IFeatureContentProvider#getFeatureEntryArchiveReferences()
	 */
	public ContentReference[] getFeatureEntryArchiveReferences(InstallMonitor monitor) throws CoreException {
		//1 jar file <-> 1 feature
		ContentReference[] references = new ContentReference[1];
		try {
			// feature may not be known, 
			// we may be asked for the manifest before the feature is set
			String archiveID = (feature != null) ? feature.getVersionIdentifier().toString() : "";
			ContentReference currentReference = new JarContentReference(archiveID, getURL());
			currentReference = asLocalReference(currentReference, monitor);
			references[0] = currentReference;
		} catch (IOException e) {
			String urlString = (feature == null) ? "NO FEATURE" : "" + feature.getURL();
			throw newCoreException("Error retrieving feature Entry Archive Reference :" + urlString, e);
		}
		return references;
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryArchiveReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryArchiveReferences(IPluginEntry pluginEntry, InstallMonitor monitor) throws CoreException {
		ContentReference[] references = new ContentReference[1];
		String archiveID = getPluginEntryArchiveID(pluginEntry);
		URL url = feature.getSite().getSiteContentProvider().getArchiveReference(archiveID);

		// protocol is a file protocol		
		if ("file".equals(url.getProtocol())) {
			// either the URL is pointing to a directory or to a JAR file
			File pluginDir = UpdateManagerUtils.decodeFile(url);
			if (!pluginDir.exists()) {
				// plugin dir does not exist attmpt to add a '/' and see if it is a directory
				if (!pluginDir.getAbsolutePath().endsWith(File.separator)) {
					pluginDir = new File(pluginDir.getAbsolutePath() + File.separator);
				}
			}

			if (pluginDir.exists()) {
				if (pluginDir.isDirectory()) {
					// directory, then attempt executable plugin
					references[0] = new ContentReference(archiveID, pluginDir);
				} else {
					// file , attemp JAR file
					references[0] = new JarContentReference(archiveID, pluginDir);
				}
			} else
				throw newCoreException("The File:" + pluginDir.getAbsolutePath() + "does not exist.", null);
		} else {
			//if the protocol is not File, we have to suppose it is a JAR file
			references[0] = new JarContentReference(archiveID, url);
		}

		return references;
	}

	/*
	 * @see IFeatureContentProvider#getNonPluginEntryArchiveReferences(INonPluginEntry)
	 */
	public ContentReference[] getNonPluginEntryArchiveReferences(INonPluginEntry nonPluginEntry, InstallMonitor monitor) throws CoreException {
		// VK: shouldn't htis be returning the non-plugin entries ???
		return null;
	}

	/*
	 * @see IFeatureContentProvider#getFeatureEntryContentReferences()
	 */
	public ContentReference[] getFeatureEntryContentReferences(InstallMonitor monitor) throws CoreException {

		return localFeatureFiles; // return cached feature references
		// Note: assumes this content provider is always called first to
		//       get the feature manifest. This forces the feature files
		//       to be unpacked and caches the references
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryContentReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryContentReferences(IPluginEntry pluginEntry, InstallMonitor monitor) throws CoreException {
		ContentReference[] references = getPluginEntryArchiveReferences(pluginEntry, monitor);
		ContentReference[] pluginReferences = new ContentReference[0];
		try {

			if (references[0] instanceof JarContentReference) {
				JarContentReference localRef = (JarContentReference) asLocalReference(references[0], monitor);
				pluginReferences = peek(localRef, null, monitor);
			} else {
				// return the list of all subdirectories
				List files = getFiles(references[0].asFile());
				pluginReferences = new ContentReference[files.size()];
				for (int i = 0; i < pluginReferences.length; i++) {
					File currentFile = (File) files.get(i);
					pluginReferences[i] = new ContentReference(null, currentFile.toURL());
				}
			};

		} catch (Exception e) {
			throw newCoreException("Error retrieving plugin Entry Archive Reference :" + pluginEntry.getVersionIdentifier().toString(), e);
		}
		return pluginReferences;
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

	/*
	* @see IFeatureContentProvider#setFeature(IFeature)
	*/
	public void setFeature(IFeature feature) {
		this.feature = feature;
	}

	private CoreException newCoreException(String s, Throwable e) throws CoreException {
		return new CoreException(new Status(IStatus.ERROR, "org.eclipse.update.core", 0, s, e));
	}

}