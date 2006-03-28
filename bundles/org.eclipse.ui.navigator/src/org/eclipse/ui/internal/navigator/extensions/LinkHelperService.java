/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.extensions;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.ILinkHelper;

/**
 * @since 3.2
 * 
 */
public class LinkHelperService {

	private static final ILinkHelper[] CANT_GET_NO_HELP = new ILinkHelper[0];

	private NavigatorContentService contentService;

	private final Map linkHelpers = new HashMap();

	/**
	 * @param aContentService
	 *            The associated content service for this link helper service.
	 */
	public LinkHelperService(NavigatorContentService aContentService) {
		contentService = aContentService;
	}

	/**
	 * 
	 * @param anObject
	 *            An object from the viewer
	 * @return An array of link helpers that know about elements in the
	 *         selection or null
	 */
	public ILinkHelper[] getLinkHelpersFor(Object anObject) {

		LinkHelperDescriptor[] descriptors = LinkHelperManager.getInstance()
				.getLinkHelpersFor(anObject, contentService);
		if (descriptors.length == 0) {
			return CANT_GET_NO_HELP;
		}

		Set helpers = new LinkedHashSet();
		for (int i = 0; i < descriptors.length; i++) {
			helpers.add(getLinkHelper(descriptors[i]));
		}
		if (helpers.size() == 0)
			return CANT_GET_NO_HELP;
		return (ILinkHelper[]) helpers.toArray(new ILinkHelper[helpers.size()]);

	}

	/**
	 * 
	 * @param input
	 *            The Editor input from the active viewer.
	 * @return An array of link helpers that know about elements in the
	 *         selection or null
	 */
	public ILinkHelper[] getLinkHelpersFor(IEditorInput input) {
		LinkHelperDescriptor[] descriptors = LinkHelperManager.getInstance()
				.getLinkHelpersFor(input, contentService);
		if (descriptors.length == 0) {
			return CANT_GET_NO_HELP;
		}

		Set helpers = new LinkedHashSet();
		for (int i = 0; i < descriptors.length; i++) {
			helpers.add(getLinkHelper(descriptors[i]));
		}
		if (helpers.size() == 0)
			return CANT_GET_NO_HELP;
		return (ILinkHelper[]) helpers.toArray(new ILinkHelper[helpers.size()]);
	}

	private ILinkHelper getLinkHelper(LinkHelperDescriptor descriptor) {
		ILinkHelper helper = (ILinkHelper) linkHelpers.get(descriptor);
		if (helper == null) {
			synchronized (this) {
				if (helper == null) {
					linkHelpers.put(descriptor, helper = descriptor
							.createLinkHelper());
				}
			}
		}
		return helper;
	}
}
