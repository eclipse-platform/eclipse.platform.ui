/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - 226046 Add filter for user-spec'd patterns
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.filters.UserFilter;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * This filter will read with {@link CommonViewer#getData(String)} the value of {@link NavigatorPlugin#RESOURCE_REGEXP_FILTER_DATA}
 * and evaluate whether one of the filters enabled on this current viewer hides resources.
 */
public class ResourceNameRegexpFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		IResource resource = Adapters.adapt(element, IResource.class);
		if (resource != null && viewer.getData(NavigatorPlugin.RESOURCE_REGEXP_FILTER_DATA) != null) {
			@SuppressWarnings("unchecked")
			List<UserFilter> filters = (List<UserFilter>)viewer.getData(NavigatorPlugin.RESOURCE_REGEXP_FILTER_DATA);
			for (UserFilter filter : filters) {
				if (filter.isEnabled() && filter.matches(resource.getName())) {
					return false;
				}
			}
		}
		return true;
	}

}
