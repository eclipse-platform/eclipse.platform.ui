/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.jface.viewers.ILabelProvider;

public class ResourceNavigatorContentExtensionFactory implements
		INavigatorContentExtensionFactory {

	private final ModelProvider provider;

	public ResourceNavigatorContentExtensionFactory(ModelProvider provider) {
		this.provider = provider;
	}

	public NavigatorContentExtension createProvider(final ModelProvider provider) {
		return new NavigatorContentExtension(provider) {
			private ResourceMappingContentProvider resourceMappingContentProvider;
			public IResourceMappingContentProvider getContentProvider() {
				if (resourceMappingContentProvider == null)
					resourceMappingContentProvider = new ResourceMappingContentProvider(provider);
				return resourceMappingContentProvider;
			}
			public void dispose() {
				resourceMappingContentProvider.dispose();
				super.dispose();
			}
		};
	}

	public ILabelProvider getLabelProvider() {
		return new ResourceMappingLabelProvider();
	}

}
