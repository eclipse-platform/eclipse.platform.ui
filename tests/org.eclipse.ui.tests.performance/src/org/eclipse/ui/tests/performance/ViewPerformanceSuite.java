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

import java.util.HashSet;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.performance.layout.ResizeTest;
import org.eclipse.ui.tests.performance.layout.ViewWidgetFactory;
import org.eclipse.ui.views.IViewDescriptor;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.1
 */
public class ViewPerformanceSuite extends TestSuite {

	public static final String PROJECT_EXPLORER = "org.eclipse.ui.navigator.ProjectExplorer";

	public static final String BASIC_PATH = "org.eclipse.ui";

	public static final String VIEWS_PATTERN = "org.eclipse.ui.views";

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static Test suite() {
		return new ViewPerformanceSuite();
	}

	/**
	 *
	 */
	public ViewPerformanceSuite() {
		addOpenCloseTests();
		addResizeTests();
		addTestSuite(OpenNavigatorFolderTest.class);
	}

	/**
	 *
	 */
	private void addOpenCloseTests() {
		String[] ids = getAllTestableViewIds();

		for (String id : ids) {
			addTest(new OpenCloseViewTest(id,
					id.equals(PROJECT_EXPLORER) ? BasicPerformanceTest.GLOBAL : BasicPerformanceTest.NONE));
		}
	}

	private void addResizeTests() {
		String[] ids = getAllTestableViewIds();

		for (String id : ids) {
			addTest(new ResizeTest(new ViewWidgetFactory(id)));
		}

	}

	public static String[] getAllTestableViewIds() {
		HashSet<String> result = new HashSet<>();

		IViewDescriptor[] descriptors = PlatformUI.getWorkbench()
				.getViewRegistry().getViews();
		for (IViewDescriptor descriptor : descriptors) {
			String[] categoryPath = descriptor.getCategoryPath();
			if (categoryPath == null)
				continue;
			for (String categoryPath1 : categoryPath) {
				// Only test basic views
				if (categoryPath1.equals(BASIC_PATH)) {
					if (descriptor.getId().indexOf(VIEWS_PATTERN) >= 0 || descriptor.getId().equals(PROJECT_EXPLORER)) {
						result.add(descriptor.getId());
					}
				}
			}
		}

		return result.toArray(new String[result.size()]);
	}
}
