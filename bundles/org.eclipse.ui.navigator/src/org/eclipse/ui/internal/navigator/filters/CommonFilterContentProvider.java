/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal.navigator.filters;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 *
 * @since 3.2
 */
public class CommonFilterContentProvider implements IStructuredContentProvider {

	private INavigatorContentService contentService;
	private Object[] NO_ELEMENTS = new Object[0];


	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof INavigatorContentService) {
			contentService = (INavigatorContentService) newInput;
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(contentService != null) {
			NavigatorFilterService filterService = (NavigatorFilterService) contentService.getFilterService();
			return filterService.getVisibleFilterDescriptorsForUI();
		}
		return NO_ELEMENTS ;

	}

	@Override
	public void dispose() {

	}

}
