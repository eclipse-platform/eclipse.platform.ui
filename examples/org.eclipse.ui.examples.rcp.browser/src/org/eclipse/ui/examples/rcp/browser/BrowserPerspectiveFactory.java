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
package org.eclipse.ui.examples.rcp.browser;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

/**
 * The perspective factory for the RCP Browser Example's perspective.
 * 
 * @since 3.0
 */
public class BrowserPerspectiveFactory implements IPerspectiveFactory {

	/**
	 * Constructs a new <code>BrowserPerspectiveFactory</code>.
	 */
	public BrowserPerspectiveFactory() {
		// do nothing
	}

	/**
	 * Creates the initial layout of the Browser perspective.
	 */
	public void createInitialLayout(IPageLayout layout) {
		layout.addView(IBrowserConstants.BROWSER_VIEW_ID, IPageLayout.RIGHT, .25f, IPageLayout.ID_EDITOR_AREA);
		layout.addPlaceholder(IBrowserConstants.HISTORY_VIEW_ID, IPageLayout.LEFT, .3f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		IViewLayout historyLayout = layout.getViewLayout(IBrowserConstants.HISTORY_VIEW_ID);
		historyLayout.setCloseable(true);
		layout.setEditorAreaVisible(false);
	}
}
