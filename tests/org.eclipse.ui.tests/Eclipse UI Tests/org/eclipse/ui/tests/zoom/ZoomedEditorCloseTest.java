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
package org.eclipse.ui.tests.zoom;

import org.eclipse.ui.IWorkbenchPart;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class ZoomedEditorCloseTest extends CloseTest {

	public ZoomedEditorCloseTest() {
		super(ZoomedEditorCloseTest.class.getSimpleName());
	}

	@Override
	public IWorkbenchPart getStackedPart1() {
		return editor1;
	}

	@Override
	public IWorkbenchPart getStackedPart2() {
		return editor2;
	}

	@Override
	public IWorkbenchPart getUnstackedPart() {
		return editor3;
	}

	/**
	 * <p>Test: Activate a view, then zoom an unstacked editor and close it.</p>
	 * <p>Expected result: The previously active Editor becomes active and unzoomed</p>
	 * <p>Note: This ensures that the activation list is used if there is nothing available
	 *    in the currently zoomed stack. It also ensures that activation never moves from
	 *    an editor to a view when an editor is closed.</p>
	 */
	@Test
	@Ignore
	public void testCloseZoomedUnstackedEditorAfterActivatingView() {
		System.out.println("Bogus test: we don't unsoom in this case");
//        IWorkbenchPart previousActive = stackedView1;
//        IWorkbenchPart zoomedPart = editor3;
//
//        page.activate(editor1);
//        page.activate(previousActive);
//        zoom(zoomedPart);
//        close(zoomedPart);
//
//        assertZoomed(null);
//        assertActive(editor1);
	}

	/**
	 * <p>Test: Activate an unstacked editor, activate an unstacked view, activate a stacked editor,
	 *    then close the active editor.</p>
	 * <p>Expected result: The previously active editor becomes active (even though a view is next
	 *    in the activation list)</p>
	 * <p>Note: This isn't really a zoom test, but it ensures that activation doesn't move from an editor
	 *    to a view when the active editor is closed. Activating an editor in a different stack first
	 *    ensures that activation WILL move between editor stacks to follow the activation order.</p>
	 */
	@Test
	public void testCloseUnzoomedStackedEditorAfterActivatingView() {
		page.activate(editor3);
		page.activate(unstackedView);
		page.activate(editor1);
		close(editor1);

		// Ensure that activation moved to the previously active editor, even though
		// a view was next in the activation list.
		assertZoomed(null);
		assertActive(editor3);
	}
}
