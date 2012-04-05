/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.update.core.*;
import org.eclipse.update.operations.*;

public class FeatureSorter extends ViewerSorter {
	
	public static final int FEATURE_VERSION = 1;
	public static final int FEATURE_LABEL = 2;
	public static final int FEATURE_PROVIDER = 3;
	
	public static final int ASCENDING = 1;
	public static final int DESCENDING = -1;
	
	private int key = FEATURE_LABEL;
	private int labelOrder;
	private int versionOrder;
	private int providerOrder;
	
	public FeatureSorter(int key, int labelOrder, int versionOrder, int providerOrder) {
		this.key = key;
		this.labelOrder = labelOrder;
		this.versionOrder = versionOrder;
		this.providerOrder = providerOrder;
	}

	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e1 instanceof IInstallFeatureOperation)
			|| !(e2 instanceof IInstallFeatureOperation))
			return super.compare(viewer, e1, e2);
			
		IFeature f1 = ((IInstallFeatureOperation)e1).getFeature();
		IFeature f2 = ((IInstallFeatureOperation)e2).getFeature();
		
		PluginVersionIdentifier v1 = f1.getVersionedIdentifier().getVersion();
		PluginVersionIdentifier v2 = f2.getVersionedIdentifier().getVersion();
		
		String label1 = f1.getLabel() == null ? "" : f1.getLabel(); //$NON-NLS-1$
		String label2 = f2.getLabel() == null ? "" : f2.getLabel(); //$NON-NLS-1$
		
		String provider1 = f1.getProvider() == null ? "" : f1.getProvider(); //$NON-NLS-1$
		String provider2 = f2.getProvider() == null ? "" : f2.getProvider(); //$NON-NLS-1$
		
		int result = 0;
		if (key == FEATURE_VERSION) {
			result = compareVersions(v1, v2) * versionOrder;
			if (result == 0) {
				result = collator.compare(label1, label2) * labelOrder;
				if (result == 0)
					result = collator.compare(provider1, provider2) * providerOrder;
			}
		} else if (key == FEATURE_LABEL) {
			result = collator.compare(label1, label2) * labelOrder;
			if (result == 0) {
				result = compareVersions(v1, v2) * versionOrder;
				if (result == 0)
					result = collator.compare(provider1, provider2) * providerOrder;
			}
		} else if (key == FEATURE_PROVIDER) {
			result = collator.compare(provider1, provider2) * providerOrder;
			if (result == 0) {
				result = collator.compare(label1, label2) * labelOrder;
				if (result == 0)
					result = compareVersions(v1, v2) * versionOrder;
			}
		}
		return result;
	}
	
	private int compareVersions(PluginVersionIdentifier v1, PluginVersionIdentifier v2) {
		if (v1.equals(v2))
			return 0;
		return v2.isGreaterThan(v1) ? -1 : 1;
	}
	
}
