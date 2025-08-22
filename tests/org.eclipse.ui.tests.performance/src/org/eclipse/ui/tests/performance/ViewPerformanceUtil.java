/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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

package org.eclipse.ui.tests.performance;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * @since 3.1
 */
public final class ViewPerformanceUtil {

	public static final String PROJECT_EXPLORER = "org.eclipse.ui.navigator.ProjectExplorer";

	private static final String BASIC_PATH = "org.eclipse.ui";

	private static final String VIEWS_PATTERN = "org.eclipse.ui.views";

	private ViewPerformanceUtil() {
	}

	public static Collection<String> getAllTestableViewIds() {
		HashSet<String> result = new HashSet<>();

		IViewDescriptor[] descriptors = PlatformUI.getWorkbench().getViewRegistry().getViews();
		for (IViewDescriptor descriptor : descriptors) {
			String[] categoryPath = descriptor.getCategoryPath();
			if (categoryPath == null)
				continue;
			for (String categoryPath1 : categoryPath) {
				// Only test basic views
				if (categoryPath1.equals(BASIC_PATH)) {
					if (descriptor.getId().contains(VIEWS_PATTERN) || descriptor.getId().equals(PROJECT_EXPLORER)) {
						result.add(descriptor.getId());
					}
				}
			}
		}

		return result;
	}
}
