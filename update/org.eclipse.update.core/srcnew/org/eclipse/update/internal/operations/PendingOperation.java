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


public abstract class PendingOperation {
	public static final int INSTALL = 0x1;
	public static final int UNINSTALL = 0x2;
	public static final int CONFIGURE = 0x3;
	public static final int UNCONFIGURE = 0x4;
	
	protected IFeature feature;
	protected IFeature oldFeature;
	protected IInstallConfiguration config;
	protected IConfiguredSite targetSite;
	
	private int jobType;
	private boolean optionalDelta;
	private boolean processed;
	
	protected PendingOperation(IFeature feature, int jobType) {
		this(null, null, feature, jobType);
	}
	
	public PendingOperation(IInstallConfiguration config, IConfiguredSite targetSite, IFeature feature, int jobType) {
		this.feature = feature;
		this.jobType = jobType;
		this.config = config;
		this.targetSite = targetSite;
	}

	public int getJobType() {
		return jobType;
	}
	
	public IFeature getFeature() {
		return feature;
	}
	
	public IFeature getOldFeature() {
		return oldFeature;
	}
	public boolean isOptionalDelta() {
		return optionalDelta;
	}
	
	public IConfiguredSite getTargetSite() {
		return targetSite;
	}
	
	public IInstallConfiguration getInstallConfiguration() {
		return config;
	}
	public void setInstallConfiguration(IInstallConfiguration config) {
		this.config = config;
	}

	public void setTargetSite(IConfiguredSite targetSite) {
		this.targetSite = targetSite;
	}
	
	public boolean isProcessed() {
		return processed;
	}
	
	public void markProcessed() {
		processed = true;
	}
	
	public void enable(boolean enable) {
		// this should register with the operation manager
		// used to be called setModel
	}
	
	/**
	 * Returns true if restart is needed
	 * @param pm
	 * @return
	 * @throws CoreException
	 */
	public abstract boolean execute(IProgressMonitor pm) throws CoreException;
	
	public void undo()  throws CoreException{
	}
}
