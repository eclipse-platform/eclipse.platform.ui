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
 * Convenience implementation of a feature.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.IFeature
 * @see org.eclipse.update.core.model.FeatureModel
 * @since 2.0
 */
public class Feature extends FeatureModel implements IFeature {

	/**
	 * Simple file name of the default feature manifest file
	 * @since 2.0
	 */
	public static final String FEATURE_FILE= "feature"; //$NON-NLS-1$

	/**
	 * File extension of the default feature manifest file
	 * @since 2.0
	 */
	public static final String FEATURE_XML= FEATURE_FILE + ".xml"; //$NON-NLS-1$

	private ISite site; // feature site
	private IFeatureContentProvider featureContentProvider; // content provider

	/**
	 * Feature default constructor
	 * 
	 * @since 2.0
	 */
	public Feature() {
	}

	/**
	 * Compares two features for equality
	 * 
	 * @param object feature object to compare with
	 * @return <code>true</code> if the two features are equal, 
	 * <code>false</code> otherwise
	 * @since 2.0
	 */
	public boolean equals(Object object) {
		if (!(object instanceof Feature))
			return false;
		IFeature f= (IFeature) object;
		return super.equals(object);

	}

	/**
	 * Returns the feature identifier.
	 * 
	 * @see IFeature#getVersionedIdentifier()
	 * @since 2.0
	 */
	public VersionedIdentifier getVersionedIdentifier() {
		return new VersionedIdentifier(getFeatureIdentifier(), getFeatureVersion());
	}

	/**
	 * Returns the site this feature is associated with.
	 * 
	 * @see IFeature#getSite()
	 * @since 2.0
	 */
	public ISite getSite() {
		return site;
	}

	/**
	 * Returns the feature URL.
	 * 
	 * @see IFeature#getURL()
	 * @since 2.0
	 */
	public URL getURL() {
		IFeatureContentProvider contentProvider= null;
		try {
			contentProvider= getFeatureContentProvider();
		} catch (CoreException e) {
			// no content provider: always log status
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
		}
		return (contentProvider != null) ? contentProvider.getURL() : null;
	}

	/**
	 * Returns an information entry referencing the location of the
	 * feature update site. 
	 * 
	 * @see IFeature#getUpdateSiteEntry()
	 * @since 2.0
	 */
	public IURLEntry getUpdateSiteEntry() {
		return (IURLEntry) getUpdateSiteEntryModel();
	}

	/**
	 * Return an array of information entries referencing locations of other
	 * update sites.
	 * 
	 * @see IFeature#getDiscoverySiteEntries()
	 * @since 2.0
	 */
	public IURLEntry[] getDiscoverySiteEntries() {
		URLEntryModel[] result= getDiscoverySiteEntryModels();
		if (result.length == 0)
			return new IURLEntry[0];
		else
			return (IURLEntry[]) result;
	}

	/**
	 * Returns and optional custom install handler entry.
	 * 
	 * @see IFeature#getInstallHandlerEntry()
	 * @since 2.0
	 */
	public IInstallHandlerEntry getInstallHandlerEntry() {
		return (IInstallHandlerEntry) getInstallHandlerModel();
	}

	/**
	 * Returns the feature description.
	 * 
	 * @see IFeature#getDescription()
	 * @since 2.0
	 */
	public IURLEntry getDescription() {
		return (IURLEntry) getDescriptionModel();
	}

	/**
	 * Returns the copyright information for the feature.
	 * 
	 * @see IFeature#getCopyright()
	 * @since 2.0
	 */
	public IURLEntry getCopyright() {
		return (IURLEntry) getCopyrightModel();
	}

	/**
	 * Returns the license information for the feature.
	 * 
	 * @see IFeature#getLicense()
	 * @since 2.0
	 */
	public IURLEntry getLicense() {
		return (IURLEntry) getLicenseModel();
	}

	/**
	 * Return optional image for the feature.
	 * 
	 * @see IFeature#getImage()
	 * @since 2.0
	 */
	public URL getImage() {
		return getImageURL();
	}

	/**
	 * Return a list of plug-in dependencies for this feature.
	 * 
	 * @see IFeature#getImports()
	 * @since 2.0
	 */
	public IImport[] getImports() {
		ImportModel[] result= getImportModels();
		if (result.length == 0)
			return new IImport[0];
		else
			return (IImport[]) result;
	}

	/**
	 * Install the contents of this feature into the specified target feature.
	 * This method is a reference implementation of the feature installation
	 * protocol. Other concrete feature implementation that override this
	 * method need to implement this protocol.
	 * 
	 * @see IFeature#install(IFeature, IVerificationListener, IProgressMonitor)
	 * @since 2.0
	 */
	public IFeatureReference install(
		IFeature targetFeature,
		IVerificationListener verificationListener,
		IProgressMonitor progress)
		throws CoreException {

		//DEBUG
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_INSTALL){
			UpdateManagerPlugin.getPlugin().debug("Installing...:"+getURL().toExternalForm());
		}

		// make sure we have an InstallMonitor		
		InstallMonitor monitor;
		if (progress == null)
			monitor= null;
		else if (progress instanceof InstallMonitor)
			monitor= (InstallMonitor) progress;
		else
			monitor= new InstallMonitor(progress);

		// Setup optional install handler
		InstallHandlerProxy handler=
			new InstallHandlerProxy(
				IInstallHandler.HANDLER_ACTION_INSTALL,
				this,
				this.getInstallHandlerEntry(),
				monitor);
		boolean success= false;
		Throwable originalException= null;

		// Get source feature provider and verifier. Initialize target variables.
		IFeatureContentProvider provider= getFeatureContentProvider();
		IVerifier verifier= provider.getVerifier();
		IFeatureReference result= null;
		IFeatureContentConsumer consumer= null;

		try {
			// determine list of plugins to install
			// find the intersection between the plugin entries already contained
			// on the target site, and plugin entries packaged in source feature
			IPluginEntry[] sourceFeaturePluginEntries= getPluginEntries();
			ISite targetSite= targetFeature.getSite();
			IPluginEntry[] targetSitePluginEntries=
				(targetSite != null) ? targetSite.getPluginEntries() : new IPluginEntry[0];
			IPluginEntry[] pluginsToInstall=
				UpdateManagerUtils.diff(sourceFeaturePluginEntries, targetSitePluginEntries);
			INonPluginEntry[] nonPluginsToInstall= getNonPluginEntries();

			// determine number of monitor tasks
			//   2 tasks for the feature jar (download/verify + install)
			// + 2*n tasks for plugin entries (download/verify + install for each)
			// + 1*m tasks per non-plugin data entry (download for each)
			// + 1 task for custom non-plugin entry handling (1 for all combined)
			int taskCount=
				2 + 2 * pluginsToInstall.length + nonPluginsToInstall.length + 1;
			if (monitor != null)
				monitor.beginTask("", taskCount);

			// Start the installation tasks			
			handler.installInitiated();

			// Download and verify feature archive(s)
			ContentReference[] references=
				provider.getFeatureEntryArchiveReferences(monitor);
			IVerificationResult vr;
			if (verifier != null) {
				for (int i= 0; i < references.length; i++) {
					vr= verifier.verify(this, references[i], true, monitor);
					if (vr != null) {
						promptForVerification(vr, verificationListener);
					}
				}
			}
			if (monitor != null){
				monitor.worked(1);
				if (monitor.isCanceled()) abort();
			}

			// Download and verify plugin archives
			for (int i= 0; i < pluginsToInstall.length; i++) {
				references=
					provider.getPluginEntryArchiveReferences(pluginsToInstall[i], monitor);
				if (verifier != null) {
					for (int j= 0; j < references.length; j++) {
						vr= verifier.verify(this, references[j], false, monitor);
						if (vr != null) {
							promptForVerification(vr, verificationListener);
						}
					}
				}
				if (monitor != null){
					monitor.worked(1);
					if (monitor.isCanceled()) abort();
				}
			}
			handler.pluginsDownloaded(pluginsToInstall);

			// Download non-plugin archives. Verification handled by optional install handler
			for (int i= 0; i < nonPluginsToInstall.length; i++) {
				references=
					provider.getNonPluginEntryArchiveReferences(nonPluginsToInstall[i], monitor);
				if (monitor != null){
					monitor.worked(1);
					if (monitor.isCanceled()) abort();
				}
			}
			handler.nonPluginDataDownloaded(nonPluginsToInstall, verificationListener);

			// All archives are downloaded and verified. Get ready to install
			consumer= targetFeature.getFeatureContentConsumer();

			//Install feature files
			if (monitor != null)
				monitor.setTaskName(Policy.bind("Feature.TaskInstallFeatureFiles")); //$NON-NLS-1$
			references= provider.getFeatureEntryContentReferences(monitor);
			for (int i= 0; i < references.length; i++) {
				if (monitor != null)
					monitor.subTask(references[i].getIdentifier());
				consumer.store(references[i], monitor);
			}
			if (monitor != null){
				monitor.worked(1);
				if (monitor.isCanceled()) abort();
			}

			// Install plugin files
			for (int i= 0; i < pluginsToInstall.length; i++) {
				if (monitor != null)
					monitor.setTaskName(
						Policy.bind(
							"Feature.TaskInstallPluginFiles",
							pluginsToInstall[i].getVersionedIdentifier().getIdentifier()));
				//$NON-NLS-1$
				IContentConsumer pluginConsumer= consumer.open(pluginsToInstall[i]);
				references=
					provider.getPluginEntryContentReferences(pluginsToInstall[i], monitor);
				for (int j= 0; j < references.length; j++) {
					if (monitor != null)
						monitor.subTask(references[j].getIdentifier());
					pluginConsumer.store(references[j], monitor);
				}
				pluginConsumer.close();
				if (monitor != null){
					monitor.worked(1);
					if (monitor.isCanceled()) abort();
				}
			}

			// call handler to complete installation (eg. handle non-plugin entries)
			handler.completeInstall(consumer);
			if (monitor != null){
				monitor.worked(1);
				if (monitor.isCanceled()) abort();
			}
			// indicate install success
			success= true;

		} catch (Throwable t) {
			originalException= t;
		} finally {
			if (monitor != null)
				monitor.done();

			Throwable newException= null;
			try {
				if (consumer != null) {
					if (success)
						result= consumer.close();
					else
						consumer.abort();
				}
				handler.installCompleted(success);
			} catch (Throwable t) {
				newException= t;
			}
			if (originalException != null) // original exception wins
				throw Utilities.newCoreException(
					Policy.bind("InstallHandler.error", this.getLabel()),
					originalException);
			if (newException != null)
				throw Utilities.newCoreException(
					Policy.bind("InstallHandler.error", this.getLabel()),
					newException);
		}
		return result;
	}

	/**
	 * Returns an array of plug-in entries referenced by this feature
	 * 
	 * @see IFeature#getPluginEntries()
	 * @since 2.0
	 */
	public IPluginEntry[] getPluginEntries() {
		PluginEntryModel[] result= getPluginEntryModels();
		if (result.length == 0)
			return new IPluginEntry[0];
		else
			return (IPluginEntry[]) result;
	}

	/**
	 * Returns the count of referenced plug-in entries.
	 * 
	 * @see IFeature#getPluginEntryCount()
	 * @since 2.0
	 */
	public int getPluginEntryCount() {
		return getPluginEntryModels().length;
	}

	/**
	 * Returns an array of non-plug-in entries referenced by this feature
	 * 
	 * @see IFeature#getNonPluginEntries()
	 * @since 2.0
	 */
	public INonPluginEntry[] getNonPluginEntries() {
		NonPluginEntryModel[] result= getNonPluginEntryModels();
		if (result.length == 0)
			return new INonPluginEntry[0];
		else
			return (INonPluginEntry[]) result;
	}

	/**
	 * Returns the count of referenced non-plug-in entries.
	 * 
	 * @see IFeature#getNonPluginEntryCount()
	 * @since 2.0
	 */
	public int getNonPluginEntryCount() {
		return getNonPluginEntryModels().length;
	}

	/**
	 * Returns the download size of the feature, if it can be determined.
	 * 
	 * @see IFeature#getDownloadSize()
	 * @since 2.0
	 */
	public long getDownloadSize() {
		try {
			return getFeatureContentProvider().getDownloadSizeFor(
				getPluginEntries(),
				getNonPluginEntries());
		} catch (CoreException e) {
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
			return ContentEntryModel.UNKNOWN_SIZE;
		}

	}

	/**
	 * Returns the install size of the feature, if it can be determined.
	 * 
	 * @see IFeature#getInstallSize()
	 * @since 2.0
	 */
	public long getInstallSize() {
		try {
			return getFeatureContentProvider().getInstallSizeFor(
				getPluginEntries(),
				getNonPluginEntries());
		} catch (CoreException e) {
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
			return ContentEntryModel.UNKNOWN_SIZE;
		}

	}

	/**
	 * Returns the content provider for this feature.
	 * 
	 * @see IFeature#getFeatureContentProvider()
	 * @since 2.0
	 */
	public IFeatureContentProvider getFeatureContentProvider()
		throws CoreException {
		if (featureContentProvider == null) {
			throw Utilities.newCoreException(
					Policy.bind("Feature.NoContentProvider", getVersionedIdentifier().toString()),
					null);
			//$NON-NLS-1$
		}
		return this.featureContentProvider;
	}

	/**
	 * Returns the content consumer for this feature.
	 * 
	 * @see IFeature#getFeatureContentConsumer()
	 * @since 2.0
	 */
	public IFeatureContentConsumer getFeatureContentConsumer()
		throws CoreException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the site for this feature.
	 * 
	 * @see IFeature#setSite(ISite)
	 * @since 2.0
	 */
	public void setSite(ISite site) throws CoreException {
		if (this.site != null) {
			String featureURLString= (getURL() != null) ? getURL().toExternalForm() : "";
			throw Utilities.newCoreException(
					Policy.bind("Feature.SiteAlreadySet", featureURLString),
					null);
			//$NON-NLS-1$
		}
		this.site= site;
	}

	/**
	 * Sets the content provider for this feature.
	 * 
	 * @see IFeature#setFeatureContentProvider(IFeatureContentProvider)
	 * @since 2.0
	 */
	public void setFeatureContentProvider(IFeatureContentProvider featureContentProvider) {
		this.featureContentProvider= featureContentProvider;
		featureContentProvider.setFeature(this);
	}

	/**
	 * Return the string representation of this fetaure
	 * 
	 * @return feature as string
	 * @since 2.0
	 */
	public String toString() {
		String URLString=
			(getURL() == null) ? Policy.bind("Feature.NoURL") : getURL().toExternalForm();
		//$NON-NLS-1$
		return Policy.bind(
			"Feature.FeatureVersionToString",
			URLString,
			getVersionedIdentifier().toString());
		//$NON-NLS-1$
	}

	/*
	 * 
	 */
	private void promptForVerification(
		IVerificationResult verificationResult,
		IVerificationListener listener)
		throws CoreException {

		if (listener == null)
			return;
			
		int result= listener.prompt(verificationResult);

		if (result == IVerificationListener.CHOICE_ABORT) {
			throw Utilities
				.newCoreException(Policy.bind("JarVerificationService.CancelInstall"),
			//$NON-NLS-1$
			verificationResult.getVerificationException());
		}
		if (result == IVerificationListener.CHOICE_ERROR) {
			throw Utilities
				.newCoreException(
					Policy.bind("JarVerificationService.UnsucessfulVerification"),
			//$NON-NLS-1$
			verificationResult.getVerificationException());
		}

		return;
	}

	/*
	 * Installation has been cancelled, abort and revert
	 */
	 private void abort() throws CoreException{
		// FIXME	 	
		throw Utilities.newCoreException(Policy.bind("Feature.InstallationCancelled"),null); //$NON-NLS-1$
	 }
}