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

import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.model.*;

/**
 * PendingOperationAdapter
 */
public class PendingOperationAdapter extends SimpleFeatureAdapter {
	private PendingOperation job;
	
	/**
	 * @param feature
	 */
	public PendingOperationAdapter(PendingOperation job) {
		super(job.getFeature());
		this.job = job;
	}

	/**
	 * @param feature
	 * @param optional
	 */
	public PendingOperationAdapter(PendingOperation job, boolean optional) {
		super(job.getFeature(), optional);
		this.job = job;
	}

	public PendingOperation getJob() {
		return job;
	}
	
	public IFeature getFeature() {
		return job.getFeature();
	}
}
