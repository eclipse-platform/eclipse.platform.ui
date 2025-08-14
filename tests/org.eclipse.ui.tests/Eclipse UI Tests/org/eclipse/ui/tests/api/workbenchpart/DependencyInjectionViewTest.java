/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
 *     Lucas Bullen (Red Hat Inc.) - [Bug 530492] Failing on Java 9
 *******************************************************************************/

package org.eclipse.ui.tests.api.workbenchpart;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.4
 */
@RunWith(JUnit4.class)
public class DependencyInjectionViewTest {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Test
	public void testDependencyInjectionLifecycle() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();
		IViewPart v = page.showView(DependencyInjectionView.ID);
		assertTrue(v instanceof DependencyInjectionView);
		DependencyInjectionView view = (DependencyInjectionView) v;
		List<String> expectedCreationCallOrder = Arrays.asList("constructor", "setInitializationData", "init", "@field",
				"@method", "@postconstruct", "createPartControl", "@focus", "setFocus");
		processViewEvents(expectedCreationCallOrder, view.creationCallOrder);

		assertTrue(view.fieldAvailable);
		assertTrue(view.methodParameterAvailable);
		assertTrue(view.postConstructParameterAvailable);

		// check if focus is correctly called
		assertTrue(view.focusParameterAvailable);
		assertTrue(!view.creationCallOrder.isEmpty());
		assertEquals(expectedCreationCallOrder, view.creationCallOrder);

		page.hideView(v);
		// v.dispose();
		List<String> expectedDisposeCallOrder = Arrays.asList("dispose", "@predestroy");
		processViewEvents(expectedDisposeCallOrder, view.disposeCallOrder);

		assertTrue(view.predestroyParameterAvailable);

		assertEquals(expectedDisposeCallOrder, view.disposeCallOrder);

		processEvents();


	}

	private void processViewEvents(List<String> expectedCalls, List<String> actualCalls) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return !display.readAndDispatch() && expectedCalls.size() == actualCalls.size();
			}
		}.waitForCondition(display, 3000);
	}
}
