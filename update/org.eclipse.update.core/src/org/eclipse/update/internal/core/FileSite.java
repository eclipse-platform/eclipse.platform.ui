package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;

/**
 * Site on the File System
 */
public class FileSite extends URLSite {

	private String path;
	public static final String INSTALL_FEATURE_PATH = "install/features/";

	/**
	 * Constructor for FileSite
	 */
	public FileSite(URL siteReference) {
		super(siteReference);
	}

	/**
	 * @see AbstractSite#createExecutableFeature(IFeature)
	 */
	public AbstractFeature createExecutableFeature(IFeature sourceFeature) throws CoreException {
		return new DefaultExecutableFeature(sourceFeature, this);
	}

	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(IPluginEntry pluginEntry,String contentKey,InputStream inStream) throws CoreException {

   		String pluginPath =	getURL().getPath() + DEFAULT_PLUGIN_PATH + pluginEntry.getIdentifier().toString();
   		pluginPath += pluginPath.endsWith(File.separator)?contentKey:File.separator+contentKey;
		try {
			UpdateManagerUtils.copyToLocal(inStream, pluginPath);
		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,id,IStatus.OK,"Error creating file:"+pluginPath,e);
			throw new CoreException(status);
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * store Feature files
	 */
	public void storeFeatureInfo(	VersionedIdentifier featureIdentifier, String contentKey,InputStream inStream) throws CoreException {

		String featurePath = getURL().getPath() + INSTALL_FEATURE_PATH + featureIdentifier.toString();
   		featurePath += featurePath.endsWith(File.separator)?contentKey:File.separator+contentKey;			
		try {
			UpdateManagerUtils.copyToLocal(inStream, featurePath);
		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,id,IStatus.OK,"Error creating file:"+featurePath,e);
			throw new CoreException(status);
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {
			}
		}

	}
	
	/*
	 * @see AbstractSite#getDefaultFeature(URL)
	 */
	public IFeature getDefaultFeature(URL featureURL) {
		return new DefaultExecutableFeature(featureURL,this);
	}

	/**
	 * We do not need to optimize the download
	 * As the archives are already available on the file system
	 */
	public boolean optimize(){
		return false;
	}

}