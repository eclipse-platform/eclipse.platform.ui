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
import org.eclipse.update.operations.*;

public class OperationsManager implements IAdaptable, IOperationFactory {

	private Vector listeners = new Vector();
	private Vector pendingOperations = new Vector();

	public OperationsManager() {
	}

	public Object getAdapter(Class key) {
		return null;
	}

	public IOperation createConfigOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature) {
		return new ConfigOperation(config, targetSite, feature);
	}

	public IOperation createBatchInstallOperation(IInstallFeatureOperation[] operations) {
		return new BatchInstallOperation(operations);
	}

	public IOperation createInstallOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature,
		IFeatureReference[] optionalFeatures,
		IFeature[] unconfiguredOptionalFeatures,
		IVerificationListener verifier) {
		return new InstallOperation(
			config,
			targetSite,
			feature,
			optionalFeatures,
			unconfiguredOptionalFeatures,
			verifier);
	}

	public IOperation createUnconfigOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature) {
		return new UnconfigOperation(config, targetSite, feature);
	}

	public IOperation createUninstallOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature) {
		return new UninstallOperation(config, targetSite, feature);
	}

	public IOperation createRevertConfigurationOperation(
		IInstallConfiguration config,
		IProblemHandler problemHandler) {
		return new RevertConfigurationOperation(
			config,
			problemHandler);
	}

	public IOperation createToggleSiteOperation(
		IConfiguredSite site) {
		return new ToggleSiteOperation(site);
	}

	public IFeatureOperation findPendingOperation(IFeature feature) {
		for (int i = 0; i < pendingOperations.size(); i++) {
			IFeatureOperation operation =
				(IFeatureOperation) pendingOperations.elementAt(i);
			if (operation.getFeature().equals(feature))
				return operation;
		}
		return null;
	}

	public void addPendingOperation(IOperation operation) {
		pendingOperations.add(operation);
		//fireObjectsAdded(this, new Object[] { change });
	}

	public void removePendingOperation(IOperation operation) {
		pendingOperations.remove(operation);
		//fireObjectsRemoved(this, new Object[] { change });
	}

	public void removePendingOperation(IFeature scheduledFeature) {
		IOperation operation = findPendingOperation(scheduledFeature);
		if (operation != null)
			removePendingOperation(operation);
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
			if (UpdateManager.hasLicense(jobs[i]))
				return true;
		}
		return false;
	}

	public static boolean hasSelectedJobsWithOptionalFeatures(PendingOperation[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateManager.hasOptionalFeatures(jobs[i].getFeature()))
				return true;
		}
		return false;
	}

	public static PendingOperation[] getSelectedJobsWithLicenses(PendingOperation[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateManager.hasLicense(jobs[i]))
				list.add(jobs[i]);
		}
		return (PendingOperation[]) list.toArray(
			new PendingOperation[list.size()]);
	}

	public static PendingOperation[] getSelectedJobsWithOptionalFeatures(PendingOperation[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateManager.hasOptionalFeatures(jobs[i].getFeature()))
				list.add(jobs[i]);
		}
		return (PendingOperation[]) list.toArray(
			new PendingOperation[list.size()]);
	}
}
