/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.text.Collator;

import org.eclipse.core.resources.mapping.IModelProviderDescriptor;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class ModelProviderViewerSorter extends ViewerSorter {

	public ModelProviderViewerSorter() {
		super();
	}

	public ModelProviderViewerSorter(Collator collator) {
		super(collator);
	}
	
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof ModelProvider && e2 instanceof ModelProvider) {
			ModelProvider mp1 = (ModelProvider) e1;
			ModelProvider mp2 = (ModelProvider) e2;
			if (isExtends(mp1, mp2.getDescriptor())) {
				return 1;
			}
			if (isExtends(mp2, mp1.getDescriptor())) {
				return -1;
			}
			return mp1.getDescriptor().getLabel().compareTo(mp2.getDescriptor().getLabel());
		}
		return super.compare(viewer, e1, e2);
	}

	private boolean isExtends(ModelProvider mp1, IModelProviderDescriptor desc) {
		String[] extended = mp1.getDescriptor().getExtendedModels();
		for (int i = 0; i < extended.length; i++) {
			String id = extended[i];
			if (id.equals(desc.getId())) {
				return true;
			}
		}
		for (int i = 0; i < extended.length; i++) {
			String id = extended[i];
			IModelProviderDescriptor desc2 = ModelProvider.getModelProviderDescriptor(id);
			if (isExtends(mp1, desc2)) {
				return true;
			}
		}
		return false;
	}

}
