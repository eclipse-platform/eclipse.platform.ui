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

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.operations.*;

public class BatchInstallOperation
	extends Operation
	implements IMultiOperation {
		
	private static final String KEY_INSTALLING = "OperationsManager.installing";
	protected IInstallOperation[] operations;

	public BatchInstallOperation(IInstallOperation[] operations) {
		super(null);
		this.operations = operations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.operations.IMultiOperation#getOperations()
	 */
	public ISingleOperation[] getOperations() {
		return operations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.operations.IOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
		int installCount = 0;

		try {
			if (operations == null || operations.length == 0)
				return false;
			UpdateManager.makeConfigurationCurrent(
				operations[0].getInstallConfiguration(),
				null);
			monitor.beginTask(
				UpdateManager.getString(KEY_INSTALLING),
				operations.length);
			for (int i = 0; i < operations.length; i++) {
				SubProgressMonitor subMonitor =
					new SubProgressMonitor(
						monitor,
						1,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);

				boolean needsRestart = operations[i].execute(subMonitor);
				UpdateManager.getOperationsManager().addPendingOperation(operations[i]);

				operations[i].markProcessed();
				if (operations[i].getOperationListener() != null)
					operations[i].getOperationListener().afterExecute(operations[i]);

				IFeature oldFeature = operations[i].getOldFeature();
				if (oldFeature != null
					//&& !operations[i].isOptionalDelta()
					&& operations[i].getOptionalElements() != null) {
					preserveOptionalState(
					operations[i].getInstallConfiguration(),
					operations[i].getTargetSite(),
						UpdateManager.isPatch(operations[i].getFeature()),
						operations[i].getOptionalElements());
				}

				//monitor.worked(1);
				UpdateManager.saveLocalSite();
				installCount++;
			}
			// should we just return true ?
			return true;
			//return installCount == operations.length;
		} catch (InstallAbortedException e) {
			throw new InvocationTargetException(e);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
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
}
