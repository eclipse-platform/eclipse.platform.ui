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

import java.util.*;

import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;


public class JobRoot {
	private IInstallConfiguration config;
	private PendingOperation job;
	private Object[] elements;
	
	public JobRoot(IInstallConfiguration config, PendingOperation job) {
		this.config = config;
		this.job = job;
	}

	public PendingOperation getJob() {
		return job;
	}

	public Object[] getElements() {
		if (elements == null)
			computeElements();
		return elements;
	}

	private void computeElements() {
		IFeature oldFeature = job.getOldFeature();
		IFeature newFeature = job.getFeature();
		ArrayList list = new ArrayList();
		boolean patch = UpdateManager.isPatch(newFeature);
		FeatureHierarchyElement2.computeElements(
			oldFeature,
			newFeature,
			oldFeature != null,
			patch,
			config,
			list);
		elements = list.toArray();
		for (int i = 0; i < elements.length; i++) {
			FeatureHierarchyElement2 element =
				(FeatureHierarchyElement2) elements[i];
			element.setRoot(this);
		}
	}
}