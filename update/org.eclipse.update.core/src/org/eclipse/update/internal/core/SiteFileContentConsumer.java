package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;

import java.net.MalformedURLException;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.FeatureModel;

/**
 * Site on the File System
 */
public class SiteFileContentConsumer extends SiteContentConsumer {

	private String path;
	private IFeature feature;

	/**
	 * Constructor for FileSite
	 */
	public SiteFileContentConsumer(IFeature feature){
		this.feature = feature;
	}

	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(IPluginEntry pluginEntry, String contentKey, InputStream inStream) throws CoreException {

		String path = UpdateManagerUtils.getPath(getSite().getURL());

		// FIXME: fragment code
		String pluginPath = null;
		if (pluginEntry.isFragment()) {
			pluginPath = path + Site.DEFAULT_FRAGMENT_PATH + pluginEntry.getIdentifier().toString();
		} else {
			pluginPath = path + Site.DEFAULT_PLUGIN_PATH + pluginEntry.getIdentifier().toString();
		}
		pluginPath += pluginPath.endsWith(File.separator) ? contentKey : File.separator + contentKey;

		try {
			UpdateManagerUtils.copyToLocal(inStream, pluginPath, null);

		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating file:" + pluginPath, e);
			throw new CoreException(status);
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {}
		}
	}

	/**
 	 * return tehepath in whichh the Feature will be installed
 	 */
	private String getFeaturePath() {
		VersionedIdentifier featureIdentifier = feature.getVersionIdentifier();
		String path = UpdateManagerUtils.getPath(getSite().getURL());
		String featurePath = path + Site.INSTALL_FEATURE_PATH + featureIdentifier.toString() + File.separator;
		return featurePath;
	}
	
	/*
	 * @see ISiteContentConsumer#open(INonPluginEntry)
	 */
	public IContentConsumer open(INonPluginEntry nonPluginEntry) throws CoreException {
		return new SiteFileNonPluginContentConsumer(getFeaturePath());
	}

	/*
	 * @see ISiteContentConsumer#open(IPluginEntry)
	 */
	public IContentConsumer open(IPluginEntry pluginEntry) throws CoreException {
		return new SiteFilePluginContentConsumer(pluginEntry,getSite());
	}

	/*
	 * @see ISiteContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(ContentReference contentReference, IProgressMonitor monitor) throws CoreException {
		InputStream inStream = null;
		String featurePath = getFeaturePath();
		String contentKey = contentReference.getIdentifier();
		featurePath += contentKey ;
		try {
			inStream = contentReference.getInputStream();
			UpdateManagerUtils.copyToLocal(inStream, featurePath, null);
		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating file:" + featurePath, e);
			throw new CoreException(status);
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {}
		}
		
	}

	/*
	 * @see ISiteContentConsumer#close()
	 */
	public IFeatureReference close() throws CoreException {
				
		FeatureReference ref = new FeatureReference();
		ref.setSite(getSite());
		File file = null;
		try {
			file = new File(getFeaturePath());
			ref.setURL(file.toURL());
		} catch (MalformedURLException e){
			throw newCoreException("Cannot create URL on File:"+ file.getAbsolutePath(),e);
		}
		return ref;
	}

	/*
	 * @see ISiteContentConsumer#abort()
	 */
	public void abort() {
	}
	
	private CoreException newCoreException(String s, Throwable e) throws CoreException {
		return new CoreException(new Status(IStatus.ERROR,"org.eclipse.update.core",0,s,e));
	}	
}
