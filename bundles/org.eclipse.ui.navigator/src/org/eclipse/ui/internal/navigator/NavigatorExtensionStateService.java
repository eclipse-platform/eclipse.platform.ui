/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.internal.navigator.extensions.ExtensionStateModel;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * 
 * A content extension may have its content and label providers disposed
 * whenever the extension is activated or deactivated. However, the state model
 * lives throughout the life of the viewer to allow Action Providers to drive
 * their behavior from it.
 * 
 * @since 3.3
 * 
 */
public class NavigatorExtensionStateService {

	private final Object lock = new Object();
	private INavigatorContentService contentService;

	/**
	 * Create an ExtensionStateServie that will keep track of the state models
	 * of content extensions.
	 * 
	 * @param theContentService
	 *            The content service which manages this state model service.
	 */
	public NavigatorExtensionStateService(INavigatorContentService theContentService) {
		contentService = theContentService;
	}

	/*
	 * A map of (String-based-Navigator-Content-Extension-IDs,
	 * NavigatorContentExtension-objects)-pairs
	 */
	private final Map/* <INavigatorContentDescriptor, IExtensionStateModel> */stateModels = new HashMap();

	/**
	 * Return the state model for the given descriptor. 
	 * 
	 * @param aDescriptor A content descriptor 
	 * @return The state model for the given descriptor.
	 */
	public IExtensionStateModel getExtensionStateModel(
			INavigatorContentDescriptor aDescriptor) {
		synchronized (lock) {
			IExtensionStateModel model = (IExtensionStateModel) stateModels
					.get(aDescriptor);
			if (model == null)
				stateModels.put(aDescriptor, model = new ExtensionStateModel(
						aDescriptor.getId(), contentService.getViewerId()));
			return model;
		}
	}

}
