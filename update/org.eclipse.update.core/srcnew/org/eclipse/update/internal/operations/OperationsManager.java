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

import java.lang.reflect.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;

public class OperationsManager implements IAdaptable {
	private static final String KEY_INSTALLING = "OperationsManager.installing";

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

	public static boolean hasSelectedJobsWithLicenses(PendingOperation[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			if (jobs[i].getJobType() != PendingOperation.INSTALL)
				continue;
			if (UpdateManager.hasLicense(jobs[i]))
				return true;
		}
		return false;
	}

	public static boolean hasSelectedJobsWithOptionalFeatures(PendingOperation[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			if (jobs[i].getJobType() != PendingOperation.INSTALL)
				continue;
			if (UpdateManager.hasOptionalFeatures(jobs[i].getFeature()))
				return true;
		}
		return false;
	}

	public static boolean hasSelectedInstallJobs(PendingOperation[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			if (jobs[i].getJobType() == PendingOperation.INSTALL)
				return true;
		}
		return false;
	}

	public static PendingOperation[] getSelectedJobsWithLicenses(PendingOperation[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			if (jobs[i].getJobType() != PendingOperation.INSTALL)
				continue;
			if (UpdateManager.hasLicense(jobs[i]))
				list.add(jobs[i]);
		}
		return (PendingOperation[]) list.toArray(
			new PendingOperation[list.size()]);
	}

	public static PendingOperation[] getSelectedJobsWithOptionalFeatures(PendingOperation[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			if (jobs[i].getJobType() != PendingOperation.INSTALL)
				continue;
			if (UpdateManager.hasOptionalFeatures(jobs[i].getFeature()))
				list.add(jobs[i]);
		}
		return (PendingOperation[]) list.toArray(
			new PendingOperation[list.size()]);
	}

	public static PendingOperation[] getSelectedInstallJobs(PendingOperation[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			if (jobs[i].getJobType() == PendingOperation.INSTALL)
				list.add(jobs[i]);
		}
		return (PendingOperation[]) list.toArray(
			new PendingOperation[list.size()]);
	}

	public void ensureUnique(
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

	public void configure(IInstallConfiguration config, IFeature feature)
		throws CoreException {
		IConfiguredSite site = UpdateManager.getConfigSite(feature, config);
		if (site != null) {
			site.configure(feature);
		}
	}

	public boolean unconfigure(IInstallConfiguration config, IFeature feature)
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

	private void preserveOptionalState(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		boolean patch,
		FeatureHierarchyElement2[] optionalElements) {
		for (int i = 0; i < optionalElements.length; i++) {
			FeatureHierarchyElement2[] children =
				optionalElements[i].getChildren(true, patch, config);
			preserveOptionalState(config, targetSite, patch, children);
			if (!optionalElements[i].isEnabled(config)) {
				IFeature newFeature = optionalElements[i].getFeature();
				try {
					IFeature localFeature =
						UpdateManager.getLocalFeature(targetSite, newFeature);
					if (localFeature != null)
						targetSite.unconfigure(localFeature);
				} catch (CoreException e) {
					// Ignore this - we will leave with it
				}
			}
		}
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
		IInstallConfiguration config,
		IConfiguredSite site,
		IFeature feature,
		boolean isConfigured,
		Object adapter)
		throws CoreException {

		PendingOperation toggleOperation = null;
		if (isConfigured)
			toggleOperation = new UnconfigOperation(config, site, feature);
		else
			toggleOperation = new ConfigOperation(config, site, feature);

		return toggleFeatureState(toggleOperation, adapter);
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
		PendingOperation toggleOperation,
		Object adapter)
		throws CoreException {

		toggleOperation.execute(null); // no progress monitor needed

		IStatus status = UpdateManager.getValidator().validateCurrentState();
		if (status != null) {
			toggleOperation.undo();
			throw new CoreException(status);
		} else {
			try {
				// Restart not needed
				boolean restartNeeded = false;

				// Check if this operation is cancelling one that's already pending
				PendingOperation oldOperation =
					findPendingChange(toggleOperation.getFeature());

				if ((toggleOperation instanceof ConfigOperation
					&& oldOperation instanceof UnconfigOperation)
					|| (toggleOperation instanceof UnconfigOperation
						&& oldOperation instanceof ConfigOperation)) {
					// no need to do either pending change
					removePendingChange(oldOperation);
				} else {
					addPendingChange(toggleOperation);
					restartNeeded = true;
				}

				toggleOperation.markProcessed();
				// is this needed ?
				UpdateManager.getOperationsManager().fireObjectChanged(
					toggleOperation,
					null);

				SiteManager.getLocalSite().save();
				// is this needed ?
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

	public boolean installFeatures(
		PendingOperation[] selectedJobs,
		IUpdateModelChangedListener listener,
		IProgressMonitor monitor)
		throws InvocationTargetException, CoreException {
		int installCount = 0;

		try {
			if (selectedJobs == null || selectedJobs.length == 0)
				return false;
			UpdateManager.makeConfigurationCurrent(
				selectedJobs[0].getInstallConfiguration(),
				null);
			monitor.beginTask(
				UpdateManager.getString(KEY_INSTALLING),
				selectedJobs.length);
			for (int i = 0; i < selectedJobs.length; i++) {
				InstallOperation installJob =
					(InstallOperation) selectedJobs[i];
				SubProgressMonitor subMonitor =
					new SubProgressMonitor(
						monitor,
						1,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);

				boolean needsRestart = installJob.execute(subMonitor);
				addPendingChange(installJob);

				installJob.markProcessed();
				//may need to fire with a different parent
				fireObjectsAdded(installJob, null);

				IFeature oldFeature = installJob.getOldFeature();
				if (oldFeature != null
					&& !installJob.isOptionalDelta()
					&& installJob.getOptionalElements() != null) {
					preserveOptionalState(
						installJob.getInstallConfiguration(),
						installJob.getTargetSite(),
						UpdateManager.isPatch(installJob.getFeature()),
						installJob.getOptionalElements());
				}

				//monitor.worked(1);
				UpdateManager.saveLocalSite();
				installCount++;
			}
		} catch (InstallAbortedException e) {
			throw new InvocationTargetException(e);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
			return installCount == selectedJobs.length;
		}
	}

}
