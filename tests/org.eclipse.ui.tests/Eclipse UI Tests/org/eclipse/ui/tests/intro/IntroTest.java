/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.PreferenceMementoRule;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.0
 */
@RunWith(JUnit4.class)
public class IntroTest extends UITestCase {

	@Rule
	public final PreferenceMementoRule preferenceMemento = new PreferenceMementoRule();

	IWorkbenchWindow window = null;

	private IntroDescriptor oldDesc;

	public IntroTest() {
		super(IntroTest.class.getSimpleName());
	}

	@Test
	public void testCloseInEmptyPerspective() {
		testClose(EmptyPerspective.PERSP_ID);
	}

	@Test
	public void testCloseInNonEmptyPerspective() {
		testClose("org.eclipse.ui.resourcePerspective");
	}

	private void testClose(String perspectiveId) {
		IPerspectiveDescriptor descriptor = window.getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(
						perspectiveId);
		window.getActivePage().setPerspective(descriptor);

		IIntroManager introManager = window.getWorkbench().getIntroManager();
		IIntroPart part = introManager.showIntro(window, false);
		introManager.closeIntro(part);

		assertTrue(((WorkbenchWindow) window).getCoolBarVisible());
		assertTrue(((WorkbenchWindow) window).getPerspectiveBarVisible());
	}

	@Test
	public void testShow() {
		IIntroManager introManager = window.getWorkbench().getIntroManager();
		IIntroPart part = introManager.showIntro(window, false);
		assertNotNull(part);
		assertFalse(introManager.isIntroStandby(part));
		introManager.closeIntro(part);
		assertNull(introManager.getIntro());

		part = introManager.showIntro(window, true);
		assertNotNull(part);
		assertTrue(introManager.isIntroStandby(part));
		assertTrue(introManager.closeIntro(part));
		assertNull(introManager.getIntro());
	}

	@Test
	public void testCreateProblemsView() throws Exception {
		IIntroManager introManager= window.getWorkbench().getIntroManager();
		IIntroPart part= introManager.showIntro(window, false);
		assertNotNull(part);
		assertFalse(introManager.isIntroStandby(part));

		IViewReference viewRef= window.getActivePage().findViewReference(IPageLayout.ID_PROBLEM_VIEW);
		assertNull(viewRef);
		IViewPart problemsView= window.getActivePage().showView(IPageLayout.ID_PROBLEM_VIEW, null, IWorkbenchPage.VIEW_CREATE);
		assertNotNull(problemsView);
		assertFalse(introManager.isIntroStandby(part));

		window.getActivePage().hideView(problemsView);
		assertTrue(introManager.closeIntro(part));
		assertNull(introManager.getIntro());
	}

	@Test
	public void testActivateProblemsView() throws Exception {
		IIntroManager introManager= window.getWorkbench().getIntroManager();
		IIntroPart part= introManager.showIntro(window, false);
		assertNotNull(part);
		assertFalse(introManager.isIntroStandby(part));

		IViewReference viewRef= window.getActivePage().findViewReference(IPageLayout.ID_PROBLEM_VIEW);
		assertNull(viewRef);
		IViewPart problemsView= window.getActivePage().showView(IPageLayout.ID_PROBLEM_VIEW);
		assertNotNull(problemsView);
		assertTrue(introManager.isIntroStandby(part));

		window.getActivePage().hideView(problemsView);
		assertTrue(introManager.closeIntro(part));
		assertNull(introManager.getIntro());
	}

	@Test
	public void testStandby() {
		IWorkbench workbench = window.getWorkbench();
		IIntroPart part = workbench.getIntroManager().showIntro(window, false);
		assertNotNull(part);
		assertFalse(workbench.getIntroManager().isIntroStandby(part));
		workbench.getIntroManager().setIntroStandby(part, true);
		assertTrue(workbench.getIntroManager().isIntroStandby(part));
		assertTrue(workbench.getIntroManager().closeIntro(part));
		assertNull(workbench.getIntroManager().getIntro());
	}

	/**
	 * Open the intro, change perspective, close the intro (ensure it still
	 * exists), change back to the first perspective, close the intro, ensure
	 * that it no longer exists.
	 */
	@Test
	public void testPerspectiveChange() {
		// These tests are hard-wired to the pre-3.3 zoom behaviour
		// Run them anyway to ensure that we preserve the 3.0 mechanism
		preferenceMemento.setPreference(PrefUtil.getAPIPreferenceStore(),
				IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);

		IWorkbench workbench = window.getWorkbench();
		IIntroPart part = workbench.getIntroManager().showIntro(window, false);
		assertNotNull(part);
		IWorkbenchPage activePage = window.getActivePage();
		IPerspectiveDescriptor oldDesc = activePage.getPerspective();
		activePage.setPerspective(WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry().findPerspectiveWithId(
						"org.eclipse.ui.tests.api.SessionPerspective"));
		assertFalse(workbench.getIntroManager().closeIntro(part));
		assertNotNull(workbench.getIntroManager().getIntro());

		activePage.setPerspective(oldDesc);
		assertTrue(workbench.getIntroManager().closeIntro(part));
		assertNull(workbench.getIntroManager().getIntro());
	}

	/**
	 * Open the intro, change perspective, close the intro
	 * and ensure that the intro has not been closed in the
	 * other perspective.
	 * See bug 174213
	 * See IntroTest2.java
	 */
	@Test
	public void testPerspectiveChangeWith32StickyBehavior() {
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
		assertNotNull(viewPart);
	}

	@Test
	public void testPerspectiveReset() {
		IWorkbench workbench = window.getWorkbench();
		IIntroPart part = workbench.getIntroManager().showIntro(window, false);
		assertNotNull(part);
		window.getActivePage().resetPerspective();
		part = workbench.getIntroManager().getIntro();
		assertNotNull(part);
		assertFalse(workbench.getIntroManager().isIntroStandby(part));

		workbench.getIntroManager().setIntroStandby(part, true);
		window.getActivePage().resetPerspective();
		part = workbench.getIntroManager().getIntro();
		assertNotNull(part);
		assertTrue(workbench.getIntroManager().isIntroStandby(part));
		assertTrue(workbench.getIntroManager().closeIntro(part));
		assertNull(workbench.getIntroManager().getIntro());
	}

	/**
	 * Test to ensure that the part is properly nulled out when the intro is
	 * closed via the view close mechanism.
	 */
	@Test
	public void testViewClosure() {
		IWorkbench workbench = window.getWorkbench();
		IIntroPart part = workbench.getIntroManager().showIntro(window, false);
		assertNotNull(part);
		IViewPart viewPart = window.getActivePage().findView(
				IIntroConstants.INTRO_VIEW_ID);
		assertNotNull(viewPart);
		window.getActivePage().hideView(viewPart);
		assertNull(workbench.getIntroManager().getIntro());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		// these tests rely on the 3.2 behavior for sticky views
		IPreferenceStore preferenceStore = PrefUtil.getAPIPreferenceStore();
		preferenceStore.putValue(IWorkbenchPreferenceConstants.ENABLE_32_STICKY_CLOSE_BEHAVIOR, "true");

		oldDesc = Workbench.getInstance().getIntroDescriptor();
		IntroDescriptor testDesc = (IntroDescriptor) WorkbenchPlugin
				.getDefault().getIntroRegistry().getIntro(
						"org.eclipse.ui.testintro");
		Workbench.getInstance().setIntroDescriptor(testDesc);
		window = openTestWindow();
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		Workbench.getInstance().setIntroDescriptor(oldDesc);
	}
}
