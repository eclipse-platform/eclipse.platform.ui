/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import org.eclipse.ui.IPageLayout;
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
	private IWorkbenchWindow window;
	/**
	 * @param testName
	 */
	public ProgressTestCase(String testName) {
		super(testName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		window = openTestWindow("org.eclipse.ui.resourcePerspective");
	}

	/**
	 * Opens the ProgresView on the specified page and process the UI events.
	 * 
	 * @throws PartInitException 
	 */
	public void openProgressView() throws PartInitException {
		
		IWorkbenchPage activePage = window.getActivePage();
		progressView = (ProgressView) activePage.showView(IPageLayout.ID_PROGRESS_VIEW);
		assertNotNull("Progress View is not created properly", progressView);
		
		processEvents();
	}
	
	public void hideProgressView() {
		window.getActivePage().hideView(progressView);
		processEvents();

	}

}