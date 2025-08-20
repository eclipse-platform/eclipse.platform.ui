/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class IPerspectiveListenerTest implements IPerspectiveListener {

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private int fEvent;

	private IWorkbench fWorkbench;

	private IWorkbenchWindow fWindow;

	private IWorkbenchPage fPageMask;

	private IPerspectiveDescriptor fPerMask;

	/**
	 * bit masks for events
	 */
	public static final int NONE = 0x00, OPEN = 0x01, CLOSED = 0x02,
			ACTIVATED = 0x04, CHANGED = 0x08;

	@Before
	public final void setUp() throws Exception {
		fEvent = NONE;
		fWorkbench = PlatformUI.getWorkbench();
		fWindow = openTestWindow();
		fWindow.addPerspectiveListener(this);
	}

	@After
	public final void tearDown() throws Exception {
		fWindow.removePerspectiveListener(this);
		fWorkbench = null;
	}

	@Test
	@Ignore
	public void testPerspectiveActivated() {
		/*
		 * Commented out because until test case can be updated to work with new
		 * window/page/perspective implementation
		 */
		 fPageMask = fWindow.getActivePage();
		 fPerMask = fWorkbench.getPerspectiveRegistry().findPerspectiveWithId(EmptyPerspective.PERSP_ID );
		 fPageMask.setPerspective( fPerMask );

		 assertEquals( isActivated( fEvent ), true );
	}

	@Test
	@Ignore
	public void testPerspectiveChanged() {
		/*
		 * Commented out because until test case can be updated to work with new
		 * window/page/perspective implementation
		 */
		 fPageMask = fWindow.getActivePage();
		 fPerMask = fWorkbench.getPerspectiveRegistry().findPerspectiveWithId(EmptyPerspective.PERSP_ID );
		 fPageMask.setPerspective( fPerMask );

		 assertEquals( isActivated( fEvent ), true );
	}

	/**
	 * @see IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveActivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		if (page == fPageMask && perspective == fPerMask) {
			fEvent = fEvent | ACTIVATED;
		}
	}

	/**
	 * @see IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
	 */
	@Override
	public void perspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective, String changeId) {
		if (page == fPageMask && perspective == fPerMask) {
			fEvent = fEvent | CHANGED;
		}
	}

	public static boolean isOpen(int bits) {
		return ((bits & OPEN) != 0);
	}

	public static boolean isClosed(int bits) {
		return ((bits & CLOSED) != 0);
	}

	public static boolean isActivated(int bits) {
		return ((bits & ACTIVATED) != 0);
	}

	public static boolean isChanged(int bits) {
		return ((bits & CLOSED) != 0);
	}
}
