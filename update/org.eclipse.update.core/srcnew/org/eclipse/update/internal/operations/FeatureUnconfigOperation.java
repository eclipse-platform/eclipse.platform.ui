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
 * FeatureUnconfigOperation
 */
public class FeatureUnconfigOperation extends PendingOperation {
	private IConfiguredSite site;
	
	public FeatureUnconfigOperation(IConfiguredSite site, IFeature feature) {
		super(feature, UNCONFIGURE);
		this.site = site;
	}
	
	public void execute() throws CoreException {
		site.unconfigure(feature);		
	}
	
	public void undo() throws CoreException{
		site.configure(feature);
	}
}
