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
import org.eclipse.update.core.*;


/**
 * Configure a feature.
 * FeatureConfigOperation
 */
public class FeatureInstallOperation extends PendingOperation {
	
	public FeatureInstallOperation(IFeature feature) {
		super(feature, INSTALL);
		
		IFeature[] installed =
		UpdateManager.getInstalledFeatures(feature);
		if (installed.length>0)
			this.oldFeature = installed[0];
	}
	
	public void execute() throws CoreException {		
	}
	
	public void undo() throws CoreException{
	}
}
