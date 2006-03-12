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

package org.eclipse.ui.internal.navigator.extensions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.ILinkHelper;

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
public class LinkHelperManager {

	private static final ILinkHelper[] NO_LINK_HELPERS = new ILinkHelper[0];

	private static final LinkHelperManager instance = new LinkHelperManager();

	private List descriptors;

//	private INavigatorContentService contentService;
	
	/**
	 * Return the singleton instance.
	 */
	public static LinkHelperManager getInstance() {
		return instance;
	}

	private LinkHelperManager() {
		new LinkHelperRegistry().readRegistry();
	}

	// TODO Define more explicitly the expected order that LinkHelpers will be
	// returned
	public ILinkHelper[] getLinkHelpersFor(IStructuredSelection aSelection) {

//		if (aSelection.isEmpty()) {
			return NO_LINK_HELPERS;
//		}
//
//		Set contentDescriptors = contentService
//				.findContentExtensionsWithPossibleChild(aSelection
//						.getFirstElement());
//		if (contentDescriptors.isEmpty()) {
//			return NO_LINK_HELPERS;
//		}
//
//		/* Use the first Navigator Content LinkHelperDescriptor for now */
//		INavigatorContentExtension contentExtension = (INavigatorContentExtension) contentDescriptors
//				.iterator().next();
//
//		List helpersList = new ArrayList();
//		ILinkHelper[] helpers = NO_LINK_HELPERS;
//		LinkHelperDescriptor descriptor = null;
//		for (Iterator itr = getDescriptors().iterator(); itr.hasNext();) {
//			descriptor = (LinkHelperDescriptor) itr.next();
//			if (descriptor.isEnabledFor(contentExtension.getId())) {
//				helpersList.add(getLinkHelper(descriptor));
//			} else if (descriptor.isEnabledFor(aSelection)) {
//				helpersList.add(getLinkHelper(descriptor));
//			}
//		}
//		if (helpersList.size() > 0) {
//			helpersList
//					.toArray((helpers = new ILinkHelper[helpersList.size()]));
//		}
//
//		return helpers;
	}

//	/**
//	 * @param descriptor
//	 * @return
//	 */
//	private Object getLinkHelper(LinkHelperDescriptor descriptor) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public ILinkHelper[] getLinkHelpersFor(IEditorInput input) {
		List helpersList = new ArrayList();
		ILinkHelper[] helpers = new ILinkHelper[0];
//		LinkHelperDescriptor descriptor = null;
//		for (Iterator itr = getDescriptors().iterator(); itr.hasNext();) {
//			descriptor = (LinkHelperDescriptor) itr.next();
//			if (descriptor.isEnabledFor(input)) {
//				helpersList.add(descriptor.getLinkHelper());
//			}
//		}
		if (helpersList.size() > 0) {
			helpersList
					.toArray((helpers = new ILinkHelper[helpersList.size()]));
		}

		return helpers;
	}

	protected List getDescriptors() {
		if (descriptors == null) {
			descriptors = new ArrayList();
		}
		return descriptors;
	}

	private class LinkHelperRegistry extends RegistryReader implements
			ILinkHelperExtPtConstants {

		private LinkHelperRegistry() {
			super(NavigatorPlugin.PLUGIN_ID, LINK_HELPER);
		}

		public boolean readElement(IConfigurationElement element) {
			if (LINK_HELPER.equals(element.getName())) {
				getDescriptors().add(new LinkHelperDescriptor(element));
				return true;
			}
			return false;
		}
	}
}
