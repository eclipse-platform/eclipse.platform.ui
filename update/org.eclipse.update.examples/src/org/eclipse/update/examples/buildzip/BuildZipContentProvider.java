package org.eclipse.update.examples.buildzip;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.ContentReference;
import org.eclipse.update.core.FeatureContentProvider;
import org.eclipse.update.core.IFeatureContentProvider;
import org.eclipse.update.core.INonPluginEntry;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.JarContentReference;
import org.eclipse.update.core.FeatureContentProvider.ContentSelector;
import org.eclipse.update.core.model.DefaultModelWriter;
import org.eclipse.update.core.model.FeatureModel;
import org.eclipse.update.core.model.PluginEntryModel;

/**
 * An example feature content provider. It handles features packaged as
 * build zip's using the format used for integration and stable builds
 * posted on the downloads pages at www.eclipse.org
 * </p>
 * @since 2.0
 */

public class BuildZipContentProvider extends FeatureContentProvider implements IFeatureContentProvider {

	private JarContentReference baseReference;
	private ContentReference generatedFeatureManifest;	
	private ContentReference[] featureEntryContentReferences;
	private IPluginEntry currentPluginEntry;
	private INonPluginEntry currentNonPluginEntry;
	
	public BuildZipContentProvider(URL base) {
		super(base);
		this.baseReference = new JarContentReference("build.zip",base);
	}
	
	/*
	 * @see IFeatureContentProvider#getFeatureManifest()
	 */
	public ContentReference getFeatureManifestReference() throws CoreException {
		if (generatedFeatureManifest == null) {
			throw newCoreException("Feature manifest is not available",null);
		}
		else
			return generatedFeatureManifest;
	}

	/*
	 * @see IFeatureContentProvider#getArchiveReferences()
	 */
	public ContentReference[] getArchiveReferences() {
		// the feature and all its plugins files are packaged in a single archive
		return new ContentReference[] { baseReference };
	}

	/*
	 * @see IFeatureContentProvider#getFeatureEntryArchiveReferences()
	 */
	public ContentReference[] getFeatureEntryArchiveReferences() {
		// the feature and all its plugins files are packaged in a single archive
		return new ContentReference[] { baseReference };
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryArchiveReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryArchiveReferences(IPluginEntry pluginEntry) {
		// the feature and all its plugins files are packaged in a single archive
		return new ContentReference[] { baseReference };
	}

	/*
	 * @see IFeatureContentProvider#getNonPluginEntryArchiveReferences(INonPluginEntry)
	 */
	public ContentReference[] getNonPluginEntryArchiveReferences(INonPluginEntry nonPluginEntry) 
		throws CoreException {
		try {
			return peekNonPluginEntryContent(nonPluginEntry);
		} catch(IOException e) {
			throw newCoreException("Unable to return content for non plugin entry "+nonPluginEntry.getIdentifier(),e);
		}
	}

	/*
	 * @see IFeatureContentProvider#getFeatureEntryContentReferences()
	 */
	public ContentReference[] getFeatureEntryContentReferences() {
		if (featureEntryContentReferences == null)
			return new ContentReference[0];
		else
			return  featureEntryContentReferences;
	}

	/*
	 * @see IFeatureContentProvider#getPluginEntryContentReferences(IPluginEntry)
	 */
	public ContentReference[] getPluginEntryContentReferences(IPluginEntry pluginEntry)
		throws CoreException {
		try {
			return peekPluginEntryContent(pluginEntry);
		} catch(IOException e) {
			throw newCoreException("Unable to return content for plugin entry "+pluginEntry.getIdentifier(),e);
		}
	}

	ContentReference getFeatureBuildManifest() throws Exception {
		return peek(baseReference,"eclipse/buildmanifest.properties",null/*ContentSelector*/, null/*ProgressMonitor*/);
	}

	ContentReference getPluginManifest(String pluginId, boolean isFragment) throws Exception {			
		String manifestName = "eclipse/plugins/" + pluginId + "/" + (isFragment ? "fragment.xml" : "plugin.xml");
		return peek(baseReference,manifestName, null/*ContentSelector*/, null/*ProgressMonitor*/);
	}
	
	void unpackFeatureEntryContent(FeatureModel feature) throws IOException {
		
		// define selector for feature entry files
		ContentSelector selector = new ContentSelector() {
			public boolean include(String entry) {
				if (entry.startsWith("eclipse/readme/"))
					return true;
				else if (entry.startsWith("eclipse/splash/"))
					return true;
				else if (entry.startsWith("eclipse/") && entry.indexOf("/",8)==-1 && entry.endsWith(".html"))
					return true;
				else
					return false;	
			}
			public String defineIdentifier(String entry) {
				if (entry.startsWith("eclipse/"))
					return entry.substring(8);
				else
					return entry;
			}
		};
		
		// unpack feature entry files
		ContentReference[] refs = unpack(baseReference, selector, null/*ProgressMonitor*/);
		
		// write out feature manifest (feature.xml);
		File manifest = createLocalFile(null/*key*/,"feature.xml");
		ContentReference manifestReference = new ContentReference("feature.xml", manifest);
		DefaultModelWriter w = new DefaultModelWriter(feature);
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(manifest);
			w.writeFeatureManifest(os);
		} finally {
			if (os != null) try { os.close(); } catch(IOException e) {}
		}
		this.generatedFeatureManifest = manifestReference;
		
		// save references (newly-written feature.xml plus rest of selected feature files)
		ContentReference[] allRefs = new ContentReference[refs.length+1];
		allRefs[0] = manifestReference;
		System.arraycopy(refs,0,allRefs,1,refs.length);
		featureEntryContentReferences = allRefs;
	}
		
	ContentReference[] peekPluginEntryContent(IPluginEntry plugin) throws IOException {
		
		// define selector for plugin entry files
		ContentSelector selector = new ContentSelector() {
			public boolean include(String entry) {
				String id = currentPluginEntry.getIdentifier().getIdentifier();
				if (id==null)
					return false;
				else if (entry.startsWith("eclipse/plugins/"+id+"/"))
					return true;
				else
					return false;	
			}
			public String defineIdentifier(String entry) {
				int ix = entry.indexOf("/",16);
				if (ix != -1) {
					String rest = entry.substring(ix);
					return "plugins/"+currentPluginEntry.getIdentifier().toString()+rest;
				} else {
					return entry;
				}
			}
		};
		
		// unpack plugin entry files
		currentPluginEntry = plugin;
		return peek(baseReference, selector, null/*ProgressMonitor*/);
	}
		
	ContentReference[] peekNonPluginEntryContent(INonPluginEntry data) throws IOException {
		
		// define selector for non plugin entry files
		ContentSelector selector = new ContentSelector() {
			public boolean include(String entry) {
				String id = currentNonPluginEntry.getIdentifier();
				if (!id.equals("root"))
					return false;
				else if (!entry.startsWith("eclipse/plugins/"))
					return true;
				else
					return false;	
			}
			public String defineIdentifier(String entry) {
				if (entry.startsWith("eclipse/"))
					return entry.substring(8);
				else
					return entry;
			}
		};
		
		// unpack non plugin entry files
		currentNonPluginEntry = data;
		return peek(baseReference, selector, null/*ProgressMonitor*/);
	}
	
	private CoreException newCoreException(String s, Throwable e) throws CoreException {
		return new CoreException(new Status(IStatus.ERROR,"org.eclipse.update.examples.buildzip",0,s,e));
	}
}
