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

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.operations.*;

public class JobRoot {
	private IInstallConfiguration config;
	private IInstallFeatureOperation job;
	private FeatureHierarchyElement2[] elements;

	public JobRoot(IInstallConfiguration config, IInstallFeatureOperation job) {
		this.config = config;
		this.job = job;
	}

	public IInstallFeatureOperation getJob() {
		return job;
	}

	public FeatureHierarchyElement2[] getElements() {
		if (elements == null)
			computeElements();
		return elements;
	}

	public IFeature[] getUnconfiguredOptionalFeatures(
		IInstallConfiguration config,
		IConfiguredSite targetSite) {

		ArrayList unconfiguredOptionalFeatures = new ArrayList();
		getUnconfiguredOptionalFeatures(unconfiguredOptionalFeatures, config, targetSite, getElements(), UpdateManager.isPatch(job.getFeature()));
		IFeature[] unconfiguredOptionalFeaturesArray =
			new IFeature[unconfiguredOptionalFeatures.size()];
		unconfiguredOptionalFeatures.toArray(unconfiguredOptionalFeaturesArray);
		return unconfiguredOptionalFeaturesArray;
	}

	private void getUnconfiguredOptionalFeatures(
		ArrayList unconfiguredOptionalFeatures,
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		FeatureHierarchyElement2[] optionalElements,
		boolean isPatch) {
		for (int i = 0; i < optionalElements.length; i++) {
			FeatureHierarchyElement2[] children =
				optionalElements[i].getChildren(true, isPatch, config);
			getUnconfiguredOptionalFeatures(
				unconfiguredOptionalFeatures,
				config,
				targetSite,
				children,
				isPatch);
			if (!optionalElements[i].isEnabled(config)) {
				IFeature newFeature = optionalElements[i].getFeature();
				try {
					IFeature localFeature =
						UpdateManager.getLocalFeature(targetSite, newFeature);
					if (localFeature != null)
						unconfiguredOptionalFeatures.add(localFeature);
				} catch (CoreException e) {
					// Ignore this - we will leave with it
				}
			}
		}
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
		elements = new FeatureHierarchyElement2[list.size()];
		list.toArray(elements);
		for (int i = 0; i < elements.length; i++) {
			elements[i].setRoot(this);
		}
	}
}