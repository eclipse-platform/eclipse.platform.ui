/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.tests.performance.layout.ResizeTest;
import org.eclipse.ui.tests.performance.layout.ViewWidgetFactory;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * @since 3.1
 */
public class ViewPerformanceSuite extends TestSuite {

	public static final String RESOURCE_NAVIGATOR = "org.eclipse.ui.views.ResourceNavigator";

	public static final String BASIC_PATH = "org.eclipse.ui";

	// public static final String [] VIEW_IDS = {BASIC_VIEW,
	// IPageLayout.ID_RES_NAV, MockViewPart.ID};

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
	}

	/**
	 * 
	 */
	private void addOpenCloseTests() {
		String[] ids = getAllTestableViewIds();

		for (int i = 0; i < ids.length; i++) {
			String id = ids[i];

			// Use the BASIC_VIEW as the fingerprint test.
			// Do not change this as this is an empty view
			// and not dependant on other components
			addTest(new OpenCloseViewTest(id,
					id.equals(RESOURCE_NAVIGATOR) ? BasicPerformanceTest.GLOBAL
							: BasicPerformanceTest.NONE));
		}
	}

	private void addResizeTests() {
		String[] ids = getAllTestableViewIds();

		for (int i = 0; i < ids.length; i++) {
			String id = ids[i];

			addTest(new ResizeTest(new ViewWidgetFactory(id)));
		}

	}

	public static String[] getAllTestableViewIds() {
		HashSet result = new HashSet();

		IViewDescriptor[] descriptors = Workbench.getInstance()
				.getViewRegistry().getViews();
		for (int i = 0; i < descriptors.length; i++) {
			IViewDescriptor descriptor = descriptors[i];
			String[] categoryPath = descriptor.getCategoryPath();

			if (categoryPath == null)
				continue;

			for (int j = 0; j < categoryPath.length; j++) {
				// Only test basic views
				if (categoryPath[j].equals(BASIC_PATH)) {
					result.add(descriptor.getId());
					continue;
				}
			}

		}

		return (String[]) result.toArray(new String[result.size()]);
	}
}
