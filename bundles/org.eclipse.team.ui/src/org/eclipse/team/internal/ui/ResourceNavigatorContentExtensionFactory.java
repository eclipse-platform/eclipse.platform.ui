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
package org.eclipse.team.internal.ui;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.team.internal.ui.dialogs.ResourceMappingLabelProvider;
import org.eclipse.team.internal.ui.mapping.*;
import org.eclipse.team.ui.mapping.*;

public class ResourceNavigatorContentExtensionFactory implements
		INavigatorContentExtensionFactory {

	public NavigatorContentExtension createProvider(ITeamViewerContext context) {
		return new NavigatorContentExtension(context) {
			private ResourceMappingContentProvider resourceMappingContentProvider;
			public IResourceMappingContentProvider getContentProvider() {
				if (resourceMappingContentProvider == null)
					resourceMappingContentProvider = new ResourceMappingContentProvider(getContext(), ModelProvider.RESOURCE_MODEL_PROVIDER_ID);
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
