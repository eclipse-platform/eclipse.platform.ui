package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.UpdateManagerPlugin;
/**
 * Abstract Class that implements most of the behavior of a feature
 * A feature ALWAYS belongs to an ISite
 */
public class Feature extends FeatureModel implements IFeature {

	/**
	 * Delegating wrapper for IProgressMonitor used for feature
	 * installation handling.
	 * 
	 * NOTE: currently is just a straight delegating wrapper.
	 *       Extended handling function TBA 
	 * 
	 * @since 2.0
	 */
	public static class ProgressMonitor implements IProgressMonitor {

		private IProgressMonitor monitor;

		public ProgressMonitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}
		/*
		 * @see IProgressMonitor#beginTask(String, int)
		 */
		public void beginTask(String name, int totalWork) {
			monitor.beginTask(name, totalWork);
		}

		/*
		* @see IProgressMonitor#done()
		*/
		public void done() {
			monitor.done();
		}

		/*
		 * @see IProgressMonitor#internalWorked(double)
		 */
		public void internalWorked(double work) {
			monitor.internalWorked(work);
		}

		/*
		 * @see IProgressMonitor#isCanceled()
		 */
		public boolean isCanceled() {
			return monitor.isCanceled();
		}

		/*
		 * @see IProgressMonitor#setCanceled(boolean)
		 */
		public void setCanceled(boolean value) {
			monitor.setCanceled(value);
		}

		/*
		 * @see IProgressMonitor#setTaskName(String)
		 */
		public void setTaskName(String name) {
			monitor.setTaskName(name);
		}

		/*
		 * @see IProgressMonitor#subTask(String)
		 */
		public void subTask(String name) {
			monitor.subTask(name);
		}

		/*
		 * @see IProgressMonitor#worked(int)
		 */
		public void worked(int work) {
			monitor.worked(work);
		}
	}

	/**
	 * 
	 */
	public static CoreException CANCEL_EXCEPTION;

	/**
	 * 
	 */
	public static final String FEATURE_FILE = "feature";

	/**
	 * 
	 */
	public static final String FEATURE_XML = FEATURE_FILE + ".xml";

	/**
	 * Site in which teh feature resides
	 */
	private ISite site;

	/**
	 * The content provider of the DefaultFeature
	 */
	private IFeatureContentProvider featureContentProvider;

	/**
	 * The content consumer of the DefaultFeature
	 */
	private IFeatureContentConsumer contentConsumer;

	/**
	 * Static block to initialize the possible CANCEL ERROR
	 * thrown when the USER cancels teh operation
	 */
	static {
		//	in case we throw a cancel exception
		String pluginId = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		IStatus cancelStatus = new Status(IStatus.ERROR, pluginId, IStatus.OK, "Install has been Cancelled", null);
		//		IStatus cancelStatus = new Status(IStatus.ERROR, "org.eclipse.update", IStatus.OK, "Install has been Cancelled", null);
		CANCEL_EXCEPTION = new CoreException(cancelStatus);
	}

	/**
	 * Constructor
	 */
	public Feature() {
	}

	/*
	 * @see IFeature#getIdentifier()
	 */
	public VersionedIdentifier getVersionIdentifier() {
		return new VersionedIdentifier(getFeatureIdentifier(), getFeatureVersion());
	}

	/*
	 * @see IFeature#getSite()
	 */
	public ISite getSite() {
		return site;
	}

	/*
	 * @see IFeature#getURL()
	 */
	public URL getURL() {
		IFeatureContentProvider contentProvider = null;
		try {
			contentProvider = getFeatureContentProvider();
		} catch (CoreException e) {
		}
		return (contentProvider != null) ? contentProvider.getURL() : null;
	}

	/*
	 * @see IFeature#getUpdateSiteEntry()
	 */
	public IURLEntry getUpdateSiteEntry() {
		return (IURLEntry) getUpdateSiteEntryModel();
	}

	/*
	 * @see IFeature#getDiscoverySiteEntries()
	 */
	public IURLEntry[] getDiscoverySiteEntries() {
		URLEntryModel[] result = getDiscoverySiteEntryModels();
		if (result.length == 0)
			return new IURLEntry[0];
		else
			return (IURLEntry[]) result;
	}

	/*
	 * @see IFeature#getDescription()
	 */
	public IURLEntry getDescription() {
		return (IURLEntry) getDescriptionModel();
	}

	/*
	 * @see IFeature#getCopyright()
	 */
	public IURLEntry getCopyright() {
		return (IURLEntry) getCopyrightModel();
	}

	/*
	 * @see IFeature#getLicense()
	 */
	public IURLEntry getLicense() {
		return (IURLEntry) getLicenseModel();
	}

	/*
	 * @see IFeature#getImage()
	 */
	public URL getImage() {
		return getImageURL();
	}
	/**
	 * Sets the site
	 * @param site The site to set
	 */
	public void setSite(ISite site) throws CoreException {
		if (this.site != null) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			String featureURLString = (getURL() != null) ? getURL().toExternalForm() : "";
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Site already set for the feature " + featureURLString, null);
			throw new CoreException(status);
		}
		this.site = site;
	}

	/**
	 * @see IPluginContainer#getDownloadSize(IPluginEntry)
	 */
	public long getDownloadSize(IPluginEntry entry) {
		return entry.getDownloadSize();
	}

	/**
	 * @see IPluginContainer#getInstallSize(IPluginEntry)
	 */
	public long getInstallSize(IPluginEntry entry) {
		return entry.getInstallSize();
	}
	/**
	 * returns the download size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the download size is unknown.
	 * 
	 * @see IFeature#getDownloadSize(ISite)
	 * 
	 */
	public long getDownloadSize(ISite site) throws CoreException {
		long result = 0;
		IPluginEntry[] entriesToInstall = this.getPluginEntries();
		if (site != null) {
			IPluginEntry[] siteEntries = site.getPluginEntries();
			entriesToInstall = intersection(entriesToInstall, siteEntries);
		}

		if (entriesToInstall == null || entriesToInstall.length == 0) {
			result = -1;
		} else {
			long pluginSize = 0;
			int i = 0;
			while (i < entriesToInstall.length && pluginSize != -1) {
				pluginSize = getDownloadSize(entriesToInstall[i]);
				result = pluginSize == -1 ? -1 : result + pluginSize;
				i++;
			}
		}
		return result;
	}
	/**
	 * returns the install size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the install size is unknown.
	 * 
	 * @see IFeature#getInstallSize(ISite)
	 */
	public long getInstallSize(ISite site) throws CoreException {
		long result = 0;
		IPluginEntry[] entriesToInstall = this.getPluginEntries();
		if (site != null) {
			IPluginEntry[] siteEntries = site.getPluginEntries();
			entriesToInstall = intersection(entriesToInstall, siteEntries);
		}
		if (entriesToInstall == null || entriesToInstall.length == 0) {
			result = -1;
		} else {
			long pluginSize = 0;
			int i = 0;
			while (i < entriesToInstall.length && pluginSize != -1) {
				pluginSize = getInstallSize(entriesToInstall[i]);
				result = pluginSize == -1 ? -1 : result + pluginSize;
				i++;
			}
		}
		return result;
	}
	/*
	 * @see IFeature#isExecutable()
	 */
	public boolean isExecutable() {
		return false;
	}
	/*
	 * @see IFeature#isInstallable()
	 */
	public boolean isInstallable() {
		return false;
	}

	/*
	 * @see IFeature#install(IFeature, IProgressMonitor) throws CoreException
	 */
	public IFeatureReference install(IFeature targetFeature, IProgressMonitor monitor) throws CoreException {

		//
		IFeatureContentConsumer consumer = targetFeature.getContentConsumer();

		try {
			//finds the contentReferences for this IFeature
			ContentReference[] references = getFeatureContentProvider().getFeatureEntryContentReferences();
			for (int i = 0; i < references.length; i++) {
				consumer.store(references[i], monitor);
			}

			// determine list of plugins to install
			// find the intersection between the two arrays of IPluginEntry...
			// The one teh site contains and teh one the feature contains
			IPluginEntry[] sourceFeaturePluginEntries = getPluginEntries();
			ISite targetSite = targetFeature.getSite();
			IPluginEntry[] targetSitePluginEntries = (targetSite != null) ? site.getPluginEntries() : new IPluginEntry[0];
			IPluginEntry[] pluginsToInstall = intersection(sourceFeaturePluginEntries, targetSitePluginEntries);

			//finds the contentReferences for this IPluginEntry
			for (int i = 0; i < pluginsToInstall.length; i++) {
				IContentConsumer pluginConsumer = consumer.open(pluginsToInstall[i]);
				references = getFeatureContentProvider().getPluginEntryContentReferences(pluginsToInstall[i]);
				for (int j = 0; j < references.length; j++) {
					pluginConsumer.store(references[j], monitor);
				}
				pluginConsumer.close();
			}

			// download and install non plugins bundles
			INonPluginEntry[] nonPluginsContentReferencesToInstall = getNonPluginEntries();
			for (int i = 0; i < nonPluginsContentReferencesToInstall.length; i++) {
				IContentConsumer nonPluginConsumer = consumer.open(nonPluginsContentReferencesToInstall[i]);
				references = getFeatureContentProvider().getNonPluginEntryArchiveReferences(nonPluginsContentReferencesToInstall[i]);
				for (int j = 0; j < references.length; j++) {
					nonPluginConsumer.store(references[j], monitor);
				}
				nonPluginConsumer.close();
			}
		} finally {
			// an error occured, abort
			consumer.abort();
		}
		return consumer.close();

	}

	/*
	 * @see IFeature#remove(IProgressMonitor) throws CoreException
	 */
	public void remove(IProgressMonitor monitor) throws CoreException {
		getSite().remove(this, monitor);
	}

	/*
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		PluginEntryModel[] result = getPluginEntryModels();
		if (result.length == 0)
			return new IPluginEntry[0];
		else
			return (IPluginEntry[]) result;
	}

	/*
	 * @see IPluginContainer#addPluginEntry(IPluginEntry)
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
		if (pluginEntry != null) {
			addPluginEntryModel((PluginEntryModel) pluginEntry);
		}
	}

	/*
	 * @see IFeature#addNonPluginEntry(INonPluginEntry)
	 */
	public void addNonPluginEntry(INonPluginEntry dataEntry) {
		if (dataEntry != null) {
			addNonPluginEntryModel((NonPluginEntryModel) dataEntry);
		}
	}

	/*
	 * @see IFeature#getDataEntries()
	 */
	public INonPluginEntry[] getNonPluginEntries() {
		NonPluginEntryModel[] result = getNonPluginEntryModels();
		if (result.length == 0)
			return new INonPluginEntry[0];
		else
			return (INonPluginEntry[]) result;
	}

	/*
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		return getPluginEntryModels().length;
	}

	/*
	 * @see IFeature#getImports()
	 */
	public IImport[] getImports() {
		ImportModel[] result = getImportModels();
		if (result.length == 0)
			return new IImport[0];
		else
			return (IImport[]) result;
	}

	/*
	* @see IAdaptable#getAdapter(Class)
	*/
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 * @see IFeature#setFeatureContentProvider(IFeatureContentProvider)
	 */
	public void setFeatureContentProvider(IFeatureContentProvider featureContentProvider) {
		this.featureContentProvider = featureContentProvider;
		featureContentProvider.setFeature(this);
	}

	/*
	 * @see IFeature#setContentConsumer(IFeatureContentConsumer)
	 */
	public void setContentConsumer(IFeatureContentConsumer contentConsumer) {
		this.contentConsumer = contentConsumer;
		contentConsumer.setFeature(this);
	}

	/*
	 * @see IFeature#getFeatureContentProvider(IFeatureContentConsumer)
	 */
	public IFeatureContentProvider getFeatureContentProvider() throws CoreException {
		if (featureContentProvider == null) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Content Provider not set for feature.", null);
			throw new CoreException(status);
		}
		return this.featureContentProvider;
	}

	/*
	 * @see IFeature#getContentConsumer()
	 */
	public IFeatureContentConsumer getContentConsumer() throws CoreException {
		if (this.contentConsumer == null) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "FeatureContentConsumer not set for feature:" + getURL().toExternalForm(), null);
			throw new CoreException(status);
		}
		return contentConsumer;
	}

	/**
	 * Returns the intersection between two array of PluginEntries.
	 */
	private IPluginEntry[] intersection(IPluginEntry[] array1, IPluginEntry[] array2) {
		if (array1 == null || array1.length == 0) {
			return array2;
		}
		if (array2 == null || array2.length == 0) {
			return array1;
		}
		List list1 = Arrays.asList(array1);
		List result = new ArrayList(0);
		for (int i = 0; i < array2.length; i++) {
			if (!list1.contains(array2[i]))
				result.add(array2[i]);
		}
		return (IPluginEntry[]) result.toArray();
	}

	/*
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream, IProgressMonitor)
	 */
	public void store(IPluginEntry entry, String name, InputStream inStream, IProgressMonitor monitor) throws CoreException {
		getSite().store(entry, name, inStream, monitor);
	}

}