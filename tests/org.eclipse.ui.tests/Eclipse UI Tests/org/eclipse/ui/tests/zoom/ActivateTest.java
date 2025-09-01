/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.ui.tests.zoom;

import org.eclipse.ui.IWorkbenchPart;
import org.junit.Test;

public abstract class ActivateTest extends ZoomTestCase {

	public abstract IWorkbenchPart getStackedPart1();
	public abstract IWorkbenchPart getStackedPart2();

	/**
	 * <p>Test: Zoom a part and activate it</p>
	 * <p>Expected result: Part remains zoomed</p>
	 */
	@Test
	public void testZoomAndActivate() {
		IWorkbenchPart stacked1 = getStackedPart1();

		zoom(stacked1);
		page.activate(stacked1);

		assertZoomed(stacked1);
		assertActive(stacked1);
	}

	/**
	 * <p>Test: Zoom a view then activate another view in the same stack</p>
	 * <p>Expected result: Stack remains zoomed</p>
	 */
	@Test
	public void testActivateSameStack() {
		IWorkbenchPart stacked1 = getStackedPart1();
		IWorkbenchPart stacked2 = getStackedPart2();

		// Ensure that every view in the stack is zoomed
		zoom(stacked1);

		// Ensure that activating another zoomed part in the same stack doesn't affect zoom
		page.activate(stacked2);

		assertZoomed(stacked2);
		assertActive(stacked2);
	}

	/**
	 * <p>Test: Zoom a pane, then reset perspective.</p>
	 * <p>Expected result: the page unzooms but the original pane remains active</p>
	 *
	 * @since 3.1
	 */
	@Test
	public void testResetPerspective() {
		IWorkbenchPart zoomedPart = getStackedPart1();

		zoom(zoomedPart);

		page.resetPerspective();

		assertZoomed(null);
		assertActive(zoomedPart);
	}

}
