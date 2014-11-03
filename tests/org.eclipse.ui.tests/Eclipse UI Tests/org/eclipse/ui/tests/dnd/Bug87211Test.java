/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.dnd;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class Bug87211Test extends TestCase {
	public static TestSuite suite() {
		return new TestSuite(Bug87211Test.class);
	}

	private WorkbenchPage fPage;

	private IWorkbenchWindow fWindow;

	@Override
	protected void setUp() throws Exception {
		fWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		fPage = (WorkbenchPage) fWindow.getActivePage();
	}

	/**
	 * Tests dragging a standalone view to a new position, then dragging
	 * another view on top of it.  The views should still be in their
	 * separate stacks.
	 *
	 * @throws Throwable
	 */
	public void testDragStandaloneView() throws Throwable {
		fPage.setPerspective(WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry().findPerspectiveWithId(
						StandaloneViewPerspective.PERSP_ID));
		try {
			IViewPart outline = fPage
					.findView(StandaloneViewPerspective.OUTLINE_ID);
			ViewDragSource source = new ViewDragSource(
					StandaloneViewPerspective.OUTLINE_ID, true);
			ViewDropTarget target = new ViewDropTarget(
					new ExistingWindowProvider(fWindow),
					StandaloneViewPerspective.RESOURCE_ID, SWT.CENTER);
			source.drag(target);

			IViewPart[] refs = fPage.getViewStack(outline);
			assertEquals(
					"Cannot drop standalone view onto another standalone view",
					1, refs.length);

			// the bug resulted in the dropped standalone view losing
			// it's standalone status, so other views could be dropped
			// on it.
			ViewDragSource secondViewSource = new ViewDragSource(
					StandaloneViewPerspective.TASK_ID, true);
			ViewDropTarget draggedTarget = new ViewDropTarget(
					new ExistingWindowProvider(fWindow),
					StandaloneViewPerspective.OUTLINE_ID, SWT.CENTER);
			secondViewSource.drag(draggedTarget);

			refs = fPage.getViewStack(outline);
			assertEquals(
					"Cannot drop a second view onto the moved standalone view",
					1, refs.length);
		} finally {
			fPage.closePerspective(fPage.getPerspective(), false, false);
		}
	}
}
