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

import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;


public class PendingOperation {

	protected IFeature feature;
	protected IFeature oldFeature;
	protected IInstallConfiguration config;
	protected IConfiguredSite targetSite;
	
	private boolean optionalDelta;
	private boolean processed;
	
	public PendingOperation(IFeature feature) {
		this(null, null, feature);
	}
	
	public PendingOperation(IInstallConfiguration config, IConfiguredSite targetSite, IFeature feature) {
		this.feature = feature;
		this.config = config;
		this.targetSite = targetSite;
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
}
