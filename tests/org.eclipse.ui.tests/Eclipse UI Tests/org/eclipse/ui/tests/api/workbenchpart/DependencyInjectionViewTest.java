/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.api.workbenchpart;

import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.4
 *
 */
public class DependencyInjectionViewTest extends UITestCase {

	/**
	 * @param testName
	 */
	public DependencyInjectionViewTest(String testName) {
		super(testName);
	}

	public void testDependencyInjectionLifecycle() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();
		IViewPart v = page.showView(DependencyInjectionView.ID);
		assertTrue(v instanceof DependencyInjectionView);
		DependencyInjectionView view = (DependencyInjectionView) v;
		processEvents();

		assertTrue(view.fieldAvailable);
		assertTrue(view.methodParameterAvailable);
		assertTrue(view.postConstructParameterAvailable);

		// check if focus is correctly called
		assertTrue(view.focusParameterAvailable);
		assertTrue(view.creationCallOrder.size() > 0);
		List<String> expectedCreationCallOrder = Arrays.asList("constructor", "setInitializationData", "init", "@field",
				"@method",
				"@postconstruct",
				"createPartControl", "@focus", "setFocus");
		assertEquals(expectedCreationCallOrder, view.creationCallOrder);

		page.hideView(v);
		// v.dispose();
		processEvents();

		assertTrue(view.predestroyParameterAvailable);
		List<String> expectedDisposeCallOrder = Arrays.asList("dispose", "@predestroy");

		assertEquals(expectedDisposeCallOrder, view.disposeCallOrder);

		processEvents();


	}
}
