/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.MalformedURLException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class BrowserLauncher implements IEditorLauncher {

	public BrowserLauncher() {
		// do nothing
	}

	public void open(IPath file) {
		IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			if (WebBrowserPreference.getBrowserChoice()== WebBrowserPreference.INTERNAL)
				support.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR,
						file.toPortableString(), null, null).openURL(file.toFile().toURI().toURL());
			else
				support.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR,
						DefaultBrowserSupport.SHARED_ID, null, null).openURL(file.toFile().toURI().toURL());
		}
		catch (MalformedURLException e) {
			// ignore
		}
		catch (PartInitException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					Messages.errorDialogTitle, e.getLocalizedMessage());
		}
	}
}
