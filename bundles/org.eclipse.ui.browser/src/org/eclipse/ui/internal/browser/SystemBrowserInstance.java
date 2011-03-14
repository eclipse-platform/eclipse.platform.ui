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

	public void openURL(URL url) throws PartInitException {
		String urlText = url.toExternalForm();
		Trace.trace(Trace.FINEST, "Launching system Web browser: " + urlText); //$NON-NLS-1$
		Program program = Program.findProgram("html"); //$NON-NLS-1$
		if (program != null) {
			if (program.execute(urlText))
				return;
		}
		if (!Program.launch(urlText))
			throw new PartInitException(NLS.bind(Messages.errorCouldNotLaunchWebBrowser, urlText));
	}
}