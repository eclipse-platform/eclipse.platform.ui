/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.rcp.browser;

import org.eclipse.jface.util.Assert;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * The workbench advisor for the browser example.
 * Configures the workbench as needed for the browser example, including
 * specifying the default perspective id.
 * Configures each new workbench window as it is being opened.
 * 
 * @since 3.0
 */
public class BrowserAdvisor extends WorkbenchAdvisor {

	/**
	 * Constructs a new <code>BrowserAdvisor</code>.
	 */
	public BrowserAdvisor() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor
	 */
	public String getInitialWindowPerspectiveId() {
		return BrowserApp.PLUGIN_ID + ".browserPerspective"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor
	 */
	public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
		configurer.setShowFastViewBars(false);
		configurer.setShowPerspectiveBar(false);
		configurer.setTitle("Browser Example");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor
	 */
	public void fillActionBars(IWorkbenchWindow window, IActionBarConfigurer configurer, int flags) {
		// FILL_PROXY is not currently handled
		Assert.isTrue((flags & WorkbenchAdvisor.FILL_PROXY) == 0);
		BrowserActionBuilder builder = new BrowserActionBuilder(window);
		builder.fillActionBars(configurer, flags);
	}
}
