package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.core.Writer;

public class Site extends SiteMapModel implements ISite, IWritable {

	/**
	 * default path under the site where features will be installed
	 */
	public static final String INSTALL_FEATURE_PATH = "install/features/";
	
	/**
	 * default path under the site where plugins will be installed
	 */
	public static final String DEFAULT_PLUGIN_PATH = "plugins/";
	/**
	 * default path under the site where plugins will be installed
	 */
	//FIXME: fragment
	public static final String DEFAULT_FRAGMENT_PATH = "fragments/";

	/**
	 * default path, under site, where featuresConfigured will be installed
	 */
	public static final String DEFAULT_FEATURE_PATH = "features/";

	public static final String SITE_FILE = "site";
	public static final String SITE_XML = SITE_FILE + ".xml";
	private SiteParser parser;

	private ListenersList listeners = new ListenersList();
	/**
	 * The content consumer of the Site
	 */
	private ISiteContentConsumer contentConsumer;

	/**
	 * The content provider of the Site
	 */
	private ISiteContentProvider siteContentProvider;
	
	/**
	 * plugin entries 
	 */
	private List pluginEntries = new ArrayList(0);	

	/**
	 * Constructor for Site
	 */
	public Site() {
		super();
	}

	/**
	 * Saves the site into the site.xml
	 */
	public void save() throws CoreException {
		File file = new File(getURL().getFile() + SITE_XML);
		try {
			PrintWriter fileWriter = new PrintWriter(new FileOutputStream(file));
			Writer writer = new Writer(); 
			writer.writeSite(this, fileWriter);
			fileWriter.close();
		} catch (FileNotFoundException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot save site into " + file.getAbsolutePath(), e);
			throw new CoreException(status);
		}
	}

	/*
	 * @see ISite#addSiteChangedListener(ISiteChangedListener)
	 */
	public void addSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * @see ISite#removeSiteChangedListener(ISiteChangedListener)
	 */
	public void removeSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/*
	 * @see ISite#install(IFeature, IProgressMonitor)
	 */
	public IFeatureReference install(IFeature sourceFeature, IProgressMonitor monitor) throws CoreException {
		// should start Unit Of Work and manage Progress Monitor
		IFeature localFeature = createExecutableFeature(sourceFeature);
		IFeatureReference localFeatureReference = sourceFeature.install(localFeature, monitor);
		this.addFeatureReference(localFeatureReference);

		// notify listeners
		Object[] siteListeners = listeners.getListeners();
		for (int i = 0; i < siteListeners.length; i++) {
			((ISiteChangedListener) siteListeners[i]).featureInstalled(localFeature);
		}
		return localFeatureReference;
	}

	/**
	 * adds a feature reference
	 * @param feature The feature reference to add
	 */
	private void addFeatureReference(IFeatureReference feature) {
		addFeatureReferenceModel((FeatureReferenceModel) feature);
	}

	/*
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor monitor) throws CoreException {

		// remove the feature and the plugins if they are not used and not activated
		// get the plugins from the feature
		IPluginEntry[] pluginsToRemove = SiteManager.getLocalSite().getUnusedPluginEntries(feature);

		//finds the contentReferences for this IPluginEntry
		for (int i = 0; i < pluginsToRemove.length; i++) {
			remove(pluginsToRemove[i], monitor);
		}

		// remove feature reference
		IFeatureReference[] featureReferences = getFeatureReferences();
		if (featureReferences != null) {
			for (int indexRef = 0; indexRef < featureReferences.length; indexRef++) {
				IFeatureReference element = featureReferences[indexRef];
				if (element.getURL().equals(feature.getURL())) {
					removeFeatureReferenceModel((FeatureReferenceModel) element);
					break;
				}
			}
		}

		// notify listeners

		Object[] siteListeners = listeners.getListeners();
		for (int i = 0; i < siteListeners.length; i++) {
			((ISiteChangedListener) siteListeners[i]).featureUninstalled(feature);
		}

	}
	
	private void remove(IPluginEntry p, IProgressMonitor m) {
		// DUMMY METHOD
	}

	/**
	 * 
	 */
	private IFeature createExecutableFeature(IFeature sourceFeature) throws CoreException {
		String executableFeatureType = getDefaultExecutableFeatureType();
		IFeature result = null;
		if (executableFeatureType != null) {
			IFeatureFactory factory = FeatureTypeFactory.getInstance().getFactory(executableFeatureType);
			result = factory.createFeature(this);
			
			// at least set the version identifier to be the same
			((FeatureModel)result).setFeatureIdentifier(sourceFeature.getVersionIdentifier().getIdentifier());
			((FeatureModel)result).setFeatureVersion(sourceFeature.getVersionIdentifier().getVersion().toString());			
		}
		return result;
	}

	/*
	 * @see ISite#getURL()
	 */
	public URL getURL() {
		URL url = null;
		try {
			url =  getSiteContentProvider().getURL();
		} catch (CoreException e){
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
		}
		return url;
	}

	/*
	 * @see ISite#getFeatureReferences()
	 */
	public IFeatureReference[] getFeatureReferences() {
		FeatureReferenceModel[] result = getFeatureReferenceModels();
		if (result.length == 0) 
			return new IFeatureReference[0];
		else
			return (IFeatureReference[]) result;
	}

	/*
	 * @see ISite#getArchives()
	 */
	public IArchiveEntry[] getArchives() {
		ArchiveReferenceModel[] result = getArchiveReferenceModels();
		if (result.length == 0) 
			return new IArchiveEntry[0];
		else
			return (IArchiveEntry[]) result;
	}

	/*
	 * @see ISite#getInfoURL()
	 */
	public URL getInfoURL() {
		URLEntryModel description = getDescriptionModel();
		if (description==null) return null;
		return description.getURL();
	}

	/*
	 * @see ISite#getCategories()
	 */
	public ICategory[] getCategories() {
		SiteCategoryModel[] result = getCategoryModels();
		if (result.length == 0) 
			return new ICategory[0];
		else
			return (ICategory[]) result;
	}

	/*
	 * @see ISite#getCategory(String)
	 */
	public ICategory getCategory(String key) {
		ICategory result = null;
		boolean found = false;
		int length = getCategoryModels().length;

		for (int i = 0; i < length; i++) {
			if (getCategoryModels()[i].getName().equals(key)) {
				result = (ICategory) getCategoryModels()[i];
				found = true;
				break;
			}
		}

		//DEBUG:
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS && !found) {
			UpdateManagerPlugin.getPlugin().debug("Cannot find:" + key + " category in site:" + this.getURL().toExternalForm());
			if (getCategoryModels().length <= 0)
				UpdateManagerPlugin.getPlugin().debug("The Site does not contain any categories.");
		}

		return result;
	}

	/*
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		IPluginEntry[] result = new IPluginEntry[0];
		if (!(pluginEntries==null || pluginEntries.isEmpty())){
			result = new IPluginEntry[pluginEntries.size()];
			pluginEntries.toArray(result);
		}
		return result;
	}

	/*
	 * @see ISite#getContentConsumer(IFeature)
	 */
	public ISiteContentConsumer createSiteContentConsumer(IFeature feature) throws CoreException {
		return null;
	}

	/*
	 * @see ISite#setSiteContentProvider(ISiteContentProvider)
	 */
	public void setSiteContentProvider(ISiteContentProvider siteContentProvider) {
		this.siteContentProvider = siteContentProvider;
	}

	/*
	 * @see ISite#getSiteContentProvider()
	 */
	public ISiteContentProvider getSiteContentProvider() throws CoreException {
		if (siteContentProvider == null) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Content Provider not set for site:" , null);
			throw new CoreException(status);
		}
		return siteContentProvider;
	}


	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {
	}

	/*
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		return getPluginEntries().length;
	}

	/*
	 * @see IPluginContainer#getDownloadSize(IPluginEntry)
	 */
	public long getDownloadSize(IPluginEntry entry) {
		return 0;
	}

	/*
	 * @see IPluginContainer#getInstallSize(IPluginEntry)
	 */
	public long getInstallSize(IPluginEntry entry) {
		return 0;
	}

	/*
	 * @see IPluginContainer#addPluginEntry(IPluginEntry)
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
		pluginEntries.add(pluginEntry);
	}

	/*
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream, IProgressMonitor)
	 */
	public void store(IPluginEntry entry, String name, InputStream inStream, IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 * @see ISite#getDefaultExecutableFeatureType()
	 */
	public String getDefaultExecutableFeatureType() {
		return null;
	}

	/*
	 * @see ISite#getDefaultInstallableFeatureType()
	 */
	public String getDefaultInstallableFeatureType() {
		String pluginID = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier()+".";
		return pluginID+IFeatureFactory.INSTALLABLE_FEATURE_TYPE;
	}

}