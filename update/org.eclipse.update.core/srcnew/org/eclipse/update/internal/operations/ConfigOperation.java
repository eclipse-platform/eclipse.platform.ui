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
public class ConfigOperation extends SingleOperation implements IConfigOperation {
	
	public ConfigOperation(IInstallConfiguration config, IConfiguredSite site, IFeature feature, IOperationListener listener) {
		super(config, site, feature, listener);
	}
	
	public boolean execute(IProgressMonitor pm) throws CoreException {
		PendingOperation op =
			new PendingOperation(
				getInstallConfiguration(),
				getTargetSite(),
				getFeature(),
				PendingOperation.CONFIGURE);

		IStatus status = UpdateManager.getValidator().validatePendingChange(op);
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
				UpdateManager.getOperationsManager().findPendingOperation(feature);

			if (pendingOperation instanceof IUnconfigOperation) {
				// no need to do either pending change
				UpdateManager.getOperationsManager().removePendingOperation(
					pendingOperation);
			} else {
				UpdateManager.getOperationsManager().addPendingOperation(this);
				restartNeeded = true;
			}

			markProcessed();
			if (listener != null)
				listener.afterExecute(this);

			SiteManager.getLocalSite().save();

			return restartNeeded;
		} catch (CoreException e) {
			undo();
			UpdateManager.logException(e);
			throw e;
		}
	}
	
	public void undo() throws CoreException{
		targetSite.unconfigure(feature);
	}
}
