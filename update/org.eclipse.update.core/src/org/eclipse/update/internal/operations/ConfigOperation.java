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

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.operations.*;

/**
 * Configure a feature.
 * ConfigOperation
 */
public class ConfigOperation
	extends FeatureOperation
	implements IConfigFeatureOperation {

	public ConfigOperation(
		IInstallConfiguration config,
		IConfiguredSite site,
		IFeature feature) {
		super(config, site, feature);
	}

	public boolean execute(IProgressMonitor pm, IOperationListener listener)
		throws CoreException {

		IStatus status =
			UpdateUtils.getValidator().validatePendingConfig(feature);
		if (status != null) {
			throw new CoreException(status);
		}

		targetSite.configure(feature);
		ensureUnique();

		try {
			// Restart not needed
			boolean restartNeeded = false;

			// Check if this operation is cancelling one that's already pending
			IOperation pendingOperation =
				OperationsManager.findPendingOperation(feature);

			if (pendingOperation instanceof IUnconfigFeatureOperation) {
				// no need to do either pending change
				OperationsManager.removePendingOperation(pendingOperation);
			} else {
				OperationsManager.addPendingOperation(this);
				restartNeeded = true;
			}

			markProcessed();
			if (listener != null)
				listener.afterExecute(this, null);

			SiteManager.getLocalSite().save();

			// notify the model
			OperationsManager.fireObjectChanged(feature, null);

			return restartNeeded;
		} catch (CoreException e) {
			undo();
			UpdateUtils.logException(e);
			throw e;
		}
	}

	public void undo() throws CoreException {
		targetSite.unconfigure(feature);
	}
}
