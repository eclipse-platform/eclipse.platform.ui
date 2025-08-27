/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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

/**
 * This class contains tests that apply to both views and editors. Subclasses
 * will overload the abstract methods to determine whether editors or views
 * are being tested, and can define additional tests that only apply to editors
 * or views (or that have different results).
 */
public abstract class CloseTest extends ZoomTestCase {

	public abstract IWorkbenchPart getStackedPart1();
	public abstract IWorkbenchPart getStackedPart2();

	/**
	 * <p>Test: Activate an unstacked editor, activate a stacked part, then close the active part.</p>
	 * <p>Expected result: The unstacked part becomes active</p>
	 * <p>Note: This isn't really a zoom test, but it ensures that the behavior tested by
	 *    testHideZoomedStackedPartAfterActivatingEditor does not affect activation when there is no zoom.</p>
	 */
	@Test
	public void testCloseUnzoomedStackedPartAfterActivatingEditor() {
		IWorkbenchPart activePart = getStackedPart1();
		IWorkbenchPart unstackedPart = editor3;

		page.activate(unstackedPart);
		page.activate(activePart);
		close(activePart);

		assertZoomed(null);
		assertActive(unstackedPart);
	}

	/**
	 * <p>Test: Zoom a stacked part and close an inactive, unstacked editor.</p>
	 * <p>Expected result: No change in activation or zoom</p>
	 */
	@Test
	public void testCloseHiddenUnstackedEditor() {
		IWorkbenchPart zoomedPart = getStackedPart1();

		// Activate another editor to ensure that we aren't closing the active editor
		page.activate(editor1);
		zoom(zoomedPart);
		close(editor3);

		assertZoomed(zoomedPart);
		assertActive(zoomedPart);
	}

	/**
	 * <p>Test: Zoom a stacked part and close an inactive, unstacked view.</p>
	 * <p>Expected result: No change in activation or zoom</p>
	 */
	@Test
	public void testCloseHiddenUnstackedView() {
		IWorkbenchPart zoomedPart = getStackedPart1();

		zoom(zoomedPart);
		close(unstackedView);

		assertZoomed(zoomedPart);
		assertActive(zoomedPart);
	}

}
