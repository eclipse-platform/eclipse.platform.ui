/*******************************************************************************
  * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River - Pawel Piech - Fixed debug context service usage (Bug 258189)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Manages the view context services for each window.
 * 
 * @since 3.2
 */
public class ViewContextManager implements IWindowListener {
		
	/**
	 * Map of services
	 */
	private Map fWindowToService = new HashMap();
	
	// singleton manager
	private static ViewContextManager fgManager;
	
	/**
	 * Returns the singleton view context manager.
	 * 
	 * @return view manager
	 */
	public static ViewContextManager getDefault() {
		if (fgManager == null) {
			fgManager = new ViewContextManager();
		}
		return fgManager;
	}
	
	
	private ViewContextManager() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
		for (int i = 0; i < workbenchWindows.length; i++) {
			IWorkbenchWindow window = workbenchWindows[i];
			windowOpened(window);
		}
		workbench.addWindowListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowClosed(IWorkbenchWindow window) {
		ViewContextService service = (ViewContextService) fWindowToService.get(window);
		if (service != null) {
			fWindowToService.remove(window);
			service.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {
		ViewContextService service = (ViewContextService) fWindowToService.get(window);
		if (service == null) {
			service = new ViewContextService(window);
			fWindowToService.put(window, service);
		}
	}
	
	/**
	 * Returns the service for the given window, or <code>null</code> if none.
	 * 
	 * @param window
	 * @return view context service or <code>null</code>
	 */
	public ViewContextService getService(IWorkbenchWindow window) {
		return (ViewContextService) fWindowToService.get(window);
	}

}
