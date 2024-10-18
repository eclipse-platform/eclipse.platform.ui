/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.markers;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

/**
 * MarkerViewTests are the tests for the marker view.
 *
 * @since 3.4
 */
public class MarkerViewTests {

	@Test
	public void testOpenView() throws PartInitException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		page.hideView(page.showView(IPageLayout.ID_BOOKMARKS));
		page.hideView(page.showView(IPageLayout.ID_PROBLEM_VIEW));
		page.hideView(page.showView(IPageLayout.ID_TASK_LIST));

	}
}
