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

	private JarFile currentOpenJarFile = null;

	private URL rootURL;

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

		ContentReference result = null;
		ContentReference[] featureContentReference =getFeatureEntryArchiveReferences();		
		try {			
			JarContentReference localContentReference = (JarContentReference)asLocalReference(featureContentReference[0],null);
			result = peek(localContentReference,Feature.FEATURE_XML,contentSelector,null);
		} catch (IOException e){
			throw  newCoreException("Error retrieving manifest file in  feature :" + featureContentReference[0].getIdentifier(), e);
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
		
		ContentReference[] references = new ContentReference[0];		
		try {
			ContentReference result = null;
			ContentReference[] featureContentReference = getFeatureEntryArchiveReferences();		
			references = peek((JarContentReference)featureContentReference[0],contentSelector,null);
		} catch (IOException e){
			throw newCoreException( "Error retrieving feature Entry Archive Reference :" + feature.getURL().toExternalForm(), e);
		}	
		return references;
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