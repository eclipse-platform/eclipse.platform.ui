package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * Parse the default feature.xml
 */
public class FeaturePackagedContentProvider  extends FeatureContentProvider {

	private ContentReference localManifest = null;
	private ContentReference[] localFeatureFiles = new ContentReference[0];

	public static final String JAR_EXTENSION = ".jar";

	public static final FilenameFilter filter = new FilenameFilter(){
		 public boolean accept(File dir, String name){
		 	return name.endsWith(FeaturePackagedContentProvider.JAR_EXTENSION);
		 }
	};	
	
	private FeatureContentProvider.ContentSelector contentSelector = new FeatureContentProvider.ContentSelector(){
		/*
		 * 
		 */
		public boolean include(String entry){
			return (!entry.endsWith(File.separator) && !entry.endsWith("/"));
		}
		
		/*
		 *
		 */
		public String defineIdentifier(String entry){
			return 	entry;
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
		String type = (entry.isFragment())?Site.DEFAULT_FRAGMENT_PATH:Site.DEFAULT_PLUGIN_PATH;
		return type+entry.getIdentifier().toString() + JAR_EXTENSION;
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
	public ContentReference getFeatureManifestReference() throws CoreException {

		// check to see if we already have local copy of the manifest
		if (localManifest != null)
			return localManifest;
			
		ContentReference result = null;
		ContentReference[] featureArchiveReference = getFeatureEntryArchiveReferences();		
		try {
			// force feature archive to local. This content provider always assumes exactly 1 archive file (index [0])		
			JarContentReference featureJarReference = (JarContentReference)asLocalReference(featureArchiveReference[0],null);
			
			// we need to unpack archive locally for UI browser references to be resolved correctly
			FeatureContentProvider.ContentSelector sel = new FeatureContentProvider.ContentSelector();
			localFeatureFiles = unpack(featureJarReference, sel, null); // unpack and cache references
			result = null;
			for (int i=0; i<localFeatureFiles.length; i++) {
				// find the manifest in the unpacked feature files
				if (localFeatureFiles[i].getIdentifier().equals(Feature.FEATURE_XML)) {
					result = localFeatureFiles[i];
					localManifest = result; // cache reference to manifest
					break;
				}
			}
			if (result == null)
				throw newCoreException("Error retrieving manifest file in  feature :" + featureArchiveReference[0].getIdentifier(), null);
		} catch (IOException e){
			throw newCoreException("Error retrieving manifest file in  feature :" + featureArchiveReference[0].getIdentifier(), e);
		}
		return result;
	}

	/*
	 * @see IFeatureContentProvider#getArchiveReferences()
	 */
	public ContentReference[] getArchiveReferences() throws CoreException {
		IPluginEntry[] entries = feature.getPluginEntries();
		INonPluginEntry[] nonEntries = feature.getNonPluginEntries();
		List listAllContentRef = new ArrayList();
		ContentReference[] allContentRef = new ContentReference[0];
		
		// feature
		listAllContentRef.addAll(Arrays.asList(getFeatureEntryArchiveReferences()));
		
		// plugins
		for (int i = 0; i < entries.length; i++) {
			listAllContentRef.addAll(Arrays.asList(getPluginEntryArchiveReferences(entries[i])));				
		}
		
		// non plugins
		for (int i = 0; i < nonEntries.length; i++) {
			listAllContentRef.addAll(Arrays.asList(getNonPluginEntryArchiveReferences(nonEntries[i])));				
		}
		
		if (listAllContentRef.size()>0){
			allContentRef = new ContentReference[listAllContentRef.size()];
			listAllContentRef.toArray(allContentRef);
		}
		
		return allContentRef;
	}

	/*
	 * @see IFeatureContentProvider#getFeatureEntryArchiveReferences()
	 */
	public ContentReference[] getFeatureEntryArchiveReferences() throws CoreException {
		//1 jar file <-> 1 feature
		ContentReference[] references = new ContentReference[1]; 		
		try {
				// feature may not be known, 
				// we may be asked for the manifest before the feature is set
				String archiveID = (feature!=null)?contentSelector.defineIdentifier(feature.getVersionIdentifier().toString()):"";				
				ContentReference currentReference = new JarContentReference(archiveID,getURL());
				currentReference = asLocalReference(currentReference,null);
				references[0] = currentReference;
		} catch (IOException e){
			throw newCoreException("Error retrieving feature Entry Archive Reference :" + feature.getURL().toExternalForm(), e);
		}
		return references;
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryArchiveReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryArchiveReferences(IPluginEntry pluginEntry) throws CoreException {
		ContentReference[] references = new ContentReference[1];
		String archiveID = getPluginEntryArchiveID(pluginEntry);
		URL url = feature.getSite().getSiteContentProvider().getArchiveReference(archiveID);
		references[0]= new JarContentReference(archiveID,url);
		return references;
	}

	/*
	 * @see IFeatureContentProvider#getNonPluginEntryArchiveReferences(INonPluginEntry)
	 */
	public ContentReference[] getNonPluginEntryArchiveReferences(INonPluginEntry nonPluginEntry) throws CoreException {
		return null;
	}

	/*
	 * @see IFeatureContentProvider#getFeatureEntryContentReferences()
	 */
	public ContentReference[] getFeatureEntryContentReferences() throws CoreException {
		
		return localFeatureFiles; // return cached feature references
		// Note: assumes this content provider is always called first to
		//       get the feature manifest. This forces the feature files
		//       to be unpacked and caches the references
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryContentReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryContentReferences(IPluginEntry pluginEntry) throws CoreException {
		ContentReference[] references = getPluginEntryArchiveReferences(pluginEntry);
		ContentReference[] pluginReferences = new ContentReference[0];
		try {
			JarContentReference localRef =	(JarContentReference)asLocalReference(references[0],null);
			pluginReferences = peek(localRef,contentSelector,null);
		} catch (IOException e){
			throw newCoreException( "Error retrieving plugin Entry Archive Reference :" + pluginEntry.getIdentifier().toString(), e);			
		}
		return pluginReferences;
	}

	/*
	 * @see IFeatureContentProvider#setFeature(IFeature)
	 */
	public void setFeature(IFeature feature) {
		this.feature = feature;
	}

	private CoreException newCoreException(String s, Throwable e) throws CoreException {
		return new CoreException(new Status(IStatus.ERROR,"org.eclipse.update.core",0,s,e));
	}


}