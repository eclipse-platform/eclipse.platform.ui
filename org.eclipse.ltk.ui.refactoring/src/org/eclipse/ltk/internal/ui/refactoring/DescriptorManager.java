/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;


public abstract class DescriptorManager {

	private String fExtensionPoint;
	private String fVariableName;
	private AbstractDescriptor[] fExtensions;

	public DescriptorManager(String extensionPoint, String variableName) {
		Assert.isNotNull(extensionPoint);
		Assert.isNotNull(variableName);
		fExtensionPoint= extensionPoint;
		fVariableName= variableName;
	}

	public AbstractDescriptor getDescriptor(Object element) throws CoreException {
		if (fExtensions == null)
			init();

		List candidates= new ArrayList(1);
		for (int i= 0; i < fExtensions.length; i++) {
			AbstractDescriptor descriptor= fExtensions[i];
			if (descriptor.matches(element, fVariableName)) {
				candidates.add(descriptor);
			}
			descriptor.clear();
		}
		if (candidates.size() == 0)
			return null;
		// No support for conflicts yet.
		return (AbstractDescriptor)candidates.get(0);
	}

	protected abstract AbstractDescriptor createDescriptor(IConfigurationElement element);

	// ---- extension point reading -----------------------------------

	private void init() {
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IConfigurationElement[] ces= registry.getConfigurationElementsFor(
			RefactoringUIPlugin.getPluginId(),
			fExtensionPoint);
		fExtensions= new AbstractDescriptor[ces.length];
		for (int i= 0; i < ces.length; i++) {
			fExtensions[i]= createDescriptor(ces[i]);
		}
	}
}
