/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.filters;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorExtensionFilter;
import org.eclipse.ui.navigator.internal.Utilities;
import org.eclipse.ui.navigator.internal.NavigatorContentService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptorManager;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class CommonViewerFilter extends ViewerFilter {

	private static final NavigatorContentDescriptorManager CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorManager.getInstance();
	private final CommonViewer commonViewer;
	private final INavigatorContentService contentService;

	/**
	 *  @param aViewer The viewer that this filter will service.
	 */
	public CommonViewerFilter(CommonViewer aViewer) {
		super();
		commonViewer = aViewer;
		contentService = aViewer.getNavigatorContentService();
	}

	/** 
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		boolean select = true;
		Set contentDescriptors = contentService.findEnabledContentDescriptors(element);

		for (Iterator descriptorsIterator = contentDescriptors.iterator(); descriptorsIterator.hasNext() && select;) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) descriptorsIterator.next();
			if (Utilities.isActive(contentService.getViewerDescriptor(), descriptor) &&
					Utilities.isVisible(contentService.getViewerDescriptor(), descriptor)) 
			{
				ExtensionFilterDescriptor[] enabledFilters = ExtensionFilterRegistryManager.getInstance().getViewerRegistry(contentService.getViewerId()).getActiveDescriptors(descriptor.getId());

				for (int filterindx = 0; filterindx < enabledFilters.length; filterindx++) {
					INavigatorExtensionFilter filter = enabledFilters[filterindx].getInstance();
					/*
					 * System.out.println("Element: " + element + " isFiltered: " +
					 * !filter.select(getExtensionSite(), parentElement, element));
					 */
					if (!filter.select(commonViewer, parentElement, element)) {
						return false; 
					}
				}
				NavigatorContentExtension extension = ((NavigatorContentService)contentService).getExtension(descriptor);
				
				INavigatorExtensionFilter[] enforcedFilters = extension.getDuplicateContentFilters();
				for(int i=0; i<enforcedFilters.length; i++)
					if(!enforcedFilters[i].select(commonViewer, parentElement, element)) 
						return false;				
			}
		}

		return select;
	}

}
