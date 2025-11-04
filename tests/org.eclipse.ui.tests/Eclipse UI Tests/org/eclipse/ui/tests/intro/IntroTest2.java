/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
package org.eclipse.ui.tests.intro;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @since 3.0
 */
@ExtendWith(CloseTestWindowsExtension.class)
public class IntroTest2 {

	

	IWorkbenchWindow window = null;

	private IntroDescriptor oldDesc;

	/**
	 * Open the intro, change perspective, close the intro
	 * and ensure that the intro has been closed in the
	 * other perspective.
	 * See bug 174213
	 */
	@Test
	public void testPerspectiveChangeWith33StickyBehavior() {
		IWorkbench workbench = window.getWorkbench();
		IIntroPart part = workbench.getIntroManager().showIntro(window, false);
		assertNotNull(part);
		IWorkbenchPage activePage = window.getActivePage();
		IPerspectiveDescriptor oldDesc = activePage.getPerspective();
		activePage.setPerspective(WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry().findPerspectiveWithId(
						"org.eclipse.ui.tests.api.SessionPerspective"));

		IViewPart viewPart = window.getActivePage().findView(
				IIntroConstants.INTRO_VIEW_ID);
		assertNotNull(viewPart);

		window.getActivePage().hideView(viewPart);
		viewPart = window.getActivePage().findView(
				IIntroConstants.INTRO_VIEW_ID);
		assertNull(viewPart);

		activePage.setPerspective(oldDesc);
		viewPart = window.getActivePage().findView(
				IIntroConstants.INTRO_VIEW_ID);
		assertNull(viewPart);

	}

	@BeforeEach
	public final void setUp() throws Exception {
		// these tests rely on the 3.3 behavior for sticky views
		IPreferenceStore preferenceStore = PrefUtil.getAPIPreferenceStore();
		preferenceStore.putValue(IWorkbenchPreferenceConstants.ENABLE_32_STICKY_CLOSE_BEHAVIOR, "false");

		oldDesc = Workbench.getInstance().getIntroDescriptor();
		IntroDescriptor testDesc = (IntroDescriptor) WorkbenchPlugin
				.getDefault().getIntroRegistry().getIntro(
						"org.eclipse.ui.testintro");
		Workbench.getInstance().setIntroDescriptor(testDesc);
		window = openTestWindow();
	}

	@AfterEach
	public final void tearDown() throws Exception {
		Workbench.getInstance().setIntroDescriptor(oldDesc);
	}
}
