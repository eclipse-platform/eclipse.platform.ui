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

/**
 * Unconfigure a feature.
 * UnconfigOperation
 */
public class UnconfigOperation extends PendingOperation {
	
	public UnconfigOperation(IInstallConfiguration config, IConfiguredSite site, IFeature feature) {
		super(config, site, feature, UNCONFIGURE);
	}
	
	public boolean execute(IProgressMonitor pm) throws CoreException {
		PatchCleaner2 cleaner = new PatchCleaner2(targetSite, feature);
		boolean result = targetSite.unconfigure(feature);
		cleaner.dispose();
		
		return result;
		// should we throw an exception when result == false ?
	}
	
	public void undo() throws CoreException{
		targetSite.configure(feature);
	}
}
