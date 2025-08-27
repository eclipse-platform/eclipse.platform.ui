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
 * @since 3.1
 */
public class ZoomedViewCloseTest extends CloseTest {

	@Override
	public IWorkbenchPart getStackedPart1() {
		return stackedView1;
	}

	@Override
	public IWorkbenchPart getStackedPart2() {
		return stackedView2;
	}

	public IWorkbenchPart getUnstackedPart() {
		return unstackedView;
	}

	/**
	 * <p>Test: Zoom a view, then close the active editor.</p>
	 * <p>Expected result: The view remains zoomed and active. A new editor is selected as the
	 *    active editor.</p>
	 * <p>Note: The behavior of this test changed intentionally on 050416. Closing the active editor
	 *    no longer unzooms if a view is zoomed.</p>
	 */
	@Test
	public void testCloseActiveEditorWhileViewZoomed() {
		page.activate(editor1);
		zoom(stackedView1);
		close(editor1);

		assertZoomed(stackedView1);
		assertActive(stackedView1);
	}

	/**
	 * <p>
	 * Test: Activate an unstacked view, zoom and activate a stacked part, then
	 * close the active part.
	 * </p>
	 * <p>
	 * Expected result: Stack remains zoomed, another part in the zoomed stack is
	 * active
	 * </p>
	 * <p>
	 * Note: This ensures that when the active part is closed, it will try to
	 * activate a part that doesn't affect the zoom even if something else was
	 * activated more recently.
	 * </p>
	 */
	@Test
	public void testCloseZoomedStackedPartAfterActivatingView() {
		IWorkbenchPart zoomPart = getStackedPart1();
		IWorkbenchPart otherStackedPart = getStackedPart2();
		IWorkbenchPart unstackedPart = getUnstackedPart();

		page.activate(unstackedPart);
		zoom(zoomPart);
		close(zoomPart);

		assertZoomed(otherStackedPart);
		assertActive(otherStackedPart);
	}

	/**
	 * <p>
	 * Test: Activate an unstacked editor, zoom and activate a stacked part, then
	 * close the active part.
	 * </p>
	 * <p>
	 * Expected result: Stack remains zoomed, another part in the zoomed stack is
	 * active
	 * </p>
	 * <p>
	 * Note: This ensures that when the active part is closed, it will try to
	 * activate a part that doesn't affect the zoom even if something else was
	 * activated more recently.
	 * </p>
	 */
	@Test
	public void testCloseZoomedStackedPartAfterActivatingEditor() {
		IWorkbenchPart zoomPart = getStackedPart1();
		IWorkbenchPart otherStackedPart = getStackedPart2();
		IWorkbenchPart unstackedPart = getUnstackedPart();

		page.activate(unstackedPart);
		zoom(zoomPart);
		close(zoomPart);

		assertZoomed(otherStackedPart);
		assertActive(otherStackedPart);
	}

}
