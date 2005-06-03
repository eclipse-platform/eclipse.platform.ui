/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import java.util.ArrayList;

import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.tests.performance.layout.ResizeTest;
import org.eclipse.ui.tests.performance.layout.ViewWidgetFactory;
import org.eclipse.ui.views.IViewDescriptor;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.1
 */
public class ViewPerformanceSuite extends TestSuite {

	public static final String BASIC_VIEW = "org.eclipse.ui.tests.perf_basic";

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
					id.equals(BASIC_VIEW) ? BasicPerformanceTest.GLOBAL
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
		ArrayList result = new ArrayList();

		IViewDescriptor[] descriptors = Workbench.getInstance()
				.getViewRegistry().getViews();
		for (int i = 0; i < descriptors.length; i++) {
			IViewDescriptor descriptor = descriptors[i];

			// Heuristically prune out any test or example views
			if (descriptor.getId().equals(BASIC_VIEW)
					|| (descriptor.getId().indexOf(".test") == -1 && descriptor
							.getId().indexOf(".examples") == -1)) {

				result.add(descriptor.getId());
			}
		}

		return (String[]) result.toArray(new String[result.size()]);
	}
}
