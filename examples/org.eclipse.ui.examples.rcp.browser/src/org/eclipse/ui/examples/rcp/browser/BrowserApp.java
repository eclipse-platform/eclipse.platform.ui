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

import org.eclipse.core.boot.IPlatformRunnable;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.PlatformUI;

/**
 * The application class for the RCP Browser Example.
 * Creates and runs the Workbench, passing a <code>BrowserAdvisor</code>
 * as the workbench advisor.
 * 
 * @issue Couldn't run without initial perspective -- it failed with NPE 
 *   on WorkbenchWindow.openPage (called from Workbench.openFirstTimeWindow).  Advisor is currently required to override 
 *   getInitialWindowPerspectiveId.
 * 
 * @issue If shortcut bar is hidden, and last view in perspective is closed, there's no way to get it open again.
 * 
 * @since 3.0
 */
public class BrowserApp implements IPlatformRunnable {

	/**
	 * ID of the RCP Browser Example plug-in.
	 */
	public static final String PLUGIN_ID = "org.eclipse.ui.examples.rcp.browser"; //$NON-NLS-1$
	
	/**
	 * Constructs a new <code>BrowserApp</code>.
	 */
	public BrowserApp() {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.boot.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		Display display = PlatformUI.createDisplay();
		try {
			int code = PlatformUI.createAndRunWorkbench(display,
					new BrowserAdvisor());
			// TODO: map return code to what the runtime expects.
			return new Integer(code);
		} finally {
			if (display != null)
				display.dispose();
		}
	}
}
