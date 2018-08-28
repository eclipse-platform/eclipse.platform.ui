/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.URL;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.AbstractWebBrowser;

/**
 * An instance of a running system Web browser.
 */
public class SystemBrowserInstance extends AbstractWebBrowser {
	public SystemBrowserInstance(String id) {
		super(id);
	}

	@Override
	public void openURL(URL url) throws PartInitException {
		String urlText = url.toExternalForm();
		Trace.trace(Trace.FINEST, "Launching system Web browser: " + urlText); //$NON-NLS-1$
		Program program = Program.findProgram("html"); //$NON-NLS-1$
		if (program != null) {
			if (program.execute(urlText))
				return;
		}
		if (!Program.launch(urlText))
			throw new PartInitException(NLS.bind(Messages.errorCouldNotLaunchExternalWebBrowser, urlText));
	}
}