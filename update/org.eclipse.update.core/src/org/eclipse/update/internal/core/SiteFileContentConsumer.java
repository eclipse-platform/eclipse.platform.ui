package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.core.model.ArchiveReferenceModel;
import org.eclipse.update.core.model.FeatureModel;

/**
 * Site on the File System
 */
public class SiteFileContentConsumer extends SiteContentConsumer {

	private String path;
	private IFeature feature;
	
	// recovery
	private String oldPath;
	private String newPath;

	/**
	 * Constructor for FileSite
	 */
	public SiteFileContentConsumer(IFeature feature) {
		this.feature = feature;
	}

	/**
		 * return the path in whichh the Feature will be installed
		 */
	private String getFeaturePath() throws CoreException {
		String featurePath = null;
		try {
			VersionedIdentifier featureIdentifier =
				feature.getVersionedIdentifier();
			String path =
				Site.DEFAULT_INSTALLED_FEATURE_PATH
					+ featureIdentifier.toString()
					+ File.separator;
			URL newURL = new URL(getSite().getURL(), path);
			featurePath = newURL.getFile();
		} catch (MalformedURLException e) {
			throw Utilities.newCoreException(
				Policy.bind("SiteFileContentConsumer.UnableToCreateURL")
					+ e.getMessage(),
				e);
			//$NON-NLS-1$
		}
		return featurePath;
	}

	/*
	 * @see ISiteContentConsumer#open(INonPluginEntry)
	 */
	public IContentConsumer open(INonPluginEntry nonPluginEntry)
		throws CoreException {
		return  new SiteFileNonPluginContentConsumer(getFeaturePath());
	}

	/*
	 * @see ISiteContentConsumer#open(IPluginEntry)
	 */
	public IContentConsumer open(IPluginEntry pluginEntry)
		throws CoreException {
		return new SiteFilePluginContentConsumer(pluginEntry, getSite());
	}

	/*
	 * @see ISiteContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(
		ContentReference contentReference,
		IProgressMonitor monitor)
		throws CoreException {
		InputStream inStream = null;
		String featurePath = getFeaturePath();
		String contentKey = contentReference.getIdentifier();
		featurePath += contentKey;

		// error recovery
		if (featurePath.endsWith(Feature.FEATURE_XML)) {
			oldPath=featurePath.replace(File.separatorChar,'/');
			featurePath =
				ErrorRecoveryLog.getLocalRandomIdentifier(featurePath);
			newPath = featurePath;
			ErrorRecoveryLog.getLog().appendPath(
				ErrorRecoveryLog.FEATURE_ENTRY,
				featurePath);
		}

		try {
			inStream = contentReference.getInputStream();
			UpdateManagerUtils.copyToLocal(inStream, featurePath, null);
		} catch (IOException e) {
			throw Utilities.newCoreException(
				Policy.bind("GlobalConsumer.ErrorCreatingFile", featurePath),
				e);
			//$NON-NLS-1$
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {
			}
		}

	}

	/*
	 * @see ISiteContentConsumer#close()
	 */
	public IFeatureReference close() throws CoreException {

		// InternalFeatureReference
		FeatureReference ref = new FeatureReference();
		ref.setSite(getSite());
		File file = null;

		try {
			file = new File(getFeaturePath());
			ref.setURL(file.toURL());
		} catch (MalformedURLException e) {
			throw Utilities.newCoreException(
				Policy.bind(
					"SiteFileContentConsumer.UnableToCreateURLForFile",
					file.getAbsolutePath()),
				e);
			//$NON-NLS-1$
		}

		//rename file back 
		if (newPath!=null){
			ErrorRecoveryLog.getLog().appendPath(ErrorRecoveryLog.RENAME_ENTRY, newPath);
			boolean sucess = false;
			File fileToRename = new File(newPath);
			if (fileToRename.exists()){
				File renamedFile = new File(oldPath);
				if (renamedFile.exists()) {
					UpdateManagerUtils.removeFromFileSystem(renamedFile);
					UpdateManagerPlugin.warn("Removing already existing file:"+oldPath);
				}
				sucess = fileToRename.renameTo(renamedFile);
			}	
			if(!sucess){
				String msg = Policy.bind("ContentConsumer.UnableToRename",newPath,oldPath);
				throw Utilities.newCoreException(msg,new Exception(msg));
			}			
		}
		
		if (ref != null) {
			// FIXME make sure we rename the XML files before
			commitPlugins(ref);
			ref.markReadOnly();
		}

		return ref;
	}

	/*
	 * 
	 */
	public void abort() throws CoreException {
		// FIXME
		String featurePath = getFeaturePath();
		UpdateManagerUtils.removeFromFileSystem(new File(featurePath));
	}

	/*
	 * commit the plugins installed as archive on the site
	 */
	private void commitPlugins(IFeatureReference localFeatureReference)
		throws CoreException {

		// get the feature
		((SiteFile) getSite()).addFeatureReferenceModel(
			(FeatureReferenceModel) localFeatureReference);
		IFeature localFeature=null;			
		try {
			localFeature = localFeatureReference.getFeature();
		} catch (CoreException e) {
			UpdateManagerPlugin.warn(null,e);
			return;
		}

		if (localFeature==null) return;

		// add the installed plugins directories as archives entry
		SiteFileFactory archiveFactory = new SiteFileFactory();
		ArchiveReferenceModel archive = null;
		IPluginEntry[] pluginEntries = localFeature.getPluginEntries();
		for (int i = 0; i < pluginEntries.length; i++) {
			String versionId =
				pluginEntries[i].getVersionedIdentifier().toString();
			String pluginID =
				Site.DEFAULT_PLUGIN_PATH
					+ versionId
					+ FeaturePackagedContentProvider.JAR_EXTENSION;
			archive = archiveFactory.createArchiveReferenceModel();
			archive.setPath(pluginID);
			try {
				URL url =
					new URL(
						getSite().getURL(),
						Site.DEFAULT_PLUGIN_PATH + versionId + File.separator);
				archive.setURLString(url.toExternalForm());
				archive.resolve(url, null);
				((SiteFile) getSite()).addArchiveReferenceModel(archive);
			} catch (MalformedURLException e) {
				String id =
					UpdateManagerPlugin
						.getPlugin()
						.getDescriptor()
						.getUniqueIdentifier();
				String urlString =
					(getSite().getURL() != null)
						? getSite().getURL().toExternalForm()
						: "";
				//$NON-NLS-1$
				urlString += Site.DEFAULT_PLUGIN_PATH
					+ pluginEntries[i].toString();
				throw Utilities.newCoreException(
					Policy.bind("SiteFile.UnableToCreateURL", urlString),
					e);
				//$NON-NLS-1$
			}
		}
		return;
	}

}