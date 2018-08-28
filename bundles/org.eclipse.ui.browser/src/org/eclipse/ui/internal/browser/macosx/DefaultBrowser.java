/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
	@Override
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
