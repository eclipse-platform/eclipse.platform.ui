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

package org.eclipse.ui.internal.navigator.dnd;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.extensions.CommonDragAssistantDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorViewerDescriptor;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorDnDService;

/**
 * 
 * Provides instances of {@link CommonDragAdapterAssistant} and
 * {@link CommonDropAdapterAssistant} for the associated
 * {@link INavigatorContentService}.
 * 
 * @since 3.2
 * 
 */
public class NavigatorDnDService implements INavigatorDnDService {

	private NavigatorContentService contentService;

	private CommonDragAdapterAssistant[] dragAssistants;

	/**
	 * 
	 * @param aContentService The associated content service
	 */
	public NavigatorDnDService(NavigatorContentService aContentService) {
		contentService = aContentService;
	}

	public synchronized CommonDragAdapterAssistant[] getCommonDragAssistants() {

		if (dragAssistants == null) {
			int i = 0;
			Set dragDescriptors = ((NavigatorViewerDescriptor) contentService
					.getViewerDescriptor()).getDragAssistants();
			dragAssistants = new CommonDragAdapterAssistant[dragDescriptors
					.size()];
			for (Iterator iter = dragDescriptors.iterator(); iter.hasNext();) {
				CommonDragAssistantDescriptor descriptor = (CommonDragAssistantDescriptor) iter
						.next();
				dragAssistants[i++] = descriptor.createDragAssistant();
			}
		}
		return dragAssistants;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorDnDService#findCommonDropAdapterAssistants(java.lang.Object)
	 */
	public CommonDropAdapterAssistant[] findCommonDropAdapterAssistants(Object aDropTarget) {
		// TODO Implement the search routine
		return null;
	}

}
