/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.navigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.extensions.LinkHelperDescriptor;
import org.eclipse.ui.internal.navigator.extensions.LinkHelperManager;

/**
 * Manages the link helpers which are used to define the behavior of
 * the link with editor function.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 3.4
 */
public class LinkHelperService {

	private static final ILinkHelper[] CANT_GET_NO_HELP = new ILinkHelper[0];

	private NavigatorContentService contentService;

	private final Map linkHelpers = new HashMap();

	/**
	 * @param aContentService
	 *            The associated content service for this link helper service.
	 */
	LinkHelperService(NavigatorContentService aContentService) {
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
	
	/**
	 * Return a selection that contains the elements that the given editor input 
	 * represent.
	 * @param input the editor input
	 * @return a selection that contains the elements that the given editor input 
	 * represent
	 */
	public IStructuredSelection getSelectionFor(IEditorInput input) {
		ILinkHelper[] helpers = getLinkHelpersFor(input);

		IStructuredSelection selection = StructuredSelection.EMPTY;
		IStructuredSelection newSelection = StructuredSelection.EMPTY;

		for (int i = 0; i < helpers.length; i++) {
			selection = helpers[i].findSelection(input);
			if (selection != null && !selection.isEmpty()) {
				newSelection = mergeSelection(newSelection, selection);
			}
		}
		return newSelection;
	} 
	
	private IStructuredSelection mergeSelection(IStructuredSelection aBase,
			IStructuredSelection aSelectionToAppend) {
		if (aBase == null || aBase.isEmpty()) {
			return (aSelectionToAppend != null) ? aSelectionToAppend
					: StructuredSelection.EMPTY;
		} else if (aSelectionToAppend == null || aSelectionToAppend.isEmpty()) {
			return aBase;
		} else {
			List newItems = new ArrayList(aBase.toList());
			newItems.addAll(aSelectionToAppend.toList());
			return new StructuredSelection(newItems);
		}
	}
}
