/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.operations;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

public class OperationsManager implements IAdaptable {
	private static final String KEY_UNABLE = "OperationsManager.error.unable";
	private static final String KEY_OLD = "OperationsManager.error.old";

	private Vector pendingChanges = new Vector();
	private Vector listeners = new Vector();

	public OperationsManager() {
	}

	public Object getAdapter(Class key) {
		return null;
	}

	public PendingOperation[] getPendingChanges() {
		return (PendingOperation[]) pendingChanges.toArray(
			new PendingOperation[pendingChanges.size()]);
	}

	public PendingOperation[] getPendingChanges(int type) {
		Vector v = new Vector();
		for (int i = 0; i < pendingChanges.size(); i++) {
			PendingOperation job =
				(PendingOperation) pendingChanges.elementAt(i);
			if (job.getJobType() == type)
				v.add(job);
		}
		return (PendingOperation[]) v.toArray(new PendingOperation[v.size()]);
	}

	public PendingOperation findPendingChange(IFeature feature) {
		for (int i = 0; i < pendingChanges.size(); i++) {
			PendingOperation job =
				(PendingOperation) pendingChanges.elementAt(i);
			if (job.getFeature().equals(feature))
				return job;
		}
		return null;
	}

	public PendingOperation findRelatedPendingChange(IFeature feature) {
		for (int i = 0; i < pendingChanges.size(); i++) {
			PendingOperation job =
				(PendingOperation) pendingChanges.elementAt(i);

			String jobId =
				job.getFeature().getVersionedIdentifier().getIdentifier();
			String id = feature.getVersionedIdentifier().getIdentifier();
			if (id.equals(jobId))
				return job;
		}
		return null;
	}

	public boolean isPending(IFeature feature) {
		return findPendingChange(feature) != null;
	}

	public void addPendingChange(PendingOperation change) {
		pendingChanges.add(change);
		change.enable(true);
		fireObjectsAdded(this, new Object[] { change });
	}

	public void removePendingChange(PendingOperation change) {
		pendingChanges.remove(change);
		change.enable(false);
		fireObjectsRemoved(this, new Object[] { change });
	}

	public void removePendingChange(IFeature scheduledFeature) {
		PendingOperation change = findPendingChange(scheduledFeature);
		if (change != null)
			removePendingChange(change);
	}

	public void addUpdateModelChangedListener(IUpdateModelChangedListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeUpdateModelChangedListener(IUpdateModelChangedListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	public void fireObjectsAdded(Object parent, Object[] children) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			IUpdateModelChangedListener listener =
				(IUpdateModelChangedListener) iter.next();
			listener.objectsAdded(parent, children);
		}
	}

	public void fireObjectsRemoved(Object parent, Object[] children) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			IUpdateModelChangedListener listener =
				(IUpdateModelChangedListener) iter.next();
			listener.objectsRemoved(parent, children);
		}
	}

	public void fireObjectChanged(Object object, String property) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			IUpdateModelChangedListener listener =
				(IUpdateModelChangedListener) iter.next();
			listener.objectChanged(object, property);
		}
	}

	public static PendingOperation[] orderJobs(PendingOperation[] jobs) {
		ArrayList result = new ArrayList();
		PendingOperation[] input = new PendingOperation[jobs.length];
		System.arraycopy(jobs, 0, input, 0, jobs.length);
		//Add jobs to unconfigure.
		addJobs(input, result, PendingOperation.UNCONFIGURE, false);
		//Add jobs to configure.
		addJobs(input, result, PendingOperation.CONFIGURE, false);
		//Add regular feature installs
		addJobs(input, result, PendingOperation.INSTALL, false);
		//Add patch installs
		addJobs(input, result, PendingOperation.INSTALL, true);
		//Add the remainder (only uninstalls)
		addJobs(input, result, -1, false);
		return (PendingOperation[]) result.toArray(
			new PendingOperation[result.size()]);
	}

	private static void addJobs(
		PendingOperation[] input,
		ArrayList result,
		int type,
		boolean patch) {
		for (int i = 0; i < input.length; i++) {
			PendingOperation job = input[i];
			if (job == null)
				continue;
			boolean match = false;
			if (type == -1)
				match = true;
			else {
				if (type == job.getJobType()) {
					if (type == PendingOperation.INSTALL) {
						if (job.getFeature().isPatch() == patch)
							match = true;
					} else
						match = true;
				}
			}
			if (match) {
				result.add(job);
				input[i] = null;
			}
		}
	}

	public static boolean hasSelectedJobsWithLicenses(Object[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			PendingOperation job = (PendingOperation) jobs[i];
			if (job.getJobType() != PendingOperation.INSTALL)
				continue;
			if (UpdateManager.hasLicense(job))
				return true;
		}
		return false;
	}

	public static boolean hasSelectedJobsWithOptionalFeatures(Object[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			PendingOperation job = (PendingOperation) jobs[i];
			if (job.getJobType() != PendingOperation.INSTALL)
				continue;
			if (UpdateManager.hasOptionalFeatures(job.getFeature()))
				return true;
		}
		return false;
	}

	public static boolean hasSelectedInstallJobs(Object[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			PendingOperation job = (PendingOperation) jobs[i];
			if (job.getJobType() == PendingOperation.INSTALL)
				return true;
		}
		return false;
	}

	public static PendingOperation[] getSelectedJobsWithLicenses(Object[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			PendingOperation job = (PendingOperation) jobs[i];
			if (job.getJobType() != PendingOperation.INSTALL)
				continue;
			if (UpdateManager.hasLicense(job))
				list.add(job);
		}
		return (PendingOperation[]) list.toArray(
			new PendingOperation[list.size()]);
	}

	public static PendingOperation[] getSelectedJobsWithOptionalFeatures(Object[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			PendingOperation job = (PendingOperation) jobs[i];
			if (job.getJobType() != PendingOperation.INSTALL)
				continue;
			if (UpdateManager.hasOptionalFeatures(job.getFeature()))
				list.add(job);
		}
		return (PendingOperation[]) list.toArray(
			new PendingOperation[list.size()]);
	}

	public static PendingOperation[] getSelectedInstallJobs(Object[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			PendingOperation job = (PendingOperation) jobs[i];
			if (job.getJobType() == PendingOperation.INSTALL)
				list.add(job);
		}
		return (PendingOperation[]) list.toArray(
			new PendingOperation[list.size()]);
	}

	public void executeOneJob(
		PendingOperation job,
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeatureReference[] optionalFeatures,
		IVerificationListener verifier,
		IProgressMonitor monitor)
		throws CoreException {

		IFeature feature = job.getFeature();
		if (job.getJobType() == PendingOperation.UNINSTALL) {
			//find the  config site of this feature
			IConfiguredSite site = UpdateManager.getConfigSite(feature, config);
			if (site != null) {
				site.remove(feature, monitor);
			} else {
				// we should do something here
				String message =
					UpdateManager.getFormattedMessage(
						KEY_UNABLE,
						feature.getLabel());
				IStatus status =
					new Status(
						IStatus.ERROR,
						UpdateManager.getPluginId(),
						IStatus.OK,
						message,
						null);
				throw new CoreException(status);
			}
		} else if (job.getJobType() == PendingOperation.INSTALL) {
			if (optionalFeatures == null)
				targetSite.install(feature, verifier, monitor);
			else
				targetSite.install(
					feature,
					optionalFeatures,
					verifier,
					monitor);
			IFeature oldFeature = job.getOldFeature();
			if (oldFeature != null && !job.isOptionalDelta()) {
				boolean oldSuccess = unconfigure(config, oldFeature);
				if (!oldSuccess) {
					if (!UpdateManager.isNestedChild(config, oldFeature)) {
						// "eat" the error if nested child
						String message =
							UpdateManager.getFormattedMessage(
								KEY_OLD,
								oldFeature.getLabel());
						IStatus status =
							new Status(
								IStatus.ERROR,
								UpdateManager.getPluginId(),
								IStatus.OK,
								message,
								null);
						throw new CoreException(status);
					}
				}
			}
			if (oldFeature == null) {
				ensureUnique(config, feature, targetSite);
			}
		} else if (job.getJobType() == PendingOperation.CONFIGURE) {
			configure(config, feature);
			ensureUnique(config, feature, targetSite);
		} else if (job.getJobType() == PendingOperation.UNCONFIGURE) {
			unconfigure(config, job.getFeature());
		} else {
			// should not be here
			return;
		}

		job.markProcessed();
		fireObjectChanged(job, null);
	}

	private void ensureUnique(
		IInstallConfiguration config,
		IFeature feature,
		IConfiguredSite targetSite)
		throws CoreException {
		boolean patch = false;
		if (targetSite == null)
			targetSite = feature.getSite().getCurrentConfiguredSite();
		IImport[] imports = feature.getImports();
		for (int i = 0; i < imports.length; i++) {
			IImport iimport = imports[i];
			if (iimport.isPatch()) {
				patch = true;
				break;
			}
		}
		// Only need to check features that patch other features.
		if (!patch)
			return;
		IFeature localFeature =
			UpdateManager.getLocalFeature(targetSite, feature);
		ArrayList oldFeatures = new ArrayList();
		// First collect all older active features that
		// have the same ID as new features marked as 'unique'.
		UpdateManager.collectOldFeatures(localFeature, targetSite, oldFeatures);
		// Now unconfigure old features to enforce uniqueness
		for (int i = 0; i < oldFeatures.size(); i++) {
			IFeature oldFeature = (IFeature) oldFeatures.get(i);
			unconfigure(config, oldFeature);
		}
	}

	private void configure(IInstallConfiguration config, IFeature feature)
		throws CoreException {
		IConfiguredSite site = UpdateManager.getConfigSite(feature, config);
		if (site != null) {
			site.configure(feature);
		}
	}

	private boolean unconfigure(IInstallConfiguration config, IFeature feature)
		throws CoreException {
		IConfiguredSite site = UpdateManager.getConfigSite(feature, config);
		if (site != null) {
			PatchCleaner2 cleaner = new PatchCleaner2(site, feature);
			boolean result = site.unconfigure(feature);
			cleaner.dispose();
			return result;
		}
		return false;
	}

	/**
	 * Returns true if a restart is needed
	 * @param site
	 * @param feature
	 * @param isConfigured
	 * @return
	 * @throws CoreException
	 */
	public boolean toggleFeatureState(
		IConfiguredSite site,
		IFeature feature,
		boolean isConfigured,
		Object adapter)
		throws CoreException {

		PendingOperation toggleOperation = null;
		if (isConfigured)
			toggleOperation = new FeatureUnconfigOperation(site, feature);
		else
			toggleOperation = new FeatureConfigOperation(site, feature);

		toggleOperation.execute();
	
		IStatus status = UpdateManager.getValidator().validateCurrentState();
		if (status != null) {
			toggleOperation.undo();
			throw new CoreException(status);
		} else {
			try {
				// Restart not needed
				boolean restartNeeded = false;

				// Check if this operation is cancelling one that's already pending
				PendingOperation oldOperation = findPendingChange(feature);

				if ((isConfigured
					&& oldOperation instanceof FeatureUnconfigOperation)
					|| (!isConfigured
						&& oldOperation instanceof FeatureConfigOperation)) {
					// no need to do either pending change
					removePendingChange(oldOperation);
				} else {
					addPendingChange(toggleOperation);
					restartNeeded = true;
				} 
				
				SiteManager.getLocalSite().save();
				UpdateManager.getOperationsManager().fireObjectChanged(
					adapter,
					"");

				return restartNeeded;
			} catch (CoreException e) {
				toggleOperation.undo();
				UpdateManager.logException(e);
				throw e;
			}
		}
	}

	/**
	 * Returns true if a restart is needed
	 * @param site
	 * @return
	 * @throws CoreException
	 */
	public boolean toggleSiteState(IConfiguredSite site) throws CoreException {
		if (site == null)
			return false;
		boolean oldValue = site.isEnabled();
		site.setEnabled(!oldValue);
		IStatus status = UpdateManager.getValidator().validateCurrentState();
		if (status != null) {
			// revert
			site.setEnabled(oldValue);
			throw new CoreException(status);
		} else {
			try {
				SiteManager.getLocalSite().save();
				UpdateManager.getOperationsManager().fireObjectChanged(
					site,
					"");
				return true; // will restart
			} catch (CoreException e) {
				//revert
				site.setEnabled(oldValue);
				UpdateManager.logException(e);
				throw e;
			}
		}
	}

}
