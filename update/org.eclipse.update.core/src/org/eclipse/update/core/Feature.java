package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.core.Policy;
/**
 * Abstract Class that implements most of the behavior of a feature
 * A feature ALWAYS belongs to an ISite
 */
public class Feature extends FeatureModel implements IFeature {

	/**
	 * 
	 */
	public static final String FEATURE_FILE = "feature"; //$NON-NLS-1$

	/**
	 * 
	 */
	public static final String FEATURE_XML = FEATURE_FILE + ".xml"; //$NON-NLS-1$

	/**
	 * 
	 */
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * Site in which teh feature resides
	 */
	private ISite site;

	/**
	 * The content provider of the DefaultFeature
	 */
	private IFeatureContentProvider featureContentProvider;

	/**
	 * Constructor
	 */
	public Feature() {
	}

	/**
	* 
	*/

	public boolean equals(Object object) {
		if (!(object instanceof Feature))
			return false;
		IFeature f = (IFeature) object;
		return (super.equals(object) && getURL().equals(f.getURL()));

	}

	/*
	 * @see IFeature#getVersionedIdentifier()
	 */
	public VersionedIdentifier getVersionedIdentifier() {
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
			// no content provider, log status
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
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
			String featureURLString = (getURL() != null) ? getURL().toExternalForm() : EMPTY_STRING;
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("Feature.SiteAlreadySet", featureURLString), null); //$NON-NLS-1$
			throw new CoreException(status);
		}
		this.site = site;
	}

	/**
	* returns the download size
	* of the feature to be installed on the site.
	* If the site is <code>null</code> returns the maximum size
	* 
	* If one plug-in entry has an unknown size.
	* then the download size is unknown.
	* 
	* @see IFeature#getDownloadSize()
	* 
	*/
	public long getDownloadSize() {
		try {
			return getFeatureContentProvider().getDownloadSizeFor(getPluginEntries(), getNonPluginEntries());
		} catch (CoreException e) {
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
			return ContentEntryModel.UNKNOWN_SIZE;
		}

	}
	/**
	 * returns the install size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the install size is unknown.
	 * 
	 * @see IFeature#getInstallSize()
	 */
	public long getInstallSize() {
		try {
			return getFeatureContentProvider().getInstallSizeFor(getPluginEntries(), getNonPluginEntries());
		} catch (CoreException e) {
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
			return ContentEntryModel.UNKNOWN_SIZE;
		}

	}

	/*
	 * @see IFeature#install(IFeature,IVerificationListener, IProgressMonitor) throws CoreException
	 */
	public IFeatureReference install(IFeature targetFeature, IVerificationListener verificationListener, IProgressMonitor progress) throws CoreException {

		// make sure we have an InstallMonitor		
		InstallMonitor monitor;
		if (progress == null)
			monitor = null;
		else if (progress instanceof InstallMonitor)
			monitor = (InstallMonitor) progress;
		else
			monitor = new InstallMonitor(progress);

		// do the install
		IFeatureContentConsumer consumer = targetFeature.getFeatureContentConsumer();
		IVerifier verifier = getFeatureContentProvider().getVerifier();

		try {
			// determine list of plugins to install
			// find the intersection between the two arrays of IPluginEntry...
			// The one the site contains and the one the feature contains
			IPluginEntry[] sourceFeaturePluginEntries = getPluginEntries();
			ISite targetSite = targetFeature.getSite();
			IPluginEntry[] targetSitePluginEntries = (targetSite != null) ? targetSite.getPluginEntries() : new IPluginEntry[0];
			IPluginEntry[] pluginsToInstall = UpdateManagerUtils.diff(sourceFeaturePluginEntries, targetSitePluginEntries);

			// determine number of monitor tasks
				int taskCount = 1 // one task for all feature files (already downloaded)
		+pluginsToInstall.length // one task for each plugin to install
	+getNonPluginEntries().length; // one task for each non-plugin file to install

			if (monitor != null)
				monitor.beginTask(EMPTY_STRING, taskCount);

			if (verifier != null) {
				ContentReference[] references = getFeatureContentProvider().getFeatureEntryArchiveReferences(monitor);
				promptForVerification(verifier.verify(this, references, monitor),verificationListener);
			}

			//finds the contentReferences for this IFeature
			if (monitor != null)
				monitor.setTaskName(Policy.bind("Feature.TaskInstallFeatureFiles")); //$NON-NLS-1$
			ContentReference[] references = getFeatureContentProvider().getFeatureEntryContentReferences(monitor);
			if (verifier != null)
				verifier.verify(this, references, monitor);
			for (int i = 0; i < references.length; i++) {
				if (monitor != null)
					monitor.subTask(references[i].getIdentifier());
				consumer.store(references[i], monitor);
			}
			if (monitor != null)
				monitor.worked(1);

			// download and install plugin plugin files
			for (int i = 0; i < pluginsToInstall.length; i++) {
				// verification
				if (verifier != null) {
					references = getFeatureContentProvider().getPluginEntryArchiveReferences(pluginsToInstall[i], monitor);
					promptForVerification(verifier.verify(this, references, monitor),verificationListener);
				}
				
			}
			
			for (int i = 0; i < pluginsToInstall.length; i++) {
				if (monitor != null)
					monitor.setTaskName(Policy.bind("Feature.TaskInstallPluginFiles") + pluginsToInstall[i].getVersionedIdentifier().getIdentifier() + "]: "); //$NON-NLS-1$ //$NON-NLS-2$
				IContentConsumer pluginConsumer = consumer.open(pluginsToInstall[i]);
				
				// installation
				references = getFeatureContentProvider().getPluginEntryContentReferences(pluginsToInstall[i], monitor);
				for (int j = 0; j < references.length; j++) {
					if (monitor != null)
						monitor.subTask(references[j].getIdentifier());
						pluginConsumer.store(references[j], monitor);
				}
				pluginConsumer.close();
				if (monitor != null)
					monitor.worked(1);
			}

			// download and install non plugins bundles
			INonPluginEntry[] nonPluginsContentReferencesToInstall = getNonPluginEntries();
			for (int i = 0; i < nonPluginsContentReferencesToInstall.length; i++) {
				if (monitor != null)
					monitor.setTaskName(Policy.bind("Feature.TaskInstallNonPluginsFiles")); //$NON-NLS-1$
				IContentConsumer nonPluginConsumer = consumer.open(nonPluginsContentReferencesToInstall[i]);
				references = getFeatureContentProvider().getNonPluginEntryArchiveReferences(nonPluginsContentReferencesToInstall[i], monitor);
				if (verifier != null)
				promptForVerification(verifier.verify(this, references, monitor),verificationListener);
				for (int j = 0; j < references.length; j++) {
					if (monitor != null)
						monitor.subTask(references[j].getIdentifier());
					nonPluginConsumer.store(references[j], monitor);
				}
				nonPluginConsumer.close();
				if (monitor != null)
					monitor.worked(1);
			}
		} catch (CoreException e) {
			// an error occured, abort 
			if (consumer != null)
				consumer.abort();
			throw e;
		} finally {
			if (monitor != null)
				monitor.done();
		}

		return consumer.close();

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
	 * @see IFeature#getNonPluginEntryCount()
	 */
	public int getNonPluginEntryCount() {
		return getNonPluginEntryModels().length;
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
	 * @see IFeature#getFeatureContentProvider(IFeatureContentConsumer)
	 */
	public IFeatureContentProvider getFeatureContentProvider() throws CoreException {
		if (featureContentProvider == null) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("Feature.NoContentProvider", getVersionedIdentifier().toString()), null); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return this.featureContentProvider;
	}

	/*
	 * @see IFeature#getContentConsumer()
	 */
	public IFeatureContentConsumer getFeatureContentConsumer() throws CoreException {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		String URLString = (getURL() == null) ? Policy.bind("Feature.NoURL") : getURL().toExternalForm(); //$NON-NLS-1$
		return Policy.bind("Feature.FeatureVersionToString", URLString, getVersionedIdentifier().toString()); //$NON-NLS-1$ 
	}


	/**
	 * 
	 */
	private void promptForVerification(IVerificationResult verificationResult, IVerificationListener listener) throws CoreException{

		if (listener==null) return;
		int result =  listener.prompt(verificationResult);
		
		if (result == IVerificationListener.CHOICE_ABORT) {
			throw Utilities.newCoreException(Policy.bind("JarVerificationService.CancelInstall"), //$NON-NLS-1$
			verificationResult.getResultException());
		}
		if (result == IVerificationListener.CHOICE_ERROR) {
			throw Utilities.newCoreException(Policy.bind("JarVerificationService.UnsucessfulVerification"), //$NON-NLS-1$
			verificationResult.getResultException());
		}

	return;
		
	}

}