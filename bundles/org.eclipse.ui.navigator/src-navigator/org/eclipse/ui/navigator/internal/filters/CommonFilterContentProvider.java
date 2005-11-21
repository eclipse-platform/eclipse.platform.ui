/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.navigator.internal.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.internal.NavigatorContentService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptorRegistry;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2 
 *
 */
public class CommonFilterContentProvider implements IStructuredContentProvider {

	private static final NavigatorContentDescriptorRegistry CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorRegistry.getInstance();
	private INavigatorContentService contentService;

	public CommonFilterContentProvider() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof NavigatorContentService)
			contentService = (INavigatorContentService) newInput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {

		ExtensionFilterViewerRegistry filterRegistry = ExtensionFilterRegistryManager.getInstance().getViewerRegistry(contentService.getViewerId());

		List results = new ArrayList();
		NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY.getAllContentDescriptors();
		for (int i = 0; i < descriptors.length; i++)
			results.addAll(Arrays.asList(filterRegistry.getAllDescriptors(descriptors[i].getId())));

		return results.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {

	}

}
