/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.mapping.IModelProviderDescriptor;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;

/**
 * Provides utility methods for dealing with model providers.
 * 
 * @since 3.2
 */
public final class ModelProviderUtil {

	public static ModelProvider[] getModelProvidersFor(ResourceMapping mapping) throws CoreException {
		// TODO: actually filter, and do a topological sort
		IModelProviderDescriptor[] descs = ModelProvider.getModelProviderDescriptors();
		List result = new ArrayList(descs.length);
		for (int i = 0; i < descs.length; i++) {
			ModelProvider provider = descs[i].getModelProvider();
			result.add(provider);
		}
		return (ModelProvider[]) result.toArray(new ModelProvider[result.size()]);
	}
	
}
