package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.security.JarVerifier;

/**
 * Parse the default feature.xml
 */
public class FeaturePackagedContentProvider extends FeatureContentProvider {

	private ContentReference localManifest = null;
	private ContentReference[] localFeatureFiles = new ContentReference[0];

	public static final String JAR_EXTENSION = ".jar"; //$NON-NLS-1$

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

	/*
	 * @see IFeatureContentProvider#getVerifier()
	 */
	public IVerifier getVerifier() throws CoreException {
		return new JarVerifier();
	}
	
	/**
	 * return the archive ID for a plugin
	 */
	private String getPluginEntryArchiveID(IPluginEntry entry) {
		String type = (entry.isFragment()) ? Site.DEFAULT_FRAGMENT_PATH : Site.DEFAULT_PLUGIN_PATH;
		return type + entry.getVersionedIdentifier().toString() + JAR_EXTENSION;
	}

	/**
	 * @see AbstractFeature#getArchiveID()
	 */
	public String[] getFeatureEntryArchiveID() {
		String[] names = new String[getFeature().getPluginEntryCount()];
		IPluginEntry[] entries = getFeature().getPluginEntries();
		for (int i = 0; i < getFeature().getPluginEntryCount(); i++) {
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
			localFeatureFiles = featureJarReference.unpack(getWorkingDirectory(), null, monitor); // unpack and cache references
			result = null;
			for (int i = 0; i < localFeatureFiles.length; i++) {
				// find the manifest in the unpacked feature files
				if (localFeatureFiles[i].getIdentifier().equals(Feature.FEATURE_XML)) {
					result = localFeatureFiles[i];
					localManifest = result; // cache reference to manifest
					break;
				}
			}
			if (result == null){
				String[] values = new String[]{Feature.FEATURE_XML, featureArchiveReference[0].getIdentifier() , getURL().toExternalForm()};
				throw Utilities.newCoreException(Policy.bind("FeaturePackagedContentProvider.NoManifestFile",values), null); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (IOException e) {
			String[] values = new String[]{Feature.FEATURE_XML, featureArchiveReference[0].getIdentifier() , getURL().toExternalForm()};			
			throw Utilities.newCoreException(Policy.bind("FeaturePackagedContentProvider.ErrorRetrieving",values), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getArchiveReferences()
	 */
	public ContentReference[] getArchiveReferences(InstallMonitor monitor) throws CoreException {
		IPluginEntry[] entries = getFeature().getPluginEntries();
		INonPluginEntry[] nonEntries = getFeature().getNonPluginEntries();
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
		ContentReference currentReference = null;
		String archiveID = null;
		try {
			// feature may not be known, 
			// we may be asked for the manifest before the feature is set
			archiveID = (getFeature() != null) ? getFeature().getVersionedIdentifier().toString() : ""; //$NON-NLS-1$
			currentReference = new JarContentReference(archiveID, getURL());
			currentReference = asLocalReference(currentReference, monitor);
			references[0] = currentReference;
		}catch (IOException e) {
			String urlString = (getFeature() == null) ? Policy.bind("FeaturePackagedContentProvider.NoFeature") : "" + getFeature().getURL(); //$NON-NLS-1$ //$NON-NLS-2$
			String refString = (currentReference==null)?Policy.bind("FeaturePackagedContentProvider.NoReference"):currentReference.getIdentifier(); //$NON-NLS-1$
			String[] values = new String[]{archiveID,refString,urlString};
			throw Utilities.newCoreException(Policy.bind("FeaturePackagedContentProvider.ErrorRetrieving",values), e); //$NON-NLS-1$
		}
		return references;
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryArchiveReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryArchiveReferences(IPluginEntry pluginEntry, InstallMonitor monitor) throws CoreException {
		ContentReference[] references = new ContentReference[1];
		String archiveID = getPluginEntryArchiveID(pluginEntry);
		URL url = getFeature().getSite().getSiteContentProvider().getArchiveReference(archiveID);

		// FIXME... Hack to support plugin executable for a packaged feature... not necessary
		try {
		// protocol is a file protocol		
		if ("file".equals(url.getProtocol())) { //$NON-NLS-1$
			// either the URL is pointing to a directory or to a JAR file
			File pluginDir = new File(url.getFile());
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
					references[0] = asLocalReference(new JarContentReference(archiveID, pluginDir),monitor);
				}
			} else
				throw Utilities.newCoreException(Policy.bind("FeaturePackagedContentProvider.FileDoesNotExist", pluginDir.getAbsolutePath()), null); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			//if the protocol is not File, we have to suppose it is a JAR file
			references[0] = asLocalReference(new JarContentReference(archiveID, url),monitor);
		}
		}catch (IOException e) {
			String urlString = (getFeature() == null) ? Policy.bind("FeaturePackagedContentProvider.NoFeature") : "" + getFeature().getURL(); //$NON-NLS-1$ //$NON-NLS-2$
			String refString = (references[0]==null)?Policy.bind("FeaturePackagedContentProvider.NoReference"):references[0].getIdentifier(); //$NON-NLS-1$
			String[] values = new String[]{archiveID,refString,urlString};
			throw Utilities.newCoreException(Policy.bind("FeaturePackagedContentProvider.ErrorRetrieving",values), e); //$NON-NLS-1$
		}
		return references;
	}

	/*
	 * @see IFeatureContentProvider#getNonPluginEntryArchiveReferences(INonPluginEntry)
	 */
	public ContentReference[] getNonPluginEntryArchiveReferences(INonPluginEntry nonPluginEntry, InstallMonitor monitor) throws CoreException {

		// archive = feature/<id>_<ver>/<file>
		String archiveID = Site.DEFAULT_FEATURE_PATH + ((getFeature() != null) ? getFeature().getVersionedIdentifier().toString() : ""); //$NON-NLS-1$
		archiveID += "/" + nonPluginEntry.getIdentifier(); //$NON-NLS-1$

		ContentReference[] references = new ContentReference[1];
		ContentReference currentReference = null;
		
		try {
			URL url = getFeature().getSite().getSiteContentProvider().getArchiveReference(archiveID);			
			currentReference = new ContentReference(nonPluginEntry.getIdentifier(), url);
			currentReference = asLocalReference(currentReference, monitor);
			references[0] = currentReference;
		} catch (IOException e) {
			String urlString = (getFeature() == null) ? Policy.bind("FeaturePackagedContentProvider.NoFeature") : "" + getFeature().getURL(); //$NON-NLS-1$ //$NON-NLS-2$
			String refString = (currentReference==null)?Policy.bind("FeaturePackagedContentProvider.NoReference"):currentReference.getIdentifier(); //$NON-NLS-1$
			String[] values = new String[]{archiveID,refString,urlString};			
			throw Utilities.newCoreException(Policy.bind("FeaturePackagedContentProvider.ErrorRetrieving",values), e); //$NON-NLS-1$
		}

		return references;
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
				pluginReferences = localRef.peek(null, monitor);
			} else {
				// return the list of all subdirectories
				List files = getFiles(references[0].asFile());
				pluginReferences = new ContentReference[files.size()];
				for (int i = 0; i < pluginReferences.length; i++) {
					File currentFile = (File) files.get(i);
					pluginReferences[i] = new ContentReference(null, currentFile.toURL());
				}
			};

		} catch (IOException e) {
			String urlString = (getFeature() == null) ? Policy.bind("FeaturePackagedContentProvider.NoFeature") : "" + getFeature().getURL(); //$NON-NLS-1$ //$NON-NLS-2$			
			String refString = (references[0]==null)?Policy.bind("FeaturePackagedContentProvider.NoReference"):references[0].getIdentifier(); //$NON-NLS-1$
			String[] values = new String[]{pluginEntry.getVersionedIdentifier().toString(),refString,urlString};
			throw Utilities.newCoreException(Policy.bind("FeaturePackagedContentProvider.ErrorRetrieving",values), e); //$NON-NLS-1$ 
		}
		return pluginReferences;
	}

	/**
	 * return all the files under the directory
	 */
	private List getFiles(File dir) throws IOException {
		List result = new ArrayList();

		if (!dir.isDirectory())
			throw new IOException(Policy.bind("FeaturePackagedContentProvider.InvalidDirectory",dir.getPath())); //$NON-NLS-1$

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


}