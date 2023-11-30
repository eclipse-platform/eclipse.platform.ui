/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * @since 3.2
 */
public class CommonContentExtensionSite extends CommonExtensionSite implements
		ICommonContentExtensionSite {

	private NavigatorContentExtension extension;

	private IMemento memento;

	private NavigatorContentService contentService;

	/**
	 * Create a config element for the initialization of Content Extensions.
	 *
	 * @param anExtensionId
	 *            The unique identifier of the associated content extension or
	 *            the top-level action provider. <b>May NOT be null.</b>
	 * @param aContentService
	 *            The associated content service to allow coordination with
	 *            content extensions via the IExtensionStateModel. Clients may
	 *            access the content providers and label providers as necessary
	 *            also to render labels or images in their UI. <b>May NOT be
	 *            null.</b>
	 * @param aMemento
	 *            The memento associated with the parent viewer.
	 */
	public CommonContentExtensionSite(String anExtensionId,
			NavigatorContentService aContentService, IMemento aMemento) {
		super(aContentService, anExtensionId);

		NavigatorContentDescriptor contentDescriptor = NavigatorContentDescriptorManager
				.getInstance().getContentDescriptor(anExtensionId);

		extension = aContentService.getExtension(contentDescriptor);
		memento = aMemento;
		contentService = aContentService;
	}

	@Override
	public IMemento getMemento() {
		return memento;
	}

	@Override
	public INavigatorContentExtension getExtension() {
		return extension;
	}

	@Override
	public INavigatorContentService getService() {
		return contentService;
	}

}
