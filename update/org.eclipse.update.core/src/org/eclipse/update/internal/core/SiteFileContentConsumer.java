package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;

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
 	 * return the path in whichh the Feature will be installed
 	 */
	private String getFeaturePath() throws CoreException {
		String featurePath = null;
		try {
		VersionedIdentifier featureIdentifier = feature.getVersionedIdentifier();
		String path = Site.DEFAULT_INSTALLED_FEATURE_PATH + featureIdentifier.toString() + File.separator;
		URL newURL = new URL(getSite().getURL(),path);
		featurePath = newURL.getFile();
		} catch (MalformedURLException e){
			throw newCoreException(Policy.bind("SiteFileContentConsumer.UnableToCreateURL")+e.getMessage(),e); //$NON-NLS-1$
		}
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
			throw newCoreException(Policy.bind("GlobalConsumer.ErrorCreatingFile") + featurePath, e); //$NON-NLS-1$
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
			throw newCoreException(Policy.bind("SiteFileContentConsumer.UnableToCreateURLForFile")+ file.getAbsolutePath(),e); //$NON-NLS-1$
		}
		return ref;
	}

	/*
	 * 
	 */
	public void abort() {
	}
	
	/**
	 * 
	 */
	private CoreException newCoreException(String s, Throwable e) throws CoreException {
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		return new CoreException(new Status(IStatus.ERROR,id,0,s,e));
	}	
}
