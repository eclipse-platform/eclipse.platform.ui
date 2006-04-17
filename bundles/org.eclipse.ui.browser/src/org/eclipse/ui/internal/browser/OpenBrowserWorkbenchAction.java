/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.*;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
/**
 * Action to open the Web broswer.
 */
public class OpenBrowserWorkbenchAction implements IWorkbenchWindowActionDelegate {
	/**
	 * OpenBrowserWorkbenchAction constructor comment.
	 */
	public OpenBrowserWorkbenchAction() {
		super();
	}

	/**
	 * Disposes this action delegate.  The implementor should unhook any references
	 * to itself so that garbage collection can occur.
	 */
	public void dispose() {
		// do nothing
	}

	/**
	 * Initializes this action delegate with the workbench window it will work in.
	 *
	 * @param window the window that provides the context for this delegate
	 */
	public void init(IWorkbenchWindow window) {
		// do nothing
	}

	/**
	 * Performs this action.
	 * <p>
	 * This method is called when the delegating action has been triggered.
	 * Implement this method to do the actual work.
	 * </p>
	 *
	 * @param action the action proxy that handles the presentation portion of the
	 *   action
	 */
	public void run(IAction action) {
		try {
			IWorkbenchBrowserSupport browserSupport = WebBrowserUIPlugin.getInstance().getWorkbench().getBrowserSupport();
			IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
			browser.openURL(null);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error opening browser", e); //$NON-NLS-1$
		}
	}

	/**
	 * Notifies this action delegate that the selection in the workbench has changed.
	 * <p>
	 * Implementers can use this opportunity to change the availability of the
	 * action or to modify other presentation properties.
	 * </p>
	 *
	 * @param action the action proxy that handles presentation portion of the action
	 * @param selection the current selection in the workbench
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}
}
