/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * @since 3.2
 * 
 */
public class LinkHelperManager {

	private static final LinkHelperManager instance = new LinkHelperManager();

	private static final LinkHelperDescriptor[] NO_DESCRIPTORS = new LinkHelperDescriptor[0];

	private List descriptors;

	/**
	 * @return the singleton instance.
	 */
	public static LinkHelperManager getInstance() {
		return instance;
	}

	private LinkHelperManager() {
		new LinkHelperRegistry().readRegistry();
	}

	/**
	 * Return the link helper descriptors for a given selection and content
	 * service.
	 * 
	 * @param anObject
	 *            An object from the viewer.
	 * @param aContentService
	 *            The content service to use for visibility filtering. Link
	 *            Helpers are filtered by contentExtension elements in
	 *            viewerContentBindings.
	 * @return An array of <i>visible</i> and <i>enabled</i> Link Helpers or
	 *         an empty array.
	 */
	public LinkHelperDescriptor[] getLinkHelpersFor(
			Object anObject,
			INavigatorContentService aContentService) {

		List helpersList = new ArrayList();
		LinkHelperDescriptor descriptor = null;
		for (Iterator itr = getDescriptors().iterator(); itr.hasNext();) {
			descriptor = (LinkHelperDescriptor) itr.next();
			if (aContentService.isVisible(descriptor.getId())
					&& descriptor.isEnabledFor(anObject)) {
				helpersList.add(descriptor);
			}
		}
		if (helpersList.size() == 0) {
			return NO_DESCRIPTORS;
		}
		return (LinkHelperDescriptor[]) helpersList
				.toArray(new LinkHelperDescriptor[helpersList.size()]);

	}

	/**
	 * Return the link helper descriptors for a given selection and content
	 * service.
	 * 
	 * @param anInput
	 *            The input of the active viewer.
	 * @param aContentService
	 *            The content service to use for visibility filtering. Link
	 *            Helpers are filtered by contentExtension elements in
	 *            viewerContentBindings.
	 * @return An array of <i>visible</i> and <i>enabled</i> Link Helpers or
	 *         an empty array.
	 */
	public LinkHelperDescriptor[] getLinkHelpersFor(IEditorInput anInput,
			INavigatorContentService aContentService) {

		List helpersList = new ArrayList();
		LinkHelperDescriptor descriptor = null;
		for (Iterator itr = getDescriptors().iterator(); itr.hasNext();) {
			descriptor = (LinkHelperDescriptor) itr.next();
			if (aContentService.isVisible(descriptor.getId())
					&& descriptor.isEnabledFor(anInput)) {
				helpersList.add(descriptor);
			}
		}
		if (helpersList.size() == 0) {
			return NO_DESCRIPTORS;
		}
		return (LinkHelperDescriptor[]) helpersList
				.toArray(new LinkHelperDescriptor[helpersList.size()]);

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

		public boolean readElement(final IConfigurationElement element) {
			if (LINK_HELPER.equals(element.getName())) {
				final boolean retValue[] = new boolean[1];
				SafeRunner.run(new NavigatorSafeRunnable(element) {
					public void run() throws Exception {
						getDescriptors().add(new LinkHelperDescriptor(element));
						retValue[0] = true;
					}
				});
				return retValue[0];
			}
			return false;
		}
	}
}
