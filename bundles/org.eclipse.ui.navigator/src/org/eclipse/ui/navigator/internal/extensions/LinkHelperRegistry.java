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
 
package org.eclipse.ui.navigator.internal.extensions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public class LinkHelperRegistry extends RegistryReader {

	private static final NavigatorContentDescriptorManager CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorManager
			.getInstance();
 

	private static final ILinkHelper[] NO_LINK_HELPERS = new ILinkHelper[0];

	private static boolean isInitialized = false;

	private List descriptors; 

	private INavigatorContentService contentService;

	public LinkHelperRegistry(INavigatorContentService aContentService) {
		super(NavigatorPlugin.PLUGIN_ID, LinkHelperDescriptor.LINK_HELPER);
		contentService = aContentService;
	}
 

	// TODO Define more explicitly the expected order that LinkHelpers will be
	// returned
	public ILinkHelper[] getLinkHelpersFor(IStructuredSelection aSelection) {

		if (aSelection.isEmpty())
			return NO_LINK_HELPERS;

		Set contentDescriptors = CONTENT_DESCRIPTOR_REGISTRY
				.getEnabledContentDescriptors(aSelection.getFirstElement(),
						contentService.getViewerDescriptor());
		if (contentDescriptors.isEmpty())
			return NO_LINK_HELPERS;

		/* Use the first Navigator Content LinkHelperDescriptor for now */
		INavigatorContentDescriptor contentDescriptor = (INavigatorContentDescriptor) contentDescriptors
				.iterator().next();

		List helpersList = new ArrayList();
		ILinkHelper[] helpers = NO_LINK_HELPERS;
		LinkHelperDescriptor descriptor = null;
		for (Iterator itr = getDescriptors().iterator(); itr.hasNext();) {
			descriptor = (LinkHelperDescriptor) itr.next();
			if (descriptor.isEnabledFor(contentDescriptor.getId()))
				helpersList.add(descriptor.getLinkHelper());
			else if (descriptor.isEnabledFor(aSelection))
				helpersList.add(descriptor.getLinkHelper());
		}
		if (helpersList.size() > 0)
			helpersList
					.toArray((helpers = new ILinkHelper[helpersList.size()]));

		return helpers;
	}

	public ILinkHelper[] getLinkHelpersFor(IEditorInput input) {
		List helpersList = new ArrayList();
		ILinkHelper[] helpers = new ILinkHelper[0];
		LinkHelperDescriptor descriptor = null;
		for (Iterator itr = getDescriptors().iterator(); itr.hasNext();) {
			descriptor = (LinkHelperDescriptor) itr.next();
			if (descriptor.isEnabledFor(input))
				helpersList.add(descriptor.getLinkHelper());
		}
		if (helpersList.size() > 0)
			helpersList
					.toArray((helpers = new ILinkHelper[helpersList.size()]));

		return helpers;
	}

 
	public boolean readElement(IConfigurationElement element) {
		if (LinkHelperDescriptor.LINK_HELPER.equals(element.getName())) {
			getDescriptors().add(new LinkHelperDescriptor(element));
			return true;
		}
		return false;
	}

	/**
	 * @return
	 */
	protected List getDescriptors() {
		if (descriptors == null)
			descriptors = new ArrayList();
		return descriptors;
	}
}
