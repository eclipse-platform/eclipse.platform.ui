/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Starts a source lookup service in each workbench window.
 * 
 * @since 3.2
 */
public class SourceLookupManager implements IWindowListener {

	private static SourceLookupManager fgDefault;
	
	/**
	 * Services per window
	 */
	private Map fServices = new HashMap();
	
	private SourceLookupManager() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
		for (int i = 0; i < workbenchWindows.length; i++) {
			IWorkbenchWindow window = workbenchWindows[i];
			windowOpened(window);
		}
		workbench.addWindowListener(this);
	}
		
	/**
	 * Returns the default source lookup manager.
	 * 
	 * @return
	 */
	public static SourceLookupManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new SourceLookupManager();
		}
		return fgDefault;
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
		SourceLookupService service = (SourceLookupService) fServices.get(window);
		if (service != null) {
			fServices.remove(window);
			service.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {
		SourceLookupService service = (SourceLookupService) fServices.get(window);
		if (service == null) {
			service = new SourceLookupService(window);
			fServices.put(window, service);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.ISourceDisplayAdapter#displaySource(java.lang.Object, org.eclipse.ui.IWorkbenchPage, boolean)
	 */
	public void displaySource(Object context, IWorkbenchPage page, boolean forceSourceLookup) {
		IWorkbenchWindow window = page.getWorkbenchWindow();
		SourceLookupService service = (SourceLookupService) fServices.get(window);
		if (service != null) {
			service.displaySource(context, page, forceSourceLookup);
		}
	}	
}
