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

/**
 * Entry point for update manager operations.
 * @since 3.0
 */
public class OperationsManager {
	private static IOperationValidator validator;
	private static IOperationFactory operationFactory;
	private static Vector listeners = new Vector();
	private static Vector pendingOperations = new Vector();
	
	private static boolean inProgress;

	private OperationsManager() {
	}
	
	/**
	 * Each update operations must be created by the operation factory. 
	 * Use this method to obtain the factory.
	 */
	public static IOperationFactory getOperationFactory() {
		if (operationFactory == null)
			operationFactory = new OperationFactory();
		return operationFactory;
	}

	/**
	 * Check if the feature is the subject of an update operation such as install,
	 * configure, etc. and return it. Currently there can only be one pending
	 * operation on a feature.
	 * @param feature
	 * @return
	 */
	public static IFeatureOperation findPendingOperation(IFeature feature) {
		for (int i = 0; i < pendingOperations.size(); i++) {
			IFeatureOperation operation =
				(IFeatureOperation) pendingOperations.elementAt(i);
			if (operation.getFeature().equals(feature))
				return operation;
		}
		return null;
	}
	
	/**
	 * Register a pending operation.
	 * @param operation
	 */
	public static void addPendingOperation(IOperation operation) {
		pendingOperations.add(operation);
		//fireObjectsAdded(this, new Object[] { change });
	}

	/**
	 * Unregister a pending operation.
	 * @param operation
	 */
	public static void removePendingOperation(IOperation operation) {
		pendingOperations.remove(operation);
		//fireObjectsRemoved(this, new Object[] { change });
	}

	/**
	 * Adds a model changed listener.
	 * @param listener
	 */
	public static void addUpdateModelChangedListener(IUpdateModelChangedListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Removes an model changed listener.
	 * @param listener
	 */
	public static void removeUpdateModelChangedListener(IUpdateModelChangedListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	/**
	 * Notifies model changed listeners when features/sites/etc. are added.
	 * @param parent
	 * @param children
	 */
	public static void fireObjectsAdded(Object parent, Object[] children) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			IUpdateModelChangedListener listener =
				(IUpdateModelChangedListener) iter.next();
			listener.objectsAdded(parent, children);
		}
	}

	/**
	 * Notifies model changed listeners when features/sites/etc are removed.
	 * @param parent
	 * @param children
	 */
	public static void fireObjectsRemoved(Object parent, Object[] children) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			IUpdateModelChangedListener listener =
				(IUpdateModelChangedListener) iter.next();
			listener.objectsRemoved(parent, children);
		}
	}

	/**
	 * Notifies model changed listeners when features/sites/etc. have changed.
	 * @param object
	 * @param property
	 */
	public static void fireObjectChanged(Object object, String property) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			IUpdateModelChangedListener listener =
				(IUpdateModelChangedListener) iter.next();
			listener.objectChanged(object, property);
		}
	}

	/**
	 * Returns true when any of the install operations requires a licence agreement.
	 * @param jobs
	 * @return
	 */
	public static boolean hasSelectedJobsWithLicenses(IInstallFeatureOperation[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateUtils.hasLicense(jobs[i].getFeature()))
				return true;
		}
		return false;
	}

	/**
	 * Returns true when any of the features to install has optional features.
	 * @param jobs
	 * @return
	 */
	public static boolean hasSelectedJobsWithOptionalFeatures(IInstallFeatureOperation[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateUtils.hasOptionalFeatures(jobs[i].getFeature()))
				return true;
		}
		return false;
	}

	/**
	 * Returns the list of operations that need a licence agreement.
	 * @param jobs
	 * @return
	 */
	public static IInstallFeatureOperation[] getSelectedJobsWithLicenses(IInstallFeatureOperation[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateUtils.hasLicense(jobs[i].getFeature()))
				list.add(jobs[i]);
		}
		return (IInstallFeatureOperation[]) list.toArray(
			new IInstallFeatureOperation[list.size()]);
	}

	/**
	 * Returns the list of operations that have optional features to install.
	 * @param jobs
	 * @return
	 */
	public static IInstallFeatureOperation[] getSelectedJobsWithOptionalFeatures(IInstallFeatureOperation[] jobs) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < jobs.length; i++) {
			if (UpdateUtils.hasOptionalFeatures(jobs[i].getFeature()))
				list.add(jobs[i]);
		}
		return (IInstallFeatureOperation[]) list.toArray(
			new IInstallFeatureOperation[list.size()]);
	}
	
	/**
	 * Sets whether any operations is in progress.
	 * @param inProgress
	 */
	public static synchronized void setInProgress(boolean inProgress) {
		OperationsManager.inProgress = inProgress;
	}
	
	/**
	 * Returns true when some operation is being executed, false otherwise.
	 * @return
	 */
	public static synchronized boolean isInProgress() {
		return inProgress;
	}

	/**
	 * Returns the operations validator.
	 * @return
	 */
	public static IOperationValidator getValidator() {
		if (validator == null)
			validator = new OperationValidator();
		return validator;
	}
	
	/**
	 * Sets a custom operation validator
	 * @param validator
	 */
	public static void setValidator(IOperationValidator validator) {
		OperationsManager.validator = validator;
	}
}
