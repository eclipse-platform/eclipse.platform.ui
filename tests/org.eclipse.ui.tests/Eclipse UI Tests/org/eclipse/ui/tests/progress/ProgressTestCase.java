/*******************************************************************************
 * Copyright (c) 2009, 2019 IBM Corporation and others.
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

package org.eclipse.ui.tests.progress;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.progress.ProgressView;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.6
 *
 */
public abstract class ProgressTestCase extends UITestCase {

	protected ProgressView progressView;
	protected IWorkbenchWindow window;

	/**
	 * @param testName
	 */
	public ProgressTestCase(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		window = openTestWindow("org.eclipse.ui.resourcePerspective");
	}

	/**
	 * Opens the ProgresView on the specified page and process the UI events.
	 *
	 * @throws PartInitException
	 */
	public void openProgressView() throws Exception {
		progressView = (ProgressView) openView(IPageLayout.ID_PROGRESS_VIEW);
	}

	public IViewPart openView(String id) throws Exception {
		IWorkbenchPage activePage = window.getActivePage();
		IViewPart view = activePage.showView(id);
		assertNotNull("View is not created properly: " + id, view);
		processEvents();
		return view;
	}

	public void hideProgressView() {
		window.getActivePage().hideView(progressView);
		processEvents();
	}
}