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
 * Configure a feature.
 * ConfigOperation
 */
public class ConfigOperation extends PendingOperation {
	private IInstallConfiguration config;
	private IConfiguredSite site;
	
	public ConfigOperation(IInstallConfiguration config, IConfiguredSite site, IFeature feature) {
		super(feature, CONFIGURE);
		this.config = config;
		this.site = site;
	}
	
	public void execute(IProgressMonitor pm) throws CoreException {
		site.configure(feature);
		UpdateManager.getOperationsManager().ensureUnique(config, feature, site);

		markProcessed();
		UpdateManager.getOperationsManager().fireObjectChanged(this, null);
	}
	
	public void undo() throws CoreException{
		site.unconfigure(feature);
	}
}
