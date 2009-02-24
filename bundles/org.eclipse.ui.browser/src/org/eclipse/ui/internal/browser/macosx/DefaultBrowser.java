/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser.macosx;

import java.io.*;
import java.net.URL;

import org.eclipse.ui.browser.AbstractWebBrowser;
import org.eclipse.ui.internal.browser.WebBrowserUIPlugin;

public class DefaultBrowser extends AbstractWebBrowser {

	public DefaultBrowser(String id) {
		super(id);
	}

	/**
	 * @see org.eclipse.help.browser.IBrowser#displayURL(String)
	 */
	public void openURL(URL url2) {
		String url = url2.toExternalForm();
		/*
		 * Code from Marc-Antoine Parent
		 */
		try {
			Runtime.getRuntime().exec(new String[] { "/usr/bin/osascript", //$NON-NLS-1$
					"-e", //$NON-NLS-1$
					"open location \"" + url + "\"" }); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException ioe) {
			WebBrowserUIPlugin.logError("Launching \"osascript\" has failed.", ioe); //$NON-NLS-1$
		}
	}
}
