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
package org.eclipse.update.operations;

import java.util.*;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;

public class OperationsManager {
	private static IOperationFactory operationFactory;
	private static Vector listeners = new Vector();
	private static Vector pendingOperations = new Vector();
	
	private static boolean inProgress;

	private OperationsManager() {
	}
	
	public static IOperationFactory getOperationFactory() {
		if (operationFactory == null)
			operationFactory = new OperationFactory();
		return operationFactory;
	}

	public static IFeatureOperation findPendingOperation(IFeature feature) {
		for (int i = 0; i < pendingOperations.size(); i++) {
			IFeatureOperation operation =
				(IFeatureOperation) pendingOperations.elementAt(i);
			if (operation.getFeature().equals(feature))
				return operation;
		}
		return null;
	}

	public static void addPendingOperation(IOperation operation) {
		pendingOperations.add(operation);
		//fireObjectsAdded(this, new Object[] { change });
	}

	public static void removePendingOperation(IOperation operation) {
		pendingOperations.remove(operation);
		//fireObjectsRemoved(this, new Object[] { change });
	}

	public static void addUpdateModelChangedListener(IUpdateModelChangedListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public static void removeUpdateModelChangedListener(IUpdateModelChangedListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	public static void fireObjectsAdded(Object parent, Object[] children) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			IUpdateModelChangedListener listener =
				(IUpdateModelChangedListener) iter.next();
			listener.objectsAdded(parent, children);
		}
	}

	public static void fireObjectsRemoved(Object parent, Object[] children) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			IUpdateModelChangedListener listener =
				(IUpdateModelChangedListener) iter.next();
			listener.objectsRemoved(parent, children);
		}
	}

	public static void fireObjectChanged(Object object, String property) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			IUpdateModelChangedListener listener =
				(IUpdateModelChangedListener) iter.next();
			listener.objectChanged(object, property);
		}
	}

	public static boolean hasSelectedJobsWithLicenses(IInstallFeatureOperation[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateManager.hasLicense(jobs[i].getFeature()))
				return true;
		}
		return false;
	}

	public static boolean hasSelectedJobsWithOptionalFeatures(IInstallFeatureOperation[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateManager.hasOptionalFeatures(jobs[i].getFeature()))
				return true;
		}
		return false;
	}

	public static IInstallFeatureOperation[] getSelectedJobsWithLicenses(IInstallFeatureOperation[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateManager.hasLicense(jobs[i].getFeature()))
				list.add(jobs[i]);
		}
		return (IInstallFeatureOperation[]) list.toArray(
			new IInstallFeatureOperation[list.size()]);
	}

	public static IInstallFeatureOperation[] getSelectedJobsWithOptionalFeatures(IInstallFeatureOperation[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateManager.hasOptionalFeatures(jobs[i].getFeature()))
				list.add(jobs[i]);
		}
		return (IInstallFeatureOperation[]) list.toArray(
			new IInstallFeatureOperation[list.size()]);
	}
	
	public static synchronized void setInProgress(boolean inProgress) {
		OperationsManager.inProgress = inProgress;
	}
	
	public static synchronized boolean isInProgress() {
		return inProgress;
	}
}
