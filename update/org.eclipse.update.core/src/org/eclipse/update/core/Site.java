package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.net.URL;
import java.util.*;
import org.eclipse.core.internal.boot.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;


/**
 * 
 */
public class Site extends SiteModel implements ISite{


	/** 
	 * 
	 */
	private static final String PACKAGED_FEATURE_TYPE = "packaged"; //$NON-NLS-1$
	private static final String INSTALLED_FEATURE_TYPE = "installed";	 //$NON-NLS-1$	

	
	/**
	 * default path under the site where features will be installed
	 */
	public static final String DEFAULT_INSTALLED_FEATURE_PATH = "install/features/"; //$NON-NLS-1$

	/**
	 * default path under the site where plugins will be installed
	 */
	public static final String DEFAULT_PLUGIN_PATH = "plugins/"; //$NON-NLS-1$
	/**
	 * default path under the site where plugins will be installed
	 */
	//FIXME: fragment
	public static final String DEFAULT_FRAGMENT_PATH = "fragments/"; //$NON-NLS-1$

	/**
	 * default path, under site, where featuresConfigured will be installed
	 */
	public static final String DEFAULT_FEATURE_PATH = "features/"; //$NON-NLS-1$

	/**
	 * 
	 */
	public static final String SITE_FILE = "site"; //$NON-NLS-1$
	
	/**
	 * 
	 */
	public static final String SITE_XML = SITE_FILE + ".xml"; //$NON-NLS-1$

	
	/**
	 * The content provider of the Site
	 */
	private ISiteContentProvider siteContentProvider;

	
	/**
	 * Constructor for Site
	 */
	public Site() {
		super();
	}

	/**
	 * @see Object
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ISite))
			return false;
		if (getURL() == null)
			return false;
		ISite otherSite = (ISite) obj;

		return getURL().equals(otherSite.getURL());
	}
	
	/*
	 * @see ISite#install(IFeature,IVerifier, IProgressMonitor)
	 */
	public IFeatureReference install(IFeature sourceFeature,IVerificationListener verificationListener, IProgressMonitor progress) throws CoreException {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor progress) throws CoreException {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see ISite#getURL()
	 */
	public URL getURL() {
		URL url = null;
		try {
			url = getSiteContentProvider().getURL();
		} catch (CoreException e) {
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
	public IArchiveReference[] getArchives() {
		ArchiveReferenceModel[] result = getArchiveReferenceModels();
		if (result.length == 0)
			return new IArchiveReference[0];
		else
			return (IArchiveReference[]) result;
	}
	
	
	/**
	 * @see ISite#getDefaultInstallableFeatureType()
	 */
	public String getDefaultPackagedFeatureType() {
		return DEFAULT_PACKAGED_FEATURE_TYPE;
	}


	/*
	 * @see ISite#getInfoURL()
	 */
	public URL getInfoURL() {
		URLEntryModel description = getDescriptionModel();
		if (description == null)
			return null;
		return description.getURL();
	}

	/*
	 * @see ISite#getCategories()
	 */
	public ICategory[] getCategories() {
		CategoryModel[] result = getCategoryModels();
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
			UpdateManagerPlugin.getPlugin().debug(Policy.bind("Site.CannotFindCategory", key, this.getURL().toExternalForm())); //$NON-NLS-1$ //$NON-NLS-2$
			if (getCategoryModels().length <= 0)
				UpdateManagerPlugin.getPlugin().debug(Policy.bind("Site.NoCategories")); //$NON-NLS-1$
		}

		return result;
	}

	/*
	 * @see IPluginContainer#getPluginEntries()
	 */


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
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("Site.NoContentProvider"), null); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return siteContentProvider;
	}

	/*
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		throw new UnsupportedOperationException();
	}	
	
	
	/**
	 * Adds a plugin entry 
	 * Either from parsing the file system or 
	 * installing a feature
	 * 
	 * We cannot figure out the list of plugins by reading the Site.xml as
	 * the archives tag are optionals
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
		throw new UnsupportedOperationException();
	}
		
	/* 
	 * @see ISite#getDownloadSizeFor(IFeature)
	 */
	public long getDownloadSizeFor(IFeature feature) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see ISite#getInstallSizeFor(IFeature)
	 */
	public long getInstallSizeFor(IFeature feature) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 * @see ISite#getFeatureReference(IFeature)
	 */
	public IFeatureReference getFeatureReference(IFeature feature) {
		IFeatureReference result = null;
		IFeatureReference[] references = getFeatureReferences();
		boolean found = false;
		for (int i = 0; i < references.length && !found; i++) {
			if (references[i].getURL().equals(feature.getURL())) {
				result = references[i];
				found = true;
			}
		}
		return result;
	}

	/*
	 * @see ISite#getDescription()
	 */
	public IURLEntry getDescription() {
		return (IURLEntry) getDescriptionModel();
	}


	/**
	 * returns a list of PluginEntries that are not used by any other configured feature
	 */
	public IPluginEntry[] getPluginEntriesOnlyReferencedBy(IFeature feature) throws CoreException {

		IPluginEntry[] pluginsToRemove = new IPluginEntry[0];

		// get the plugins from the feature
		IPluginEntry[] entries = feature.getPluginEntries();
		if (entries != null) {
			// get all the other plugins from all the other features
			Set allPluginID = new HashSet();
			IFeatureReference[] features = getFeatureReferences();
			if (features != null) {
				for (int indexFeatures = 0; indexFeatures < features.length; indexFeatures++) {
					if (!features[indexFeatures].equals(feature)) {
						IPluginEntry[] pluginEntries = features[indexFeatures].getFeature().getPluginEntries();
						if (pluginEntries != null) {
							for (int indexEntries = 0; indexEntries < pluginEntries.length; indexEntries++) {
								allPluginID.add(pluginEntries[indexEntries].getVersionedIdentifier());
							}
						}
					}
				}
			}

			// create the delta with the plugins that may be still used by other configured or unconfigured feature
			List plugins = new ArrayList();
			for (int indexPlugins = 0; indexPlugins < entries.length; indexPlugins++) {
				if (!allPluginID.contains(entries[indexPlugins].getVersionedIdentifier())) {
					plugins.add(entries[indexPlugins]);
				}
			}

			// move List into Array
			if (!plugins.isEmpty()) {
				pluginsToRemove = new IPluginEntry[plugins.size()];
				plugins.toArray(pluginsToRemove);
			}

		}

		return pluginsToRemove;
	}

}