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
package org.eclipse.update.internal.ui.model;

import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.IFeature;


public class PendingChange extends SimpleFeatureAdapter {
	public static final int INSTALL = 0x1;
	public static final int UNINSTALL = 0x2;
	public static final int CONFIGURE = 0x3;
	public static final int UNCONFIGURE = 0x4;
	private int jobType;
	private IFeature oldFeature;
	private boolean optionalDelta;
	private IConfiguredSite defaultTargetSite;
	private boolean processed;
	
	public PendingChange(IFeature feature, int jobType) {
		super(feature);
		this.jobType = jobType;
	}
	
	public PendingChange(IFeature feature, IConfiguredSite targetSite) {
		this(feature, INSTALL);
		this.defaultTargetSite = targetSite;
	}
	
	public PendingChange(IFeature oldFeature, IFeature newFeature) {
		this(newFeature, INSTALL);
		this.oldFeature = oldFeature;
	}
	
	public PendingChange(IFeature oldFeature, IFeature newFeature, boolean optionalDelta) {
		this(oldFeature, newFeature);
		this.optionalDelta = optionalDelta;
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
	
	public IConfiguredSite getDefaultTargetSite() {
		return defaultTargetSite;
	}
	
	public boolean isProcessed() {
		return processed;
	}
	
	public void markProcessed() {
		processed = true;
	}
}
