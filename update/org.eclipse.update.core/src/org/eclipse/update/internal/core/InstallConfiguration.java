package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.update.core.*;
import org.eclipse.update.core.IActivity;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.IInstallConfigurationChangedListener;

public class InstallConfiguration implements IInstallConfiguration {

	private ListenersList listeners = new ListenersList();
	private boolean isCurrent;
	private List installSites;
	private List linkedSites;
	private List features;

	/*
	 * default constructor. Create
	 */
	public InstallConfiguration() {
		this.isCurrent = true;
	}

	/*
	 * @see IInstallConfiguration#getFeatures()
	 */
	public IFeatureReference[] getFeatures() {

		IFeatureReference[] result = new IFeatureReference[0];

		// initialize if needed
		if (features == null) {
			features = new ArrayList();
			//FIXME: what about startup
			//don't they resolve the plugin list
			if (installSites != null) {
				Iterator iter = installSites.iterator();
				while (iter.hasNext()) {
					ISite currentSite = (ISite) iter.next();
					features.addAll(Arrays.asList(currentSite.getFeatureReferences()));
				}
			}
			if (linkedSites != null) {
				Iterator iter = linkedSites.iterator();
				while (iter.hasNext()) {
					ISite currentSite = (ISite) iter.next();
					features.addAll(Arrays.asList(currentSite.getFeatureReferences()));
				}
			}
		}

		System.out.println(features);
		if (features != null && !features.isEmpty()) {
			// move List in Array
			result = new IFeatureReference[features.size()];
			features.toArray(result);
		}

		return result;
	}

	/*
	 * @see IInstallConfiguration#getInstallSites()
	 */
	public ISite[] getInstallSites() {
		ISite[] sites = new ISite[0];
		if (installSites != null && !installSites.isEmpty()) {
			sites = new ISite[installSites.size()];
			installSites.toArray(sites);
		}
		return sites;
	}

	/*
	 * @see IInstallConfiguration#addInstallSite(ISite)
	 */
	public void addInstallSite(ISite site) {
		if (!isCurrent)
			return;
		if (installSites == null) {
			installSites = new ArrayList();
		}
		installSites.add(site);

		// notify listeners
		Object[] configurationListeners = listeners.getListeners();
		for (int i = 0; i < configurationListeners.length; i++) {
			((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteAdded(site);
		}
	}

	/*
	 * @see IInstallConfiguration#removeInstallSite(ISite)
	 */
	public void removeInstallSite(ISite site) {
		if (!isCurrent)
			return;
		//FIXME: remove should make sure we synchronize
		if (installSites != null) {
			installSites.remove(site);

			// notify listeners
			Object[] configurationListeners = listeners.getListeners();
			for (int i = 0; i < configurationListeners.length; i++) {
				((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteRemoved(site);
			}
		}
	}

	/*
	 * @see IInstallConfiguration#getLinkedSites()
	 */
	public ISite[] getLinkedSites() {
		ISite[] sites = new ISite[0];
		if (linkedSites != null) {
			sites = new ISite[linkedSites.size()];
			linkedSites.toArray(sites);
		}
		return sites;
	}

	/*
	 * @see IInstallConfiguration#addLinkedSite(ISite)
	 */
	public void addLinkedSite(ISite site) {
		if (!isCurrent)
			return;
		if (linkedSites == null) {
			linkedSites = new ArrayList();
		}
		linkedSites.add(site);
		// notify listeners
		Object[] configurationListeners = listeners.getListeners();
		for (int i = 0; i < configurationListeners.length; i++) {
			((IInstallConfigurationChangedListener) configurationListeners[i]).linkedSiteAdded(site);
		}
	}

	/*
	 * @see IInstallConfiguration#removeLinkedSite(ISite)
	 */
	public void removeLinkedSite(ISite site) {
		if (!isCurrent)
			return;
		//FIXME: remove should make sure we synchronize
		if (linkedSites != null) {
			linkedSites.remove(site);
			// notify listeners
			Object[] configurationListeners = listeners.getListeners();
			for (int i = 0; i < configurationListeners.length; i++) {
				((IInstallConfigurationChangedListener) configurationListeners[i]).linkedSiteRemoved(site);
			}
		}
	}

	/*
	 * @see IInstallConfiguration#isCurrent()
	 */
	public boolean isCurrent() {
		return isCurrent;
	}

	/*
	 * @see IInstallConfiguration#addInstallConfigurationChangedListener(IInstallConfigurationChangedListener)
	 */
	public void addInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * @see IInstallConfiguration#removeInstallConfigurationChangedListener(IInstallConfigurationChangedListener)
	 */
	public void removeInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/*
	 * @see IInstallConfiguration#getConfiguredFeatures()
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		// FIXME: 
		return getFeatures();
	}

	/*
	 * @see IInstallConfiguration#getUnconfiguredFeatures()
	 */
	public IFeatureReference[] getUnconfiguredFeatures() {
		IFeatureReference[] result = new IFeatureReference[0];
		// FIXME:
		return result;
	}

	/*
	 * @see IInstallConfiguration#export(File)
	 */
	public void export(File exportFile) {
	}

	/*
	 * @see IInstallConfiguration#getActivities()
	 */
	public IActivity[] getActivities() {
		return null;
	}

	/*
	 * @see IInstallConfiguration#getCreationDate()
	 */
	public Date getCreationDate() {
		return null;
	}

	/*
	 * @see IInstallConfiguration#configure(IFeature)
	 */
	public void configure(IFeature feature) {
	}

	/*
	 * @see IInstallConfiguration#unconfigure(IFeature)
	 */
	public void unconfigure(IFeature feature) {
	}

}