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
public class Site extends SiteMapModel implements ISite{


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
	 * @see ISite#addSiteChangedListener(IConfiguredSiteChangedListener)
	 */

	/*
	 * @see ISite#removeSiteChangedListener(IConfiguredSiteChangedListener)
	 */

	/*
	 * @see ISite#install(IFeature, IProgressMonitor)
	 */
	public IFeatureReference install(IFeature sourceFeature, IProgressMonitor progress) throws CoreException {

		if (sourceFeature==null) return null;

		// make sure we have an InstallMonitor		
		InstallMonitor monitor;
		if (progress == null)
			monitor = null;
		else if (progress instanceof InstallMonitor)
			monitor = (InstallMonitor) progress;
		else
			monitor = new InstallMonitor(progress);

		// create new executable feature and install source content into it
		IFeature localFeature = createExecutableFeature(sourceFeature);
		IFeatureReference localFeatureReference = sourceFeature.install(localFeature, monitor);
		if (localFeature instanceof FeatureModel)
			 ((FeatureModel) localFeature).markReadOnly();
		this.addFeatureReference(localFeatureReference);

	
		return localFeatureReference;
	}

	/*
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor progress) throws CoreException {

		// make sure we have an InstallMonitor		
		InstallMonitor monitor;
		if (progress == null)
			monitor = null;
		else if (progress instanceof InstallMonitor)
			monitor = (InstallMonitor) progress;
		else
			monitor = new InstallMonitor(progress);

		// remove the feature and the plugins if they are not used and not activated
		// get the plugins from the feature
		IPluginEntry[] pluginsToRemove = getPluginEntriesOnlyReferencedBy(feature);

		//finds the contentReferences for this IPluginEntry
		for (int i = 0; i < pluginsToRemove.length; i++) {
			remove(feature, pluginsToRemove[i], monitor);
		}

		// remove the feature content
		ContentReference[] references = feature.getFeatureContentProvider().getFeatureEntryArchiveReferences(monitor);
		for (int i = 0; i < references.length; i++) {
			try {
				UpdateManagerUtils.removeFromFileSystem(references[i].asFile());
			} catch (IOException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("Site.CannotRemoveFeature", feature.getVersionedIdentifier().getIdentifier(), getURL().toExternalForm()), e); //$NON-NLS-1$
				throw new CoreException(status);
			}
		}

		// remove feature reference from the site
		IFeatureReference[] featureReferences = getFeatureReferences();
		if (featureReferences != null) {
			for (int indexRef = 0; indexRef < featureReferences.length; indexRef++) {
				IFeatureReference element = featureReferences[indexRef];
				if (element.equals(feature)) {
					removeFeatureReferenceModel((FeatureReferenceModel) element);
					break;
				}
			}
		}


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

	/**
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		IPluginEntry[] result = new IPluginEntry[0];
		if (!(pluginEntries == null || pluginEntries.isEmpty())) {
			result = new IPluginEntry[pluginEntries.size()];
			pluginEntries.toArray(result);
		}
		return result;
	}

	/**
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		return getPluginEntries().length;
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
		pluginEntries.add(pluginEntry);
	}
		
	/**
	 * returns the download size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the download size is unknown.
	 * 
	 * @see ISite#getDownloadSize(IFeature)
	 * 
	 */
	public long getDownloadSizeFor(IFeature feature) {
		long result = 0;
		IPluginEntry[] entriesToInstall = feature.getPluginEntries();
		IPluginEntry[] siteEntries = this.getPluginEntries();
		entriesToInstall = UpdateManagerUtils.intersection(entriesToInstall, siteEntries);

		// FIXME Intersection for NonPluginEntry (using Install Handler)
		try {
			result = feature.getFeatureContentProvider().getDownloadSizeFor(entriesToInstall, /* non plugin entry []*/
			null);
		} catch (CoreException e) {
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
			result = ContentEntryModel.UNKNOWN_SIZE;
		}
		return result;
	}

	/**
	 * returns the download size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the download size is unknown.
	 * 
	 * @see ISite#getDownloadSizeFor(IFeature)
	 * 
	 */
	public long getInstallSizeFor(IFeature feature) {
		long result = 0;
		IPluginEntry[] entriesToInstall = feature.getPluginEntries();
		IPluginEntry[] siteEntries = this.getPluginEntries();
		entriesToInstall = UpdateManagerUtils.intersection(entriesToInstall, siteEntries);

		// FIXME Intersection for NonPluginEntry (using Install Handler)
		try {
			result = feature.getFeatureContentProvider().getInstallSizeFor(entriesToInstall, /* non plugin entry []*/
			null);
		} catch (CoreException e) {
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
			result = ContentEntryModel.UNKNOWN_SIZE;
		}

		return result;
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
	 * adds a feature reference
	 * @param feature The feature reference to add
	 */
	private void addFeatureReference(IFeatureReference feature) {
		addFeatureReferenceModel((FeatureReferenceModel) feature);
	}

	/**
	 * 
	 */
	private void remove(IFeature feature, IPluginEntry pluginEntry, InstallMonitor monitor) throws CoreException {

		if (pluginEntry == null)
			return;

		ContentReference[] references = feature.getFeatureContentProvider().getPluginEntryArchiveReferences(pluginEntry, monitor);
		for (int i = 0; i < references.length; i++) {
			try {
				UpdateManagerUtils.removeFromFileSystem(references[i].asFile());
			} catch (IOException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("Site.CannotRemovePlugin", pluginEntry.getVersionedIdentifier().toString(), getURL().toExternalForm()), e); //$NON-NLS-1$
				throw new CoreException(status);
			}
		}
	}

	/**
	 * 
	 */
	private IFeature createExecutableFeature(IFeature sourceFeature) throws CoreException {
		IFeature result = null;
		IFeatureFactory factory = FeatureTypeFactory.getInstance().getFactory(DEFAULT_INSTALLED_FEATURE_TYPE);
		result = factory.createFeature(/*URL*/null, this);

		// at least set the version identifier to be the same
		((FeatureModel) result).setFeatureIdentifier(sourceFeature.getVersionedIdentifier().getIdentifier());
		((FeatureModel) result).setFeatureVersion(sourceFeature.getVersionedIdentifier().getVersion().toString());
		return result;
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
								allPluginID.add(entries[indexEntries].getVersionedIdentifier());
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